# Keep Hilt-generated classes and metadata.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

# Firebase and Google Play services ship consumer Proguard rules.
# Avoid blanket SDK-wide keep rules here, otherwise release shrinking is heavily degraded.

# Keep Kotlin metadata used by libraries relying on reflection.
-keep class kotlin.Metadata { *; }

# Keep coroutine debug metadata classes referenced indirectly.
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# Keep enum names used in persisted/network payloads.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
