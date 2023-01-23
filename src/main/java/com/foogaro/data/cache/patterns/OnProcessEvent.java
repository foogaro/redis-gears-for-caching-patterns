package com.foogaro.data.cache.patterns;

import gears.records.KeysReaderRecord;

@FunctionalInterface
public interface OnProcessEvent {

    void onProcessEvent(KeysReaderRecord record);
}
