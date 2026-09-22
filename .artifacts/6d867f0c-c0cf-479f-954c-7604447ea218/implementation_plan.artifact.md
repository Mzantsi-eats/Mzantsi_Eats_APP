# Fix Daemon compilation failed: null

The error `Daemon compilation failed: null` with `java.lang.IllegalArgumentException: 25.0.3` indicates a severe incompatibility between the Gradle version, Android Gradle Plugin (AGP), and the Kotlin compiler daemon. The project is currently using non-standard/experimental versions (`Gradle 9.3.0` and `AGP 8.13.2`) which are likely the root cause.

## Proposed Changes

### Build Configuration
#### [MODIFY] [gradle-wrapper.properties](file:///C:/Users/dumez/OneDrive/Desktop/OPSC Season 2/MzantsiTable-Android/MzantsiTable-Android/gradle/wrapper/gradle-wrapper.properties)
- Downgrade Gradle from `9.3.0` to `8.10` (stable).

#### [MODIFY] [build.gradle.kts](file:///C:/Users/dumez/OneDrive/Desktop/OPSC Season 2/MzantsiTable-Android/MzantsiTable-Android/build.gradle.kts)
- Downgrade Android Application Plugin from `8.13.2` to `8.5.2` (stable).

#### [MODIFY] [gradle.properties](file:///C:/Users/dumez/OneDrive/Desktop/OPSC Season 2/MzantsiTable-Android/MzantsiTable-Android/gradle.properties)
- Increase memory allocation for Gradle and the Kotlin daemon to prevent crashes.
- Disable incremental compilation temporarily to clear any corrupted caches.

## Verification Plan

### Automated Tests
- Run `./gradlew clean :app:compileDebugKotlin` to verify the Kotlin compiler daemon no longer crashes.
- Run `./gradlew :app:assembleDebug` to ensure a full build succeeds.
