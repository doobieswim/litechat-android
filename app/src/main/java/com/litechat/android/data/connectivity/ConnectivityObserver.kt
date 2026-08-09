package com.litechat.android.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Observes Android connectivity state and exposes a [LiveData] that consumers
 * can observe to pause/resume network operations.
 *
 * When connectivity drops to [State.Disconnected], SSE streams and polling
 * should pause and the UI should show "Waiting for connection…" rather than
 * hammering endpoints with repeated failures.
 *
 * Zero deps beyond androidx.lifecycle (already in the project).
 */
class ConnectivityObserver(context: Context) {

    enum class State { Connected, Disconnected }

    private val _state = MutableLiveData(State.Connected)
    val state: LiveData<State> = _state

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _state.postValue(State.Connected)
        }

        override fun onLost(network: Network) {
            _state.postValue(State.Disconnected)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities,
        ) {
            val connected = capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET,
            )
            _state.postValue(if (connected) State.Connected else State.Disconnected)
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    /** Release the network callback. Call from ViewModel.onCleared() or lifecycle owner. */
    fun unregister() {
        connectivityManager.unregisterNetworkCallback(callback)
    }

    /** Current connectivity snapshot (synchronous, non-blocking). */
    val isConnected: Boolean
        get() {
            val caps = connectivityManager.getNetworkCapabilities(
                connectivityManager.activeNetwork,
            ) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
}