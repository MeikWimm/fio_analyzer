package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericTypeTest {

    private Settings settings;
    private List<Job> jobs;

    @BeforeEach
    void setUp() {
        File[] files = new File[3];

        files[0] = new File("src/test/resources/loop_40_512m.log");
        files[1] = new File("src/test/resources/generated_50_loops.log");
        files[2] = new File("src/test/resources/100_lines");
        settings = new Settings(null);
        settings.setAnovaSkipRunsCounter(2);
        settings.setAnovaUseAdjacentRun(true);
        settings.setGroupSize(3);
        settings.setWindowSize(5);

        InputModule inputModule = new InputModule(settings);
        inputModule.readFiles(files);
        jobs = inputModule.getJobs();
    }

    @Test
    void testInitialization() {
        for(Job job: jobs){
            List<GenericTest> tests = Arrays.asList(
                    new TTest(job, settings, job.getAlpha()),
                    new ConInt(job, settings, job.getAlpha()),
                    new Anova(job, settings, job.getAlpha()),
                    new MannWhitney(job, settings, job.getAlpha())
            );
            for (GenericTest test: tests){
                test = new Anova(job, settings, job.getAlpha());
                assertNotNull(test, "Anova instance should be initialized.");
                assertEquals(0.05, test.getAlpha(), "Alpha value should be set correctly.");
                assertEquals(5, settings.getWindowSize(), "Settings window size should match initialization.");
            }
        }
    }

    @Test
    void testCalculate() {
        for(Job job: jobs){
            List<GenericTest> tests = Arrays.asList(
                    new TTest(job, settings, job.getAlpha()),
                    new ConInt(job, settings, job.getAlpha()),
                    new Anova(job, settings, job.getAlpha()),
                    new MannWhitney(job, settings, job.getAlpha())
            );

            for (GenericTest test: tests){
                test.calculate();
                Job calculatedJob = test.getJob();
                assertNotNull(calculatedJob, "Calculated job should not be null.");
                assertEquals(job.getData().size(), calculatedJob.getData().size(), "Data size should match the original data size.");
                assertEquals(job.getRunsCounter(), calculatedJob.getRunsCounter(), "Runs counter should match the original runs counter.");
                assertNotNull(test.getGroups(), "Runs should not be empty after calculation.");
                assertEquals(job.getRunsCounter() - settings.getAnovaSkipRunsCounter(), test.getGroups().size(), "Groups size should match the original runs counter.");
                for(List<Run> group: test.getGroups()){
                    assertNotNull(group, "Group should not be null.");
                    assertEquals(settings.getGroupSize(), group.size(), "Group size should match the group size setting.");
                    for(Run run: group){
                        assertNotNull(run, "Run should not be null.");
                    }
                }
                assertNotNull(test.getResultGroups(), "Result groups should not be null.");
                assertEquals(test.getGroups().size() - 1, test.getResultGroups().size(), "Result groups size should match the groups size.");
                for(List<Run> group: test.getResultGroups()){
                    assertNotNull(group, "Result group should not be null.");
                    for(Run run: group){
                        assertNotNull(run, "Result run should not be null.");
                    }
                }

                //Assert result list of runs
                assertNotNull(test.getResultRuns(), "Result runs should not be null.");
                for(Run run: test.getResultRuns()){
                    assertNotNull(run, "Result run should not be null.");
                }
            }
        }
    }
}
