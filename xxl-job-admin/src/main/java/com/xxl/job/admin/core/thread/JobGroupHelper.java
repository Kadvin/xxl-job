package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.core.biz.model.JobGroupParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * job group management, simplified as sync mode
 */
public class JobGroupHelper {
    private static final Logger logger = LoggerFactory.getLogger(JobGroupHelper.class);

    private static final JobGroupHelper instance = new JobGroupHelper();

    public static JobGroupHelper getInstance() {
        return instance;
    }

    public void start() {

    }

    public void toStop() {
    }


    // ---------------------- helper ----------------------

    public ReturnT<String> registerJobGroup(JobGroupParam jobGroupParam) {
        // 对于每个调用者，都搞成同步的，避免各个应用在 configure job group + configure job list之间的依赖同步
        // valid
        if (!StringUtils.hasText(jobGroupParam.getAppName())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "Illegal Argument: app name must set");
        }

        try {
            XxlJobGroupDao dao = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao();
            XxlJobGroup exist = dao.findByAppName(jobGroupParam.getAppName());
            if (exist == null) {
                XxlJobGroup jobGroup = new XxlJobGroup();
                jobGroup.setAppname(jobGroupParam.getAppName());
                jobGroup.setTitle(jobGroupParam.getTitle());
                jobGroup.setAddressType(0);
                dao.save(jobGroup);
                logger.info("Job group {} auto registered with title: {}", jobGroupParam.getAppName(), jobGroupParam.getTitle());
            } else {
                if (exist.getTitle().equals(jobGroupParam.getTitle())) {
                    dao.update(exist);
                } else {
                    logger.debug("Job group {} registered before, no changes", jobGroupParam.getAppName());
                }
            }
            freshJobGroupInfo(jobGroupParam);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }


    private void freshJobGroupInfo(JobGroupParam jobGroupParam) {
        // Under consideration, prevent affecting core tables
    }


}
