.PHONY: config-links all clean

all: config-links

clean:
	rm -rf build/

config-links: build/indev/config-links

build/indev/config-links:
	mkdir -p ./build/indev/
	kotlinc-native ./src/nativeMain/kotlin/*
	mv program.kexe ./build/indev/config-links