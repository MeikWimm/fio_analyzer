package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class SteadyStateEval implements Initializable {
    @FXML private Label labelHeader;
    @FXML private TableView<TestEval> evalTable;
    @FXML private TableColumn<TestEval, String> testColumn;
    @FXML private TableColumn<TestEval, String> timeColumn;
    @FXML private TableColumn<TestEval, Integer> skippedRunColumn;
    @FXML private TableColumn<TestEval, Integer> aveSpeedBeforeSkip;
    @FXML private TableColumn<TestEval, Integer> aveSpeedAfterSkip;
    @FXML Button saveEvalButton;
    private final Job job;
    private File path;
    private final List<TestEval> testEvals;
    private final Settings settings;
    private final GenericTest[] tests;
    private final OutputModule outputModule;
    private Window owner;

    public SteadyStateEval(Job job, Settings settings){
        this.job = job;
        this.settings = settings;
        tests = new  GenericTest[4];
        outputModule = new OutputModule(path);

        Anova anova = new Anova(job, settings);
        TukeyHSD tukey = new TukeyHSD();
        anova.setPostHocTest(tukey);
        tests[0] = anova;
        tests[1] = new ConInt(job, settings);
        tests[2] = new MannWhitney(job, settings);
        tests[3] = new AtoolTTest(job, settings);
        testEvals = new ArrayList<>();
        prepareTests();
    }

    public void setPath(File path){
        this.path = path;
    }

    private void prepareTests() {
        for (GenericTest genericTest : tests) {
            TestEval testEval = new TestEval(genericTest);
            TestEval postHocTestEval = testEval.getPostHocTest();

            if (postHocTestEval != null) {
                testEvals.add(postHocTestEval);
            }

            testEvals.add(testEval);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        testColumn.setCellValueFactory(new PropertyValueFactory<>("TestName"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("Time"));
        skippedRunColumn.setCellValueFactory(new PropertyValueFactory<>("SkippedRunVal"));
        aveSpeedBeforeSkip.setCellValueFactory(new PropertyValueFactory<>("AverageSpeedBeforeSkip"));
        aveSpeedAfterSkip.setCellValueFactory(new PropertyValueFactory<>("AverageSpeedAfterSkip"));

        saveEvalButton.setOnAction(e -> onActionSaveEval());

        labelHeader.setText(this.job.toString());
        evalTable.setItems(FXCollections.observableArrayList(testEvals));
    }

    public void openWindow() {
        Scene scene = getScene();
        Stage stage = new Stage();
        stage.setMaxWidth(1200);
        stage.setMaxHeight(700);
        stage.setMinHeight(700);
        stage.setMinWidth(800);
        stage.setTitle("Job Evaluation");
        stage.setScene(scene);
        setLabeling();
        stage.show();

    }

    private void setLabeling() {
        labelHeader.setText("Job Evaluation | Job alpha: " + this.job.getAlpha() + " | Required accepted sections for steady state: " + settings.getRequiredRunsForSteadyState() + " seconds");
    }

    public void setOwner(Window owner){
        this.owner = owner;
    }

    private void onActionSaveEval(){
        outputModule.openDirectoryChooser(this.owner);
        saveEval();
    }

    public void saveEval(){
        outputModule.setPath(this.path);
        OutputModule.STATUS status = outputModule.saveEval(this);
        Logging.log(Level.INFO, "SteadyStateEval", status.toString());
    }

    public Scene getScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/SteadyStateEval.fxml"));
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Scene(root);
    }

    public GenericTest[] getTests() {
        return tests;
    }

    public Job getJob() {
        return job;
    }

    public TableView<TestEval> getEvalTable() {
        return evalTable;
    }
}
