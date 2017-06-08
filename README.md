[![Build Status](https://travis-ci.org/quran/quran_android.svg?branch=master)](https://travis-ci.org/quran/quran_android)

# Quran for Android


this is a simple (madani based) quran app for android.

* madani images from [quran images project](https://github.com/quran/quran.com-images) on github.
* qaloon images used with permission of Nous Memes Editions Et Diffusion (Tunisia).
* naskh images used with permission of SHL Info Systems.
* translation, tafsir and Arabic data come from [tanzil](http://tanzil.net) and [King Saud University](https://quran.ksu.edu.sa).

## Contributing

If you'd like to contribute, please take a look at the [PRs Welcome](https://github.com/quran/quran_android/issues?q=is%3Aissue+is%3Aopen+label%3A%22PRs+Welcome%22) label on the issue tracker. For new features, please open an issue to discuss it before beginning implementation.

Use [`quran_android-code_style.xml`](https://github.com/quran/quran_android/blob/master/quran_android-code_style.xml) for Android Studio / IntelliJ code styles. Import it by copying it to the Android Studio/IntelliJ IDEA codestyles folder. For Android Studio, that folder is located at `~/.AndroidStudio[Version]/config/codestyles` (the root folder name may differ depending on the host machine and Android Studio version, but the rest of the path should be same). After copying the `quran_android-code_style.xml`, go to Code Style preferences screen and choose `quran_android-code_style` from Code Style Schemes.

Though very rarely, we do push beta versions in Play Store for early testing. If you would like to participate in beta program, please join our  community in Google+.

May Allah reward all the awesome [Contributors and Translators](https://github.com/quran/quran_android/blob/master/CONTRIBUTORS.md).


## Setup

### Command Line

You can build Quran from the command line by running `./gradlew assembleMadaniDebug`.

### Android Studio / IntelliJ

Choose "Import Project," and choose the `build.gradle` file from the top level directory. Under "Build Variants" (a tab on the left side), choose "madaniDebug."


