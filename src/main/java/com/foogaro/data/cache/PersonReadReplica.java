package com.foogaro.data.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foogaro.data.cache.patterns.ReadReplica;
import com.foogaro.data.entities.Person;
import gears.GearsBuilder;
import gears.records.KeysReaderRecord;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonReadReplica extends ReadReplica {

    private String lastEventId = "0";
    public PersonReadReplica() {
        RGManager.register(this);
    }

    @Override
    public String getKeyPattern() {
        return "vdt.vdt.person";
    }

    public void onProcessEvent(KeysReaderRecord record) {
        boolean avoidNotifications = false;
        try {
            GearsBuilder.acquireRedisGil();
            avoidNotifications = GearsBuilder.setAvoidNotifications(true);
            GearsBuilder.log("PersonReadReplica.onProcessEvent.Record: [" + record + "]");

            String key = record.getKey();
            GearsBuilder.log("PersonReadReplica.onProcessEvent.key: " + key);
            Object response = GearsBuilder.executeArray(new String[]{"XREAD", "COUNT", "1", "STREAMS", record.getKey(), lastEventId});
            GearsBuilder.log("PersonReadReplica.onProcessEvent.response[" + lastEventId + "]: " + response);
            if (response != null) {
                final List<String> results = new ArrayList<>();
                if (response != null && response.getClass().isArray()) {
                    Object[] arr = (Object[]) response;
                    for (Object obj : arr) {
                        parseEvent(obj, results);
                    }
                    results.forEach(s -> GearsBuilder.log("List item: " + s));

                    final String[] eventStructArr = results.toArray(results.toArray(new String[0]));
                    lastEventId = eventStructArr[1];
                    final String jsonEntityId = eventStructArr[2];
                    final JSONObject objEntityId = new JSONObject(jsonEntityId);
                    final int entityId = objEntityId.getJSONObject("payload").getInt("id");

                    final String jsonPayload = eventStructArr[3];
                    final JSONObject objPayload = new JSONObject(jsonPayload);
                    final JSONObject payload = objPayload.getJSONObject("payload");
                    processPayload(payload, entityId);
                }
                GearsBuilder.log("PersonReadReplica.onProcessEvent.GearsBuilder.executeArray " + response);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            GearsBuilder.setAvoidNotifications(avoidNotifications);
            GearsBuilder.releaseRedisGil();
        }
    }

    private void processPayload(JSONObject payload, int entityId) throws JsonProcessingException, IllegalAccessException {
        Object before = payload.get("before");
        Object after = payload.get("after");

        if (before != null && after == null) {
            // deleted person
            Object response = GearsBuilder.executeArray(new String[]{"DEL", "person:" + entityId});
            GearsBuilder.log("PersonReadReplica.processPayload.response: " + response);
        } else {
            // new/updated person
            JSONObject jsonPerson = payload.getJSONObject("after");
            final ObjectMapper mapper = new ObjectMapper();
            Person person = mapper.readValue(jsonPerson.toString(), Person.class);
            GearsBuilder.log("PersonReadReplica.processPayload.person: " + person);
            Object response = GearsBuilder.executeArray(generateHSET(person,entityId+""));
            GearsBuilder.log("PersonReadReplica.processPayload.response: " + response);
        }
    }
    public String[] getCommands() { return new String[]{EventType.CDC.getEventType()}; }

    public static void main(String[] args) {
        new PersonReadReplica();
    }

    private Object parseEvent(Object event, List<String> list) {
        if (event != null) {
            if (event.getClass().isArray()) {
                Object[] arr = (Object[]) event;
                for (Object obj : arr) {
                    parseEvent(obj, list);
                }
            } else {
                list.add((String) event);
                return event;
            }
        } else {
            return event;
        }
        return event;
    }

}
