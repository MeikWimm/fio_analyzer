package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Section;

public class TestEval{
    private final GenericTest test;
    private PostHocTest postHoctest;
    private String testName;
    private String steadyStateRunString;
    private String time;
    private String skippedRunVal;
    private String bonferroniVal;
    private String comparedRunsVal;
    private TestEval postHocEval;
    private double averageSpeedBeforeSkip;
    private double averageSpeedAfterSkip;

    public TestEval(GenericTest test){
        this.test = test;
        this.test.calculate();
        prepareItem();

        if (test.getPostHocTest() != null){
            postHocEval = new TestEval(this.test, test.getPostHocTest());
        }
    }

    public TestEval getPostHocTest(){
        return postHocEval;
    }

    private TestEval(GenericTest test, PostHocTest postHoctest){
        this.test = test;
        this.postHoctest = postHoctest;
        preparePostHocItem();
    }

    private void preparePostHocItem() {
        Section steadyStateSection = postHoctest.getSteadyStateRun();
        testName = postHoctest.getTestName();

        if(steadyStateSection != null){
            steadyStateRunString = String.format("Section %s", steadyStateSection.getID());
            time = String.format("%f", steadyStateSection.getStartTime() / 1000.0);
        } else {
            steadyStateRunString = "No steady state found";
        }

        if(test.isApplyBonferroni()){
            bonferroniVal = "Yes";
        } else {
            bonferroniVal = "No";
        }
        skippedRunVal = Integer.toString(test.getSkippedRunCount());
        comparedRunsVal = Integer.toString(test.getGroupSize());
        averageSpeedBeforeSkip = test.getAverageSpeedBeforeSkip();
        averageSpeedAfterSkip = test.getAverageSpeedAfterSkip();
    }

    private void prepareItem() {
        Section steadyStateSection = test.getSteadyStateRun();
        testName = test.getTestName();

        if(steadyStateSection != null){
            steadyStateRunString = String.format("Section %s", steadyStateSection.getID());
            time = String.format("%f", steadyStateSection.getStartTime() / 1000.0);
        } else {
            steadyStateRunString = "No steady state found";
        }

        if(test.isApplyBonferroni()){
            bonferroniVal = "Yes";
        } else {
            bonferroniVal = "No";
        }
        skippedRunVal = Integer.toString(test.getSkippedRunCount());
        comparedRunsVal = Integer.toString(test.getGroupSize());
        averageSpeedBeforeSkip = test.getAverageSpeedBeforeSkip();
        averageSpeedAfterSkip = test.getAverageSpeedAfterSkip();
    }

    public String getBonferroniVal() {
        return bonferroniVal;
    }

    public String getComparedRunsVal() {
        return comparedRunsVal;
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

    public String getSkippedRunVal() {
        return skippedRunVal;
    }

    public double getAverageSpeedBeforeSkip() {
        return averageSpeedBeforeSkip;
    }

    public double getAverageSpeedAfterSkip() {
        return averageSpeedAfterSkip;
    }



}