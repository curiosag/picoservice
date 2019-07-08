package miso.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Message {
    public Map<String, Object> params = new HashMap<>();

    public static Message message(){
        return new Message();
    }
    public static final Message NULL = new Message();

    public Object get(String key) {
        return params.get(key);
    }

    public Optional<Object> maybe(String key) {
        return Optional.ofNullable(params.get(key));
    }

    public Boolean hasKey(String key) {
        return get(key) != null;
    }


    public static Message of(String key, Object value) {
        Message result = new Message();
        result.put(key, value);
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
