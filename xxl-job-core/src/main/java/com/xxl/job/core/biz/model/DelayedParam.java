package com.xxl.job.core.biz.model;

public class DelayedParam {
    private long delayedSeconds;
    private String params;

    public DelayedParam(long delayedSeconds, String params) {
        this.delayedSeconds = delayedSeconds;
        this.params = params;
    }

    public DelayedParam() {
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public long getDelayedSeconds() {
        return delayedSeconds;
    }

    public void setDelayedSeconds(long delayedSeconds) {
        this.delayedSeconds = delayedSeconds;
    }
}
