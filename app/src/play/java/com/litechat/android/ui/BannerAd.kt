package com.litechat.android.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.litechat.android.BuildConfig
import com.litechat.android.data.ads.AdMobLazyInit

/** Play flavor: real AdMob banner (C-001 lazy init, C-002 play-only). */
@Composable
fun BannerAd() {
    val unitId = BuildConfig.ADMOB_BANNER_ID
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            // C-001: one-time lazy SDK init on first banner need (non-Pro only —
            // this composable is never created for Pro users).
            AdMobLazyInit.ensureInitialized(ctx)
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
