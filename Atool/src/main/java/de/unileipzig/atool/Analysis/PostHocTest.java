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
        for (List<Run> group : resultGroups) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    Run r1 = group.get(i);
                    Run r2 = group.get(j);
                    Run copyRun1 = new Run(r1);
                    Run copyRun2 = new Run(r2);


                    List<Run> pair = new ArrayList<>();
                    pair.add(copyRun1);
                    pair.add(copyRun2);
                    copyRun1.setGroup("Run: " + copyRun1.getID() + " - " + "Run "+ copyRun2.getID() );
                    copyRun1.setGroupID(ID);
                    postHocGroups.add(pair);
                    ID++;
                }
            }
        }
    }

    private void checkSteadyStateRun(){
        boolean isSteadyState = true;
        for(List<Run> group: postHocGroups){
            Run run = group.getFirst();
            if(!run.getNullhypothesis()){
                isSteadyState = false;
                break;
            }
        }
        if(isSteadyState){
            steadyStateRun = postHocGroups.getFirst().getFirst();
        }
    }

    public Run getSteadyStateRun() {
        return steadyStateRun;
    }

    public abstract String getTestName();

    protected abstract void calculateTest(List<List<Run>> postHocGroup, List<Run> resultRuns);

    public void calculate(){
        if(this.firstGroup.size() != this.secondGroup.size()){
            Logger.getLogger(PostHocTest.class.getName()).log(Level.WARNING, "Number of groups do not match");
            return;
        }

        calculateTest(this.postHocGroups, this.resultRuns);
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
