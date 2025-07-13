package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;
import de.unileipzig.atool.Section;
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
    protected Section steadyStateSection;
    private boolean isFirst = true;
    private Scene scene;
    protected final Charter charter;
    protected final List<List<Section>> firstGroup;
    protected final List<List<Section>> secondGroup;
    private final List<List<Section>> postHocGroups;
    private final List<Section> resultRuns;
    private Run run;

    public PostHocTest(){
        super();
        this.charter = new Charter();
        this.firstGroup = new ArrayList<>();
        this.secondGroup = new ArrayList<>();
        this.postHocGroups = new ArrayList<>();
        this.resultRuns = new ArrayList<>();
    }

    public void setupGroups(List<List<Section>> resultGroups) {
        int ID = 1;
        for (int i = 0; i < resultGroups.size() - 1; i++) {
            for (int j = i + 1; j < resultGroups.size(); j++) {
                List<Section> group1 = resultGroups.get(i);
                List<Section> group2 = resultGroups.get(j);

                postHocGroups.add(group1);
                Section section = group1.getFirst();
                section.setGroup(group1.getFirst().getGroup() + " | " + group2.getFirst().getGroup());
                section.setGroupID(ID);

                postHocGroups.add(group2);

                this.firstGroup.add(group1);
                this.secondGroup.add(group2);
                ID++;
            }
        }
    }

    private void checkSteadyStateRun(){

    }

    public Run getSteadyStateRun() {
        return steadyStateRun;
    }

    public abstract String getTestName();

    protected abstract void calculateTest(List<Section> firstGroup, List<Section> secondGroup, List<Section> resultSections);

    protected abstract void  initPostHocTest(Run run, GenericTest test, List<List<Section>> postHocGroups);

    public void calculate(){
        for (Run run : this.test.getResultRuns()) {
            List<Section> resultSections = new ArrayList<>();
            initPostHocTest(run, this.test, this.postHocGroups);

            if(this.firstGroup.size() != this.secondGroup.size()){
                Logger.getLogger(PostHocTest.class.getName()).log(Level.WARNING, "Number of groups do not match");
                return;
            }

            for(int i = 0; i < this.firstGroup.size(); i++){
                calculateTest(this.firstGroup.get(i), this.secondGroup.get(i), resultSections);
            }

            checkForHypothesis();
            checkSteadyStateRun();
        }
    }

    protected abstract double extractValue(Section section);

    protected abstract boolean isWithinThreshold(double value);

    private void checkForHypothesis() {
        for(Section section: resultRuns){
            section.setNullhypothesis(isWithinThreshold(extractValue(section)));
        }
    }

    public ObservableList<Section> getPostHocRuns() {
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

    public void setRun(Run run) {
        this.run = run;
    }
}
