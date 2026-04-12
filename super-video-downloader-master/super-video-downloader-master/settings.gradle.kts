pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        flatDir {
            dirs("libs", "app/libs")
        }
        maven(url = "https://jfrog.anythinktech.com/artifactory/overseas_sdk")
        maven(url = "https://artifact.bytedance.com/repository/pangle")
    }
    versionCatalogs {
        create("downloaderLibs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "super-video-downloader"
include(":app")
include(":topon-ads")
