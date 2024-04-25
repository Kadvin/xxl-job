package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.*;

import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {


    // ---------------------- callback ----------------------

    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);


    // ---------------------- registry ----------------------

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registryRemove(RegistryParam registryParam);


    // ---------------------- biz (custome) ----------------------
    // group、job ... manage

    /**
     * Register current app as a new job group
     * <p>
     * update title if app name exist
     *
     * @param jobGroupParam the job group
     * @return result
     */
    ReturnT<String> registerJobGroup(JobGroupParam jobGroupParam);

    /**
     * Register current app jobs
     *
     * @param appName the app name
     * @param jobList jobs
     * @return result
     */
    ReturnT<String> registerJobList(String appName, List<JobParam> jobList);
}
