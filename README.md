<div align="center">
  <br/>
  <img src="./docs/images/logo.png" width="200" />
  <br/>
  <br/>
  <p>
    The simple, lightweight, distributed in-memory key-value store using netty. <br/>
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
![IMAGE](./docs/images/intro-key-value.png)

Keycafe is the simple distributed key-value in-memory database implementation supporting `String` data structure, for research purpose how to build the simple distributed system from scratch. Keycafe is focusing on stroing key-value entry in memory which means reading faster than existing relational database. Also, more than one nodes form a cluster system and distribute key-value entries to extend the entire throughput. User can do `GET`, `SET`, `DELETE` commands. Entries are stored in `slot`, the logical block of Keycafe, which came from [the keys distribution model of Redis](https://redis.io/topics/cluster-spec#keys-distribution-model). There are multiple logical slots in Keycafe cluster and the hash alogorithm is used to map the key to consistent hash slot. Slots are maintained by Keycafe cluster nodes.


### Clustering
![IMAGE](./docs/images/clustering.png)

### Slots
![IMAGE](./docs/images/clustering-slot.png)

## Architecture
![IMAGE](./docs/images/architecture.png)

### Coordinate Server
`coordinate-server` is a centralized service for maintaining server configuration or providing a solution to divide slots which is shared resource of cluster into Keycafe server cluster. With the server configuration information provided by `coordinate-server`, Keycafe server can discover other Keycafe servers and have a chance to share slots fairly between nodes. 


### Keycafe Server
As explain above, `keycafe-server` stores key-value entries and read value faster than existing relational database. User can read, write, and delete values with the commands of `get`, `set`, and `delete`. Forming a cluster, `keycafe-server` nodes share logical slots between their cluster nodes.
 
### Keycafe Client
Because Keycafe uses its own TCP binary protocol, `keycafe-client` provides a simple method to communicate Keycafe server. 
- Example code 
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

### Example (auth-server, auth-web)
![IMAGE](./docs/images/example-auth-web.png)

`auth-server` is a simple authentication server to show how to use Keycafe key-value store. Displaying a simple auth with `auth-web`, the example provides the web-based use case in an application level. 

## Build and run
```shell script
$ sh build.sh
$ sh run-coordinate.sh
$ sh run-server.sh
$ sh run-example-server.sh
$ sh run-example.web.sh
# and try with email: 'limpett0@smugmug.com', password: 'pCHecmGBZ7'
# for detail information, see: examples/auth-server/src/main/resources/data.sql
```
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
- Dockerfile
```shell script
$ docker build -f docker/coordinate-server/Dockerfile .
$ docker build -f docker/server/Dockerfile .
```
- docker-compose build
```shell script
$  docker-compose -f docker/docker-compose.yml build
```

## Internal specification
### Keycafe commands
- Request
- Reply

### Keycafe cluster
- TCP connection bus
- Keycafe cluster messages
- Client keycafe cluster discover

### Coordination
- Register node information.
- Notify new node joining the cluster.



