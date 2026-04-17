import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

buildscript {
    dependencies {
        classpath(libs.gradle)
    }
}

val vProtobuf = "3.25.3"

val FRAMEWORK_PREBUILTS_DIR = "$rootDir/prebuilt/libs"
val addFrameworkJar = { name: String ->
    val frameworkJar = File(FRAMEWORK_PREBUILTS_DIR, name)
    if (!frameworkJar.exists()) {
        throw IllegalArgumentException("Framework jar path ${frameworkJar.path} doesn't exist")
    }
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile>().configureEach {
            classpath = files(frameworkJar, classpath)
        }
        tasks.withType<KotlinCompile>().configureEach {
            libraries.setFrom(files(frameworkJar, libraries))
        }
    }
    dependencies {
        compileOnly(files(frameworkJar))
    }
}
addFrameworkJar("framework-16.jar")

fun Project.stringProperty(name: String): String = findProperty(name)?.toString().orEmpty()

fun Project.booleanProperty(name: String, defaultValue: Boolean = false): Boolean =
    findProperty(name)?.toString()?.toBooleanStrictOrNull() ?: defaultValue

fun Project.activeProperty(
    productionName: String,
    testName: String,
    isTestMode: Boolean
): String {
    if (!isTestMode) {
        return stringProperty(productionName)
    }
    return stringProperty(testName).ifBlank { stringProperty(productionName) }
}

val isTopOnTestMode = booleanProperty("TOPON_TEST_MODE")
val activeTopOnAppId = activeProperty("TOPON_APP_ID", "TEST_TOPON_APP_ID", isTopOnTestMode)
val activeTopOnAppKey = activeProperty("TOPON_APP_KEY", "TEST_TOPON_APP_KEY", isTopOnTestMode)
val activeTopOnSplashPlacementId = activeProperty(
    "TOPON_SPLASH_PLACEMENT_ID",
    "TEST_TOPON_SPLASH_PLACEMENT_ID",
    isTopOnTestMode
)
val activeTopOnInterstitialPlacementId = activeProperty(
    "TOPON_INTERSTITIAL_PLACEMENT_ID",
    "TEST_TOPON_INTERSTITIAL_PLACEMENT_ID",
    isTopOnTestMode
)
val activeTopOnRewardedPlacementId = activeProperty(
    "TOPON_REWARDED_PLACEMENT_ID",
    "TEST_TOPON_REWARDED_PLACEMENT_ID",
    isTopOnTestMode
)
val activeTopOnBannerPlacementId = activeProperty(
    "TOPON_BANNER_PLACEMENT_ID",
    "TEST_TOPON_BANNER_PLACEMENT_ID",
    isTopOnTestMode
)
val activeTopOnNativePlacementId = activeProperty(
    "TOPON_NATIVE_PLACEMENT_ID",
    "TEST_TOPON_NATIVE_PLACEMENT_ID",
    isTopOnTestMode
)
val activeTopOnPackageName = activeProperty(
    "APP_PACKAGE_NAME",
    "TEST_TOPON_APP_PACKAGE_NAME",
    isTopOnTestMode
)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.ksp)
}
allprojects {
    plugins.withType<AndroidBasePlugin>().configureEach {
        extensions.configure<BaseExtension> {
            buildToolsVersion = "36.1.0"

            defaultConfig {
                minSdk = 26
                targetSdk = 36
                vectorDrawables.useSupportLibrary = true
            }
            compileOptions {
                sourceCompatibility = JavaVersion.toVersion(21)
                targetCompatibility = JavaVersion.toVersion(21)
            }
        }
        dependencies {
            add("implementation", libs.core.ktx)
            add("implementation", platform(libs.compose.bom))
        }
    }
}

