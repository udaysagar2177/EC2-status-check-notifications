package io.github.udaysagar2177.ec2StatusChecks;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.TagDescription;

/**
 * Fetches tags attached to an EC2 instance.
 *
 * @author uday
 */
public class EC2TagsFetcher {
    private final Ec2Client ec2Client;

    public EC2TagsFetcher(Ec2Client ec2Client) {
        this.ec2Client = ec2Client;
    }

    public Map<String, String> getInstanceTags(String instanceId, String[] tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            return Collections.emptyMap();
        }
        Filter instanceResourceFilter = Filter.builder()
                .name("resource-type")
                .values("instance")
                .build();
        Filter instanceIdFilter = Filter.builder()
                .name("resource-id")
                .values(instanceId)
                .build();
        Filter[] filters = new Filter[] { instanceResourceFilter, instanceIdFilter, Filter.builder()
                        .name("key")
                        .values(tagNames)
                        .build() };
        DescribeTagsRequest request = DescribeTagsRequest.builder()
                .filters(filters)
                .build();
        DescribeTagsResponse response = this.ec2Client.describeTags(request);
        Map<String, String> tags = response.tags()
                .stream()
                .collect(Collectors.toMap(TagDescription::key, TagDescription::value));
        Map<String, String> sortedTags = new LinkedHashMap<>(tagNames.length);
        for (String tagName : tagNames) {
            String tagValue = tags.get(tagName);
            if (tagValue != null) {
                sortedTags.put(tagName, tagValue);
            }
        }
        return sortedTags;
    }
}
