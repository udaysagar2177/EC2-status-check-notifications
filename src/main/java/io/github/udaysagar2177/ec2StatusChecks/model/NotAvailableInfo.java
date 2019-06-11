package io.github.udaysagar2177.ec2StatusChecks.model;

/**
 * Utility class to store constants related to not available info.
 *
 * @author uday
 */
public class NotAvailableInfo {
    public static final String TEXT = "N/A";

    private NotAvailableInfo() {}

    public static String check(String text) {
        return text == null ? "N/A" : text;
    }
}
