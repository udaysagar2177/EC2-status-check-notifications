package io.github.udaysagar2177.ec2StatusChecks;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import io.github.udaysagar2177.ec2StatusChecks.database.InstanceStatusDB;
import io.github.udaysagar2177.ec2StatusChecks.statushandlers.InstanceStatusHandler;
import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatus;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;

/**
 * This class
 *  - queries for bad instances using {@link EC2BadInstancesFetcher}
 *  - notifies all the registered {@link InstanceStatusHandler}s about bad instances
 *  - persists the bad {@link InstanceStatus} on {@link InstanceStatusDB}
 *
 * @author uday
 */
public class EC2StatusChecker {

    private static final Logger LOG = LogManager.getLogger(EC2StatusChecker.class);

    private final EC2TagsFetcher ec2TagsFetcher;
    private final Set<InstanceStatusHandler> instanceStatusHandlers;
    private final InstanceStatusDB instanceStatusDB;
    private final EC2BadInstancesFetcher ec2BadInstancesFetcher;
    private final String[] instanceTagNames;

    public EC2StatusChecker(EC2BadInstancesFetcher ec2BadInstancesFetcher,
                            EC2TagsFetcher ec2TagsFetcher,
                            Set<InstanceStatusHandler> instanceStatusHandlers,
                            InstanceStatusDB instanceStatusDB,
                            String[] instanceTagNames) {
        this.ec2TagsFetcher = ec2TagsFetcher;
        this.instanceStatusHandlers = instanceStatusHandlers;
        this.instanceStatusDB = instanceStatusDB;
        this.ec2BadInstancesFetcher = ec2BadInstancesFetcher;
        this.instanceTagNames = instanceTagNames;
    }

    public void check() {
        Map<String, InstanceStatus> badInstances = ec2BadInstancesFetcher.get();
        if (badInstances.size() == 0) {
            Map<String, SerializableInstanceStatus> oldInstances = instanceStatusDB.getAll();
            // TODO: notify Slack about cleared status.
            for (String instanceId : oldInstances.keySet()) {
                LOG.info("Bad instance is back to normal for instanceId: {}", instanceId);
            }
        } else {
            for (InstanceStatus instanceStatus : badInstances.values()) {
                SerializableInstanceStatus newInstanceStatus = SerializableInstanceStatus
                        .create(instanceStatus);
                SerializableInstanceStatus oldInstanceStatus = instanceStatusDB
                        .get(newInstanceStatus.getInstanceId());
                LOG.info("Found new bad instance {}", instanceStatus);
                LOG.debug("Old object: {}", oldInstanceStatus);
                LOG.debug("New object: {}", newInstanceStatus);
                for (InstanceStatusHandler handler : instanceStatusHandlers) {
                    Map<String, String> instanceTags = ec2TagsFetcher.getInstanceTags(
                            newInstanceStatus.getInstanceId(), instanceTagNames);
                    handler.handle(oldInstanceStatus, newInstanceStatus, instanceTags);
                }
            }
            save(badInstances);
        }
    }

    private void save(Map<String, InstanceStatus> badInstances) {
        instanceStatusDB.clear();
        badInstances.forEach((key, value) -> {
            instanceStatusDB.set(key, SerializableInstanceStatus.create(value));
        });
        instanceStatusDB.save();
    }
}
