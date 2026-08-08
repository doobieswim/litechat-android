package com.litechat.android.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.litechat.android.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One-time Pro product (remove ads). SKU from BuildConfig.PLAY_PRO_SKU.
 */
class BillingRepository(context: Context) : PurchasesUpdatedListener {

    private val _proOwned = MutableStateFlow(false)
    val proOwned: StateFlow<Boolean> = _proOwned.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private var purchaseCallback: ((Boolean, String?) -> Unit)? = null

    @Suppress("DEPRECATION")
    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startConnection(onReady: (Boolean) -> Unit = {}) {
        if (client.isReady) {
            onReady(true)
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                val ok = result.responseCode == BillingClient.BillingResponseCode.OK
                if (ok) {
                    queryProduct()
                    queryOwned()
                }
                onReady(ok)
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    private fun queryProduct() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BuildConfig.PLAY_PRO_SKU)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = list.firstOrNull()
            }
        }
    }

    fun queryOwned() {
        if (!client.isReady) {
            startConnection { if (it) queryOwned() }
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val owned = purchases.any { p ->
                    p.products.contains(BuildConfig.PLAY_PRO_SKU) &&
                        p.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _proOwned.value = owned
                purchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                }.forEach { acknowledge(it) }
            }
        }
    }

    fun launchPurchase(activity: Activity, onResult: (Boolean, String?) -> Unit) {
        purchaseCallback = onResult
        if (!client.isReady) {
            startConnection { ready ->
                if (!ready) {
                    onResult(false, "Billing unavailable")
                    return@startConnection
                }
                launchPurchase(activity, onResult)
            }
            return
        }
        val details = _productDetails.value
        if (details == null) {
            queryProduct()
            onResult(false, "Product not loaded yet — try again")
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        client.launchBillingFlow(activity, flow)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val ok = purchases?.any {
                    it.products.contains(BuildConfig.PLAY_PRO_SKU) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                } == true
                if (ok) {
                    _proOwned.value = true
                    purchases?.forEach { if (!it.isAcknowledged) acknowledge(it) }
                }
                purchaseCallback?.invoke(ok, null)
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                purchaseCallback?.invoke(false, "Canceled")
            else -> purchaseCallback?.invoke(false, result.debugMessage)
        }
        purchaseCallback = null
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { /* ignore */ }
    }

    fun endConnection() {
        client.endConnection()
    }
}
