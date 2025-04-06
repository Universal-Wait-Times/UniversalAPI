package me.matthewe.universal.universalapi.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class GsonUtils {
    public static final Gson GSON_PRETTY;
    public static final Gson GSON_SHORT;

    static {
        // Create a serializer for OffsetDateTime that formats using ISO_OFFSET_DATE_TIME
        JsonSerializer<OffsetDateTime> offsetDateTimeSerializer = (src, typeOfSrc, context) -> {
            return src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        };

        // Create a deserializer for OffsetDateTime that parses using ISO_OFFSET_DATE_TIME
        JsonDeserializer<OffsetDateTime> offsetDateTimeDeserializer = (json, typeOfT, context) -> {
            return OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        };



        GSON_PRETTY =  new GsonBuilder() .registerTypeAdapter(OffsetDateTime.class, offsetDateTimeSerializer)
                .registerTypeAdapter(OffsetDateTime.class, offsetDateTimeDeserializer).setPrettyPrinting().create();

        GSON_SHORT =  new GsonBuilder() .registerTypeAdapter(OffsetDateTime.class, offsetDateTimeSerializer)
                .registerTypeAdapter(OffsetDateTime.class, offsetDateTimeDeserializer).create();



    }
}
