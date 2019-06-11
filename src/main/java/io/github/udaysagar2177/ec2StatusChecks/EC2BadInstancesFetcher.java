package io.github.udaysagar2177.ec2StatusChecks;

import java.util.HashMap;
import java.util.Map;


import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;

/**
 * Uses {@link Ec2Client} to fetch EC2 instances with bad status checks or scheduled events.
 *
 * Bad status checks include:
 *  - impaired
 *  - insufficient-data
 *
 * Scheduled events of interest include:
 *  - instance-reboot
 *  - system-reboot
 *  - system-maintenance
 *  - instance-retirement
 *  - instance-stop
 *
 * @author uday
 */
public class EC2BadInstancesFetcher {
    private final Ec2Client ec2Client;

    public EC2BadInstancesFetcher(Ec2Client ec2Client) {
        this.ec2Client = ec2Client;
    }

    public Map<String, InstanceStatus> get() {
        Map<String, InstanceStatus> data = new HashMap<>();
        Filter badInstanceStatusFilter = Filter.builder()
                .name("instance-status.status")
                .values("impaired", "insufficient-data")
                .build();
        Filter badSystemStatusFilter = Filter.builder()
                .name("system-status.status")
                .values("impaired", "insufficient-data")
                .build();
        Filter eventsFilter = Filter.builder()
                .name("event.code")
                .values("instance-reboot", "system-reboot", "system-maintenance",
                        "instance-retirement", "instance-stop")
                .build();
        data.putAll(this.findBadInstances(badInstanceStatusFilter));
        data.putAll(this.findBadInstances(badSystemStatusFilter));
        data.putAll(this.findBadInstances(eventsFilter));
        return data;
    }

    private Map<String, InstanceStatus> findBadInstances(Filter filter) {
        boolean complete = false;
        Map<String, InstanceStatus> data = new HashMap<>();
        String nextToken = "";
        while (!complete) {
            DescribeInstanceStatusRequest request = DescribeInstanceStatusRequest
                    .builder()
                    .filters(filter)
                    .nextToken(nextToken)
                    .includeAllInstances(true)
                    .build();
            DescribeInstanceStatusResponse response = this.ec2Client
                    .describeInstanceStatus(request);
            for (InstanceStatus instanceStatus : response.instanceStatuses()) {
                data.put(instanceStatus.instanceId(), instanceStatus);
            }
            nextToken = response.nextToken();
            complete = nextToken == null;
        }
        return data;
    }
}
