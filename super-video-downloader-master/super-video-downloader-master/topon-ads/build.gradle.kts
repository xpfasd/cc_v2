plugins {
    id("com.android.library")
}

if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION.substringBefore(".").toInt() < 9) {
    pluginManager.apply("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cc.ads.topon"
    compileSdk = downloaderLibs.versions.targetSdk.get().toInt()

    defaultConfig {
        minSdk = downloaderLibs.versions.minSdk.get().toInt()
        consumerProguardFiles("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
//TU (Necessary)
    api("com.thinkup.sdk:core-tpn:6.5.73")
//Androidx (Necessary)
    api("androidx.appcompat:appcompat:1.6.1")
    api("androidx.browser:browser:1.4.0")
//Vungle
    api("com.thinkup.sdk:adapter-tpn-vungle:7.6.1.1.1")
    api("com.vungle:vungle-ads:7.6.1")
    api("com.google.android.gms:play-services-basement:18.1.0")
    api("com.google.android.gms:play-services-ads-identifier:18.0.1")
//Bigo
    api("com.thinkup.sdk:adapter-tpn-bigo:5.7.1.1.1")
    api("com.bigossp:bigo-ads:5.7.1")
//Pangle
    api("com.thinkup.sdk:adapter-tpn-pangle:7.9.1.0.1.0")
    api("com.pangle.global:pag-sdk:7.9.1.0")
    api("com.google.android.gms:play-services-ads-identifier:18.2.0")
//Facebook
    api("com.thinkup.sdk:adapter-tpn-facebook:6.21.0.1.1")
    api("com.facebook.android:audience-network-sdk:6.21.0")
    api("androidx.annotation:annotation:1.0.0")
//Admob
    api("com.thinkup.sdk:adapter-tpn-admob:25.0.0.1.0")
    api("com.google.android.gms:play-services-ads:25.0.0")
//TU Adx SDK(Necessary)
    api("com.thinkup.sdk:adapter-tpn-sdm:6.5.56.1.1")
    api("com.smartdigimkttech.sdk:smartdigimkttech-sdk:6.5.56")
//Tramini
    api("com.thinkup.sdk:tramini-plugin-tpn:6.5.73")
}
