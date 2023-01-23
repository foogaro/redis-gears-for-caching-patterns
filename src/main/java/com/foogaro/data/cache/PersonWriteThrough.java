package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.WriteThrough;
import com.foogaro.data.entities.Person;
import gears.GearsBuilder;
import gears.records.KeysReaderRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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
        GearsBuilder.log("PersonWriteThrough.Record: [" + record + "]");
        Map<String, String> hash = record.getHashVal();
        for (String key : hash.keySet()) {
            String val = hash.get(key);
            GearsBuilder.log("PersonWriteThrough.Record.Hash: [" + key + "," + val + "]");
        }

        Person person = new Person();
        person.setId(Long.parseLong(record.getKey().substring(getKeyPattern().length()-1)));
        person.setName(hash.get("name"));
        person.setLastname(hash.get("lastname"));
        person.setAge(Integer.parseInt(hash.get("age")));

        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Person.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.saveOrUpdate(person);
        session.getTransaction().commit();
        GearsBuilder.log("PersonWriteThrough.Record " + record.getHashVal() + " processed.");
    }

    public static void main(String[] args) {
        new PersonWriteThrough();
    }

}
