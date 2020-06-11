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
연결된 여러 분산 Cache Server들을 각각 조율해야 하는 필요성을 줄이면서, 동시에 단일 장애점(Single Point of Failure) 문제를 축소하여 시스템의 신뢰성을 높이기 위한 기능을 한다. 또한 분산되어 있는 어플리케이션이 제대로 조율되지 못할 경우 각 어플리케이션이 공유하고 있는 클러스터 자원에 무분별한 쓰기 동작(write operation) 이 발생할 수 있으므로 이런 상황에 대한 경쟁상태(Race Condition)를 최소화하기 위한 목적을 가진다.
- 서버 상태 관리. (alive / dead, failover)
- 서버 클러스터 동기화 (sync bucket)
- 서버 클러스터 node 참여 notify

### Keycafe Server
Keycafe의 목표는 메모리에 키 - 값 엔트리를 저장하여, 기존의 데이터베이스 시스템 보다 더 빨리 엔트리를 조회할 수 있도록 하는 것이다. 두번째는 하나 이상의 노드가 클러스터를 형성한 뒤 엔트리를 분산시켜 저장하여 용량 및 처리량을 확장하거나, 축소할 수 있도록 하는 것이다. 사용자는 엔트리를 조회 (GET), 저장 (SET) 및 삭제 (DELETE) 할 수 있다. 엔트리는 문자열 (String) 타입만을 지원한다. 엔트리들은 슬롯 (Slot) 이라 하는 논리적 엔트리 블록에 저장된다. Keycafe에는 다수의 논리적 슬롯이 존재하며, 키를 총 슬롯 수에 대해 일관적 해싱을 취한 값과 같은 인자를 가지는 버킷에 엔트리를 저장한다. 슬롯들은 Keycafe 클러스터의 노드들이 유지한다.
 
### Keycafe Client
Cache Server는 독자적인 프로토콜을 사용하므로, 이를 간편히 쓰기 위해서는 그에 따른 프로토콜을 지원하는 라이브러리가 필요하다. 추상화된 라이브러리를 제공함으로써, 해당 Key-Value Store을 사용하려는 개발자가 보다 더 용이하게 어플리케이션을 구현할 수 있게 돕는다.

### Example (auth-server)
위에서 구현한 K-V Store을 어떻게 활용할 수 있을지에 대한 간단한 예시를 설명한다. 여기에서는 간단한 사용자 인증 서버 및 클라이언트를 구현함으로써 어플리케이션 레벨에서의 실질적인 웹 기반의 사용 사례를 보인다.

## Build and run
```shell script
$ ./build.sh
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

## Internal specification
### Keycafe commands

### Keycafe cluster

### Coordination



