# --- Jetpack Compose Core ---
-keep class androidx.compose.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.savedstate.** { *; }

# --- Material & UI (Optional but Recommended) ---
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.runtime.saveable.** { *; }

# --- Keep your resource references (themes/colors/fonts) ---
-keepclassmembers class **.R$* {
    public static <fields>;
}

