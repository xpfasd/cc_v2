import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import java.util.Properties
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("com.google.devtools.ksp")
}

val isEmbeddedDownloaderLibrary = project.name == "downloaderlib"

if (isEmbeddedDownloaderLibrary) {
    apply(plugin = "com.android.library")
} else {
    apply(plugin = "com.android.application")
}

if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION.substringBefore(".").toInt() < 9) {
    pluginManager.apply("org.jetbrains.kotlin.android")
}

configurations.configureEach {
    resolutionStrategy {
        force(downloaderLibs.kotlin.stdlib)
    }
}

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
val activeAdmobAppId = activeProperty("ADMOB_APP_ID", "TEST_ADMOB_APP_ID", isTopOnTestMode)
val googleServicesProductionFile = file("google-services-production.json")
val googleServicesTestFile = file("google-services-test.json")
val googleServicesOutputFile = file("google-services.json")

tasks.register("prepareGoogleServicesJson") {
    inputs.property("toponTestMode", isTopOnTestMode)
    inputs.files(googleServicesProductionFile, googleServicesTestFile)
    outputs.file(googleServicesOutputFile)

    doLast {
        val selectedFile = if (isTopOnTestMode) {
            googleServicesTestFile
        } else {
            googleServicesProductionFile
        }

        if (!selectedFile.exists()) {
            if (googleServicesOutputFile.exists()) {
                googleServicesOutputFile.delete()
            }
            logger.warn(
                "Missing ${selectedFile.name}. Skipping google-services.json materialization for " +
                    if (isTopOnTestMode) "test mode." else "production mode."
            )
            return@doLast
        }

        selectedFile.copyTo(googleServicesOutputFile, overwrite = true)
        logger.lifecycle("Prepared ${googleServicesOutputFile.name} from ${selectedFile.name}")
    }
}

plugins.withId("com.android.application") {
    extensions.configure<ApplicationExtension> {
        namespace = "com.myAllVideoBrowser"
        compileSdk = downloaderLibs.versions.targetSdk.get().toInt()
        ndkVersion = downloaderLibs.versions.ndk.get()

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
            isCoreLibraryDesugaringEnabled = true
        }

        defaultConfig {
            applicationId = activeTopOnPackageName
            minSdk = downloaderLibs.versions.minSdk.get().toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            manifestPlaceholders["toponTestMode"] = isTopOnTestMode.toString()
            manifestPlaceholders["toponAppId"] = activeTopOnAppId
            manifestPlaceholders["toponAppKey"] = activeTopOnAppKey
            manifestPlaceholders["toponSplashPlacementId"] = activeTopOnSplashPlacementId
            manifestPlaceholders["toponInterstitialPlacementId"] = activeTopOnInterstitialPlacementId
            manifestPlaceholders["toponRewardedPlacementId"] = activeTopOnRewardedPlacementId
            manifestPlaceholders["toponBannerPlacementId"] = activeTopOnBannerPlacementId
            manifestPlaceholders["toponNativePlacementId"] = activeTopOnNativePlacementId
            manifestPlaceholders["toponAppPackageName"] = activeTopOnPackageName
            manifestPlaceholders["admobAppId"] = activeAdmobAppId
            buildConfigField("boolean", "TOPON_TEST_MODE", isTopOnTestMode.toString())
            buildConfigField("String", "TOPON_ACTIVE_PACKAGE_NAME", "\"$activeTopOnPackageName\"")
            buildConfigField("String", "ADMOB_APP_ID", "\"$activeAdmobAppId\"")
        }

        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                isShrinkResources = false
            }
            maybeCreate("neo").apply {
                initWith(getByName("debug"))
                matchingFallbacks += listOf("debug")
            }
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        buildFeatures {
            buildConfig = true
        }
        viewBinding {
            enable = true
        }
        dataBinding {
            enable = true
        }

        packaging {
            resources {
                excludes += listOf(
                    "mozilla/public-suffix-list.txt",
                    "META-INF/*.kotlin_module",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/NOTICE",
                    "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/ASL2.0"
                )
            }
            jniLibs {
                useLegacyPackaging = true
                keepDebugSymbols += listOf(
                    "**/libffmpeg.zip.so",
                    "**/libpython.zip.so",
                    "**/libffmpeg.so",
                    "**/libffprobe.so",
                    "**/libgojni.so",
                    "**/libpython.so",
                    "**/libqjs.so"
                )
            }
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }

        lint {
            abortOnError = false
        }

        sourceSets {
            getByName("main") {
                manifest.srcFile("src/main/AndroidManifest.xml")
                kotlin.directories.add("src/main/java")
                res.directories.add("src/main/res")
                assets.directories.add("src/main/assets")
                jniLibs.directories.add("src/main/jniLibs")
            }
        }
    }
}

