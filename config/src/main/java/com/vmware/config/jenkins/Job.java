package com.vmware.config.jenkins;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.vmware.util.UrlUtils;

public class Job {
    /**
     * Display name for the job in the testing done section
     */
    @Expose(serialize = false, deserialize = false)
    public String jobDisplayName = "Build";
    public String name;
    public String url;
    public String color;

    @Expose(serialize = false, deserialize = false)
    public List<JobParameter> parameters = Collections.emptyList();

    /**
     * Buildweb jobs can be either sandbox or official builds
     */
    public static Job buildwebJob(String url, String jobDisplayName) {
        Job job = new Job(url);
        job.jobDisplayName = jobDisplayName;
        return job;
    }

    public Job() {}

    private Job(String url) {
        this.url = url;
    }

    public void constructUrl(String baseUrl, String jobName) {
        this.name = jobName;
        baseUrl = UrlUtils.addTrailingSlash(baseUrl);
        this.url = baseUrl + "job/" + jobName + "/";
    }

    public String getBuildWithParametersUrl() {
        return url + "buildWithParameters";
    }

    public String getInfoUrl() {
        return url + "api/json";
    }
}
