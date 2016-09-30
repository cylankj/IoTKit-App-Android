package com.cylan.jiafeigou.support.download.report.listener;

/**
 * Created by hunt on 16-4-22.
 */
public class FailReason {
    private String reason;

    public FailReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "reason: " + reason;
    }
}