plugins.withId("com.android.library") {
    extensions.configure<LibraryExtension> {
        namespace = "com.myAllVideoBrowser"
        compileSdk = downloaderLibs.versions.targetSdk.get().toInt()
        ndkVersion = downloaderLibs.versions.ndk.get()

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
            isCoreLibraryDesugaringEnabled = true
        }

        defaultConfig {
            minSdk = downloaderLibs.versions.minSdk.get().toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            manifestPlaceholders["toponTestMode"] = isTopOnTestMode.toString()
            manifestPlaceholders["toponAppId"] = activeTopOnAppId
            manifestPlaceholders["toponAppKey"] = activeTopOnAppKey
            manifestPlaceholders["toponSplashPlacementId"] = activeTopOnSplashPlacementId
            manifestPlaceholders["toponInterstitialPlacementId"] = activeTopOnInterstitialPlacementId
            manifestPlaceholders["toponRewardedPlacementId"] = activeTopOnRewardedPlacementId
            manifestPlaceholders["toponBannerPlacementId"] = activeTopOnBannerPlacementId
            manifestPlaceholders["toponNativePlacementId"] = activeTopOnNativePlacementId
            manifestPlaceholders["toponAppPackageName"] = activeTopOnPackageName
            manifestPlaceholders["admobAppId"] = activeAdmobAppId
            buildConfigField("boolean", "TOPON_TEST_MODE", isTopOnTestMode.toString())
            buildConfigField("String", "TOPON_ACTIVE_PACKAGE_NAME", "\"$activeTopOnPackageName\"")
            buildConfigField("String", "ADMOB_APP_ID", "\"$activeAdmobAppId\"")
        }

        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                isShrinkResources = false
            }
            maybeCreate("neo").apply {
                initWith(getByName("debug"))
                matchingFallbacks += listOf("debug")
            }
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        buildFeatures {
            buildConfig = true
        }
        viewBinding {
            enable = true
        }
        dataBinding {
            enable = true
        }

        packaging {
            resources {
                excludes += listOf(
                    "mozilla/public-suffix-list.txt",
                    "META-INF/*.kotlin_module",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/NOTICE",
                    "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/ASL2.0"
                )
            }
            jniLibs {
                useLegacyPackaging = true
                keepDebugSymbols += listOf(
                    "**/libffmpeg.zip.so",
                    "**/libpython.zip.so",
                    "**/libffmpeg.so",
                    "**/libffprobe.so",
                    "**/libgojni.so",
                    "**/libpython.so",
                    "**/libqjs.so"
                )
            }
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }

        lint {
            abortOnError = false
        }

        sourceSets {
            getByName("main") {
                manifest.srcFile("src/main/AndroidManifest.xml")
                kotlin.directories.add("src/main/java")
                res.directories.add("src/main/res")
                assets.directories.add("src/main/assets")
                jniLibs.directories.add("src/main/jniLibs")
            }
        }
    }
}

