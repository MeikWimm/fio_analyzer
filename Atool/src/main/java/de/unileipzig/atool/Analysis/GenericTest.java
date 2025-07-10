package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class GenericTest {
    public static final byte ACCEPTED = 1;
    public static final byte REJECTED = 0;
    public static final byte UNDEFINED = -1;

    private final String className = this.getClass().getSimpleName();
    protected Job job;
    private final List<List<Run>> groups;
    protected List<List<Run>> resultGroups;
    private final List<Run> resultRuns;
    protected List<Run> possibleSteadyStateRuns;
    protected double alpha;
    protected boolean skipGroup;
    protected int thresholdSectionsForSteadyState;
    protected boolean applyBonferroni;
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
        this.postHocTest.setGenericTest(this);
    }

    public PostHocTest getPostHocTest() {
        return this.postHocTest;
    }


    protected void checkForHypothesis(){
        for (Run run : this.resultRuns) {
            run.setNullhypothesis(isWithinThreshold(extractValue(run)));
        }

        for(List<Run> group : this.groups){
            Run run = group.getFirst();
            if(run.getNullhypothesis()){
                this.resultGroups.add(group);
            }
        }
    }

    protected void calculateSteadyState() {
        possibleSteadyStateRuns = new ArrayList<>(thresholdSectionsForSteadyState);
        boolean isSteadyStateFound;

        if((this.groups.size() < thresholdSectionsForSteadyState)){
            return;
        }

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
    }

    public Run getSteadyStateRun(){
        if(this.possibleSteadyStateRuns.isEmpty()){
            return null;
        } else {
            return this.possibleSteadyStateRuns.getFirst();
        }
    }

    public void calculate(){
        if(isDataApplicable()){
            Logging.log(Level.INFO, className, "Calculating Job " + this.job.getFileName());
            this.calculateTest(this.groups,this.resultRuns);
            this.checkForHypothesis();
            this.calculateSteadyState();
            this.calculatePostHoc();
            Logging.log(Level.INFO, className, "Done calculating.");
        } else {
            Logging.log(Level.WARNING, className,"Loaded data can't be calculated!");
        }

    }

    private boolean isDataApplicable() {
        //check for FDistribution
        int num = groups.size() - 1;
        int denom = (num + 1) * (this.job.getData().size() - 1);
        //FDistribution fDistribution = new FDistribution(num, denom);

        if(num < 0 || denom < 0){
            Logging.log(Level.WARNING, className,String.format("FDistribution -> nominator: %d | denominator: %d", num, denom));
            return false;
        }

        if(groups.size() <= 1){
            Logging.log(Level.WARNING, className,"Group size is smaller than 1!");
            return false;
        }

        return true;
    }

    public List<Run> getPossibleSteadyStateRuns() {
        return possibleSteadyStateRuns;
    }

    public void calculatePostHoc() {
        if (postHocTest == null) {
            return;
        }

        if (this.resultGroups.size() < 2) {
            Logging.log(Level.WARNING, className, " group size of test result is smaller than 2!");
            return;
        }

        postHocTest.setupGroups(this.resultGroups);
        postHocTest.calculate();
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

    public abstract double getCriticalValue();

    public List<List<Run>> getGroups() {
        return groups;
    }

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
        stage.setMaxHeight(650);
        stage.setMinHeight(650);
        stage.setMinWidth(800);
        stage.setTitle(getWindowTitle());
        stage.setScene(scene);
        setLabeling();
        stage.show();
    }

    public Scene getScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getFXMLPath());
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene = new Scene(root);
        return scene;
    }

    public abstract String getTestName();

    public abstract TableView<Run> getTable();


}
