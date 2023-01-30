# Caching Patterns

## Introduction
Caching patterns are well known techniques to keep your data up to date in your caching layer.

One of the biggest challenges, it's to have data in the cache with the same state of the data in to the persistent datastore, that is the database.

In this example I'll explain the most common patterns and how to implement them using Java 11, Redis and MySQL.

For sake of simplicity, the architecture will be based on Linux containers.

## Most common Caching Patterns
Here is the list of the most common used caching patterns:
- Cache-Aside (Lazy-loading)
- Read-Through
- Write-Through
- Write-Behind (Write-back)
- Refresh Ahead
- Read Replica

### Cache-Aside (Lazy-loading) read
This pattern describes how the data should be retrieved.
The application layer, the service, first checks if the data is available into the Redis cache.

If the data is available, it gets returned immediately to the requester.

If the data is __not__ available, the service looks for it into the database.
Once the data is retrieved from the database, it gets also set into the cache for later use, and finally returned to the requester.

![Cache-Aside read](images/Cache-Aside-AL.png)

More detail in the [Caching at application layer](#caching-at-application-layer) section.

### Read-Through
This pattern describes how the data should be retrieved.
The application layer, the service, only connects to the Redis cache, without ever connecting to the database.

Thus, the service requests the data from the Redis cache.
The caching layer, Redis, first checks if it has the data.

If the data is available, it gets returned immediately to the service.

If the data is __not__ available, Redis looks for it into the database.
Once the data is retrieved from the database, it gets set into the cache for later use, and finally returned to the service, and then to the requester.

More detail in the demo [section](#pattern-read-through).

![Read-Through](images/Read-Through-CL.png)

### Write-Through
This pattern describes how the data should be stored.

The application layer, the service, only connects to the Redis cache, without ever connecting to the database.

Thus, the service sends the data to be written into the Redis cache.
The caching layer, Redis, stores the data, and then sends the data to be written into the database as well.

Then entire chain is synchronous.

![Write-Through](images/Write-Through-AL.png)

Moving the logic from the application layer to the cache layer.

This can be done by using standard library API, or by using proprietary caching API provided by the vendor, so whatever works best you use case.

More detail in the demo [section](#pattern-write-through).

![Write-Through](images/Write-Through-CL.png)

### Write-Behind (Write-back)
This pattern describes how the data should be stored.

The application layer, the service, only connects to the Redis cache, without ever connecting to the database.

Thus, the service sends the data to be written into the Redis cache.
The caching layer, Redis, stores the data, and then sends the data to be written into the database as well.

The last step of the chain is asynchronous, that is Redis calling the persistence layer.

![Write-Behind (Write-back)](images/Write-Behind-AL.png)

Moving the logic from the application layer to the cache layer.

This can be done by using standard library API, or by using proprietary caching API provided by the vendor, so whatever works best you use case.

![Write-Behind (Write-back)](images/Write-Behind-CL.png)

More detail in the demo [section](#pattern-write-behind).

### Refresh Ahead
This pattern describes how the data can be updated automatically whenever it's expired.

![Refresh-Ahead](images/Refresh-Ahead-CL.png)

More detail in the demo [section](#pattern-refresh-ahead).

### Read Replica
This pattern describes how the data can be updated automatically from the database to the caching layer.

![Read-Replica](images/Read-Replica-CL.png)

More detail in the demo [section](#pattern-read-replica).

# The Cache layer
The caching layer is basically composed by two main components:
- Redis
- RedisGears

## Redis
Redis is the well known in-memory multi-model database, that is used as distributed cache, primary database, pubsub, streams, and so on.

![Redis](images/redis-logo-full-color-rgb.png)

## RedisGears
Redis provides a way to add custom logic (your code) into its engine, by using RedisGears, which is a serverless engine.

![RedisGears](images/redisGears-white.png)

As a Serverless engine, RedisGears supports three programming languages:
- C
- Python
- JVM (Java, Scala)

JavaScript is coming soon...

However, if you still want the whole control to be at application layer, Spring and its caching capabilities, that is what you are looking for.

## Caching at application layer
Using Spring is the key point to easy the entire process, without adding any custom logic to Redis.

![Spring Data](images/spring-data.png)

The integration between the application and Redis is delegated to Spring using its
```@EnableCaching``` annotation,
and by specifying Redis as its cache layer into the ```application.properties```, as follows:

```properties
spring.cache.type=redis
spring.redis.client-type=jedis
spring.redis.cluster.max-redirects= # Maximum number of redirects to follow when executing commands across the cluster.
spring.redis.cluster.nodes= # Comma-separated list of "host:port" pairs to bootstrap from.
spring.redis.database=0 # Database index used by the connection factory.
spring.redis.url= # Connection URL, will override host, port and password (user will be ignored), e.g. redis://user:password@example.com:6379
spring.redis.host=localhost # Redis server host.
spring.redis.password= # Login password of the redis server.
spring.redis.ssl=false # Enable SSL support.
spring.redis.pool.max-active=8 # Max number of connections that can be allocated by the pool at a given time. Use a negative value for no limit.
spring.redis.pool.max-idle=8 # Max number of "idle" connections in the pool. Use a negative value to indicate an unlimited number of idle connections.
spring.redis.pool.max-wait=-1 # Maximum amount of time (in milliseconds) a connection allocation should block before throwing an exception when the pool is exhausted. Use a negative value to block indefinitely.
spring.redis.pool.min-idle=0 # Target for the minimum number of idle connections to maintain in the pool. This setting only has an effect if it is positive.
spring.redis.port=6379 # Redis server port.
spring.redis.sentinel.master= # Name of Redis server.
spring.redis.sentinel.nodes= # Comma-separated list of host:port pairs.
spring.redis.timeout=0 # Connection timeout in milliseconds.
```

At service layer, whenever the persistence layer is invoked, Spring can automatically cache any kind of operation (save, update, delete), thus keeping the caching layer always aligned with the database.

```java
@Service
@CacheConfig(cacheNames={"actors"})
public class ActorService {

    private final ActorRepository repository;

    public ActorService(ActorRepository repository) {
        this.repository = repository;
    }

    @Cacheable(unless="#result == null")
    public Optional<Actor> findById(Long id) {
        return repository.findById(id);
    }

    @Cacheable(unless="#result == null")
    public List<Actor> findAll() {
        return repository.findAll();
    }

    @CachePut(key = "#result.id", unless = "#result == null")
    public Actor save(Actor actor) {
        return repository.save(actor);
    }

    @CacheEvict
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @CacheEvict
    public void deleteAll() {
        repository.deleteAll();
    }
}
```

### @Cacheable
That's the Spring annotation to use when you want to cache whatever you are looking for in your persistence layer.

### @CachePut
That's the Spring annotation to use when you want to update whatever you are updating in your persistence layer.

### @CacheEvict
That's the Spring annotation to use when you want to delete whatever you are deleting from your persistence layer.

# Demo
It shouldn't be too complex. The demo is based on git and Docker Compose, which should be already in your environment, if not, follow the instructions for your specific OS at the following links:
- https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
- https://docs.docker.com/compose/install/

## Prerequisites
### Step 1
Clone the git repository as follows:
```shell
git clone git@github.com:foogaro/redis-gears-for-caching-patterns.git
cd redis-gears-for-caching-patterns
```

### Step 2
Run the services using Docker Compose, as follows:
```shell
docker-compose up
[+] Running 4/4
 ⠿ Network redis-gears-for-caching-patterns_redis-workshop  Created                                                                                                                                                                      0.3s
 ⠿ Container mysql                                          Created                                                                                                                                                                      0.3s
 ⠿ Container redis                                          Created                                                                                                                                                                      0.3s
 ⠿ Container redis-setup                                    Created                                                                                                                                                                      0.2s
Attaching to mysql, redis, redis-setup
mysql        | 2023-01-29 15:07:48+00:00 [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.31-1.el8 started.
mysql        | 2023-01-29 15:07:49+00:00 [Note] [Entrypoint]: Switching to dedicated user 'mysql'
mysql        | 2023-01-29 15:07:49+00:00 [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.31-1.el8 started.
redis        | 1:C 29 Jan 2023 15:07:49.668 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
redis        | 1:C 29 Jan 2023 15:07:49.669 # Redis version=6.2.5, bits=64, commit=00000000, modified=0, pid=1, just started
redis        | 1:C 29 Jan 2023 15:07:49.669 # Configuration loaded
redis        | 1:M 29 Jan 2023 15:07:49.671 * monotonic clock: POSIX clock_gettime
redis        |                 _._
redis        |            _.-``__ ''-._
redis        |       _.-``    `.  `_.  ''-._           Redis 6.2.5 (00000000/0) 64 bit
redis        |   .-`` .-```.  ```\/    _.,_ ''-._
redis        |  (    '      ,       .-`  | `,    )     Running in standalone mode
redis        |  |`-._`-...-` __...-.``-._|'` _.-'|     Port: 6379
redis        |  |    `-._   `._    /     _.-'    |     PID: 1
redis        |   `-._    `-._  `-./  _.-'    _.-'
redis        |  |`-._`-._    `-.__.-'    _.-'_.-'|
redis        |  |    `-._`-._        _.-'_.-'    |           https://redis.io
redis        |   `-._    `-._`-.__.-'_.-'    _.-'
redis        |  |`-._`-._    `-.__.-'    _.-'_.-'|
redis        |  |    `-._`-._        _.-'_.-'    |
redis        |   `-._    `-._`-.__.-'_.-'    _.-'
redis        |       `-._    `-.__.-'    _.-'
redis        |           `-._        _.-'
redis        |               `-.__.-'
redis        |
redis        | 1:M 29 Jan 2023 15:07:49.675 # Server initialized
redis        | 1:M 29 Jan 2023 15:07:49.699 * <rg> RedisGears version 1.2.5, git_sha=2fc2cca83faea6ece258a2dbd37df4d1862cc995, compiled_os=linux-bullseye-x64

...

redis        | 1:M 29 Jan 2023 15:07:51.833 * <module> JAVA_GEARS: PersonWriteBehind.onRegistered - registrationId: 0000000000000000000000000000000000000000-1
redis-setup  | OK
redis        | 1:M 29 Jan 2023 15:07:52.328 * <module> JAVA_GEARS: PersonWriteThrough.onRegistered - registrationId: 0000000000000000000000000000000000000000-4
redis-setup  | OK
redis        | 1:M 29 Jan 2023 15:07:52.949 * <module> JAVA_GEARS: PersonReadThrough.onRegistered - registrationId: 0000000000000000000000000000000000000000-7
redis-setup  | OK
redis        | 1:M 29 Jan 2023 15:07:53.298 * <module> JAVA_GEARS: PersonRefreshAhead.onRegistered - registrationId: 0000000000000000000000000000000000000000-10
redis-setup  | OK
redis-setup exited with code 0
mysql        | 2023-01-29T15:07:53.748652Z 6 [Warning] [MY-010453] [Server] root@localhost is created with an empty password ! Please consider switching off the --initialize-insecure option.
mysql        | 2023-01-29 15:07:57+00:00 [Note] [Entrypoint]: Database files initialized
mysql        | 2023-01-29 15:07:57+00:00 [Note] [Entrypoint]: Starting temporary server
mysql        | 2023-01-29T15:07:57.386721Z 0 [Warning] [MY-011068] [Server] The syntax '--skip-host-cache' is deprecated and will be removed in a future release. Please use SET GLOBAL host_cache_size=0 instead.
mysql        | 2023-01-29T15:07:57.393679Z 0 [System] [MY-010116] [Server] /usr/sbin/mysqld (mysqld 8.0.31) starting as process 131
mysql        | 2023-01-29T15:07:57.418124Z 1 [System] [MY-013576] [InnoDB] InnoDB initialization has started.
mysql        | 2023-01-29T15:07:57.839713Z 1 [System] [MY-013577] [InnoDB] InnoDB initialization has ended.
mysql        | 2023-01-29T15:07:58.285047Z 0 [Warning] [MY-010068] [Server] CA certificate ca.pem is self signed.
mysql        | 2023-01-29T15:07:58.285159Z 0 [System] [MY-013602] [Server] Channel mysql_main configured to support TLS. Encrypted connections are now supported for this channel.
mysql        | 2023-01-29T15:07:58.289186Z 0 [Warning] [MY-011810] [Server] Insecure configuration for --pid-file: Location '/var/run/mysqld' in the path is accessible to all OS users. Consider choosing a different directory.
mysql        | 2023-01-29T15:07:58.327323Z 0 [System] [MY-011323] [Server] X Plugin ready for connections. Socket: /var/run/mysqld/mysqlx.sock
mysql        | 2023-01-29T15:07:58.327467Z 0 [System] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections. Version: '8.0.31'  socket: '/var/run/mysqld/mysqld.sock'  port: 0  MySQL Community Server - GPL.

...

mysql        | 2023-01-29T15:08:05.144020Z 0 [System] [MY-011323] [Server] X Plugin ready for connections. Bind-address: '::' port: 33060, socket: /var/run/mysqld/mysqlx.sock
mysql        | 2023-01-29T15:08:05.144198Z 0 [System] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections. Version: '8.0.31'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306  MySQL Community Server - GPL.
```

### Step 3
Check that the containers are up and running by exposing the appropriate ports; 6379 for Redis and 3306 for MySQL, as follows:
```shell
docker ps
CONTAINER ID   IMAGE                        COMMAND                  CREATED          STATUS          PORTS                               NAMES
cd7b62e58a69   redislabs/redisgears:1.2.5   "docker-entrypoint.s…"   17 minutes ago   Up 17 minutes   0.0.0.0:6379->6379/tcp              redis
b344c5d25cf9   mysql:8.0.31                 "docker-entrypoint.s…"   17 minutes ago   Up 17 minutes   0.0.0.0:3306->3306/tcp, 33060/tcp   mysql
```


## Pattern Write-Through
The patter Write-Through implies that the data inserted into the cache are also propagated to the database in a synchronous way. The client will have to wait the entire data flow to happen before receiving the reply from Redis. Might be seconds.

To do this, let's enter a key-value pair of type ```HASH``` data structure, by connecting to the redis container and using the ```redis-cli``` tool, as follows:
```shell
docker exec -it redis /bin/bash
root@cd7b62e58a69:/data# redis-cli
127.0.0.1:6379> keys *
(empty list or set)
127.0.0.1:6379> hset person:1 name Luigi lastname Fugaro age 44
Executing command...
(integer) 3
127.0.0.1:6379> keys *
1) "person:1"
127.0.0.1:6379> type person:1
"hash"
127.0.0.1:6379> hmget person:1 name lastname age
1) "Luigi"
2) "Fugaro"
3) "44"
127.0.0.1:6379>  
```

In the log of the ```redis``` service there should be the following entries:

```shell
redis        | 1:M 29 Jan 2023 15:18:03.656 * <module> JAVA_GEARS: PersonWriteThrough.Record: [{"key":"person:1","event":"hset","type":3,"stringVal":null,"hashVal":{"name":"Luigi","age":"44","lastname":"Fugaro"},"listVal":null,"setVal":null}]
redis        | Jan 29, 2023 3:18:05 PM org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl logSelectedDialect
redis        | INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQLDialect
```

Which means the ```PersonWriteThrough``` was triggered and its logic was executed by inserting the cache value as ```record``` into the table ```person``` in MySQL.

Let's check the database, by connecting to it and listing the records of the table person, as follows:

```shell
docker exec -it mysql mysql -u root -proot
mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 9
Server version: 8.0.31 MySQL Community Server - GPL

Copyright (c) 2000, 2022, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
| vdt                |
+--------------------+
5 rows in set (0.03 sec)

mysql> use vdt;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> show tables;
+---------------+
| Tables_in_vdt |
+---------------+
| person        |
+---------------+
1 row in set (0.01 sec)

mysql> select * from person;
+----+-------+----------+------+
| id | name  | lastname | age  |
+----+-------+----------+------+
|  1 | Luigi | Fugaro   |   44 |
+----+-------+----------+------+
1 row in set (0.00 sec)

mysql>
```

All looks good!

## Pattern Write-Behind
The pattern Write-Behind implies that the data inserted into the cache are also propagated to the database in an _asynchronous_ way. The client will immediately receive the reply from Redis. Might be sub-millisecond.

Because you cannot have a Write-Through and a Write-Behind strategy at the same time for the same ```entity```, to do the test I set the keyPattern of the class ```com.foogaro.data.cache.PersonWriteBehind``` to "developer:*", so the new entry will bound to it.

To do this, let's enter a key-value pair of type ```HASH``` data structure, by connecting to the redis container and using the ```redis-cli``` tool, as follows:
```shell
docker exec -it redis /bin/bash
root@cd7b62e58a69:/data# redis-cli
127.0.0.1:6379> keys *
(empty list or set)
127.0.0.1:6379> hset developer:2 name Luigi lastname Fugaro age 44
(integer) 3
127.0.0.1:6379> keys *
1) "developer:2"
2) "person:1"
127.0.0.1:6379> type developer:2
"hash"
127.0.0.1:6379> hmget developer:2 name lastname age
1) "Luigi"
2) "Fugaro"
3) "44"
127.0.0.1:6379>  
```

In the log of the ```redis``` service there should be the following entries:

```shell
redis        | 1:M 29 Jan 2023 15:53:45.580 * <module> JAVA_GEARS: PersonWriteBehind.Record: [{"key":"developer:2","event":"hset","type":3,"stringVal":null,"hashVal":{"name":"Luigi","age":"44","lastname":"Fugaro"},"listVal":null,"setVal":null}]
redis        | Jan 29, 2023 3:53:47 PM org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl logSelectedDialect
redis        | INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQLDialect
```

Which means the ```PersonWriteBehind``` was triggered and its logic was executed by inserting the cache value as ```record``` into the table ```person``` in MySQL.

Let's check the database, by connecting to it and listing the records of the table person, as follows:

```shell
docker exec -it mysql mysql -u root -proot
mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 9
Server version: 8.0.31 MySQL Community Server - GPL

Copyright (c) 2000, 2022, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
| vdt                |
+--------------------+
5 rows in set (0.03 sec)

mysql> use vdt;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> show tables;
+---------------+
| Tables_in_vdt |
+---------------+
| person        |
+---------------+
1 row in set (0.01 sec)

mysql> select * from person;
+----+-------+----------+------+
| id | name  | lastname | age  |
+----+-------+----------+------+
|  1 | Luigi | Fugaro   |   44 |
|  2 | Luigi | Fugaro   |   44 |
+----+-------+----------+------+
2 rows in set (0.00 sec)

mysql>
```

All looks good!

## Pattern Read-Through
The pattern Read-Through implies that when a data is requested from the cache, and it's not available, the data will be fetched directly from the database, synchronously, put it into the cache and returned to the client. The latter will have to wait the entire data flow to happen before receiving the reply from Redis. Might be seconds.

To do this, let's try to read the value for key "person:2", which shouldn't be found as we entered "developer:2".

Let's connect to the redis container and using the ```redis-cli``` tool, as follows:

```shell
docker exec -it redis /bin/bash
root@cd7b62e58a69:/data# redis-cli
127.0.0.1:6379> keys *
1) "developer:2"
2) "person:1"
127.0.0.1:6379>  hmget person:2 name lastname age
1) "Luigi"
2) "Fugaro"
3) "44"
127.0.0.1:6379> keys *
1) "person:2"
2) "developer:2"
3) "person:1"
127.0.0.1:6379>
```
In the log of the ```redis``` service there should be the following entries:

```shell
redis        | 1:M 29 Jan 2023 16:31:36.601 * <module> JAVA_GEARS: PersonReadThrough.Record: [{"key":"person:2","event":"keymiss","type":-1,"stringVal":null,"hashVal":null,"listVal":null,"setVal":null}]
redis        | Jan 29, 2023 4:31:37 PM org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl logSelectedDialect
redis        | INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQLDialect
```

Which means the ```PersonReadThrough``` was triggered and its logic was executed by fetching the value from the table ```person``` in MySQL with ```ID``` "2".

All looks good!

## Pattern Refresh-Ahead
The purpose of the pattern Refresh-Ahead is to keep the data up-to-date. An example could be to refresh new/updated data for all those entries that were kept into the cache for too long, by using a time-to-live (TTL) on the key.

To do this, let's try to add a TTL of 5 seconds to the key "person:2", and wait for the expiration time, so that the key gets deleted from the cache.

Let's connect to the redis container and using the ```redis-cli``` tool, as follows:

```shell
docker exec -it redis /bin/bash
root@cd7b62e58a69:/data# redis-cli
127.0.0.1:6379> keys *
1) "developer:2"
2) "person:1"
127.0.0.1:6379> expire person:2 5
(integer) 1
127.0.0.1:6379>
```
In the log of the ```redis``` service there should be the following entries:

```shell
redis        | 1:M 29 Jan 2023 16:41:11.553 * <module> JAVA_GEARS: PersonRefreshAhead.Record: [{"key":"person:2","event":"expired","type":-1,"stringVal":null,"hashVal":null,"listVal":null,"setVal":null}]
redis        | Jan 29, 2023 4:41:12 PM org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl logSelectedDialect
redis        | INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQLDialect
```

Which means the ```PersonRefreshAhead``` was triggered and its logic was executed by fetching the value from the table ```person``` in MySQL with ```ID``` "2" and put it back into the cache.

The logic is almost the same as per the Read-Through patterns, except for the fact that the patterns gets triggered on the **expired** event and not on the _keymiss_ event.

## Pattern Read-Replica

Stay tuned.

