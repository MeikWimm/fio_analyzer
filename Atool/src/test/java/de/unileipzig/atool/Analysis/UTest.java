package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class UTest {

    private Settings[] settings;
    private List<Job> jobs;

    @BeforeEach
    void setUp() throws URISyntaxException {
        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File[] files = logfilesDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        assertNotNull(files);
        settings = new Settings[6];
        settings[0] = new Settings(null);
        settings[0].setUTestSkipRunsCounter(0);

        settings[1] = new Settings(null);
        settings[1].setUTestSkipRunsCounter(1);

        settings[2] = new Settings(null);
        settings[2].setUTestSkipRunsCounter(2);

        settings[3] = new Settings(null);
        settings[3].setUTestSkipRunsCounter(3);

        settings[4] = new Settings(null);
        settings[4].setUTestSkipRunsCounter(4);

        settings[5] = new Settings(null);
        settings[5].setUTestSkipRunsCounter(5);

        InputModule inputModule = new InputModule(settings[0]);

        inputModule.readFiles(files);
        jobs = inputModule.getJobs();
    }

    @Test
    void testInitialization() {
        for(Job job: jobs){
            for (Settings setting: settings){
                MannWhitney utest = new MannWhitney(job, setting);
                assertNotNull(utest, "U-Test instance should be initialized.");
                assertEquals(0.05, utest.getAlpha(), "Alpha value should be set correctly.");
            }
        }
    }

    @Test
    void testCalculate() {
        for(Job job: jobs){
            for (Settings setting: settings){
                MannWhitney utest = new MannWhitney(job, setting);
                utest.calculate();
                assertNotNull(utest.getResultRuns(), "U-Test result should not be null.");
                assertNotNull(utest.getResultGroups(), "U-Test result group should not be null.");
                //assertNotNull(anova.getSteadyStateRun(), "Steady state run should not be null.");
            }
        }
    }
}
