package com.xxl.job.core.biz.model;

public class JobGroupParam {
    private String appName;
    private String title;

    public JobGroupParam(String appName, String title) {
        this.appName = appName;
        this.title = title;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
