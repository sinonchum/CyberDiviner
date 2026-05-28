# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.cyberdiviner.**$$serializer { *; }
-keepclassmembers class com.cyberdiviner.** { *** Companion; }
-keepclasseswithmembers class com.cyberdiviner.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# MediaPipe (native methods)
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.proto.CalculatorProfileProto$CalculatorProfile
-dontwarn com.google.mediapipe.proto.GraphTemplateProto$CalculatorGraphTemplate

# javax.lang.model (used by MediaPipe internally)
-dontwarn javax.lang.model.**
-dontwarn javax.lang.model.element.**
-dontwarn javax.lang.model.type.**
-dontwarn javax.lang.model.util.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel