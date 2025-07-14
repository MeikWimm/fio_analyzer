package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;

public class TestEval{
    private final GenericTest test;
    private String testName;
    private String steadyStateRunString;
    private String time;
    private String typeOfComparedRuns;
    private String skippedRunVal;
    private String bonferroniVal;
    private String comparedRunsVal;
    private TestEval postHocEval;

    public TestEval(GenericTest test){
        this.test = test;
        this.test.calculate();
        prepareItem();
    }

    public TestEval getPostHocTest(){
        return postHocEval;
    }

    private void prepareItem() {
        //Run steadyStateRun = test.getSteadyStateRun();
        Run steadyStateRun = null;
                if(true) return;
        testName = test.getTestName();

        if(steadyStateRun != null){
            steadyStateRunString = String.format("Run %s", steadyStateRun.getID());
//            time = String.format("%f - %f", steadyStateRun.getStartTime() / 1000.0, steadyStateRun.getEndTime() / 1000.0);
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
        skippedRunVal = Integer.toString(test.getSkippedRunCount());
        comparedRunsVal = Integer.toString(test.getGroupSize());
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

    public String getTypeOfComparedRuns() {
        return typeOfComparedRuns;
    }

    public String getSkippedRunVal() {
        return skippedRunVal;
    }
}