package com.litechat.android.data.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * C-001: AdMob SDK initialization is lazy and happens at most once per process.
 *
 * Cold start no longer calls [MobileAds.initialize]; the banner path calls
 * [ensureInitialized] right before creating an [com.google.android.gms.ads.AdView],
 * so Pro users (who never see a banner) never trigger the ads SDK at startup.
 *
 * Note: the play-services-ads AAR ships an auto-init ContentProvider
 * (`com.google.android.gms.ads.MobileAdsInitProvider`) that would otherwise warm
 * the SDK at app start for every user. It is removed in AndroidManifest.xml via
 * `tools:node="remove"` so this explicit call is the *only* init path.
 *
 * Thread-safe: exactly one caller (any thread) performs the init.
 */
object AdMobLazyInit {
    private val initialized = AtomicBoolean(false)

    /** Idempotent. Safe to call multiple times / from any thread. */
    fun ensureInitialized(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            // C-032: EEA/UK-safe branch — non-personalized ads only. play-services-ads
            // 23.x removed AdRequest's npa/setNonPersonalizedAds APIs; the supported
            // non-personalized switch is RequestConfiguration, so no UMP consent SDK
            // is needed (zero APK cost). Satisfies the Play-policy "consent OR
            // non-personalized-only" requirement.
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setPublisherPrivacyPersonalizationState(
                        RequestConfiguration.PublisherPrivacyPersonalizationState.DISABLED
                    )
                    .build()
            )
            MobileAds.initialize(context.applicationContext) {}
        }
    }
}
