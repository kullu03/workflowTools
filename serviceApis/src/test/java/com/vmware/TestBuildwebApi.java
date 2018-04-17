package com.vmware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.vmware.buildweb.Buildweb;
import com.vmware.buildweb.domain.BuildwebBuild;

/**
 * Tests Buildweb api calls. This is VMware specific.
 */
public class TestBuildwebApi extends BaseTests {

    private Buildweb buildweb;

    @Before
    public void init() {
        String url = testProperties.getProperty("buildweb.url");
        String apiUrl = testProperties.getProperty("buildweb.api.url");
        String logFileName = testProperties.getProperty("buildweb.log.file.name");
        String username = testProperties.getProperty("buildweb.username");
        buildweb = new Buildweb(url, apiUrl, logFileName, username);
    }

    @Test
    public void canGetSandboxBuild() {
        BuildwebBuild build = buildweb.getSandboxBuild("13170580");
        assertEquals(13170580, build.id);
        assertEquals(BuildResult.SUCCESS, build.buildResult);
    }

    @Test
    public void buildWithCompileErrorStateIsTreatedAsFailed() {
        BuildwebBuild build = buildweb.getSandboxBuild("11330096");
        assertEquals(11330096, build.id);
        assertEquals(BuildResult.FAILURE, build.buildResult);
        String buildOutput = buildweb.getBuildOutput(String.valueOf(build.id), 300);
        System.out.println(buildOutput);
    }

    @Test
    public void getBuildOutput() {
        String buildOutput = buildweb.getBuildOutput("13174750", 50);
        assertNotNull(buildOutput);
    }

}
