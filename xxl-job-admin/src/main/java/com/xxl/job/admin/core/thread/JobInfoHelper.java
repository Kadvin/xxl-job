package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.JobParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * job info management, copied from JobRegistryHelper
 */
public class JobInfoHelper {
    private static final Logger logger = LoggerFactory.getLogger(JobInfoHelper.class);

    private static final JobInfoHelper instance = new JobInfoHelper();

    public static JobInfoHelper getInstance() {
        return instance;
    }

    private ThreadPoolExecutor jobInfoThreadPool = null;

    // private volatile boolean toStop = false;

    public void start() {

        // for registry or remove
        jobInfoThreadPool = new ThreadPoolExecutor(
                1,
                5,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(6),
                r -> new Thread(r, "xxl-job, admin job info " + r.hashCode()),
                (r, executor) -> {
                    r.run();
                    logger.warn(">>>>>>>>>>> xxl-job, job info register too fast, match thread pool rejected handler(run now).");
                });
    }

    public void toStop() {
        // toStop = true;

        // stop registryOrRemoveThreadPool
        jobInfoThreadPool.shutdownNow();

    }

    public ReturnT<String> registerJobList(String appName, List<JobParam> jobList) {
        XxlJobAdminConfig adminConfig = XxlJobAdminConfig.getAdminConfig();
        XxlJobGroupDao groupDao = adminConfig.getXxlJobGroupDao();
        XxlJobGroup group = groupDao.findByAppName(appName);
        if (group == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, appName + " job group hasn't been registered");
        }
        jobInfoThreadPool.execute(() -> {
            XxlJobService service = adminConfig.getXxlJobService();
            service.registerJobList(group, jobList);

        });
        return ReturnT.SUCCESS;
    }

    public ReturnT<String> performLater(String jobHandler, long delayedSeconds, String params) {
        XxlJobAdminConfig adminConfig = XxlJobAdminConfig.getAdminConfig();
        XxlJobInfoDao dao = adminConfig.getXxlJobInfoDao();
        XxlJobInfo jobInfo = dao.findByHandler(jobHandler);
        if( jobInfo == null ){
            return new ReturnT<>(ReturnT.FAIL_CODE, "There is no job handler: " + jobHandler);
        }
        jobInfoThreadPool.execute(() -> {
            XxlJobService service = adminConfig.getXxlJobService();
            ReturnT<String> result = service.performLater(jobInfo, delayedSeconds, params);
            if( result.success() ){
                logger.info("performLater({}, {}, {}) registered a schedule execution", jobInfo.getExecutorHandler(), delayedSeconds, params);
            } else {
                logger.warn("performLater({}, {}, {}) register failed: {}", jobInfo.getExecutorHandler(), delayedSeconds, params, result.getMsg());
            }
        });
        return ReturnT.SUCCESS;
    }

}
