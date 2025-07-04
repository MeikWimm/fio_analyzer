package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
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
        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File[] files = logfilesDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        assertNotNull(files);
        settings = new Settings[5];
        settings[0] = new Settings(null);

        settings[1] = new Settings(null);
        settings[1].setAnovaSkipRunsCounter(0);
        settings[1].setGroupSize(2);

        settings[2] = new Settings(null);
        settings[2].setAnovaSkipRunsCounter(5);
        settings[2].setGroupSize(5);

        settings[3] = new Settings(null);
        settings[3].setAnovaSkipRunsCounter(2);
        settings[3].setGroupSize(1);

        settings[4] = new Settings(null);
        settings[4].setAnovaSkipRunsCounter(3);
        settings[4].setGroupSize(2);

        InputModule inputModule = new InputModule();

        inputModule.readFiles(files);
        jobs = inputModule.getJobs();
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
        for(Settings setting: settings){
            Logging.log(Level.INFO, "Anova Test", "Setting used " + setting.toString());
            for (Job job: jobs){
                Anova anova = new Anova(job, setting);
                anova.calculate();
                assertNotNull(anova.getResultRuns(), "Anova result should not be null.");
                assertNotNull(anova.getResultGroups(), "Anova result group should not be null.");
                //assertNotNull(anova.getSteadyStateRun(), "Steady state run should not be null.");
            }
        }
    }
}