dependencies {
    add("implementation", project(":topon-ads"))

    add("implementation", downloaderLibs.appcompat)
    add("implementation", downloaderLibs.material)
    add("implementation", downloaderLibs.constraintlayout)
    add("implementation", downloaderLibs.recyclerview)
    add("implementation", downloaderLibs.webkit)
    add("implementation", downloaderLibs.coreKtx)
    add("implementation", downloaderLibs.coreSplashscreen)
    add("implementation", downloaderLibs.legacySupportV4)
    add("implementation", downloaderLibs.kotlin.stdlib)
    add("implementation", downloaderLibs.workRuntimeKtx)
    add("implementation", downloaderLibs.workRxjava3)
    add("implementation", downloaderLibs.workMultiprocess)
    add("implementation", downloaderLibs.fragmentKtx)
    add("implementation", downloaderLibs.concurrentFuturesKtx)
    add("implementation", downloaderLibs.lifecycleExtensions)
    add("implementation", downloaderLibs.lifecycleCommonJava8)
    add("implementation", downloaderLibs.lifecycleLivedata)
    add("implementation", downloaderLibs.lifecycleViewmodel)
    add("implementation", downloaderLibs.roomRuntime)
    add("implementation", downloaderLibs.roomKtx)
    add("implementation", downloaderLibs.roomRxjava3)
    add("implementation", downloaderLibs.roomGuava)
    add("ksp", downloaderLibs.roomCompiler)
    add("implementation", downloaderLibs.daggerRuntime)
    add("implementation", downloaderLibs.daggerAndroid)
    add("implementation", downloaderLibs.daggerAndroidSupport)
    add("ksp", downloaderLibs.daggerCompiler)
    add("ksp", downloaderLibs.daggerAndroidProcessor)
    add("implementation", downloaderLibs.okHttpRuntime)
    add("implementation", downloaderLibs.okHttpLogging)
    add("implementation", downloaderLibs.retrofitRuntime)
    add("implementation", downloaderLibs.retrofitGson)
    add("implementation", downloaderLibs.retrofitRxjava3)
    add("implementation", downloaderLibs.persistentCookieJar)
    add("implementation", downloaderLibs.rxjava3)
    add("implementation", downloaderLibs.rxandroid3)
    add("implementation", downloaderLibs.youtubedl)
    //implementation(downloaderLibs.youtubedl.ffmpeg)
    add("implementation", downloaderLibs.ffmpegKit)
    add("implementation", downloaderLibs.media3Exoplayer)
    add("implementation", downloaderLibs.media3ExoplayerDash)
    add("implementation", downloaderLibs.media3ExoplayerHls)
    add("implementation", downloaderLibs.media3ExoplayerRtsp)
    add("implementation", downloaderLibs.media3Ui)
    add("implementation", downloaderLibs.media3Extractor)
    add("implementation", downloaderLibs.media3Database)
    add("implementation", downloaderLibs.media3Decoder)
    add("implementation", downloaderLibs.media3Datasource)
    add("implementation", downloaderLibs.media3Common)
    add("implementation", downloaderLibs.media3DatasourceOkhttp)
    add("implementation", downloaderLibs.glideRuntime)
    add("implementation", downloaderLibs.kotlinxSerializationJson)
    add("implementation", downloaderLibs.kotlinxSerializationCore)
    add("implementation", downloaderLibs.jsoup)
    add("implementation", downloaderLibs.timeago)
    add("coreLibraryDesugaring", downloaderLibs.desugarJdk)

    add("testImplementation", downloaderLibs.junit)
    add("testImplementation", downloaderLibs.mockitoCore)
    add("testImplementation", downloaderLibs.mockitoKotlin)
    add("androidTestImplementation", downloaderLibs.testRunner)
    add("androidTestImplementation", downloaderLibs.mockitoAndroid)
    add("androidTestImplementation", downloaderLibs.espressoCore)
    add("androidTestImplementation", downloaderLibs.espressoIntents)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "false")
    arg("room.expandProjection", "true")
}

tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("Kotlin") }.configureEach {
    doFirst {
        val kspCacheDir = layout.buildDirectory.dir("kspCaches").get().asFile
        if (kspCacheDir.exists()) {
            project.delete(kspCacheDir)
        }
    }
}

val v2rayRepo = "https://github.com/2dust/AndroidLibXrayLite.git"
val v2rayCommit = "93a711245dec705be8dd6aa6a47f8aafa7898c40"
val buildDirV2ray = file("${rootProject.layout.buildDirectory.get().asFile}/v2ray")
val nativeProxyBuildEnabled = false
val execOperations = project.serviceOf<ExecOperations>()

