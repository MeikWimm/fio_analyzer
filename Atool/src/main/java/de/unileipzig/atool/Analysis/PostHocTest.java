package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public abstract class PostHocTest {
    protected GenericTest test;
    protected Run steadyStateRun;
    protected Run anovaSteadyStateRun;
    private boolean isFirst = true;

    public PostHocTest(GenericTest test){
        super();
        this.test = test;
    }

    protected void checkSteadyStateRun(Run savedHypothesisRun, List<Run> group1, List<Run> group2){
        List<Run> possibleRuns = this.test.getPossibleSteadyStateRuns();
        if(possibleRuns == null || possibleRuns.isEmpty()){
            return;
        }

        anovaSteadyStateRun = possibleRuns.getFirst();

        for (Run possibleRun : possibleRuns) {
            boolean found = isFound(group1, group2, possibleRun);

            if(found){
                if(savedHypothesisRun.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
                    if(isFirst){
                        isFirst = false;
                        steadyStateRun = possibleRun;
                    }
                } else {
                    System.out.println("[PostHoc] Found at: " + possibleRun.getID() + " but rejected");
                }
            }
        }
    }

    public Run getSteadyStateRun() {
        return steadyStateRun;
    }

    public abstract String getTestName();

    public abstract void apply(List<Run> resultWithRuns, List<List<Run>> resultWithGroups);

    private static boolean isFound(List<Run> group1, List<Run> group2, Run possibleRun) {
        int ID = possibleRun.getID();
        boolean found = false;
        Run firstOfGroup1 = group1.getFirst();
        Run lastOfGroup1 = group1.getLast();
        Run firstOfGroup2 = group2.getFirst();
        Run lastOfGroup2 = group2.getLast();

        // Found if the last run of group1 is the same as the first run of group2
        // i.e., 4-5 | 5-6 -> 4 possible steady state
        if(lastOfGroup1.getID() == ID + 1 && firstOfGroup2.getID() == ID + 1){
            found = true;
        }
        // Found if the last run of group1 is the same as the second run of group2
        // i.e., 4-5 | 6-7 -> 4 possible steady state
        if(lastOfGroup1.getID() == ID + 1 && firstOfGroup2.getID() - 1 == ID + 1){
            found = true;
        }

        // Found if the first run of group1 is the same as the second run of group2
        // i.e., 10-14 | 15-19 -> 10 possible steady state if both groups are accepted
        if(firstOfGroup1.getID() == ID && lastOfGroup1.getID() == firstOfGroup2.getID() - 1){
            found = true;
        }

        return found;
    }
}
