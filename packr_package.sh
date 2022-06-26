#!/bin/zsh
PACKR_JAR=$1
MAC_JDK=$2
WIN_JDK=$3

mkdir -p build/mac
mkdir -p build/windows
rm -rf build/mac/*
rm -rf build/windows/*

# generate jar
./gradlew desktop:dist
cp desktop/build/libs/desktop-1.0.jar build

# package into Mac .app
java -jar $PACKR_JAR \
	--platform mac \
	--jdk $MAC_JDK \
	--executable ChessBot \
	--classpath desktop/build/libs/desktop-1.0.jar \
	--mainclass com.chessbot.DesktopLauncher \
	--vmargs XstartOnFirstThread \
	--icon assets/board/board.icns \
	--output build/mac/ChessBot.app

# package into Windows .exe
java -jar $PACKR_JAR \
	--platform Windows64 \
	--jdk $WIN_JDK \
	--useZgcIfSupportedOs \
	--executable ChessBot \
	--classpath desktop/build/libs/desktop-1.0.jar \
	--mainclass com.chessbot.DesktopLauncher \
	--icon assets/board/board.icns \
	--output build/windows/ChessBot
