package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.ReadThrough;
import com.foogaro.data.entities.Person;
import gears.GearsBuilder;
import gears.records.KeysReaderRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class PersonReadThrough extends ReadThrough {

    public PersonReadThrough() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        return "person:*";
    }

    @Override
    public String[] getCommands() {
        return new String[]{"HGET", "HMGET"};
    }

    public void onProcessEvent(KeysReaderRecord record) {
        GearsBuilder.log("PersonReadThrough.Record: [" + record + "]");
        Long entityId = Long.parseLong(record.getKey().substring(getKeyPattern().length()-1));
        GearsBuilder.log("PersonReadThrough.Record.entityId: [" + entityId + "]");
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Person.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        Person person = session.find(Person.class, entityId);
        GearsBuilder.log("PersonReadThrough.Record.Person: [" + person + "]");
        Object response = GearsBuilder.executeArray(new String[]{"HSET", "person:" + person.getId(), "name", person.getName(), "lastname", person.getLastname(), "age", person.getAge()+""});
        GearsBuilder.log("PersonReadThrough.GearsBuilder.executeArray " + response);
        List<String> commands = new ArrayList<>();
        byte[][] commandBytes = GearsBuilder.getCommand();
        for (byte[] arg : commandBytes) {
            commands.add(new String(arg));
        }
        commands.forEach(s -> GearsBuilder.log("Command arg: " + s));
        response = GearsBuilder.executeArray(commands.toArray(commands.toArray(new String[0])));
        GearsBuilder.log("PersonReadThrough.GearsBuilder.executeArray " + response);
        GearsBuilder.overrideReply(response);
    }

    public static void main(String[] args) {
        new PersonReadThrough();
    }

}
