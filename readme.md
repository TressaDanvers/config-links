# config-links
This project is inspired by [chezmoi](https://www.chezmoi.io/).

## Project Goals

- Instead of copying configuration files to the target directory, it will hard-link them.
  This is for speed and space constraints when configuration includes theme files, large images, etc...
  which is common in rice.
- It will include OS-Switching; that is it will include ways to configure programs when running on
  many operating systems. (e.g. Debian, Arch, Slackware, Windows, macOS, etc...)
- It will account for both absolute and relative symlinks in the source directory.
- It will have optional directory folding for ext4 case-folding.

## Build Instructions

### Requirements

The [kotlinc-native](https://github.com/JetBrains/kotlin) compiler on your path.

### Instructions

If you have make, run `make all` in your terminal; otherwise, run `bash make.sh all`.

You will find the executable file at `./build/indev/config-links`
