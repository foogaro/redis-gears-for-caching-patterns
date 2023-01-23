package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.RefreshAhead;
import com.foogaro.data.cache.patterns.WriteThrough;
import com.foogaro.data.entities.Person;
import gears.GearsBuilder;
import gears.records.KeysReaderRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Map;

public class PersonRefreshAhead extends RefreshAhead {

    public PersonRefreshAhead() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        return "person:*";
    }

    public void onProcessEvent(KeysReaderRecord record) {
        GearsBuilder.log("PersonRefreshAhead.Record: [" + record + "]");
    }

    public static void main(String[] args) {
        new PersonRefreshAhead();
    }

}
