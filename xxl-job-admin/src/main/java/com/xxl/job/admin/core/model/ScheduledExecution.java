package com.xxl.job.admin.core.model;

import java.util.Date;

public class ScheduledExecution implements Comparable<ScheduledExecution>{
    private String params;
    private int systemTimeSeconds;

    public ScheduledExecution(String params, int systemTimeSeconds) {
        this.params = params;
        this.systemTimeSeconds = systemTimeSeconds;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public int getSystemTimeSeconds() {
        return systemTimeSeconds;
    }

    public void setSystemTimeSeconds(int systemTimeSeconds) {
        this.systemTimeSeconds = systemTimeSeconds;
    }

    public Date toDate() {
        return new Date(this.systemTimeSeconds * 1000L);
    }

    @Override
    public String toString() {
        return params + "@" + systemTimeSeconds;
    }

    @Override
    public int compareTo(ScheduledExecution other) {
        return this.systemTimeSeconds - other.systemTimeSeconds;
    }

    public static ScheduledExecution parse(String scheduleConf) {
        String[] paramsAndSystemTimeSeconds = scheduleConf.split("@");
        String params = paramsAndSystemTimeSeconds[0];
        int systemTimeSeconds = Integer.parseInt(paramsAndSystemTimeSeconds[1]);
        return new ScheduledExecution(params, systemTimeSeconds);
    }

    public static ScheduledExecution parse(String params, long delayedInSeconds) {
        return new ScheduledExecution(params, (int) (System.currentTimeMillis() / 1000 + delayedInSeconds));
    }
}
