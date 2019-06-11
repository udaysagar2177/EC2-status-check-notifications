package io.github.udaysagar2177.ec2StatusChecks.database;

import java.util.Map;


import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatus;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;

/**
 * Interface for implementations that can persist {@link InstanceStatus}.
 *
 * Implementations can choose to be lazy in loading/persisting the data. See {@link this#load()}
 * and {@link this#save()}.
 *
 * @author uday
 */
public interface InstanceStatusDB {

    /**
     * Returns {@link SerializableInstanceStatus} stored for the given instanceId, null otherwise.
     */
    SerializableInstanceStatus get(String instanceId);

    /**
     * Sets the given {@link SerializableInstanceStatus} against the given instanceId key.
     */
    void set(String instanceId, SerializableInstanceStatus instanceStatus);

    /**
     * Returns all known {@link SerializableInstanceStatus}s.
     */
    Map<String, SerializableInstanceStatus> getAll();

    /**
     * Clears the database.
     */
    void clear();

    /**
     * Persist the set data from memory into database.
     */
    void save();

    /**
     * Load the data from database into memory.
     */
    void load();

    /**
     * Returns the number of instances stored in this database.
     */
    int getCount();
}
