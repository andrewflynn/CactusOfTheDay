package org.aflynn.cactusoftheday;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Supports both 'yyyy-MM-dd HH:mm:ss' and Unix time for reading.
// Doesn't support writing.
public class DateAdapter extends TypeAdapter<Date> {
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Date read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        String dateString = reader.nextString();
        try {
            return FORMAT.parse(dateString);
        } catch (ParseException e) {
            // continue since we still try unix time
        }

        long epochTime = Long.parseLong(dateString) * 1000L;
        return new Date(epochTime);
    }

    @Override
    public void write(JsonWriter writer, Date date) throws IOException {
        writer.nullValue();
    }
}
