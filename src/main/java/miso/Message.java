package miso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class Message {
    private final Actress sender;

    public Map<String, Object> params = new HashMap<>();

    public Message(){
        sender = null;
    }

    public Message(Actress sender) {
        this.sender = sender;
    }

    public Object get(String key) {
        return params.get(key);
    }

    public Boolean hasKey(String key) {
        return get(key) != null;
    }

    public static Message of(Actress sender) {
        return new Message(sender);
    }

    public static Message of(String key, Object value) {
        Message result = of(null);
        result.put(key, value);
        return result;
    }

    public static Message of(Actress sender, String key, Object value) {
        Message result = new Message(sender);
        result.params.put(key, value);
        return result;
    }

    public Message put(String key, Object value) {
        params.put(key, value);
        return this;
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
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            sb.append("  ");
            sb.append(k);
            sb.append(": ");
            sb.append(v);
            sb.append("\n");
        });

        return sb.toString();
    }
}
