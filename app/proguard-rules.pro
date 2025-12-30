# ProGuard Rules for Laitoxx Android Security Toolkit
# Optimized for security and minimal APK size

# ========================
# ESSENTIAL ATTRIBUTES
# ========================
# Keep annotations for Retrofit, Room, etc.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# ========================
# GSON / JSON SERIALIZATION
# ========================
# SECURITY: Only keep what's necessary for JSON parsing
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.annotations.** { *; }

# Keep generic signatures for Gson
-keepattributes Signature

# Keep data model classes for JSON serialization/deserialization
# These need to match JSON field names exactly
-keep class com.laitoxx.security.data.model.** { *; }

# Prevent obfuscation of field names in model classes
-keepclassmembers class com.laitoxx.security.data.model.** {
    <fields>;
}

# ========================
# RETROFIT & OKHTTP
# ========================
# Retrofit interface methods must be preserved
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit service method annotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ========================
# KOTLIN COROUTINES
# ========================
# Keep coroutines internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ========================
# KOTLIN SERIALIZATION
# ========================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========================
# ANDROID COMPONENTS
# ========================
# Keep all View constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ========================
# JETPACK COMPOSE
# ========================
# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Keep @Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }

# ========================
# CHAQUOPY PYTHON INTEGRATION
# ========================
# CRITICAL: Keep Python bridge classes and methods
-keep class com.chaquo.python.** { *; }
-keep class com.laitoxx.security.python.PythonBridge { *; }

# Keep Python module access methods
-keepclassmembers class com.laitoxx.security.python.PythonBridge {
    public *;
}

# ========================
# SECURITY: MINIMAL EXPOSURE
# ========================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep only necessary exception information
-keepattributes Exceptions

# ========================
# OPTIMIZATION FLAGS
# ========================
# Enable aggressive optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Optimization filters
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ========================
# WARNINGS SUPPRESSION
# ========================
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.**
