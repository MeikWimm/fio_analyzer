package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
        handler.setFormatter(new Utils.CustomFormatter(GenericTest.class.getName()));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    protected Job job;
    protected List<List<Run>> groups;
    protected List<List<Run>> resultGroups;
    protected List<Run> resultRuns;
    protected List<List<Run>> postHocGroups;
    protected List<Run> postHocRuns;
    protected List<Run> possibleSteadyStateRuns;
    protected double alpha;
    protected boolean skipGroup;
    protected int thresholdSectionsForSteadyState;
    protected boolean applyBonferroni;
    protected int averageTimePerMillisec;
    protected boolean sequentialCompareType;
    protected int skipCounter;
    private PostHocTest postHocTest;

    public GenericTest(Job job, int skipFirstRun, boolean skipGroup, int groupSize, double alpha, boolean applyBonferroni, int thresholdSectionsForSteadyState) {
        this.job = new Job(job);
        this.job.prepareSkippedData(skipFirstRun);
        this.groups = Job.setupGroups(this.job, skipGroup, groupSize);
        this.resultGroups = new ArrayList<>();
        this.resultRuns = new ArrayList<>();
        this.postHocRuns = new ArrayList<>();
        this.postHocGroups = new ArrayList<>();
        this.possibleSteadyStateRuns = new ArrayList<>();
        this.skipGroup = skipGroup;
        this.alpha = alpha;
        this.thresholdSectionsForSteadyState = thresholdSectionsForSteadyState;
        this.applyBonferroni = applyBonferroni;
        this.averageTimePerMillisec = 1; //TODO
        this.sequentialCompareType = false; // TODO
        this.skipCounter = 0; // TODO
        if (applyBonferroni) {
            recalculateAlpha();
        }
    }

    private void recalculateAlpha() {
        this.alpha = this.alpha / this.groups.size();
    }

    protected abstract void calculateTest();

    protected abstract double extractValue(Run run);

    public void setPostHocTest(PostHocTest postHocTest) {
        this.postHocTest = postHocTest;
    }

    public PostHocTest getPostHocTest() {
        return this.postHocTest;
    }
    
    protected abstract boolean isWithinThreshold(double value);

    public void calculateSteadyState() {
        possibleSteadyStateRuns = new ArrayList<>(thresholdSectionsForSteadyState);
        boolean isSteadyStateFound = false;

        for (int j = 0; j < this.resultRuns.size(); j++) {
            isSteadyStateFound = true;
            int counter = 0;
            for (int i = j; i < j + thresholdSectionsForSteadyState; i++) {
                if(i < this.resultRuns.size()){
                    Run run = this.resultRuns.get(i);
                    double VALUE = extractValue(run);
                    if (isWithinThreshold(VALUE)) {
                        possibleSteadyStateRuns.add(run);
                    } else {
                        isSteadyStateFound = false;
                        j = j + counter;
                        possibleSteadyStateRuns.clear();
                        break;
                    }
                    counter++;
                }
            }

            if(isSteadyStateFound){
                break;
            }
        }

        if(isSteadyStateFound && possibleSteadyStateRuns.size() >= thresholdSectionsForSteadyState){
            LOGGER.log(Level.INFO, String.format("Found steady state at Time %f and Section ID: %d", possibleSteadyStateRuns.getFirst().getStartTime(), possibleSteadyStateRuns.getFirst().getID()));
        } else {
            possibleSteadyStateRuns.clear();
            LOGGER.log(Level.INFO, "No steady state found");
        }
    }

    public Run getSteadyStateRun(){
        if(this.possibleSteadyStateRuns.isEmpty()){
            return null;
        } else {
            return this.possibleSteadyStateRuns.getFirst();
        }
    }

    public void calculate(){
        this.calculateTest();
        this.calculateSteadyState();
        this.calculatePostHoc();
    }

    public List<Run> getPossibleSteadyStateRuns() {
        return possibleSteadyStateRuns;
    }

    public void calculatePostHoc() {
        if (postHocTest == null) {
            LOGGER.log(Level.WARNING, "PostHocAnalyzer is null");
            return;
        }

        if (this.resultGroups.size() < 2) {
            LOGGER.log(Level.WARNING, String.format("%s group size of test result is 1", this.getClass().getName()));
            return;
        }
        //TODO andere Tests brauchen doch kein this.result.add
        for (List<Run> runs : this.resultGroups) {
            for (Run run : runs) {
                if (run.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS) {
                    this.resultRuns.add(run);
                }
            }
        }
        job.resetRuns();
        setupGroups();
        postHocTest.apply(this.postHocRuns, this.postHocGroups);
    }

    private void setupGroups() {
        int ID = 1;
        for (int i = 0; i < resultGroups.size() - 1; i++) {
            for (int j = i + 1; j < resultGroups.size(); j++) {
                List<Run> group1 = resultGroups.get(i);
                List<Run> group2 = resultGroups.get(j);
                List<Run> copyGroup1 = new ArrayList<>();
                List<Run> copyGroup2 = new ArrayList<>();

                for (Run run : group1) {
                    copyGroup1.add(new Run(run));
                }

                for (Run run : group2) {
                    copyGroup2.add(new Run(run));
                }

                postHocGroups.add(copyGroup1);
                Run run = postHocGroups.getLast().getFirst();
                run.setGroup(group1.getFirst().getGroup() + " | " + group2.getFirst().getGroup());
                run.setGroupID(ID);
                postHocRuns.add(run);

                postHocGroups.add(copyGroup2);


                ID++;
            }
        }
    }

    public ObservableList<Run> getPostHocRuns() {
        // postHocRuns.removeIf(run -> run.getNullhypothesis() == Run.REJECTED_NULLHYPOTHESIS);
        //postHocRuns.removeIf(run -> run.getNullhypothesis() == Run.REJECTED_NULLHYPOTHESIS);
        return FXCollections.observableArrayList(postHocRuns);
    }

    public Job getJob() {
        return this.job;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getCriticalValue() {
        return Run.UNDEFINED_DOUBLE_VALUE;
    }

    public List<List<Run>> getGroups() {
        return groups;
    }

    public List<Run> getResultRuns() {
        return resultRuns;
    }

    public List<List<Run>> getResultGroups() {
        return resultGroups;
    }

    public boolean isApplyBonferroni() {
        return applyBonferroni;
    }

    public boolean isSkipGroup() {
        return skipGroup;
    }

    public int getAverageTimePerMillisec() {
        return averageTimePerMillisec;
    }

    public int getSkipCounter() {
        return skipCounter;
    }


    public abstract String getTestName();

}
