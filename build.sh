#!/bin/sh

set -xe

mkdir -p build

cd src
javac -d ../build me/tim/Crypto.java me/tim/Message.java me/tim/client/Client.java me/tim/server/ClientHandler.java me/tim/server/Server.java
