/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Job class.
 */
class InputModuleTest {
    private List<Job> jobs;

    @BeforeEach
    void setUp() throws URISyntaxException {
        File logfilesDir = new File(Objects.requireNonNull(this.getClass().getResource("/logfiles")).toURI());
        File[] files = logfilesDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        assertNotNull(files);

        InputModule inputModule = new InputModule();
        inputModule.readFiles(files);

        jobs = inputModule.getJobs();
        assertEquals(2, jobs.size());
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
