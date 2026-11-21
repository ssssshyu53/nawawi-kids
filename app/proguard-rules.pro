# Keep kotlinx.serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nawawikids.app.model.**$$serializer { *; }
-keepclassmembers class com.nawawikids.app.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.nawawikids.app.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
