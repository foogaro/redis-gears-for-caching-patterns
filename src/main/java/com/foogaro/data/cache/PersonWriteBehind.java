package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.WriteBehind;
import com.foogaro.data.entities.Person;
import com.foogaro.data.jpa.HibernateUtils;
import gears.GearsBuilder;
import gears.LogLevel;
import gears.records.KeysReaderRecord;

import java.util.Map;

public class PersonWriteBehind extends WriteBehind {

    public PersonWriteBehind() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        // For demo/testing purpose.
        // The Key pattern should match your entity, and it should be used as an alternative of the WT pattern.
        //return "person:*";
        return "developer:*";
    }

    public void onProcessEvent(KeysReaderRecord record) {
        try {
            GearsBuilder.log("PersonWriteBehind.Record: [" + record + "]");
            Map<String, String> hash = record.getHashVal();
            Person person = new Person();
            person.setId(Long.parseLong(record.getKey().split(":")[1]));
            person.setName(hash.get("name"));
            person.setLastname(hash.get("lastname"));
            person.setAge(Integer.parseInt(hash.get("age")));

            HibernateUtils.saveOrUpdate(person);
            GearsBuilder.log("PersonWriteBehind.Record " + record.getHashVal() + " processed.", LogLevel.DEBUG);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PersonWriteBehind();
    }
}
