package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;
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
import java.util.logging.Logger;

public abstract class PostHocTest {
    private GenericTest test;
    protected Run steadyStateRun;
    protected Run anovaSteadyStateRun;
    private boolean isFirst = true;
    private Scene scene;
    protected final Charter charter;
    protected final List<List<Run>> firstGroup;
    protected final List<List<Run>> secondGroup;
    private final List<List<Run>> postHocGroups;
    private final List<Run> resultRuns;

    public PostHocTest(){
        super();
        this.charter = new Charter();
        this.firstGroup = new ArrayList<>();
        this.secondGroup = new ArrayList<>();
        this.postHocGroups = new ArrayList<>();
        this.resultRuns = new ArrayList<>();
    }

    public void setupGroups(List<List<Run>> resultGroups) {
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
                Run run = copyGroup1.getFirst();
                run.setGroup(group1.getFirst().getGroup() + " | " + group2.getFirst().getGroup());
                run.setGroupID(ID);

                postHocGroups.add(copyGroup2);

                this.firstGroup.add(copyGroup1);
                this.secondGroup.add(copyGroup2);
                ID++;
            }
        }
    }

    private void checkSteadyStateRun(){

        if(this.firstGroup.size() != this.secondGroup.size()){
            Logger.getLogger(PostHocTest.class.getName()).log(Level.WARNING, "Number of groups do not match");
            return;
        }

        for(int i = 0; i < this.firstGroup.size(); i++){
            checkSteadyStateRun(this.firstGroup.get(i), this.secondGroup.get(i));
        }
    }

    private void checkSteadyStateRun(List<Run> group1, List<Run> group2){
        Run savedHypothesisRun = group1.getFirst();
        List<Run> possibleRuns = this.test.getPossibleSteadyStateRuns();
        if(possibleRuns == null || possibleRuns.isEmpty()){
            return;
        }

        anovaSteadyStateRun = possibleRuns.getFirst();

        for (Run possibleRun : possibleRuns) {
            boolean found = isFound(group1, group2, possibleRun);

            if(found){
                if(savedHypothesisRun.getNullhypothesis()){
                    if(isFirst){
                        isFirst = false;
                        steadyStateRun = possibleRun;
                    }
                }
            }
        }
    }

    public Run getSteadyStateRun() {
        return steadyStateRun;
    }

    public abstract String getTestName();

    protected abstract void calculateTest(List<Run> firstGroup, List<Run> secondGroup, List<Run> postHocRuns);

    protected abstract void  initPostHocTest(GenericTest test, List<List<Run>> postHocGroups);

    public void calculate(){
        initPostHocTest(this.test, this.postHocGroups);

        if(this.firstGroup.size() != this.secondGroup.size()){
            Logger.getLogger(PostHocTest.class.getName()).log(Level.WARNING, "Number of groups do not match");
            return;
        }

        for(int i = 0; i < this.firstGroup.size(); i++){
            calculateTest(this.firstGroup.get(i), this.secondGroup.get(i), this.resultRuns);
        }

        checkForHypothesis();
        checkSteadyStateRun();
    }

    protected abstract double extractValue(Run run);

    protected abstract boolean isWithinThreshold(double value);

    private void checkForHypothesis() {
        for(Run run: resultRuns){
            run.setNullhypothesis(isWithinThreshold(extractValue(run)));
        }
    }

    public ObservableList<Run> getPostHocRuns() {
        return FXCollections.observableList(resultRuns);
    }


    private static boolean isFound(List<Run> group1, List<Run> group2, Run possibleRun) {
        int ID = possibleRun.getID();
        boolean found = false;
        Run firstOfGroup1 = group1.getFirst();
        Run lastOfGroup1 = group1.getLast();
        Run firstOfGroup2 = group2.getFirst();

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

    public abstract Scene getCharterScene();

    public abstract TableView<Run> getTable();

    public abstract double getCriticalValue();

    public GenericTest getTest() {
        return test;
    }

    public void setGenericTest(GenericTest test) {
        this.test = test;
    }
}