configurations.all {
    resolutionStrategy {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.android.launcher3"
    compileSdk = 36

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    defaultConfig {
        minSdk = 30
        targetSdk = 36
        applicationId = "com.video.downloader"
        javaCompileOptions.annotationProcessorOptions.arguments["dagger.hilt.disableModulesHaveInstallInCheck"] =
            "true"
        versionName = "1.0.10"
        versionCode = 1010
        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        buildConfigField("boolean", "ENABLE_AUTO_INSTALLS_LAYOUT", "false")
        buildConfigField("boolean", "IS_DEBUG_DEVICE", "false")
        buildConfigField("boolean", "IS_STUDIO_BUILD", "false")
        buildConfigField("boolean", "WIDGETS_ENABLED", "true")
        buildConfigField("boolean", "NOTIFICATION_DOTS_ENABLED", "true")
        buildConfigField("boolean", "WIDGET_ON_FIRST_SCREEN", "true")
        manifestPlaceholders["toponTestMode"] = isTopOnTestMode.toString()
        manifestPlaceholders["toponAppPackageName"] = activeTopOnPackageName
        manifestPlaceholders["toponAppId"] = activeTopOnAppId
        manifestPlaceholders["toponAppKey"] = activeTopOnAppKey
        manifestPlaceholders["toponSplashPlacementId"] =
            activeTopOnSplashPlacementId
        manifestPlaceholders["toponInterstitialPlacementId"] =
            activeTopOnInterstitialPlacementId
        manifestPlaceholders["toponRewardedPlacementId"] =
            activeTopOnRewardedPlacementId
        manifestPlaceholders["toponBannerPlacementId"] =
            activeTopOnBannerPlacementId
        manifestPlaceholders["toponNativePlacementId"] =
            activeTopOnNativePlacementId

        val langsList: MutableSet<String> = HashSet()

        // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
        fileTree("res").visit {
            if (this.file.path.endsWith("strings.xml")
                && this.file.canonicalFile.readText().contains("<string")
            ) {
                var languageCode = this.file.parentFile?.name?.replace("values-", "")
                languageCode = if (languageCode == "values") "en" else languageCode
                languageCode?.let {
                    langsList.add(languageCode)
                }
            }
        }
        val langsListString = "{${langsList.sorted().joinToString(",") { "\"${it}\"" }}}"
        buildConfigField("String[]", "DETECTED_ANDROID_LOCALES", langsListString)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Launcher_${variant.versionName}_${variant.buildType.name}.apk"
        }
        variant.resValue(
            "string",
            "launcher_component",
            "${variant.applicationId}/com.neoapps.neolauncher.NeoLauncher"
        )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        register("neo") {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false
            setProguardFiles(listOf("proguard-android-optimize.txt", "proguard.flags"))
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("primary") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        aidl = true
    }

    packaging {
        jniLibs {
            pickFirsts += listOf("**/libeasyBypass.so")
        }
        resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin") // TODO remove when issue is fixed (https://github.com/Kotlin/kotlinx.coroutines/issues/3668)
    }

    flavorDimensionList.clear()
    flavorDimensionList.addAll(listOf("app", "custom"))

    productFlavors {
        create("aosp") {
            dimension = "app"
            testApplicationId = "com.android.launcher3.tests"
        }

        create("omega") {
            dimension = "custom"
        }
    }

    sourceSets {
        named("main") {
            java.directories.addAll(listOf("src", "src_plugins", "src_no_quickstep", "compose"))
            kotlin.directories.addAll(listOf("src", "src_plugins", "src_no_quickstep", "compose"))
            res.directories.add("res")
            assets.directories.add("assets")
            manifest.srcFile("AndroidManifest-common.xml")
        }
        named("aosp") {
            java.directories.addAll(listOf("src_flags"))
            kotlin.directories.addAll(listOf("src_flags"))
        }
        named("omega") {
            java.directories.addAll(listOf("Omega/src"))
            kotlin.directories.addAll(listOf("Omega/src"))
            res.directories.add("Omega/res")
            aidl.directories.add("Omega/aidl")
            manifest.srcFile("Omega/AndroidManifest.xml")
        }
        named("test") {
            java.directories.addAll(listOf("unitTest"))
            kotlin.directories.addAll(listOf("unitTest"))
        }

        protobuf {
            // Configure the protoc executable
            protoc {
                artifact = "com.google.protobuf:protoc:$vProtobuf"
            }
            generateProtoTasks {
                all().forEach { task ->
                    task.builtins {
                        create("java") {
                            option("lite")
                        }
                    }
                }
            }
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}

dependencies {

    implementation(project(":animationlib"))
    implementation(project(":concurrent"))
    implementation(project(":downloaderlib"))
    implementation(project(":topon-ads"))
    implementation(project(":iconloaderlib"))
    implementation(project(":flags"))
    implementation(project(":msdllib"))
    implementation(project(":plugincore"))
    implementation(project(":shared"))
    implementation(project(":smartspace"))
    implementation(project(":widgetpicker"))
    implementation(project(":wmshell"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/SystemUI-core-16.jar"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/SystemUI-statsd-16.jar"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/WindowManager-Shell-16.jar"))

    implementation(libs.accompanist.drawablepainter)
    implementation(libs.alwan)
    implementation(libs.annotation)
    implementation(libs.coil.compose)
    implementation(libs.collections.immutable)
    implementation(libs.compose.activity)
    implementation(libs.compose.adaptive)
    implementation(libs.compose.adaptive.layout)
    implementation(libs.compose.adaptive.navigation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.navigation)
    implementation(libs.compose.reorderable)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.constraint.layout)
    implementation(libs.coordinator.layout)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(downloaderLibs.daggerRuntime)
    implementation(downloaderLibs.daggerAndroid)
    implementation(downloaderLibs.daggerAndroidSupport)
    implementation(libs.dynamic.animation)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.fuzzywuzzy)
    implementation(libs.graphics.shapes)
    implementation(libs.google.mobile.ads)
    implementation(libs.guava)
    implementation(libs.hilt.compiler)
    ksp(libs.hilt.android)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.kotlin.stdlib) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    implementation(libs.koin.workmanager)
    implementation(libs.jakarta.inject)
    implementation(libs.java.inject)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.material)
    implementation(libs.material.kolor)
    implementation(libs.okhttp)
    implementation(libs.owm)
    implementation(libs.palette.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.protobuf.javalite)
    //implementation(libs.restriction.bypass)
    implementation(libs.recyclerview)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.serialization.json)
    implementation(libs.slice.core)
    coreLibraryDesugaring(downloaderLibs.desugarJdk)

    api(platform(libs.compose.bom))
    protobuf(files("protos/"))
    protobuf(files("protos_overrides/"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.dexmaker.mockito)
    androidTestImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.test.junit)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.uiautomator.v18)

    androidTestImplementation(libs.dexmaker.lib)
}

// Returns the build date in a RFC3339 compatible format. TZ is always converted to UTC
fun getBuildDate(): String {
    val RFC3339_LIKE = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    RFC3339_LIKE.timeZone = TimeZone.getTimeZone("UTC")
    return RFC3339_LIKE.format(Date())
}
