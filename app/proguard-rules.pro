# Keep streaming / reflection-free paths small
-keepattributes *Annotation*, InnerClasses
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.litechat.android.data.api.** { *; }
# Play Billing
-keep class com.android.vending.billing.** { *; }
# Tink (androidx.security-crypto) references error_prone annotations that are
# compile-only; R8 is strict about missing classes but these are never loaded.
-dontwarn com.google.errorprone.annotations.**
