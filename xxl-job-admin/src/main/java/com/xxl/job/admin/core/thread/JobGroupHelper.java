package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.core.biz.model.JobGroupParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * job group registry instance
 */
public class JobGroupHelper {
    private static final Logger logger = LoggerFactory.getLogger(JobGroupHelper.class);

    private static final JobGroupHelper instance = new JobGroupHelper();

    public static JobGroupHelper getInstance() {
        return instance;
    }

    private ThreadPoolExecutor jobGroupThreadPool = null;
    private Thread monitorThread;
    private volatile boolean toStop = false;

    public void start() {

        // for registry or remove
        jobGroupThreadPool = new ThreadPoolExecutor(
                1,
                5,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> new Thread(r, "xxl-job, admin job group" + r.hashCode()),
                (r, executor) -> {
                    r.run();
                    logger.warn(">>>>>>>>>>> xxl-job, job group register too fast, match thread pool rejected handler(run now).");
                });

        // for monitor
        monitorThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // auto registry group
                    List<XxlJobGroup> groupList = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().findByAddressType(0);
                    if (groupList != null && !groupList.isEmpty()) {

                        // remove dead address (admin/executor)
                        List<Integer> ids = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().findDead(RegistryConfig.DEAD_TIMEOUT, new Date());
                        if (ids != null && ids.size() > 0) {
                            XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().removeDead(ids);
                        }

                        // fresh online address (admin/executor)
                        HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
                        List<XxlJobRegistry> list = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
                        if (list != null) {
                            for (XxlJobRegistry item : list) {
                                if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
                                    String appname = item.getRegistryKey();
                                    List<String> registryList = appAddressMap.get(appname);
                                    if (registryList == null) {
                                        registryList = new ArrayList<String>();
                                    }

                                    if (!registryList.contains(item.getRegistryValue())) {
                                        registryList.add(item.getRegistryValue());
                                    }
                                    appAddressMap.put(appname, registryList);
                                }
                            }
                        }

                        // fresh group address
                        for (XxlJobGroup group : groupList) {
                            List<String> registryList = appAddressMap.get(group.getAppname());
                            String addressListStr = null;
                            if (registryList != null && !registryList.isEmpty()) {
                                Collections.sort(registryList);
                                StringBuilder addressListSB = new StringBuilder();
                                for (String item : registryList) {
                                    addressListSB.append(item).append(",");
                                }
                                addressListStr = addressListSB.toString();
                                addressListStr = addressListStr.substring(0, addressListStr.length() - 1);
                            }
                            group.setAddressList(addressListStr);
                            group.setUpdateTime(new Date());

                            XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().update(group);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, job registry monitor thread stop");
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("xxl-job, admin job group");
        monitorThread.start();
    }

    public void toStop() {
        toStop = true;

        // stop registryOrRemoveThreadPool
        jobGroupThreadPool.shutdownNow();

        // stop monitor (interrupt and wait)
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }


    // ---------------------- helper ----------------------

    public ReturnT<String> registerJobGroup(JobGroupParam jobGroupParam) {

        // valid
        if (!StringUtils.hasText(jobGroupParam.getAppName())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "Illegal Argument: app name must set");
        }

        // async execute
        jobGroupThreadPool.execute(() -> {
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
        });

        return ReturnT.SUCCESS;
    }


    private void freshJobGroupInfo(JobGroupParam jobGroupParam) {
        // Under consideration, prevent affecting core tables
    }


}
