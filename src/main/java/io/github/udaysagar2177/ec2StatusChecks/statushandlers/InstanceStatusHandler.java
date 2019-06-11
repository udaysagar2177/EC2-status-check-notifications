package io.github.udaysagar2177.ec2StatusChecks.statushandlers;

import java.util.Map;


import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatus;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;

/**
 * Interface for implementations that handle {@link InstanceStatus} updates.
 *
 * @author uday
 */
public interface InstanceStatusHandler {

    /**
     * Handle a {@link InstanceStatus} update notification.
     */
    void handle(SerializableInstanceStatus oldInstanceStatus,
                SerializableInstanceStatus newInstanceStatus,
                Map<String, String> tags);
}
