# Repository Guidelines

## Project Structure & Module Organization
This repository is a workspace around two imported Android codebases plus planning docs. Active app work is centered in `super-video-downloader-master/super-video-downloader-master/`.
- `super-video-downloader-master` 这个文件夹才是这次项目的主文件夹，非laucher相关问题的开发和排查，请从这个文件夹开始
- `super-video-downloader-master/super-video-downloader-master/app/src/main/java`: Kotlin app code, organized by feature (`ui/main/...`, `util/...`, `di/...`).
- `super-video-downloader-master/super-video-downloader-master/app/src/main/res`: layouts, drawables, menus, and localized strings.
- `super-video-downloader-master/super-video-downloader-master/app/src/test`: JVM unit tests.
- `docs/plans`: implementation notes and design plans for current work.
- `Neo-Launcher-main/Neo-Launcher-main`: tracked launcher source snapshot; touch only for integration work.


## Build, Test, and Development Commands
Run commands from `super-video-downloader-master/super-video-downloader-master/`.

- `.\gradlew.bat :app:assembleDebug`: build the debug APK.
- `.\gradlew.bat :app:testDebugUnitTest`: run the JVM unit test suite.
- `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.*"`: run a focused test package.
- `.\gradlew.bat clean`: remove Gradle build output.
- `.\gradlew.bat :app:vendorGoDependencies`: vendor Go deps for native proxy code before native builds.

## Coding Style & Naming Conventions
Follow the existing Kotlin-first Android style: 4-space indentation, `PascalCase` for classes, `camelCase` for methods and properties, and `UPPER_SNAKE_CASE` for constants. Keep feature packages aligned with screen or domain names. Android resources use lowercase snake case such as `fragment_browser.xml` and `item_video_info.xml`. No formatter is wired in; keep edits consistent with surrounding code and preserve Data Binding/View Binding patterns already in use.

## Testing Guidelines
Unit tests use JUnit4 with Mockito under `app/src/test/java`. Add tests beside the production package they cover and name them `SomethingTest.kt`. Prefer focused pure-Kotlin tests for new state mappers, helpers, and view-model logic before touching UI code. Run targeted tests first, then `:app:testDebugUnitTest` before opening a PR.

## Commit & Pull Request Guidelines
Recent history uses short, imperative commit subjects, often written as brief Chinese phrases. Keep commits narrow and descriptive. PRs should include a short summary, affected paths, linked issue or plan doc, and screenshots for any `res/layout` or visible UI change. Do not commit generated folders such as `.gradle/`, `.gradle-user-home/`, vendored `jniLibs/`, or zip artifacts.

## Configuration Notes
Native proxy tasks may require `go`, Android NDK, and `local.properties` or `ANDROID_NDK_HOME`. If `go` is installed outside `PATH`, set `GO_EXECUTABLE` before running `:app:vendorGoDependencies`.

## Skill Usage
If work involves recreating Android UI from a Figma design, use the `figma_android_ex` skill for the implementation.
