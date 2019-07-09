package io.github.udaysagar2177.ec2StatusChecks.statushandlers.slack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;


import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatus;
import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatusDetails;
import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatusEvent;
import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatusSummary;
import io.github.udaysagar2177.ec2StatusChecks.statushandlers.InstanceStatusHandler;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;

/**
 * Notifies Slack about {@link InstanceStatus} updates.
 *
 * @author uday.
 */
public class PostInstanceStatusToSlackHandler implements InstanceStatusHandler {
    private static final Logger LOG = LogManager.getLogger(PostInstanceStatusToSlackHandler.class);

    private final SlackNotifier slackNotifier;

    public PostInstanceStatusToSlackHandler(SlackNotifier slackNotifier) {
        this.slackNotifier = slackNotifier;
    }

    @Override
    public void handle(SerializableInstanceStatus oldInstanceStatus,
                       SerializableInstanceStatus newInstanceStatus, Map<String, String> tags) {
        try {
            boolean handleThisUpdate = handleFilter(oldInstanceStatus, newInstanceStatus, tags);
            if (!handleThisUpdate) {
                return;
            }
            Payload payload = this.preparePayload(oldInstanceStatus, newInstanceStatus, tags);
            if (payload != null) {
                this.slackNotifier.send(payload);
            }
        } catch (Exception e) {
            LOG.error("Exception occurred while sending slack notification for {}",
                    newInstanceStatus, e);
        }
    }

    /**
     * Returns true if this handler should handle this notification.
     */
    private boolean handleFilter(SerializableInstanceStatus oldInstanceStatus,
                                 SerializableInstanceStatus newInstanceStatus,
                                 Map<String, String> tags) {
        if (newInstanceStatus.equals(oldInstanceStatus)) {
            return false;
        }
        if (oldInstanceStatus == null) {
            if (isCompleted(newInstanceStatus)) {
                return false;
            }
            return checkForFailedStatus(newInstanceStatus.getInstanceStatus())
                    || checkForFailedStatus(newInstanceStatus.getSystemStatus());
        }
        Set<SerializableInstanceStatusEvent> newEvents = new HashSet<>(newInstanceStatus.getEvents());
        Set<SerializableInstanceStatusEvent> oldEvents = new HashSet<>(oldInstanceStatus.getEvents());
        return !newEvents.equals(oldEvents);
    }

    private boolean checkForFailedStatus(SerializableInstanceStatusSummary instanceStatusSummary) {
        return instanceStatusSummary.getDetails()
                .stream()
                .map(SerializableInstanceStatusDetails::getStatus)
                .anyMatch(s -> s.contains("Status: failed"));
    }

    private boolean isCompleted(SerializableInstanceStatus instanceStatus) {
        return instanceStatus.getEvents()
                .stream()
                .map(SerializableInstanceStatusEvent::getDescription)
                .filter(Objects::nonNull)
                .anyMatch(i -> i.contains("[Completed]"));
    }

    private Payload preparePayload(SerializableInstanceStatus oldInstanceStatus,
                                   SerializableInstanceStatus newInstanceStatus,
                                   Map<String, String> tags) {
        String color = "#F0E68C";
        for (SerializableInstanceStatusEvent event : newInstanceStatus.getEvents()) {
            if (!event.getDescription().contains("[Completed]")) {
                color = "danger";
                break;
            } else {
                color = "good";
            }
        }

        List<Field> fields = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = tags.entrySet()
                .iterator();
        int evenNumberOfEntries = tags.size() - tags.size() % 2;
        for (int i = 0; i < evenNumberOfEntries && iterator.hasNext(); ++i) {
            Map.Entry<String, String> tag = iterator.next();
            fields.add(Field.builder()
                    .title(capitalizeFirstLetter(tag.getKey()))
                    .value(tag.getValue())
                    .valueShortEnough(true)
                    .build());
        }
        if (evenNumberOfEntries != tags.size()) {
            Map.Entry<String, String> tag = iterator.next();
            fields.add(Field.builder()
                    .title(capitalizeFirstLetter(tag.getKey()))
                    .value(tag.getValue())
                    .build());
        }
        fields.add(Field.builder()
                .title("Instance ID")
                .value(newInstanceStatus.getInstanceId())
                .valueShortEnough(true)
                .build());
        fields.add(Field.builder()
                .title("Instance State")
                .value(newInstanceStatus.getInstanceState().getName())
                .valueShortEnough(true)
                .build());
        fields.add(Field.builder()
                .title("Instance Status")
                .value(newInstanceStatus.getInstanceStatus().getStatus())
                .valueShortEnough(true)
                .build());
        fields.add(Field.builder()
                .title("System Status")
                .value(newInstanceStatus.getSystemStatus().getStatus())
                .valueShortEnough(true)
                .build());
        if (!newInstanceStatus.getInstanceStatus()
                .getDetails()
                .isEmpty()) {
            fields.add(Field.builder()
                    .title("Instance Status Details")
                    .value(asList(newInstanceStatus.getInstanceStatus().getDetails()))
                    .valueShortEnough(true)
                    .build());
        }

        if (!newInstanceStatus.getSystemStatus().getDetails().isEmpty()) {
            fields.add(Field.builder()
                    .title("System Status Details")
                    .value(asList(newInstanceStatus.getSystemStatus().getDetails()))
                    .valueShortEnough(true)
                    .build());
        }

        for (SerializableInstanceStatusEvent event : newInstanceStatus.getEvents()) {
            fields.add(Field.builder()
                    .title(String.format("Event (%s : %s)", event.getNotBefore().getTimeAsString(),
                            event.getNotAfter().getTimeAsString()))
                    .value(event.getDescription())
                    .valueShortEnough(false)
                    .build());
        }

        List<Attachment> attachments = Collections.singletonList(Attachment
                .builder()
                .fields(fields)
                .color(color)
                .build());
        return Payload.builder()
                .username("EC2 status checks")
                .iconEmoji(":aws:")
                .attachments(attachments)
                .build();
    }

    private String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String asList(List<SerializableInstanceStatusDetails> details) {
        StringBuilder sb = new StringBuilder();
        for (SerializableInstanceStatusDetails detail : details) {
            sb.append(String.format("Name: %s, Status: %s\n", detail.getName(),
                    detail.getStatus()));
            if (detail.getImpairedSince().getTime() != -1L) {
                sb.append(String.format("ImpairedSince: %s ",
                        detail.getImpairedSince().getTimeAsString()));
            }
        }
        return sb.toString();
    }
}
