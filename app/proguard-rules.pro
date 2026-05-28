# Keep annotation and generic-signature metadata used by Kotlin serialization,
# Navigation 3 route keys, and ML Kit consumer rules after R8 optimization.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,Signature,InnerClasses,EnclosingMethod

# ML Kit discovers these registrars from AndroidManifest metadata and creates
# them reflectively during provider startup. Keep their names and constructors.
-keep class * implements com.google.firebase.components.ComponentRegistrar {
  public <init>();
}
