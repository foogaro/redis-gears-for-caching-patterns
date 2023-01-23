package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.WriteBehind;
import com.foogaro.data.entities.Person;
import gears.GearsBuilder;
import gears.records.KeysReaderRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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
        GearsBuilder.log("PersonWriteBehind.Record: [" + record + "]");
        Map<String, String> hash = record.getHashVal();
        for (String key : hash.keySet()) {
            String val = hash.get(key);
            GearsBuilder.log("PersonWriteBehind.Record.Hash: [" + key + "," + val + "]");
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
        GearsBuilder.log("PersonWriteBehind.Record " + record.getHashVal() + " processed.");
    }

    public static void main(String[] args) {
        new PersonWriteBehind();
    }
}
