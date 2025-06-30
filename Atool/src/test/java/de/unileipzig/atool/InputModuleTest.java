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
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Job class.
 */
class InputModuleTest {
    private static final Logger LOGGER = Logger.getLogger(InputModuleTest.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("InputModuleTest"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private List<Job> jobs;

    @BeforeEach
    void setUp() {
        File[] files = new File[3];
        files[0] = new File("src/test/resources/empty.log");
        files[1] = new File("src/test/resources/broken.log");
        files[2] = new File("src/test/resources/broken2.log");

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
}
