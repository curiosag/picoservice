package miso.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import miso.ingredients.Address;
import miso.ingredients.OpId;

import java.io.IOException;
import java.util.*;

public class Message {
    public final Object value;
    public final String key;
    public final Address sender;
    public final OpId opId;

    public Message(String key, Object value, Address sender, OpId opId) {
        this.sender = sender;
        this.opId = opId;
        this.key = key;
        this.value = value;
    }

    public boolean hasKey(String value)
    {
        return key.equals(value);
    };

    public Message(String key, Object value, Address sender, Long executionId, Integer recursionLevel) {
        this(key, value, sender, OpId.opId(executionId, recursionLevel));
    }

    public static Message of(String key, Object value, Address sender, OpId opId) {
        return new Message(key, value, sender, opId);
    }

    public static Message of(String key, Object value, Address sender, Message trigger) {
        return of(key, value, sender, trigger.opId);
    }


    public static Optional<Message> fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode j = mapper.readTree(json);
            String urlSender = j.get("urlSender").textValue();
            String portSender = j.get("urlSender").textValue();
            String id = j.get("urlSender").textValue();
            String respondingTo = j.get("respondingTo").textValue();

            JsonNode params = j.get("params");
            for (int i = 0; i < params.size(); i++) {
                JsonNode param = params.get(i);
                // this.params.put(param.get("key"), param.get)
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return " " + key + ":" + (value == null ? "NULL" : value.toString()) + " (" +  (sender == null ? "NULL" : sender.value)+ ")";
    }

}
