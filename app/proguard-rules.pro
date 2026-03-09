# Moshi — keep generated adapters and all annotated classes
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
# Keep KSP-generated JsonAdapter classes (named <Class>JsonAdapter)
-keep class **JsonAdapter { *; }
-keep class **JsonAdapter$* { *; }

# Keep Kotlin metadata so type tokens survive ProGuard
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault
-keep class kotlin.Metadata { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }

# Xtream DTOs
-keep class com.twomax.live.data.remote.dto.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Coil
-keep class coil3.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# Domain models (keep for Room + serialization)
-keep class com.twomax.live.core.model.** { *; }
-keep class com.twomax.live.data.local.db.entity.** { *; }
