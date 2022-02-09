package bg.fmi.mjt.splitwise.gson.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LocalDateTimeAdapterTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Gson GSON =
        new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    private static final LocalDateTime DATE_TIME_TESTED = LocalDateTime.of(2021, Month.AUGUST, 12, 13, 0);
    private static final String DATE_TIME_SERIALIZED = "\"2021-08-12T13:00:00\"";

    @Test
    void testDateSerializesInCorrectFormat() {
        var json = GSON.toJson(DATE_TIME_TESTED);

        assertEquals(DATE_TIME_SERIALIZED, json, "The adapter does not serialize in the correct format");
    }

    @Test
    void testDateDeserializesInCorrectFormat() {
        var date = GSON.fromJson(DATE_TIME_SERIALIZED, LocalDateTime.class);

        assertEquals(DATE_TIME_TESTED, date, "The adapter did not deserialize the same date");
    }

    @Test
    void testDateSerializesNull() {
        LocalDateTime nullDate = null;
        var json = GSON.toJson(nullDate);

        assertEquals("null", json, "The adapter does not serialize null dates");
    }

    @Test
    void testDateDeserializesNull() {
        var nullDate = GSON.fromJson("null", LocalDateTime.class);

        assertNull(nullDate, "The adapter did not deserialize null date");
    }


}