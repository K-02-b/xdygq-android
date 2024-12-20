package com.example.xdygq3;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class MyJobService extends JobService {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        EventBus.getDefault().post(new EventMessage(1, "UPDATE"));
        scheduleJobAgain();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(EventMessage event) {
    }

    private void scheduleJobAgain() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(shareData.JobInfo_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(shareData.getConfig() != null ? shareData.getConfig().DelayTime : 10000)
                .setBackoffCriteria(shareData.getConfig() != null ? shareData.getConfig().DelayTime * 2L : 20000, JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        scheduler.cancelAll();
        scheduler.schedule(jobInfo);
    }
}
