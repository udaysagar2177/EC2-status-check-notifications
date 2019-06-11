package io.github.udaysagar2177.ec2StatusChecks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import software.amazon.awssdk.services.ec2.model.InstanceStatusDetails;

/**
 * Jackson serializable version of {@link InstanceStatusDetails}.
 *
 * @author uday
 */
public class SerializableInstanceStatusDetails {
    private final SerializableInstant impairedSince;
    private final String name;
    private final String status;

    @JsonCreator
    public SerializableInstanceStatusDetails(
            @JsonProperty("impairedSince") SerializableInstant impairedSince,
            @JsonProperty("name") String name, @JsonProperty("status") String status) {
        this.impairedSince = impairedSince;
        this.name = NotAvailableInfo.check(name);
        this.status = NotAvailableInfo.check(status);
    }

    public static List<SerializableInstanceStatusDetails> create(
            List<InstanceStatusDetails> details) {
        if (details != null && !details.isEmpty()) {
            List<SerializableInstanceStatusDetails> list = new ArrayList<>();
            for (InstanceStatusDetails detail : details) {
                list.add(new SerializableInstanceStatusDetails(
                        SerializableInstant.create(detail.impairedSince()), detail.nameAsString(),
                        detail.statusAsString()));
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public SerializableInstant getImpairedSince() {
        return impairedSince;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SerializableInstanceStatusDetails)) {
            return false;
        } else {
            SerializableInstanceStatusDetails that = (SerializableInstanceStatusDetails) o;
            if (!impairedSince.equals(that.impairedSince)) {
                return false;
            } else {
                return name.equals(that.name) && status.equals(that.status);
            }
        }
    }

    public int hashCode() {
        int result = impairedSince.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }

    public String toString() {
        return "SerializableInstanceStatusDetails{impairedSince=" + impairedSince + ", name='"
                + name + '\'' + ", status='" + status + '\'' + '}';
    }
}
