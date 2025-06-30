/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.Anova;
import javafx.scene.chart.XYChart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Job class.
 */
class JobTest {
    private static final Logger LOGGER = Logger.getLogger(JobTest.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("JobTest"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private List<Job> jobs;

    @BeforeEach
    void setUp() throws URISyntaxException {
        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File[] files = logfilesDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        assertNotNull(files);

        Settings settings = new Settings(null);
        InputModule inputModule = new InputModule(settings);

        inputModule.readFiles(files);
        jobs = inputModule.getJobs();

    }

    @Test
    void testJobInitialization() {
        for (Job job: jobs){
            assertNotNull(job, "Job instance should be initialized.");
            //assertEquals(10, job.getData().size(), "Job data should contain the expected number of DataPoint objects.");
            assertEquals(4, job.getRunsCounter(), "Job runsCounter should be initialized correctly.");
        }
    }

    @Test
    void testUpdateRunsData() {
        int[] testValsAverageSpeedPerMilli = {1, 25, 77, 100};
        int groupSize = 2;
        for (Job job: jobs){
            for (int vals: testValsAverageSpeedPerMilli){
                job.setAverageTimePerMilliSec(vals);
                job.updateRunsData();
                List<List<Run>> groups = Job.setupGroups(job, false, groupSize);
                List<Run> runs = job.getRuns();

                // Assert
                assertEquals(4, runs.size(), "Job should have the expected number of runs after update.");
                assertFalse(runs.isEmpty(), "Runs should not be empty after updating runs data.");
                assertFalse(groups.isEmpty(), "Runs should not be empty after updating runs data.");
                assertEquals(groupSize, groups.getFirst().size(), "Job should have the expected number of 2 after update.");

                //LOGGER.log(Level.FINEST, () -> String.format("Average speed per millisecond: %d", vals));
                //LOGGER.log(Level.FINEST, () -> String.format("Average speed per run: %f", job.getAverageSpeed()));
            }
        }
    }

    @Test
    void testPrepareSkippedData() {
        for (Job job: jobs){
            // Arrange
            int initialSize = job.getData().size();

            // Act
            job.prepareSkippedData(1);

            // Assert
            assertTrue(job.getData().size() < initialSize, "Data size should be reduced after skipping runs.");
        }
    }

    @Test
    void testGetSeries() {
        for (Job job: jobs){
            List<XYChart.Data<Number, Number>> series = job.getSeries();

            // Assert
            assertNotNull(series, "Speed series should not be null.");
            //assertEquals(10, series.size(), "Speed series should contain the expected number of data points.");
        }
    }

    @Test
    void testMADNormalization() {
        // Act
        for (Job job: jobs){
            List<DataPoint> normalizedData = job.getMADNormalized();
            List<DataPoint> originalData = job.getData();
            assertNotNull(normalizedData, "MAD normalized data should not be null.");
            assertEquals(originalData.size(), normalizedData.size(), "Normalized data size should match the original data size.");
        }
    }

    @Test
    void testGetAverageSpeed() {
        for (Job job: jobs){
            // Arrange
            job.setAverageSpeed(50.0);

            // Act
            double averageSpeed = job.getAverageSpeed();

            // Assert
            assertEquals(50.0 / Settings.CONVERSION_VALUE, averageSpeed, 1e-9, "Average speed should be calculated correctly.");
        }
    }
}
