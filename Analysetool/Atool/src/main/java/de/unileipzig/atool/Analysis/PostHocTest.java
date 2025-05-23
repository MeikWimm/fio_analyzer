package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;

import java.util.List;

public abstract class PostHocTest {
    Job job;

    public PostHocTest(GenericTest test){
        this.job = test.getJob();
    }
}
