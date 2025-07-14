package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GenericTypeTest {

    private Settings settings;
    private List<Job> jobs;

    @BeforeEach
    void setUp() throws URISyntaxException {
        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File[] files = logfilesDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        assertNotNull(files);

        settings = new Settings(null);
        settings.setAnovaSkipRunsCounter(2);
        settings.setAnovaUseAdjacentRun(true);
        settings.setGroupSize(3);

        InputModule inputModule = new InputModule();

        inputModule.readFiles(files);
        jobs = inputModule.getJobs();
    }

    @Test
    void testInitialization() {
        for(Job job: jobs){
            List<GenericTest> tests = Arrays.asList(
                    new AtoolTTest(job, settings),
                    new ConInt(job, settings),
                    new Anova(job, settings),
                    new MannWhitney(job, settings)
            );
            for (GenericTest test: tests){
                assertNotNull(test, "Anova instance should be initialized.");
                assertEquals(0.05, test.getAlpha(), "Alpha value should be set correctly.");
            }
        }
    }

    @Test
    void testCalculate() {
        for(Job job: jobs){
            List<GenericTest> tests = Arrays.asList(
                    new AtoolTTest(job, settings),
                    new ConInt(job, settings),
                    new Anova(job, settings),
                    new MannWhitney(job, settings)
            );

            for (GenericTest test: tests){
                test.calculate();
                List<Section> result = test.getResultRuns();
                assertNotNull(result, "result list should not be null.");
                assertNotNull(test.getGroups(), "Runs should not be empty after calculation.");
                for(List<Section> group: test.getGroups()){
                    assertNotNull(group, "Group should not be null.");
                    for(Section section : group){
                        assertNotNull(section, "Run should not be null.");
                    }
                }
                assertNotNull(test.getResultGroups(), "Result groups should not be null.");
                for(List<Section> group: test.getResultGroups()){
                    assertNotNull(group, "Result group should not be null.");
                    for(Section section : group){
                        assertNotNull(section, "Result run should not be null.");
                    }
                }

                //Assert result list of runs
                assertNotNull(test.getResultRuns(), "Result runs should not be null.");
                for(Section section : test.getResultRuns()){
                    assertNotNull(section, "Result run should not be null.");
                }
            }
        }
    }
}
