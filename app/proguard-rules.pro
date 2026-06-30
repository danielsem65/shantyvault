-keepattributes Signature
-keepattributes *Annotation*

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep Gson models
-keep class com.shanty.vault.data.model.** { *; }
-keep class com.shanty.vault.domain.model.** { *; }

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
