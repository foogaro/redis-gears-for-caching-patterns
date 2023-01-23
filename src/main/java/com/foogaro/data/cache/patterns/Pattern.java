package com.foogaro.data.cache.patterns;

import gears.ExecutionMode;
import gears.GearsBuilder;
import gears.operations.OnRegisteredOperation;
import gears.operations.OnUnregisteredOperation;

public abstract class Pattern implements OnProcessEvent, OnRegisteredOperation, OnUnregisteredOperation {


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

}
