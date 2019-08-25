package miso.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import miso.ingredients.Source;

import java.io.IOException;
import java.util.Optional;

public class Message {
    public final Object value;
    public final String key;
    public final Source source;

    public Message(String key, Object value, Source source) {
        this.source = source;
        this.key = key;
        this.value = value;
    }

    public boolean hasKey(String value)
    {
        return key.equals(value);
    };

    public Message withSource(Source source){
        return new Message(key, value, source);
    }

    public static Message of(String key, Object value, Source source) {
        return new Message(key, value, source);
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
        return " " + key + ":" + (value == null ? "NULL" : value.toString()) + " " + source.toString();
    }

}
