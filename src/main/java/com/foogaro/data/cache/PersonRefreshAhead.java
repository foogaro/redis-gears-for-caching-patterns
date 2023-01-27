package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.RefreshAhead;
import com.foogaro.data.entities.Person;
import com.foogaro.data.jpa.HibernateUtils;
import gears.GearsBuilder;
import gears.LogLevel;
import gears.records.KeysReaderRecord;

public class PersonRefreshAhead extends RefreshAhead {

    public PersonRefreshAhead() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        return "person:*";
    }

    public void onProcessEvent(KeysReaderRecord record) {
        try {
            GearsBuilder.acquireRedisGil();
            GearsBuilder.log("PersonRefreshAhead.Record: [" + record + "]");
            String key = record.getKey();
            Long entityId = Long.parseLong(key.split(":")[1]);
            Person person = (Person) HibernateUtils.find(Person.class, entityId);
            if (person != null) {
                boolean avoidNotifications = GearsBuilder.setAvoidNotifications(true);
                Object response = GearsBuilder.executeArray(new String[]{"HSET", "person:" + person.getId(), "name", person.getName(), "lastname", person.getLastname(), "age", person.getAge() + ""});
                GearsBuilder.setAvoidNotifications(avoidNotifications);
                GearsBuilder.log("PersonRefreshAhead.GearsBuilder.executeArray " + response, LogLevel.DEBUG);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            GearsBuilder.releaseRedisGil();
        }
    }

    public static void main(String[] args) {
        new PersonRefreshAhead();
    }

}
