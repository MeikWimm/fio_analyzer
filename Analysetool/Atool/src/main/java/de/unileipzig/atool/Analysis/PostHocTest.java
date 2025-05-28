package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;

import java.util.List;

public abstract class PostHocTest {
    protected GenericTest test;

    public PostHocTest(GenericTest test){
        this.test = test;
    }

}
