package com.foogaro.data.cache.patterns;

import gears.ExecutionMode;
import gears.GearsBuilder;
import gears.operations.OnRegisteredOperation;
import gears.operations.OnUnregisteredOperation;
import gears.readers.BaseReader;
import gears.readers.KeysReader;

public abstract class Pattern implements OnProcessEvent, OnRegisteredOperation, OnUnregisteredOperation {

    public BaseReader<?> getReader() {
        KeysReader reader = new KeysReader()
                .setPattern(getKeyPattern())
                .setNoScan(true)
                .setReadValues(true);
        if (getEventsType() != null) reader.setEventTypes(getEventsType());
        if (getCommands() != null) reader.setCommands(getCommands());
        return reader;
    }

    @Override
    public void onRegistered(String registrationId) throws Exception {
        GearsBuilder.log(this.getClass().getSimpleName() + ".onRegistered - registrationId: " + registrationId);
    }

    @Override
    public void onUnregistered() throws Exception {
        GearsBuilder.log(this.getClass().getSimpleName() + ".onUnregistered");
    }

    public abstract ExecutionMode getExecutionMode();
    public abstract String[] getEventsType();
    public abstract String getKeyPattern();
    public String[] getCommands() { return null; };

    public String[] generateHSET(Object obj, String id) throws IllegalAccessException {
        String hashName = obj.getClass().getSimpleName().toLowerCase();
        var fields = obj.getClass().getDeclaredFields();
        int redisCommandIndex = 0;
        String[] redisCommand = new String[(fields.length*2) + 2];
        redisCommand[redisCommandIndex++] = "HSET";
        redisCommand[redisCommandIndex++] = hashName+":"+id;

        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            field.setAccessible(true);
            String fieldName = field.getName();
            Object fieldValue = field.get(obj);
            redisCommand[redisCommandIndex++] = fieldName;
            redisCommand[redisCommandIndex++] = (fieldValue != null ? fieldValue.toString() : "") ;
        }

        return redisCommand;
    }

}
