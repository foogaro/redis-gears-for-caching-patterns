package com.foogaro.data.cache.patterns;

import gears.ExecutionMode;

import static com.foogaro.data.cache.EventType.*;

public abstract class ReadThrough extends Pattern {

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.SYNC;
    }

    @Override
    public String[] getEventsType() {
        return new String[]{KEY_MISS.getEventType(), HGET.getEventType(), HMGET.getEventType()};
    }


}
