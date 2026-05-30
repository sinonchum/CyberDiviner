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

# MediaPipe — keep ALL classes (tasks + framework + native bridge)
-keep class com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** {
    native <methods>;
    static <methods>;
}
-dontwarn com.google.mediapipe.proto.CalculatorProfileProto$CalculatorProfile
-dontwarn com.google.mediapipe.proto.GraphTemplateProto$CalculatorGraphTemplate

# CRITICAL: Flogger uses stack inspection (FluentLogger.forEnclosingClass)
# R8 obfuscation breaks the caller lookup → ExceptionInInitializerError
-keep class com.google.common.flogger.** { *; }
-keep class com.google.common.flogger.FluentLogger { *; }
-keepclassmembers class com.google.common.flogger.** {
    static <methods>;
}

# protobuf-javalite (used by MediaPipe GenAI)
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# javax.lang.model (used by MediaPipe internally)
-dontwarn javax.lang.model.**
-dontwarn javax.lang.model.element.**
-dontwarn javax.lang.model.type.**
-dontwarn javax.lang.model.util.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Lifecycle Compose — keep LocalLifecycleOwner (used by VisionScreen camera)
-keep class androidx.lifecycle.compose.** { *; }
-keep class androidx.compose.ui.platform.LocalLifecycleOwner* { *; }

# LiteRT-LM (on-device LLM, loaded through reflection)
-keep class com.google.ai.edge.litertlm.** { *; }
-keepclassmembers class com.google.ai.edge.litertlm.** { *; }
-dontwarn com.google.ai.edge.litertlm.**
