package io.github.udaysagar2177.ec2StatusChecks.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import software.amazon.awssdk.services.ec2.model.InstanceState;

/**
 * Jackson serializable version of {@link InstanceState}.
 *
 * @author uday
 */
public class SerializableInstanceState {

    private final Integer code;
    private final String name;

    @JsonCreator
    public SerializableInstanceState(@JsonProperty("code") Integer code,
                                     @JsonProperty("name") String name) {
        this.code = code == null ? -2147483648 : code;
        this.name = NotAvailableInfo.check(name);
    }

    public static SerializableInstanceState create(InstanceState instanceState) {
        return new SerializableInstanceState(instanceState.code(), instanceState.nameAsString());
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SerializableInstanceState)) {
            return false;
        } else {
            SerializableInstanceState that = (SerializableInstanceState) o;
            return code.equals(that.code) && name.equals(that.name);
        }
    }

    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String toString() {
        return "SerializableInstanceState{code=" + (code == null ? "N/A" : code)
                + ", name='" + name + '\'' + '}';
    }
}
