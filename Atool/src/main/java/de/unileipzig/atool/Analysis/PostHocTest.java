package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;
import javafx.beans.Observable;
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
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PostHocTest {
    protected GenericTest test;
    protected Run steadyStateRun;
    protected Run anovaSteadyStateRun;
    private boolean isFirst = true;
    private Scene scene;
    protected final Charter charter;
    protected final List<List<Run>> firstGroup;
    protected final List<List<Run>> secondGroup;
    protected final List<List<Run>> postHocGroups;
    protected final List<Run> postHocRuns;
    protected Job job;

    public PostHocTest(GenericTest test){
        super();
        this.test = test;
        this.charter = new Charter();
        this.firstGroup = new ArrayList<>();
        this.secondGroup = new ArrayList<>();
        this.postHocGroups = new ArrayList<>();
        this.postHocRuns = new ArrayList<>();
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
                Run run = postHocGroups.getLast().getFirst();
                run.setGroup(group1.getFirst().getGroup() + " | " + group2.getFirst().getGroup());
                run.setGroupID(ID);
                postHocRuns.add(run);

                postHocGroups.add(copyGroup2);


                ID++;
            }
        }
    }

    protected void checkSteadyStateRun(){

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
                if(savedHypothesisRun.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
                    if(isFirst){
                        isFirst = false;
                        steadyStateRun = possibleRun;
                    }
                } else {
                    //System.out.println("[PostHoc] Found at: " + possibleRun.getID() + " but rejected");
                }
            }
        }
    }

    public Run getSteadyStateRun() {
        return steadyStateRun;
    }

    public abstract String getTestName();

    public abstract void calculate();

    public ObservableList<Run> getPostHocRuns() {
        return FXCollections.observableList(postHocRuns);
    }

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
        stage.setTitle(getWindowTitle());
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

    public abstract Scene getCharterScene();

    public abstract TableView<Run> getTable();

    public abstract double getCriticalValue();

    public void setJob(Job job) {
        this.job = new Job(job);
        //this.job.resetRuns();
    }


}
