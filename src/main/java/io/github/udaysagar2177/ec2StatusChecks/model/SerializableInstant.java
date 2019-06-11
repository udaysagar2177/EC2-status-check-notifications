package io.github.udaysagar2177.ec2StatusChecks.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson serializable version of {@link Instant}.
 *
 * @author uday
 */
public class SerializableInstant {
    public static final long NOT_AVAILABLE_TIME = -1L;

    private final long time;
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @JsonCreator
    public SerializableInstant(@JsonProperty("time") long time) {
        this.time = time;
    }

    public static SerializableInstant create(Instant instant) {
        return instant == null
                ? new SerializableInstant(NOT_AVAILABLE_TIME)
                : new SerializableInstant(instant.toEpochMilli());
    }

    public long getTime() {
        return time;
    }

    @JsonIgnore
    public String getTimeAsString() {
        return time == NOT_AVAILABLE_TIME
                ? NotAvailableInfo.TEXT
                : dateFormat.format(time);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SerializableInstant)) {
            return false;
        } else {
            SerializableInstant that = (SerializableInstant) o;
            return time == that.time;
        }
    }

    public int hashCode() {
        return (int) (time ^ time >>> 32);
    }

    public String toString() {
        return "SerializableInstant{time=" + getTimeAsString() + '}';
    }
}
