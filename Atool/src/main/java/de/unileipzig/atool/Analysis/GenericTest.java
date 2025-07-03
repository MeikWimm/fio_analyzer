package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericTest {
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    protected Job job;
    private final List<List<Run>> groups;
    protected List<List<Run>> resultGroups;
    private final List<Run> resultRuns;
    protected List<Run> possibleSteadyStateRuns;
    protected double alpha;
    protected boolean skipGroup;
    protected int thresholdSectionsForSteadyState;
    protected boolean applyBonferroni;
    protected int averageTimePerMillisec;
    protected int skipCounter;
    protected int groupSize;
    private PostHocTest postHocTest;
    private Scene scene;
    protected final Charter charter;

    public GenericTest(Job job, int skipFirstRun, boolean skipGroup, int groupSize, double alpha, boolean applyBonferroni, int thresholdSectionsForSteadyState) {
        this.job = new Job(job);
        this.job.prepareSkippedData(skipFirstRun);
        this.groups = Job.setupGroups(this.job, skipGroup, groupSize);
        this.groupSize = groupSize;
        this.resultGroups = new ArrayList<>();
        this.resultRuns = new ArrayList<>();
        this.possibleSteadyStateRuns = new ArrayList<>();
        this.charter = new Charter();
        this.skipGroup = skipGroup;
        this.alpha = alpha;
        this.thresholdSectionsForSteadyState = thresholdSectionsForSteadyState;
        this.applyBonferroni = applyBonferroni;
        this.skipCounter = skipFirstRun;
        if (applyBonferroni) {
            recalculateAlpha();
        }
        setupLogger();
    }
    private void setupLogger(){
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter(this.getClass().getName()));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    public int getSkippedRunCount(){
        return skipCounter;
    }

    public int getGroupSize() {
        return groupSize;
    }

    private void recalculateAlpha() {
        this.alpha = this.alpha / this.groups.size();
    }

    protected abstract void calculateTest(List<List<Run>> groups, List<Run> resultRuns);

    protected abstract double extractValue(Run run);

    protected abstract boolean isWithinThreshold(double value);

    public void setPostHocTest(PostHocTest postHocTest) {
        this.postHocTest = postHocTest;
    }

    public PostHocTest getPostHocTest() {
        return this.postHocTest;
    }


    protected void checkForHypothesis(){
        for (Run run : this.resultRuns) {
            if(isWithinThreshold(extractValue(run))){
                run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
            } else {
                run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            }
        }

        for(List<Run> group : this.groups){
            Run run = group.getFirst();
            if(run.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
                this.resultGroups.add(group);
            }
        }
    }

    protected void calculateSteadyState() {
        possibleSteadyStateRuns = new ArrayList<>(thresholdSectionsForSteadyState);
        boolean isSteadyStateFound;

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

        if((possibleSteadyStateRuns.size() < thresholdSectionsForSteadyState) && !possibleSteadyStateRuns.isEmpty()){
            LOGGER.log(Level.WARNING, String.format("Max possible steady state runs: %d", possibleSteadyStateRuns.size()));
            LOGGER.log(Level.WARNING, String.format("Threshold is set to: %d", thresholdSectionsForSteadyState));
            possibleSteadyStateRuns.clear();
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
        this.calculateTest(this.groups,this.resultRuns);
        this.checkForHypothesis();
        this.calculateSteadyState();
        this.calculatePostHoc();
    }

    public List<Run> getPossibleSteadyStateRuns() {
        return possibleSteadyStateRuns;
    }

    public void calculatePostHoc() {
        if (postHocTest == null) {
            return;
        }

        if (this.resultGroups.size() < 2) {
            LOGGER.log(Level.WARNING, String.format("%s group size of test result is 1", this.getClass().getName()));
            return;
        }

        postHocTest.setupGroups(this.resultGroups);
        postHocTest.setJob(this.job);
        postHocTest.calculate();
        postHocTest.checkSteadyStateRun();
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

//    public List<Run> getResultRuns() {
//        return resultRuns;
//    }

    public ObservableList<Run> getResultRuns() {
        return FXCollections.observableArrayList(resultRuns);
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

    public abstract Scene getCharterScene();

    // Optional method for setting labels, can be overridden
    protected void setLabeling() {
        // Default implementation can be empty or include common labeling logic
    }

    protected abstract URL getFXMLPath();

    protected abstract String getWindowTitle();

    public final void openWindow() {
        scene = getScene();
        Stage stage = new Stage();
        stage.setMaxWidth(1200);
        stage.setMaxHeight(600);
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.setTitle("Calculated ANOVA");
        stage.setScene(scene);
        setLabeling();
        stage.show();
    }

    public Scene getScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getFXMLPath());
        fxmlLoader.setController(this);
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene = new Scene(root1);
        return scene;
    }

    public abstract String getTestName();

    public abstract TableView<Run> getTable();


}
