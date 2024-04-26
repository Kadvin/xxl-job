package com.xxl.job.core.biz.model;


public class JobParam {
    private String handler, description, scheduleType, scheduleConf, alarmEmail, owner, routeStrategy, misfireStrategy, blockStrategy;
    private int timeout = 30, failRetryCount = 0;

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getScheduleConf() {
        return scheduleConf;
    }

    public void setScheduleConf(String scheduleConf) {
        this.scheduleConf = scheduleConf;
    }

    public String getAlarmEmail() {
        return alarmEmail;
    }

    public void setAlarmEmail(String alarmEmail) {
        this.alarmEmail = alarmEmail;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRouteStrategy() {
        return routeStrategy;
    }

    public void setRouteStrategy(String routeStrategy) {
        this.routeStrategy = routeStrategy;
    }

    public String getMisfireStrategy() {
        return misfireStrategy;
    }

    public void setMisfireStrategy(String misfireStrategy) {
        this.misfireStrategy = misfireStrategy;
    }

    public String getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(String blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getFailRetryCount() {
        return failRetryCount;
    }

    public void setFailRetryCount(int failRetryCount) {
        this.failRetryCount = failRetryCount;
    }

    // 校验一个任务是否有效
    public boolean validate() {
        if (isBlank(handler)) return false;
        if (isBlank(scheduleType)) return false;
        if ("cron".equalsIgnoreCase(scheduleType) && isBlank(scheduleConf)) return false;
        return true;
    }

    public JobParam clone() {
        try {
            return (JobParam) super.clone();
        } catch (CloneNotSupportedException e) {
            JobParam cloned = new JobParam();
            cloned.setHandler(this.handler);
            cloned.setDescription(this.description);
            cloned.setScheduleType(this.scheduleType);
            cloned.setScheduleConf(this.scheduleConf);
            cloned.setAlarmEmail(this.alarmEmail);
            cloned.setOwner(this.owner);
            cloned.setRouteStrategy(this.routeStrategy);
            cloned.setMisfireStrategy(this.misfireStrategy);
            cloned.setBlockStrategy(this.blockStrategy);
            cloned.setTimeout(this.timeout);
            cloned.setFailRetryCount(this.failRetryCount);
            return cloned;
        }
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen == 0) {
            return true;
        } else {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

}
