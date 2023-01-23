package com.foogaro.data.cache.patterns;

import gears.ExecutionMode;

import static com.foogaro.data.cache.EventType.HSET;

public abstract class WriteThrough extends Pattern {

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.SYNC;
    }

    @Override
    public String[] getEventsType() {
        return new String[]{HSET.getEventType()};
    }

}
