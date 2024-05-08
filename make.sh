#!/bin/bash
PROJ_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$PROJ_DIR" || exit

if ! [ "$(which make)" = "" ]; then
  make "$1"
else
  if [ "$1" = "clean" ]; then
    rm -rf ./build
  elif [ "$1" = "all" ]; then
    mkdir -p ./build/indev/
    # build/indev/config-links
    if ! [ -f "$PROJ_DIR"/build/indev/config-links ]; then
      kotlinc-native ./src/nativeMain/kotlin/*
      mv program.kexe ./build/indev/config-links
    fi
  fi
fi