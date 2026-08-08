# Keep streaming / reflection-free paths small
-keepattributes *Annotation*, InnerClasses
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.litechat.android.data.api.** { *; }
# Play Billing
-keep class com.android.vending.billing.** { *; }
