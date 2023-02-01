#!/bin/bash
set -e

# redis-cli -x -h redis config set requirepass redis.2023

# redis-server restart

# redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.CatchAll < /tmp/redis-gears-for-caching-patterns-1.0.0.jar

redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonWriteBehind < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonWriteThrough < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonReadThrough < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonRefreshAhead < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonReadReplica < /tmp/redis-gears-for-caching-patterns-1.0.0.jar

