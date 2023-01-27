#!/bin/bash

redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonWriteBehind < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonWriteThrough < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonReadThrough < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
redis-cli -x -h redis RG.JEXECUTE com.foogaro.data.cache.PersonRefreshAhead < /tmp/redis-gears-for-caching-patterns-1.0.0.jar
