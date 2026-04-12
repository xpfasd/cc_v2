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
            dirs("libs")
        }
        maven(url = "https://jfrog.anythinktech.com/artifactory/overseas_sdk")
    }
    versionCatalogs {
        create("downloaderLibs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "super-video-downloader"
include(":app")
