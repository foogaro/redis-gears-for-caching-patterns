package com.foogaro.data.cache.patterns;

import gears.ExecutionMode;

import static com.foogaro.data.cache.EventType.CDC;

public abstract class ReadReplica extends Pattern {

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.ASYNC_LOCAL;
    }

    @Override
    public String[] getEventsType() {
        return new String[]{CDC.getEventType()};
    }

}
