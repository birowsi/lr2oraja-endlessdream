<div align="center" style="line-height: 1;">

# LR2oraja \~Endless Dream\~

[![DISCORD](https://dcbadge.limes.pink/api/server/HutCHCZHns?theme=default)](https://discord.gg/HutCHCZHns)

</div>

## 🌟 Fork Features (`birowsi`)
* **Auto-Screenshot on Clear**: Automatically captures successful result screens after 2.0 seconds, waiting up to 4.5 seconds for IR ranking data when necessary. Failed plays are ignored.
* **IR Result Reliability**: Keeps each result's uploads separate from the retry queue, prevents concurrent queue modification, and displays rankings when the primary IR lookup succeeds even if another provider fails.
* **Runtime Logging**: Routes SLF4J messages to Java's console/file logger so IR login, upload, and ranking failures remain diagnosable.
* **Top-Right Toast Notifications**: Anchors in-game toast notifications (`ImGuiNotify`) strictly to the top-right (`Top-Right`) using `ImGuiCond.Always`.
* **Konmai Downloader Fix**: Configures JSON deserialization to ignore unknown fields, fixing song downloads via the Konmai API.
* **Minor Fixes**:
  * Fixes `ClassCastException` in `DifficultyTableParser` when score rate or miss rate are parsed as integers.

> [!NOTE]
> IR providers are installed as separate plugin JARs. This fork does not bundle an IR implementation or spoof BMS-IR's client hash. BMS-IR may reject custom builds that are not on its server-side allowlist.

---

Endless Dream is a community fork and drop-in replacement for [beatoraja](https://github.com/exch-bms2/beatoraja) that integrates quality of life patches and new features not present in the upstream version of the game.

Based on [LR2oraja](https://github.com/wcko87/lr2oraja), which is itself a fork with LR2 judges and gauges, Endless Dream aims to fix some of the outstanding issues with the upstream project while being a central place for modifications and extensions that may never be accepted by upstream.

### Key Features
* In-game song downloader
* LR2 GBATTLE support
* Osu file support
* On the fly ratemods/freq
* Increased performance by using the latest graphics backends available to libgdx
* Faster Table Processing
* Compatible with beatoraja 0.8.8 installs
* Built in Mod Menu, accessible using **`F5` or `Insert`**

## Downloads
Official Endless Dream builds are published in the [upstream releases](https://github.com/seraxis/lr2oraja-endlessdream/releases). This fork's custom changes are built from this repository.

> [!NOTE]
> As of 0.3.0 the Java version has changed from 8 to 17, please check the releases page to update your installations java version

- [**Windows Download**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-windows-0.4.0.jar)
- [**Linux Download**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-linux-0.4.0.jar)
- [**Macos Download (Apple Silicon)**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-macos-aarch64-0.4.0.jar)
- [**Macos Download (Intel)**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-macos-0.4.0.jar)

### Installing from scratch
If you don't have an existing beatoraja installation download the latest [`beatoraja-0.8.8 JRE`](https://mocha-repository.info/download/beatoraja0.8.8-jre-win64.zip) bundled version, or choose a version yourself from the [beatoraja release page](https://mocha-repository.info/download.php).

### Post Install
Once you're set up with a copy of LR2oraja Endless Dream you might want to check out the excellent [Beatoraja English Guide](https://github.com/wcko87/beatoraja-english-guide/wiki) that has answers to all of your questions about beatoraja and BMS, including a list of skins, where to get songs, and how to use tables.

Alternatively you can ask in our [Discord](https://discord.gg/HutCHCZHns) and we'll be happy to help you out.

## Building from source
A JDK 17 **with javafx** is required to build and run. Consider using [liberica JDK](https://bell-sw.com/pages/downloads/#jdk-17-lts), ensure that you download `Package: Full JDK` to get the JavaFX version.

Clone this repository with submodules
```sh
git clone --recurse-submodules https://github.com/birowsi/lr2oraja-endlessdream.git
```
Run the gradle wrapper for your operating system and specify your desired platform as a [gradle system property](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties)

**Windows:**
```powershell
.\gradlew.bat core:shadowJar -Dplatform=windows
```
**Linux:**
```sh
./gradlew core:shadowJar -Dplatform=linux
```
**MacOS:**
```sh
./gradlew core:shadowJar -Dplatform=macos
```

> [!NOTE]
> For `arm` user: add -Darch=aarch64

This task will create a jar located in `dist/` that can be used with any working installation of the game.
### Testing changes
Use of an IDE, such as [Intellij](https://www.jetbrains.com/idea/download/other.html), is recommended for working on Endless Dream.

The gradle `core:runShadow` task can be used to quickly test and debug changes made to the project.

Configure the `runDir` system property to point to a beatoraja install or leave blank to have it run in the assets folder

Click 'More Actions' next to the run configurations panel in the window bar, Edit the runShadow configuration, and add `-DrunDir="[FULL PATH TO RAJA INSTALL]"`

![Run box clearly shown in the edit configuration menu](https://media.discordapp.net/attachments/1409976036963385547/1418936266514235483/idea64_bmidlXWb16.png?ex=68cfeee0&is=68ce9d60&hm=344ce793bff0fc3cdc47cc2b1a1692ec3a119b50e698a77c9c305508c1c6854e&=&format=webp&quality=lossless&width=547&height=208)

If you'd like to test IR dependent changes, set the `useIR` system property to `true` (e.g. `-DuseIR=true`).

**Windows:**
```powershell
.\gradlew.bat core:runShadow -Dplatform=windows -DrunDir="C:\beatoraja0.8.8"
```

## Contributing
Consider joining the Discord server. It's the place where most communication and collaboration happens.

Please state the impact your changes will have on the drop-in compatibility of Endless Dream with the latest beatoraja distribution when it is a relevant factor to your feature submission.
