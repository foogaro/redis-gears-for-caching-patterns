package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.ReadReplica;
import gears.GearsBuilder;
import gears.records.KeysReaderRecord;

public class PersonReadReplica extends ReadReplica {

    public PersonReadReplica() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        return "person:*";
    }

    public void onProcessEvent(KeysReaderRecord record) {
        try {
            GearsBuilder.log("PersonReadReplica.Record: [" + record + "]");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PersonReadReplica();
    }

}
