package io.github.udaysagar2177.ec2StatusChecks.statushandlers.slack;

import java.io.IOException;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;

/**
 * Utility class to notify Slack.
 *
 * @author uday
 */
public class SlackNotifier {
    private final String webhookUrl;
    private final Slack slack;

    public SlackNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.slack = Slack.getInstance();
    }

    public WebhookResponse send(Payload payload) throws IOException {
        return this.slack.send(this.webhookUrl, payload);
    }

    public WebhookResponse send(String message) throws IOException {
        return this.slack.send(this.webhookUrl, Payload.builder().text(message).build());
    }
}
