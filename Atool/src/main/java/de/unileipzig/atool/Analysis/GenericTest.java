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
    private final String className = this.getClass().getSimpleName();
    private Job job;
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
    private final List<Run> runs;

    public GenericTest(Job job, int skipFirstRun, boolean skipGroup, int groupSize, double alpha, boolean applyBonferroni, int thresholdSectionsForSteadyState) {
        this.job = new Job(job);
        this.runs = this.job.getRuns();
        this.job.prepareSkippedData(skipFirstRun);
        for ( Run run : this.job.getRuns() ) {
            List<List<Section>> groups = Run.setupGroups(run, skipGroup, groupSize);
            run.setGroups(groups);
        }
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
        this.alpha = this.alpha / this.runs.getFirst().getSections().size();
    }

    protected abstract void calculateTest(Run run, List<Section> resultSections);
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


    protected void checkForHypothesis(Run run, List<Section> resultSections){
        for (Section section : resultSections) {
            section.setNullhypothesis(isWithinThreshold(extractValue(section)));

            if(section.getNullhypothesis()){
                run.setNullhypothesis(true);
            }
        }
    }

    protected abstract double extractValue(Section section);

    protected void calculateSteadyState() {

    }

    public Run getSteadyStateRun(){
//        if(this.possibleSteadyStateRuns.isEmpty()){
//            return null;
//        } else {
//            return this.possibleSteadyStateRuns.getFirst();
//        }
        return null;
    }

    public void calculate(){
        for(Run run: this.runs){
            List<Section> resultSections = new ArrayList<>();
            if(isDataApplicable(run)){
                this.calculateTest(run, resultSections);
                this.checkForHypothesis(run, resultSections);
                if(run.getNullhypothesis()){
                    this.resultRuns.add(run);
                }
                //this.calculatePostHoc();
            } else {
                Logging.log(Level.WARNING, className,"Loaded data can't be calculated!");
            }
        }

        for(Run run: this.runs){
            calculateSteadyState();
        }

        Logging.log(Level.INFO, className, "Done calculating.");
    }

    private boolean isDataApplicable(Run run) {
        //check for FDistribution
        List<List<Section>> sectionGroups = run.getGroups();
        int num = sectionGroups.size() - 1;
        int denom = (num + 1) * (run.getData().size() - 1);
        //FDistribution fDistribution = new FDistribution(num, denom);

        if(num < 0 || denom < 0){
            Logging.log(Level.WARNING, className,String.format("FDistribution -> nominator: %d | denominator: %d", num, denom));
            return false;
        }

        if(sectionGroups.size() <= 1){
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
