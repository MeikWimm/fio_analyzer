package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AtoolTTestTest {

    private Settings[] settings;
    private List<Job> jobs;

    @BeforeEach
    void setUp() throws URISyntaxException {
        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File[] files = logfilesDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        assertNotNull(files);
        settings = new Settings[5];
        settings[0] = new Settings(null);

        settings[1] = new Settings(null);
        settings[1].setTTestSkipRunsCounter(0);

        settings[2] = new Settings(null);
        settings[2].setTTestSkipRunsCounter(5);

        settings[3] = new Settings(null);
        settings[3].setTTestSkipRunsCounter(2);

        settings[4] = new Settings(null);
        settings[4].setTTestSkipRunsCounter(3);

        InputModule inputModule = new InputModule();

        inputModule.readFiles(files);
        jobs = inputModule.getJobs();
    }

    @Test
    void testInitialization() {
        for(Job job: jobs){
            for (Settings setting: settings){
                AtoolTTest ttest = new AtoolTTest(job, setting);
                assertNotNull(ttest, "T-Test instance should be initialized.");
                assertEquals(0.05, ttest.getAlpha(), "Alpha value should be set correctly.");
            }
        }
    }

    @Test
    void testCalculate() {
        for(Job job: jobs){
            for (Settings setting: settings){
                AtoolTTest ttest = new AtoolTTest(job, setting);
                ttest.calculate();
                assertNotNull(ttest.getResultRuns(), "T-Test result should not be null.");
                assertNotNull(ttest.getResultGroups(), "T-Test result group should not be null.");
                //assertNotNull(anova.getSteadyStateRun(), "Steady state run should not be null.");
            }
        }
    }
}
