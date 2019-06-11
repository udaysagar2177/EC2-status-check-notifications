package io.github.udaysagar2177.ec2StatusChecks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import software.amazon.awssdk.services.ec2.model.InstanceStatusEvent;

/**
 * Jackson serializable version of {@link InstanceStatusEvent}.
 *
 * @author uday
 */
public class SerializableInstanceStatusEvent {
    private final String code;
    private final String description;
    private final SerializableInstant notAfter;
    private final SerializableInstant notBefore;

    @JsonCreator
    public SerializableInstanceStatusEvent(@JsonProperty("code") String code,
                                           @JsonProperty("description") String description,
                                           @JsonProperty("notAfter") SerializableInstant notAfter,
                                           @JsonProperty("notBefore") SerializableInstant notBefore) {
        this.code = NotAvailableInfo.check(code);
        this.description = NotAvailableInfo.check(description);
        this.notAfter = notAfter;
        this.notBefore = notBefore;
    }

    public static List<SerializableInstanceStatusEvent> create(List<InstanceStatusEvent> events) {
        if (events != null && !events.isEmpty()) {
            List<SerializableInstanceStatusEvent> list = new ArrayList<>();
            for (InstanceStatusEvent event : events) {
                list.add(new SerializableInstanceStatusEvent(event.codeAsString(),
                        event.description(), SerializableInstant.create(event.notAfter()),
                        SerializableInstant.create(event.notBefore())));
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public SerializableInstant getNotAfter() {
        return notAfter;
    }

    public SerializableInstant getNotBefore() {
        return notBefore;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SerializableInstanceStatusEvent)) {
            return false;
        } else {
            SerializableInstanceStatusEvent that = (SerializableInstanceStatusEvent) o;
            if (!code.equals(that.code)) {
                return false;
            } else if (!description.equals(that.description)) {
                return false;
            } else {
                return notAfter.equals(that.notAfter) && notBefore.equals(that.notBefore);
            }
        }
    }

    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + notAfter.hashCode();
        result = 31 * result + notBefore.hashCode();
        return result;
    }

    public String toString() {
        return "SerializableInstanceStatusEvent{code='" + code + '\'' + ", description='"
                + description + '\'' + ", notAfter=" + notAfter + ", notBefore="
                + notBefore + '}';
    }
}
