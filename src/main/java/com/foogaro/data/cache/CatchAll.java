package com.foogaro.data.cache;

import gears.ExecutionMode;
import gears.GearsBuilder;
import gears.operations.ForeachOperation;
import gears.operations.OnRegisteredOperation;
import gears.operations.OnUnregisteredOperation;
import gears.readers.KeysReader;
import gears.records.KeysReaderRecord;

public class CatchAll implements OnRegisteredOperation, OnUnregisteredOperation {

    public CatchAll() {
    }

    @Override
    public void onRegistered(String registrationId) throws Exception {
        GearsBuilder.log("CatchAll.onRegistered - registrationId: " + registrationId);
    }

    @Override
    public void onUnregistered() throws Exception {
        GearsBuilder.log("CatchAll.onUnregistered");
    }

    public static void main(String[] args) {
        KeysReader reader = new KeysReader().setPattern("*");

        GearsBuilder gb = GearsBuilder.CreateGearsBuilder(reader);
        gb.foreach((ForeachOperation<KeysReaderRecord>) record -> {
            GearsBuilder.log("CatchAll.Record.getEvent: " + record.getEvent());
            GearsBuilder.log("CatchAll.Record.getKey: " + record.getKey());
            GearsBuilder.log("CatchAll.Record.getType: " + record.getType());
            GearsBuilder.log("CatchAll.Record.getStringVal: " + record.getStringVal());
            GearsBuilder.log("CatchAll.Record.getListVal: " + record.getListVal());
            GearsBuilder.log("CatchAll.Record.getSetVal: " + record.getSetVal());
            GearsBuilder.log("CatchAll.Record.getHashVal: " + record.getHashVal());
        });

        CatchAll catchAll = new CatchAll();
        ExecutionMode em = ExecutionMode.ASYNC_LOCAL;
        gb.register(em, catchAll::onRegistered, catchAll::onUnregistered);
    }

}
