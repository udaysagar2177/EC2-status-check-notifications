package io.github.udaysagar2177.ec2StatusChecks.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import software.amazon.awssdk.services.ec2.model.InstanceStatusSummary;

/**
 * Jackson serializable version of {@link InstanceStatusSummary}.
 *
 * @author uday
 */
public class SerializableInstanceStatusSummary {
    private final String status;
    private final List<SerializableInstanceStatusDetails> details;

    @JsonCreator
    public SerializableInstanceStatusSummary(@JsonProperty("status") String status,
                                             @JsonProperty("details") List<SerializableInstanceStatusDetails> details) {
        this.status = NotAvailableInfo.check(status);
        this.details = details;
    }

    public static SerializableInstanceStatusSummary create(
            InstanceStatusSummary instanceStatusSummary) {
        if (instanceStatusSummary == null) {
            return new SerializableInstanceStatusSummary("N/A", Collections.emptyList());
        } else {
            List<SerializableInstanceStatusDetails> details = Collections.emptyList();
            if (instanceStatusSummary.details() != null) {
                details = SerializableInstanceStatusDetails.create(instanceStatusSummary.details());
            }
            return new SerializableInstanceStatusSummary(instanceStatusSummary.statusAsString(),
                    details);
        }
    }

    public String getStatus() {
        return status;
    }

    public List<SerializableInstanceStatusDetails> getDetails() {
        return details;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SerializableInstanceStatusSummary)) {
            return false;
        } else {
            SerializableInstanceStatusSummary that = (SerializableInstanceStatusSummary) o;
            return status.equals(that.status) && details.equals(that.details);
        }
    }

    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + details.hashCode();
        return result;
    }

    public String toString() {
        return "SerializableInstanceStatusSummary{status='" + status + '\'' + ", details="
                + details + '}';
    }
}
