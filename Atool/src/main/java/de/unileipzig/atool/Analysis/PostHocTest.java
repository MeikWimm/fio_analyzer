package de.unileipzig.atool.Analysis;

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
    protected Section steadyStateSection;
    private boolean isFirst = true;
    private Scene scene;
    protected final Charter charter;
    protected final List<List<Section>> firstGroup;
    protected final List<List<Section>> secondGroup;
    private final List<List<Section>> postHocGroups;
    private final List<Section> resultSections;

    public PostHocTest(){
        super();
        this.charter = new Charter();
        this.firstGroup = new ArrayList<>();
        this.secondGroup = new ArrayList<>();
        this.postHocGroups = new ArrayList<>();
        this.resultSections = new ArrayList<>();
    }

    public void setupGroups(List<List<Section>> resultGroups) {
        int ID = 1;
        for (List<Section> group : resultGroups) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    Section r1 = group.get(i);
                    Section r2 = group.get(j);
                    Section copySection1 = new Section(r1);
                    Section copySection2 = new Section(r2);


                    List<Section> pair = new ArrayList<>();
                    pair.add(copySection1);
                    pair.add(copySection2);
                    copySection1.setGroup("Section: " + copySection1.getID() + " - " + "Section "+ copySection2.getID() );
                    copySection1.setGroupID(ID);
                    postHocGroups.add(pair);
                    ID++;
                }
            }
        }
    }

    private void checkSteadyStateRun(){
        boolean isSteadyState = true;
        for(List<Section> group: postHocGroups){
            Section section = group.getFirst();
            if(!section.getNullhypothesis()){
                isSteadyState = false;
                break;
            }
        }
        if(isSteadyState){
            steadyStateSection = postHocGroups.getFirst().getFirst();
        }
    }

    public Section getSteadyStateRun() {
        return steadyStateSection;
    }

    public abstract String getTestName();

    protected abstract void calculateTest(List<List<Section>> postHocGroup, List<Section> resultSections);

    public void calculate(){
        if(this.firstGroup.size() != this.secondGroup.size()){
            Logger.getLogger(PostHocTest.class.getName()).log(Level.WARNING, "Number of groups do not match");
            return;
        }

        calculateTest(this.postHocGroups, this.resultSections);
        checkForHypothesis();
        checkSteadyStateRun();
    }

    protected abstract double extractValue(Section section);

    protected abstract boolean isWithinThreshold(double value, Section section);

    private void checkForHypothesis() {
        for(Section section : resultSections){
            section.setNullhypothesis(isWithinThreshold(extractValue(section), section));
        }
    }

    public ObservableList<Section> getPostHocRuns() {
        return FXCollections.observableList(resultSections);
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

    public abstract TableView<Section> getTable();

    public abstract double getCriticalValue();

    public GenericTest getTest() {
        return test;
    }

    public void setGenericTest(GenericTest test) {
        this.test = test;
    }
}
