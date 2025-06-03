package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
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
        handler.setFormatter(new Utils.CustomFormatter("GenericTest"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    protected Job job;
    protected List<List<Run>> groups;
    protected List<List<Run>> resultGroups;
    protected List<Run> resultRuns;
    protected List<List<Run>> postHocGroups;
    protected List<Run> postHocRuns;
    //protected Run steadyStateRun;
    protected List<Run> possibleSteadyStateRuns;
    protected double alpha;
    protected boolean skipGroup;

    public GenericTest(Job job, int skipFirstRun, boolean skipGroup, int groupSize, double alpha, boolean applyBonferroni) {
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
        if (applyBonferroni) {
            recalculateAlpha();
        }
    }

    protected static double calculateCoV(List<Run> group) {
        if (group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group cannot be null or empty");
        }

        double average = average(group);
        if (average == 0) {
            throw new IllegalArgumentException("Cannot calculate CoV when mean is zero");
        }

        double n = 0;
        double sum = 0;

        for (Run run : group) {
            if (run == null || run.getData() == null) {
                throw new IllegalArgumentException("Invalid run data");
            }
            for (DataPoint dp : run.getData()) {
                sum += Math.pow(dp.getSpeed() - average, 2);
                n++;
            }
        }

        if (n <= 1) {
            throw new IllegalArgumentException("Need at least two data points to calculate CoV");
        }

        double std = Math.sqrt(sum / (n - 1));
        return (std / average);
    }

    public static double average(Run run) {
        double sum = 0;
        double n = run.getData().size();
        for (DataPoint dp : run.getData()) {
            sum += dp.getSpeed();
        }
        return sum / n;
    }

    public static double average(List<Run> group) {
        double sum = 0;
        double n = 0;
        for (Run run : group) {
            for (DataPoint dp : run.getData()) {
                sum += dp.getSpeed();
                n++;
            }
        }
        return sum / n;
    }

    // Calculate the median of a sorted list
    public static double median(Run run) {
        ArrayList<DataPoint> sorted = new ArrayList<DataPoint>(run.getData());
        sorted.sort(new Utils.SpeedComparator());
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2).getSpeed();
        } else {
            return (sorted.get(n / 2 - 1).getSpeed() + sorted.get(n / 2).getSpeed()) / 2.0;
        }
    }

    // Calculate the median of a sorted list
    public static double median(List<DataPoint> data) {
        ArrayList<DataPoint> sorted = new ArrayList<DataPoint>(data);
        sorted.sort(new Utils.SpeedComparator());
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2).getSpeed();
        } else {
            return (sorted.get(n / 2 - 1).getSpeed() + sorted.get(n / 2).getSpeed()) / 2.0;
        }
    }

    // Calculate MAD
    public static double mad(Run run, double median) {
        List<DataPoint> deviations = new ArrayList<>();
        for (DataPoint dp : run.getData()) {
            deviations.add(new DataPoint(Math.abs(dp.getSpeed() - median), dp.getTime()));
        }
        deviations.sort(new Utils.SpeedComparator());
        return median(deviations);
    }

    public static double variance(Run run) {
        double sum = 0;
        double average = average(run);
        double n = run.getData().size();
        for (DataPoint dp : run.getData()) {
            sum += Math.pow(dp.getSpeed() - average, 2);
        }
        return sum / (n - 1);
    }

    public static double variance(List<Double> list, double average) {
        double sum = 0;
        double n = list.size();
        for (Double d : list) {
            sum += Math.pow(d - average, 2);
        }
        return sum / (n - 1);
    }

    public static double standardError(Run run1, Run run2) {
        double std = 0;
        std = Math.sqrt(0.5 * (GenericTest.variance(run1) / run1.getData().size() + GenericTest.variance(run2) / run2.getData().size()));
        return std;
    }

    public static double calcualteCoVOverStd(double std, List<Run> group) {
        return std / average(group);
    }

    public void recalculateAlpha() {
        this.alpha = this.alpha / this.groups.size();
    }

    public void calculate() {
    }

    public void calculateSteadyState() {
        Run steadyStateRun;
        possibleSteadyStateRuns = new ArrayList<>();
        for (int i = 0; i < this.resultRuns.size() - 2; i++) {
            boolean found = false;
            if (i < this.resultRuns.size() - 2) {
                found = true;
                if (!skipGroup) {
                    for (int j = i; j < i + 2; j++) { // Compare 3 groups
                        steadyStateRun = this.resultRuns.get(j);
                        if (steadyStateRun.getNullhypothesis() == Run.REJECTED_NULLHYPOTHESIS) {
                            found = false;
                        }
                    }
                } else {
                    for (int j = i; j < i + 3; j++) { // Compare 2 groups
                        steadyStateRun = this.resultRuns.get(j);
                        if (steadyStateRun.getNullhypothesis() == Run.REJECTED_NULLHYPOTHESIS) {
                            found = false;
                        }
                    }
                }
            }
            if (found) {
                possibleSteadyStateRuns.add(this.resultRuns.get(i + 1));
            }
        }

        if (possibleSteadyStateRuns != null) {
            for (Run run : possibleSteadyStateRuns) {
                System.out.println("[ANOVA] Found at: " + run.getID());
            }
        }
    }

    public List<Run> getPossibleSteadyStateRuns() {
        return possibleSteadyStateRuns;
    }

    public Run getSteadyStateRun() {
        if (possibleSteadyStateRuns.isEmpty()) {
            return null;
        } else {
            return possibleSteadyStateRuns.getFirst();
        }
    }

    public void calculatePostHoc(PostHocAnalyzer postHocAnalyzer) {
        if (postHocAnalyzer == null) {
            LOGGER.log(Level.WARNING, "PostHocAnalyzer is null");
            return;
        }

        if (this.resultGroups.size() < 2) {
            LOGGER.log(Level.WARNING, String.format("%s group size of test result is 1", this.getClass().getName()));
            return;
        }

        for (List<Run> runs : this.resultGroups) {
            for (Run run : runs) {
                if (run.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS) {
                    this.resultRuns.add(run);
                }
            }
        }

//        for (int i = 0; i < resultGroups.size() - 1; i++) {
//            var currentGroup = resultGroups.get(i).getFirst();
//            var nextGroup = resultGroups.get(i + 1).getFirst();
//            if (currentGroup != null && nextGroup != null) {
//                String currentGroupValue = currentGroup.getGroup();
//                String nextGroupValue = nextGroup.getGroup();
//                currentGroup.setGroup(currentGroupValue + " | " + nextGroupValue);
//            }
//        }
//        resultGroups.getLast().getFirst().setGroup("");
        job.resetRuns();
        setupGroups();
        postHocAnalyzer.apply(this.postHocRuns, this.postHocGroups);
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

    protected void draw() {

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


}
