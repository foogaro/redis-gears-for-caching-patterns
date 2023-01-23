package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.Pattern;
import gears.GearsBuilder;
import gears.operations.ForeachOperation;
import gears.readers.KeysReader;
import gears.records.KeysReaderRecord;

public class RGManager {

    public static void register(Pattern pattern) {
        KeysReader reader = new KeysReader()
                .setPattern(pattern.getKeyPattern())
                .setNoScan(true)
                .setReadValues(true);
        if (pattern.getEventsType() != null) reader.setEventTypes(pattern.getEventsType());
        if (pattern.getCommands() != null) reader.setCommands(pattern.getCommands());

        GearsBuilder gb = GearsBuilder.CreateGearsBuilder(reader);

        gb.foreach((ForeachOperation<KeysReaderRecord>) pattern::onProcessEvent);

        gb.register(pattern.getExecutionMode(), pattern::onRegistered, pattern::onUnregistered);
    }
}
