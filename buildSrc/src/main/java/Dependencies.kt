@file:Suppress("unused")
// items here are not correctly detected by IDE plugins, therefore need to be marked manually

object Versions {
    const val minSDK = 26
    const val targetSDK = 33
    const val buildToolsVersion = "32.0.0"
    const val kotlin = "1.7.10"
    const val dagger = "2.24"
    const val retrofit = "2.9.0"
    const val okHttp3 = "4.9.0"
    const val lifecycleVersion = "2.5.1"
    const val googleServices = "4.3.14"
    const val appCenterSdk = "4.1.0"
    const val multidex_version = "2.0.1"
    const val jjwtVersion = "0.11.2"

    //Warning: update to 10.12.0 causes issue:
    //https://billennium.visualstudio.com/MF.NKSPO/_workitems/edit/191735/
    const val realm_version = "10.5.0"
    
    const val navVersion = "2.5.3"
    
    const val biometricVersion = "1.0.1"

    // firebase related
    const val firebase = "31.0.1"
    const val crashlyticsPlugin = "2.9.2"

    // compose related
    const val compose_compiler_version = "1.3.1"
    const val compose_bom_version = "2022.10.00"
}

object Dependencies {
    const val tests_junit = "junit:junit:4.12"
    const val tests_androidX_junit = "androidx.test.ext:junit:1.1.1"
    const val tests_coreX = "androidx.arch.core:core-testing:2.1.0"

    /*
      Consider adding testProguardFile 'proguard-test.pro', if we would run instrumentation
      tests with minifyEnabled true in the future.
    */
    const val tests_androidX_espresso = "androidx.test.espresso:espresso-core:3.2.0"
    const val tests_mockito = "org.mockito:mockito-core:3.4.4"

    //No proguard rules were found, that would be necessary
    const val tests_mockk = "io.mockk:mockk:1.10.0"

    const val kotlin_StandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_ktxCore = "androidx.core:core-ktx:1.5.0"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0"
    const val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0"

    //Most androidx libraries includes proguard rules automatically (see configuration.txt)
    const val androidX_AppCompat = "androidx.appcompat:appcompat:1.5.1"
    const val androidX_constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.1"
    const val androidX_lifecycle =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleVersion}"
    const val androidX_viewmodel =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycleVersion}"
    const val androidX_viewmodel_savedstate =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycleVersion}"
    const val androidX_multidex = "androidx.multidex:multidex:${Versions.multidex_version}"

    const val androidX_navigationComponent_fragment =
        "androidx.navigation:navigation-fragment-ktx:${Versions.navVersion}"
    const val androidX_navigationComponent_uiKtx =
        "androidx.navigation:navigation-ui-ktx:${Versions.navVersion}"
    const val androidX_biometric =
        "androidx.biometric:biometric:${Versions.biometricVersion}"

    //According to dagger doc w don't need any proguard rules (https://proguard-rules.blogspot.com/2017/05/dagger-2-proguard-rules.html)
    const val dagger_core = "com.google.dagger:dagger:${Versions.dagger}"
    const val dagger_compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val dagger_compiler_annotations = "javax.annotation:jsr250-api:1.0"

    //No extra proguard rules found, that would be necessary
    const val rx_kotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
    const val rx_android = "io.reactivex.rxjava2:rxandroid:2.1.1"
    const val rx_java = "io.reactivex.rxjava2:rxjava:2.2.13"


    //Retrofit automatically includes some proguard rules (see configuration.txt)
    const val networking_retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val networking_retrofit2_gsonadapter =
        "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val networking_retrofit2_rxadapter =
        "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val networking_retrofit2_interceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.okHttp3}"
    const val networking_okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okHttp3}"

    //"From v.2.9.9 you don't need to add proguard rules"
    //(https://stackoverflow.com/questions/27844261/using-joda-time-android-with-proguard)
    const val utils_jodatime = "joda-time:joda-time:2.10.6"


    //security-crypto automatically includes proguard rules (see configuration.txt)
    const val security_crypto = "androidx.security:security-crypto:1.1.0-alpha02"

    //Proguard rules added
    const val networking_jjwt = "io.jsonwebtoken:jjwt-api:${Versions.jjwtVersion}"
    const val networking_jjwt_impl = "io.jsonwebtoken:jjwt-impl:${Versions.jjwtVersion}"
    const val networking_jjwt_orgjson = "io.jsonwebtoken:jjwt-orgjson:${Versions.jjwtVersion}"

    //Realm automatically includes proguard rules (see configuration.txt)
    const val realm_annotations_processor =
        "io.realm:realm-annotations-processor:${Versions.realm_version}"
    const val realm_annotations = "io.realm:realm-annotations:${Versions.realm_version}"

    //No proguard rules were found, that would be necessary
    const val play_services_location = "com.google.android.gms:play-services-location:19.0.1"
    const val play_services_maps = "com.google.android.gms:play-services-maps:18.0.2"

    const val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"

    const val material_design = "com.google.android.material:material:1.2.0"

    //No proguard rules were found, that would be necessary
    const val viewpager_indicator = "com.tbuonomo:dotsindicator:4.2"

    const val safetyNet = "com.google.android.gms:play-services-safetynet:17.0.0"

    //No proguard rules were found, that would be necessary
    const val rootBeer = "com.scottyab:rootbeer-lib:0.0.8"

    // https://mvnrepository.com/artifact/org.lz4/lz4-java
    const val lz4 = "org.lz4:lz4-java:1.7.1"

    // pdf stuff
    // This is 3.1.0-beta1, check for newer releases here
    // https://jitpack.io/#barteksc/AndroidPdfViewer
    const val pdf = "com.github.barteksc:AndroidPdfViewer:master-SNAPSHOT"

    const val lottie = "com.airbnb.android:lottie:5.2.0"

    const val firebaseCore = "com.google.firebase:firebase-bom:${Versions.firebase}"
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics"
    const val firebase_messaging = "com.google.firebase:firebase-messaging-ktx:23.1.0"

    //Compose
    const val composeBom =  "androidx.compose:compose-bom:${Versions.compose_bom_version}"
    const val composeMaterial = "androidx.compose.material:material"
    const val composeUiTooling = "androidx.compose.ui:ui-tooling"
    const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview"

    const val composeUiTestsJUnit = "androidx.compose.ui:ui-test-junit4"
    const val composeUiTestsManifest = "androidx.compose.ui:ui-test-manifest"

    const val composeActivity = "androidx.activity:activity-compose:1.6.1"
    const val composeViewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1"
    const val composeLiveData = "androidx.compose.runtime:runtime-livedata"

    const val composeHtml = "com.github.ireward:compose-html:1.0.2"
}