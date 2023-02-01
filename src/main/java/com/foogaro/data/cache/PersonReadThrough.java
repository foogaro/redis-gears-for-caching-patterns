package com.foogaro.data.cache;

import com.foogaro.data.cache.patterns.ReadThrough;
import com.foogaro.data.entities.Person;
import com.foogaro.data.jpa.HibernateUtils;
import gears.GearsBuilder;
import gears.LogLevel;
import gears.records.KeysReaderRecord;

import java.util.ArrayList;
import java.util.Arrays;
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
        try {
            GearsBuilder.log("PersonReadThrough.Record: [" + record + "]");
            Long entityId = Long.parseLong(record.getKey().split(":")[1]);
            Person person = (Person) HibernateUtils.find(Person.class, entityId);
            if (person != null) {
                Object response = GearsBuilder.executeArray(generateHSET(person,entityId+""));
                GearsBuilder.log("PersonReadThrough.GearsBuilder.executeArray.generateHSET: " + response);
                List<String> commands = new ArrayList<>();
                byte[][] commandBytes = GearsBuilder.getCommand();
                for (byte[] arg : commandBytes) {
                    commands.add(new String(arg));
                }
                response = GearsBuilder.executeArray(commands.toArray(commands.toArray(new String[0])));
                GearsBuilder.log("PersonReadThrough.GearsBuilder.redo.executeArray " + response);
                if (response != null && response.getClass().isArray()) {
                    Object[] arr = (Object[]) response;
                    List<String> resp = new ArrayList<>();
                    Arrays.asList(arr).forEach(o -> resp.add((String) o));
                    GearsBuilder.overrideReply(resp);
                } else {
                    GearsBuilder.overrideReply(response);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PersonReadThrough();
    }

}
