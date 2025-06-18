package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;

public class TestEval{
    private GenericTest test;
    private PostHocTest postHoctest;
    private final Job job;
    private String testName;
    private String steadyStateRunString;
    private String time;
    private String averageTimePerMilliVal;
    private String typeOfComparedRuns;
    private String skippedRunVal;
    private String bonferroniVal;
    private String comparedRunsVal;

    public TestEval(Job job, GenericTest test){
        this.test = test;
        this.job = job;
        prepareItem();
    }

    public TestEval(Job job, GenericTest test, PostHocTest postHoctest){
        this.test = test;
        this.postHoctest = postHoctest;
        this.job = job;
        preparePostHocItem();
    }

    private void preparePostHocItem() {
        Run steadyStateRun = postHoctest.getSteadyStateRun();
        testName = postHoctest.getTestName();

        if(steadyStateRun != null){
            steadyStateRunString = String.format("Run %s", steadyStateRun.getID());
            time = String.format("%f - %f", steadyStateRun.getStartTime() / 1000.0, steadyStateRun.getEndTime() / 1000.0);
        } else {
            steadyStateRunString = "No steady state run found";
        }

        averageTimePerMilliVal = "1"; //TODO
        if(test.isSkipGroup()){
            typeOfComparedRuns = "sequential";
        } else {
            typeOfComparedRuns = "adjacent";
        }

        if(test.isApplyBonferroni()){
            bonferroniVal = "Yes";
        } else {
            bonferroniVal = "No";
        }
        skippedRunVal = "0"; //TODO
        comparedRunsVal = "2"; //TODO
    }

    public String getSteadyStateRun() {
        return steadyStateRunString;
    }

    public String getTestName() {
        return testName;
    }

    public String getTime() {
        return time;
    }

    public String getAverageTimePerMilliVal() {
        return averageTimePerMilliVal;
    }

    public String getTypeOfComparedRuns() {
        return typeOfComparedRuns;
    }

    public String getSkippedRunVal() {
        return skippedRunVal;
    }

    private void prepareItem() {
        test.calculate();
        Run steadyStateRun = test.getSteadyStateRun();
        testName = test.getTestName();
        averageTimePerMilliVal = "1"; //TODO

        if(steadyStateRun != null){
            steadyStateRunString = String.format("Run %s", steadyStateRun.getID());
            time = String.format("%f - %f", steadyStateRun.getStartTime() / 1000.0, steadyStateRun.getEndTime() / 1000.0);
        } else {
            steadyStateRunString = "No steady state run found";
        }

        if(test.isSkipGroup()){
            typeOfComparedRuns = "sequential";
        } else {
            typeOfComparedRuns = "adjacent";
        }

        if(test.isApplyBonferroni()){
            bonferroniVal = "Yes";
        } else {
            bonferroniVal = "No";
        }
        skippedRunVal = "0"; //TODO
        comparedRunsVal = "2"; //TODO
    }

    public String getBonferroniVal() {
        return bonferroniVal;
    }

    public String getComparedRunsVal() {
        return comparedRunsVal;
    }
}