val goExecutable = run {
    val envOverride = System.getenv("GO_EXECUTABLE")
    if (envOverride != null && file(envOverride).exists()) {
        envOverride
    } else {
        val propOverride = project.findProperty("GO_EXECUTABLE")?.toString()
        if (propOverride != null && file(propOverride).exists()) {
            propOverride
        } else {
            "go"
        }
    }
}

val gitExecutable = "git"

fun findNdkPath(): String {
    val envVar = System.getenv("ANDROID_NDK_HOME") ?: System.getenv("ANDROID_NDK_ROOT")
    if (!envVar.isNullOrEmpty()) {
        return envVar
    }

    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().use { properties.load(it) }
        val propVar = properties.getProperty("ndk.dir")
        if (!propVar.isNullOrEmpty()) {
            return propVar
        }
    }

    throw GradleException(
        "NDK path not found. Please define ANDROID_NDK_HOME, ANDROID_NDK_ROOT, or ndk.dir in local.properties."
    )
}

fun resolveNdkPrebuiltFolder(ndkPath: String): String {
    val prebuiltToolchainsDir = file("$ndkPath/toolchains/llvm/prebuilt")
    if (!prebuiltToolchainsDir.exists()) {
        throw GradleException("NDK toolchains prebuilt directory not found at ${prebuiltToolchainsDir.path}")
    }
    val prebuiltChildren = prebuiltToolchainsDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
    if (prebuiltChildren.isEmpty()) {
        throw GradleException("No prebuilt toolchain directory found under ${prebuiltToolchainsDir.path}")
    }
    return prebuiltChildren.first().name
}

fun verifyGoExecutable(builderDir: File, executablePath: String) {
    execOperations.exec {
        workingDir(builderDir)
        commandLine(executablePath, "version")
    }
}

fun createGoModule(builderDir: File) {
    val goModFile = file("$builderDir/go.mod")
    goModFile.writeText(
        """
        module builder
        go 1.25.0

        replace gvisor.dev/gvisor => gvisor.dev/gvisor v0.0.0-20250606001031-fa4c4dd86b43
        """.trimIndent()
    )
    file("$builderDir/go.sum").delete()
}

fun goRelativePath(fromDir: File, targetDir: File): String {
    return fromDir.toPath()
        .relativize(targetDir.toPath())
        .toString()
        .replace(File.separatorChar, '/')
}

fun vendorGoDependencies(builderDir: File, executablePath: String) {
    val goEnv = mapOf("GOPROXY" to "https://proxy.golang.org,direct")
    val v2raySrcDir = file("$buildDirV2ray/src")
    val v2rayReplacePath = goRelativePath(builderDir, v2raySrcDir)
    execOperations.exec {
        workingDir(builderDir)
        environment(goEnv)
        commandLine(
            executablePath,
            "mod",
            "edit",
            "-replace=github.com/2dust/AndroidLibXrayLite=$v2rayReplacePath"
        )
    }
    execOperations.exec {
        workingDir(builderDir)
        environment(goEnv)
        commandLine(executablePath, "mod", "tidy")
    }
    execOperations.exec {
        workingDir(builderDir)
        environment(goEnv)
        commandLine(executablePath, "mod", "vendor")
    }
}

tasks.register<DefaultTask>("cloneV2raySource") {
    val srcDir = file("$buildDirV2ray/src")
    outputs.dir(srcDir)
    onlyIf { nativeProxyBuildEnabled && !srcDir.exists() }

    doLast {
        execOperations.exec {
            workingDir(rootProject.projectDir)
            commandLine(gitExecutable, "clone", "--depth=1", v2rayRepo, srcDir.absolutePath)
        }
        execOperations.exec {
            workingDir(srcDir)
            commandLine(gitExecutable, "fetch", "origin", v2rayCommit)
        }
        execOperations.exec {
            workingDir(srcDir)
            commandLine(gitExecutable, "checkout", v2rayCommit)
        }
    }
}

