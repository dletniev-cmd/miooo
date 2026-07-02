# Default ProGuard rules.
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# --- Safety keep rules (in case minification is re-enabled later) ---
# Coil + its SVG decoder and the underlying AndroidSVG renderer resolve
# classes at runtime; keep them so R8 can't strip/rename them.
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**
# Haze (declared as a dependency); keep defensively.
-keep class dev.chrisbanes.haze.** { *; }
-dontwarn dev.chrisbanes.haze.**