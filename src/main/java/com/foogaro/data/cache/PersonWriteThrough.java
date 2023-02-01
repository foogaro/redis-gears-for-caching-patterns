package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.WriteThrough;
import com.foogaro.data.entities.Person;
import com.foogaro.data.jpa.HibernateUtils;
import gears.GearsBuilder;
import gears.LogLevel;
import gears.records.KeysReaderRecord;

import java.util.Map;

public class PersonWriteThrough extends WriteThrough {

    public PersonWriteThrough() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        // For demo/testing purpose.
        // The Key pattern should match your entity, and it should be used as an alternative of the WB pattern.
        return "person:*";
    }

    public void onProcessEvent(KeysReaderRecord record) {
        try {
            GearsBuilder.log("PersonWriteThrough.Record: [" + record + "]");
            Map<String, String> hash = record.getHashVal();
            Person person = new Person();
            person.setId(Long.parseLong(record.getKey().split(":")[1]));
            person.setFirstname(hash.get("firstname"));
            person.setLastname(hash.get("lastname"));
            person.setAge(Integer.parseInt(hash.get("age")));

            HibernateUtils.saveOrUpdate(person);
            GearsBuilder.log("PersonWriteThrough.Record " + record.getHashVal() + " processed.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PersonWriteThrough();
    }

}