tasks.register<DefaultTask>("vendorGoDependencies") {
    val builderDir = file("src/main/go/builder")
    val vendorDir = file("$builderDir/vendor")

    dependsOn("cloneV2raySource")
    inputs.file("$builderDir/builder.go")
    outputs.dir(vendorDir)
    onlyIf { nativeProxyBuildEnabled }

    doFirst {
        verifyGoExecutable(builderDir, goExecutable)
    }

    doLast {
        createGoModule(builderDir)
        vendorGoDependencies(builderDir, goExecutable)
    }
}

val prepareGoBuild = tasks.register("prepareGoBuild") {
    dependsOn("vendorGoDependencies")
    onlyIf { nativeProxyBuildEnabled }
}

val copyAllGoSharedLibs = tasks.register("copyAllGoSharedLibs") {
    onlyIf { nativeProxyBuildEnabled }
}
val nativeBuildRequested = gradle.startParameter.taskNames.any { taskName ->
    listOf("assemble", "bundle", "install", "copyAllGoSharedLibs", "buildGoSharedLib").any {
        taskName.contains(it, ignoreCase = true)
    }
}
val goExecutableAvailable = runCatching {
    execOperations.exec {
        isIgnoreExitValue = true
        commandLine(goExecutable, "version")
    }.exitValue == 0
}.getOrDefault(false)
val shouldBuildNativeArtifacts = nativeProxyBuildEnabled && nativeBuildRequested && goExecutableAvailable

if (nativeProxyBuildEnabled && nativeBuildRequested && !goExecutableAvailable) {
    logger.warn("Go executable '$goExecutable' not found. Skipping downloader native proxy artifacts.")
}

copyAllGoSharedLibs.configure {
    onlyIf { shouldBuildNativeArtifacts }
}

data class ArchConfig(
    val abi: String,
    val goArch: String,
    val target: String
)

listOf(
    ArchConfig("arm64-v8a", "arm64", "aarch64-linux-android"),
    ArchConfig("armeabi-v7a", "arm", "armv7a-linux-androideabi"),
    ArchConfig("x86_64", "amd64", "x86_64-linux-android"),
    ArchConfig("x86", "386", "i686-linux-android")
).forEach { arch ->
    val buildTask = tasks.register<Exec>("buildGoSharedLib_${arch.abi}") {
        val builderDir = file("src/main/go/builder")
        val outputDir = file("${layout.buildDirectory.get().asFile}/generated/go_build/${arch.abi}")
        val outputSo = file("$outputDir/libgojni.so")

        dependsOn(prepareGoBuild)
        inputs.dir(builderDir)
        outputs.file(outputSo)
        onlyIf { nativeProxyBuildEnabled }

        doFirst {
            val ndkPath = findNdkPath()
            val ndkPrebuiltFolder = resolveNdkPrebuiltFolder(ndkPath)
            val apiLevel = 21
            val toolchainPath = "$ndkPath/toolchains/llvm/prebuilt/$ndkPrebuiltFolder"
            val compiler = "$toolchainPath/bin/${arch.target}$apiLevel-clang"

            if (!file(compiler).exists()) {
                throw GradleException("C compiler for ${arch.abi} not found at $compiler")
            }

            workingDir = builderDir
            environment("CGO_ENABLED", "1")
            environment("GOOS", "android")
            environment("GOARCH", arch.goArch)
            environment("CC", compiler)
            environment("CGO_CFLAGS", "")
            environment("CGO_LDFLAGS", "-llog -Wl,-z,max-page-size=16384")
            commandLine(
                goExecutable,
                "build",
                "-mod=vendor",
                "-buildvcs=false",
                "-buildmode=c-shared",
                "-trimpath",
                "-ldflags",
                "-s -w -buildid=",
                "-o",
                outputSo.absolutePath,
                "."
            )
        }
    }

    val copyTask = tasks.register<Copy>("copyGoSharedLib_${arch.abi}") {
        dependsOn(buildTask)
        from(buildTask.map { it.outputs.files })
        into("src/main/jniLibs/${arch.abi}")
        onlyIf { nativeProxyBuildEnabled }
    }

    copyAllGoSharedLibs.configure {
        dependsOn(copyTask)
    }
}

tasks.named("preBuild") {
    dependsOn("prepareGoogleServicesJson")
    if (shouldBuildNativeArtifacts) {
        dependsOn(copyAllGoSharedLibs)
    }
}
