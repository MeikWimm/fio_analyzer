package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import net.sourceforge.jdistlib.F;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class AnovaTest {

    private Settings[] settings;
    private List<Job> jobs;

    @BeforeEach
    void setUp() throws URISyntaxException {
        File[] files = new File[6];

        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File file1 = new File(logfilesDir + "/" + "1_Line.log");
        files[0] = file1;
        File file2 = new File(logfilesDir + "/" + "broken.log");
        files[1] = file2;
        File file3 = new File(logfilesDir + "/" + "broken2.log");
        files[2] = file3;
        File file4 = new File(logfilesDir + "/" + "empty.log");
        files[3] = file4;
        File file5 = new File(logfilesDir + "/" + "mytest_5_loops_bw.1.log");
        files[4] = file5;
        File file6 = new File(logfilesDir + "/" + "vmTest_loop_20_128MB_bw.1.log");
        files[5] = file6;

        InputModule inputModule = new InputModule();
        inputModule.readFiles(files);
        jobs = inputModule.getJobs();

        jobs.get(0).setRunsCounter(5);
        jobs.get(0).setCvThreshold(.1);

        jobs.get(1).setRunsCounter(20);
        jobs.get(1).setCvThreshold(.25);

        settings = new Settings[5];
        settings[0] = new Settings(null);

        settings[1] = new Settings(null);
        settings[1].setAnovaSkipRunsCounter(0);
        settings[1].setGroupSize(2);
        settings[1].setAnovaUseAdjacentRun(false);

        settings[2] = new Settings(null);
        settings[2].setAnovaSkipRunsCounter(5);
        settings[2].setGroupSize(5);

        settings[3] = new Settings(null);
        settings[3].setAnovaSkipRunsCounter(2);
        settings[3].setGroupSize(1);

        settings[4] = new Settings(null);
        settings[4].setAnovaSkipRunsCounter(3);
        settings[4].setGroupSize(2);




    }

    @Test
    void testInitialization() {
        for(Job job: jobs){
            for (Settings setting: settings){
                Anova anova = new Anova(job, setting);
                assertNotNull(anova, "Anova instance should be initialized.");
                assertEquals(0.05, anova.getAlpha(), "Alpha value should be set correctly.");
            }
        }
    }

    @Test
    void testCalculate() {
        for(Job job: jobs){
            Logging.log(Level.INFO, "Anova Test", "Testing Job: " + job.getFileName());
            for (Settings setting: settings){
                Anova anova = new Anova(job, setting);
                anova.calculate();
                assertNotNull(anova.getResultRuns(), "Anova result should not be null.");
                assertNotNull(anova.getResultGroups(), "Anova result group should not be null.");
            }
        }
    }
}
