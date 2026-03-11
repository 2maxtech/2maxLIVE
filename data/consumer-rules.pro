# Retrofit — preserve generic signatures so it can resolve List<T> return types
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep interface com.twomax.live.data.remote.api.** { *; }
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Moshi — keep all generated JsonAdapters and annotated classes
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class **JsonAdapter { *; }
-keep class **JsonAdapter$* { *; }

# Xtream DTOs and generated adapters
-keep class com.twomax.live.data.remote.dto.** { *; }

# Domain models used by Room and the rest of the app
-keep class com.twomax.live.core.model.** { *; }
-keep class com.twomax.live.data.local.db.entity.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Kotlin metadata needed for type reflection
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
