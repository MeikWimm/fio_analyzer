package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericTest {
    private static final Logger LOGGER = Logger.getLogger(GenericTest.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    protected Job job;
    protected List<List<Run>> groups;
    List<List<Run>> resultGroups;
    List<Run> resultRuns;
    double alpha;
    public GenericTest(Job job, boolean skipGroup, int groupSize, double alpha){
        this.job = new Job(job);
        this.groups = Job.setupGroups(this.job, skipGroup, groupSize);
        this.resultGroups = new ArrayList<>();
        this.resultRuns = new ArrayList<>();
        this.alpha = alpha;
    }

    public void draw(){}

    public void calculate(){}

    public void calculatePostHoc(PostHocAnalyzer postHocAnalyzer){
        if(this.resultGroups.size() < 2) {
            LOGGER.log(Level.WARNING, String.format("%s group size of test result is 1", this.getClass().getName()));
            return;
        }

        for (List<Run> runs: this.resultGroups){
            for (Run run: runs){
                if(run.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
                    this.resultRuns.add(run);
                }
            }
        }

        job.resetRuns();
        postHocAnalyzer.apply(this.resultRuns, this.resultGroups);
    }

    public Job getJob() {
        return this.job;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getCriticalValue(){
        return Run.UNDEFINED_DOUBLE_VALUE;
    }
}
