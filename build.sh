./gradlew build
./gradlew :coordinate-server:jar
./gradlew :server:jar
./gradlew :examples:auth-server:bootJar

version=0.0.1

mkdir -p build
rm ./build/*
cp coordinate-server/build/libs/coordinate-server-${version}.jar build/coordinate-server.jar
cp coordinate-server/cm-server.conf build
cp coordinate-server/cm-session1.conf build

cp server/build/libs/server-${version}.jar build/server.jar
cp server/cm-client.conf build
cp server/config.yaml build

cp examples/auth-server/build/libs/auth-server-${version}.jar build/auth-server.jar
