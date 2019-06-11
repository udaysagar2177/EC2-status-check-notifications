package io.github.udaysagar2177.ec2StatusChecks.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


import software.amazon.awssdk.services.ec2.model.InstanceStatus;

/**
 * Jackson serializable version of {@link InstanceStatus}.
 *
 * @author uday
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SerializableInstanceStatus {
    private final String instanceId;
    private final String availabilityZone;
    private final SerializableInstanceState serializableInstanceState;
    private final List<SerializableInstanceStatusEvent> events;
    private final SerializableInstanceStatusSummary instanceStatus;
    private final SerializableInstanceStatusSummary systemStatus;

    @JsonCreator
    public SerializableInstanceStatus(@JsonProperty("instanceId") String instanceId,
                                      @JsonProperty("availabilityZone") String availabilityZone,
                                      @JsonProperty("instanceState") SerializableInstanceState serializableInstanceState,
                                      @JsonProperty("events") List<SerializableInstanceStatusEvent> events,
                                      @JsonProperty("instanceStatus") SerializableInstanceStatusSummary instanceStatus,
                                      @JsonProperty("systemStatus") SerializableInstanceStatusSummary systemStatus) {
        this.instanceId = instanceId;
        this.availabilityZone = availabilityZone;
        this.serializableInstanceState = serializableInstanceState;
        this.events = events;
        this.instanceStatus = instanceStatus;
        this.systemStatus = systemStatus;
    }

    public static SerializableInstanceStatus create(InstanceStatus instanceStatus) {
        return new SerializableInstanceStatus(instanceStatus.instanceId(),
                instanceStatus.availabilityZone(),
                SerializableInstanceState.create(instanceStatus.instanceState()),
                SerializableInstanceStatusEvent.create(instanceStatus.events()),
                SerializableInstanceStatusSummary.create(instanceStatus.instanceStatus()),
                SerializableInstanceStatusSummary.create(instanceStatus.systemStatus()));
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public List<SerializableInstanceStatusEvent> getEvents() {
        return events;
    }

    public SerializableInstanceStatusSummary getInstanceStatus() {
        return instanceStatus;
    }

    public SerializableInstanceStatusSummary getSystemStatus() {
        return systemStatus;
    }

    public SerializableInstanceState getInstanceState() {
        return serializableInstanceState;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SerializableInstanceStatus)) {
            return false;
        } else {
            SerializableInstanceStatus that = (SerializableInstanceStatus) o;
            if (!instanceId.equals(that.instanceId)) {
                return false;
            } else if (!availabilityZone.equals(that.availabilityZone)) {
                return false;
            } else if (!serializableInstanceState.equals(that.serializableInstanceState)) {
                return false;
            } else if (!events.equals(that.events)) {
                return false;
            } else {
                return instanceStatus.equals(that.instanceStatus) && systemStatus.equals(
                        that.systemStatus);
            }
        }
    }

    public int hashCode() {
        int result = instanceId.hashCode();
        result = 31 * result + availabilityZone.hashCode();
        result = 31 * result + serializableInstanceState.hashCode();
        result = 31 * result + events.hashCode();
        result = 31 * result + instanceStatus.hashCode();
        result = 31 * result + systemStatus.hashCode();
        return result;
    }

    public String toString() {
        return "SerializableInstanceStatus{instanceId='" + instanceId + '\''
                + ", availabilityZone='" + availabilityZone + '\''
                + ", serializableInstanceState=" + serializableInstanceState + ", events="
                + events + ", instanceStatus=" + instanceStatus + ", systemStatus="
                + systemStatus + '}';
    }
}

