import java.util.Properties
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations

plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
}

if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION.substringBefore(".").toInt() < 9) {
    pluginManager.apply("org.jetbrains.kotlin.android")
}

configurations.configureEach {
    resolutionStrategy {
        force(downloaderLibs.kotlin.stdlib)
    }
}

android {
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
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["toponAppId"] = findProperty("TOPON_APP_ID")?.toString().orEmpty()
        manifestPlaceholders["toponAppKey"] = findProperty("TOPON_APP_KEY")?.toString().orEmpty()
        manifestPlaceholders["toponSplashPlacementId"] =
            findProperty("TOPON_SPLASH_PLACEMENT_ID")?.toString().orEmpty()
        manifestPlaceholders["toponInterstitialPlacementId"] =
            findProperty("TOPON_INTERSTITIAL_PLACEMENT_ID")?.toString().orEmpty()
        manifestPlaceholders["toponRewardedPlacementId"] =
            findProperty("TOPON_REWARDED_PLACEMENT_ID")?.toString().orEmpty()
        manifestPlaceholders["toponBannerPlacementId"] =
            findProperty("TOPON_BANNER_PLACEMENT_ID")?.toString().orEmpty()
        manifestPlaceholders["toponNativePlacementId"] =
            findProperty("TOPON_NATIVE_PLACEMENT_ID")?.toString().orEmpty()
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        create("neo") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
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

dependencies {
    implementation(project(":topon-ads"))

    implementation(downloaderLibs.appcompat)
    implementation(downloaderLibs.material)
    implementation(downloaderLibs.constraintlayout)
    implementation(downloaderLibs.recyclerview)
    implementation(downloaderLibs.webkit)
    implementation(downloaderLibs.coreKtx)
    implementation(downloaderLibs.coreSplashscreen)
    implementation(downloaderLibs.legacySupportV4)
    implementation(downloaderLibs.kotlin.stdlib)
    implementation(downloaderLibs.workRuntimeKtx)
    implementation(downloaderLibs.workRxjava3)
    implementation(downloaderLibs.workMultiprocess)
    implementation(downloaderLibs.fragmentKtx)
    implementation(downloaderLibs.concurrentFuturesKtx)
    implementation(downloaderLibs.lifecycleExtensions)
    implementation(downloaderLibs.lifecycleCommonJava8)
    implementation(downloaderLibs.lifecycleLivedata)
    implementation(downloaderLibs.lifecycleViewmodel)
    implementation(downloaderLibs.roomRuntime)
    implementation(downloaderLibs.roomKtx)
    implementation(downloaderLibs.roomRxjava3)
    implementation(downloaderLibs.roomGuava)
    ksp(downloaderLibs.roomCompiler)
    implementation(downloaderLibs.daggerRuntime)
    implementation(downloaderLibs.daggerAndroid)
    implementation(downloaderLibs.daggerAndroidSupport)
    ksp(downloaderLibs.daggerCompiler)
    ksp(downloaderLibs.daggerAndroidProcessor)
    implementation(downloaderLibs.okHttpRuntime)
    implementation(downloaderLibs.okHttpLogging)
    implementation(downloaderLibs.retrofitRuntime)
    implementation(downloaderLibs.retrofitGson)
    implementation(downloaderLibs.retrofitRxjava3)
    implementation(downloaderLibs.persistentCookieJar)
    implementation(downloaderLibs.rxjava3)
    implementation(downloaderLibs.rxandroid3)
    implementation(downloaderLibs.youtubedl)
    //implementation(downloaderLibs.youtubedl.ffmpeg)
    implementation(downloaderLibs.ffmpegKit)
    implementation(downloaderLibs.media3Exoplayer)
    implementation(downloaderLibs.media3ExoplayerDash)
    implementation(downloaderLibs.media3ExoplayerHls)
    implementation(downloaderLibs.media3ExoplayerRtsp)
    implementation(downloaderLibs.media3Ui)
    implementation(downloaderLibs.media3Extractor)
    implementation(downloaderLibs.media3Database)
    implementation(downloaderLibs.media3Decoder)
    implementation(downloaderLibs.media3Datasource)
    implementation(downloaderLibs.media3Common)
    implementation(downloaderLibs.media3DatasourceOkhttp)
    implementation(downloaderLibs.glideRuntime)
    implementation(downloaderLibs.kotlinxSerializationJson)
    implementation(downloaderLibs.kotlinxSerializationCore)
    implementation(downloaderLibs.jsoup)
    implementation(downloaderLibs.timeago)
    coreLibraryDesugaring(downloaderLibs.desugarJdk)

    testImplementation(downloaderLibs.junit)
    testImplementation(downloaderLibs.mockitoCore)
    testImplementation(downloaderLibs.mockitoKotlin)
    androidTestImplementation(downloaderLibs.testRunner)
    androidTestImplementation(downloaderLibs.mockitoAndroid)
    androidTestImplementation(downloaderLibs.espressoCore)
    androidTestImplementation(downloaderLibs.espressoIntents)
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
    if (shouldBuildNativeArtifacts) {
        dependsOn(copyAllGoSharedLibs)
    }
}
