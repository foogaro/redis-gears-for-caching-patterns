package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.Pattern;
import gears.GearsBuilder;
import gears.operations.ForeachOperation;
import gears.readers.BaseReader;
import gears.readers.KeysReader;
import gears.records.KeysReaderRecord;

import java.util.logging.Level;

public class RGManager {

    public static void register(Pattern pattern) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);

        GearsBuilder gb = GearsBuilder.CreateGearsBuilder(pattern.getReader());

        gb.foreach((ForeachOperation<KeysReaderRecord>) pattern::onProcessEvent);

        gb.register(pattern.getExecutionMode(), pattern::onRegistered, pattern::onUnregistered);
    }
}
