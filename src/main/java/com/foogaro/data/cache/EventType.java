package com.foogaro.data.cache;

public enum EventType {

    HSET("hset"), HGET("hget"), HMGET("hmget"), KEY_MISS("keymiss"), EXPIRED("expired"), CDC("xadd");
    private String eventType;

    EventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

}
