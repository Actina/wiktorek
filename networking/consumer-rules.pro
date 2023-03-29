
#1. Models' fields' with @SerializedName without this rule ARE NOT PROTECTED FROM BEING ALTERED BY THE PROGUARD
#(Please disable rule below, build and decompile apk, and find sample model class in mapping.txt to see it)
#2. Even if app works fine with changed fields' names', we can't be sure that it will be working
# fine during all another builds (please see link below). So we have to keep and not rename all fields in all models.
# On the other side models' classes' names can be obfuscated.
# https://medium.com/androiddevelopers/practical-proguard-rules-examples-5640a3907dc9
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Rules from jjwt start ---
# Note. Underlined rules are also required!
-keepattributes InnerClasses
-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }

-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
# Rules from jjwt end ---

#rules for lz4
-keep class net.jpountz.** { *; }