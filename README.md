<div align="center">
  <br/>
  <img src="./docs/images/logo.png" width="200" />
  <br/>
  <br/>
  <p>
    The simple, lightweight, distributed in-memory key-value store. <br/>
    Focusing on supporiting scalable, high-availability application.  
  </p>
  <p>
    <a href="https://github.com/actumn/keycafe/blob/master/LICENSE">
      <img src="https://img.shields.io/badge/license-MIT-blue.svg"/>
    </a>
  </p>
</div>

---
## Introduction
Keycafe is blah blah blah.  
For research purpose.

## Modules
- coordinate-server
- keycafe-server
- keycafe-client


## Build and run
### Gradle
- Build
```shell script
$ ./gradlew build
```
- Make Executable jar
```shell script
$ ./gradlew :coordinate-server:jar
$ ./gradlew :server:jar
```
- run
```shell script
$ java -jar coordinate-server/build/libs/coordinate-server-0.0.1.jar
$ java -jar server/build/libs/server-0.0.1.jar
```
### Docker
```
```

## Client sample code
- Cluster client
```java
public class Main() {
    public static void main(String[] args) {
        KeycafeCluster keycafe = new KeycafeCluster("localhost", 9814);
        System.out.println(keycafe.set("example_key", "KEY"));  // "ok"
        System.out.println(keycafe.get("example_key"));         // "KEY"
        System.out.println(keycafe.get("no_key"));              // null
        keycafe.close();
    }
}

```

## Specification
### Keycafe Protocol

### Keycafe cluster

### Coordination

### Cluster Protocol


