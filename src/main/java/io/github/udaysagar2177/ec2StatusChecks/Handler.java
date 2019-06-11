package io.github.udaysagar2177.ec2StatusChecks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


import io.github.udaysagar2177.ec2StatusChecks.database.InstanceStatusDB;
import io.github.udaysagar2177.ec2StatusChecks.database.InstanceStatusDBOnS3;
import io.github.udaysagar2177.ec2StatusChecks.statushandlers.InstanceStatusHandler;
import io.github.udaysagar2177.ec2StatusChecks.statushandlers.slack.PostInstanceStatusToSlackHandler;
import io.github.udaysagar2177.ec2StatusChecks.statushandlers.slack.SlackNotifier;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

/**
 * Entry point into this lambda.
 *
 * Posts notifications about EC2 instances with 'bad' status checks and scheduled events. See
 * {@link EC2BadInstancesFetcher} for more details.
 *
 * @author uday
 */
public class Handler implements RequestHandler<Map<String, Object>, Boolean> {

    public Boolean handleRequest(Map<String, Object> input, Context context) {
        String region = getFromEnv("REGION");
        String bucket = getFromEnv("BUCKET");
        String dbFileOnS3 = getFromEnv("DB_FILE_ON_S3");
        String slackWebhookUrl = getFromEnv("SLACK_WEBHOOK_URL");
        String instanceTagNames = getFromEnv("INSTANCE_TAG_NAMES", null);
        String[] instanceTagNamesArr = null;
        if (instanceTagNames != null) {
            instanceTagNamesArr = instanceTagNames.split(",");
        }

        SlackNotifier slackNotifier = new SlackNotifier(slackWebhookUrl);
        Set<InstanceStatusHandler> instanceStatusHandlers = new HashSet<>();
        instanceStatusHandlers.add(new PostInstanceStatusToSlackHandler(slackNotifier));

        Ec2Client ec2Client = Ec2Client.builder()
                .region(Region.of(region))
                .build();
        EC2BadInstancesFetcher ec2BadInstancesFetcher = new EC2BadInstancesFetcher(ec2Client);
        EC2TagsFetcher ec2TagsFetcher = new EC2TagsFetcher(ec2Client);
        InstanceStatusDB instanceStatusDB = new InstanceStatusDBOnS3(region, bucket, dbFileOnS3);
        instanceStatusDB.load();
        EC2StatusChecker ec2StatusChecker = new EC2StatusChecker(ec2BadInstancesFetcher,
                ec2TagsFetcher, instanceStatusHandlers, instanceStatusDB, instanceTagNamesArr);
        ec2StatusChecker.check();
        return true;
    }

    private static String getFromEnv(String name) {
        String result = getFromEnv(name, null);
        if (result == null) {
            throw new IllegalStateException(
                    String.format("Environment variable not found for %s", name));
        } else {
            return result;
        }
    }

    private static String getFromEnv(String name, String defaultValue) {
        String result = System.getenv(name);
        return result == null ? defaultValue : result;
    }
}
