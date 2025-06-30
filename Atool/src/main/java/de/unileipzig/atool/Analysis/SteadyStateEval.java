package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Settings;
import de.unileipzig.atool.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SteadyStateEval implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(SteadyStateEval.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("SteadyStateEval"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }
    @FXML
    private Label labelHeader;

    @FXML private TableView<TestEval> evalTable; // Replace '?' with the type of objects in the table.
    @FXML private TableColumn<TestEval, String> testColumn; // Replace the first '?' with the type of the table's data, and the second '?' with the type of the column's value.
    @FXML private TableColumn<TestEval, String> runColumn;
    @FXML private TableColumn<TestEval, String> timeColumn;
    @FXML private TableColumn<TestEval, Integer> averageTimePerMilliColumn;
    @FXML private TableColumn<TestEval, String> typeOfComparedRunsColumn;
    @FXML private TableColumn<TestEval, Integer> skippedRunColumn;
    @FXML private TableColumn<TestEval, Boolean> bonferroniColumn;
    @FXML private TableColumn<TestEval, Integer> comparedRunsColumn;

    private final Job job;
    private final List<TestEval> testEvals;
    private final Settings settings;
    private final GenericTest[] test;


    public SteadyStateEval(Job job, Settings settings){
        this.job = job;
        this.settings = settings;
        test = new  GenericTest[5];

        Anova anova = new Anova(job, settings);
        TukeyHSD tukey = new TukeyHSD(anova);
        anova.setPostHocTest(tukey);
        test[0] = anova;

        test[1] = new ConInt(job, settings);
        test[2] = new CoV(job, settings);
        //test[3] = new CoVWindowed(job, settings);
        test[3] = new MannWhitney(job, settings);
        test[4] = new TTest(job, settings);
        testEvals = new ArrayList<>();
        prepareTests();
    }

    private void prepareTests() {
        for (GenericTest genericTest : test) {
            TestEval testEval;
            PostHocTest postHocTest = genericTest.getPostHocTest();
            testEval = new TestEval(this.job, genericTest);
            testEvals.add(testEval);

            if(postHocTest != null){
                TestEval postHocTestEval = new TestEval(this.job, genericTest, postHocTest);
                testEvals.add(postHocTestEval);
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        runColumn.setCellValueFactory(new PropertyValueFactory<>("SteadyStateRun"));
        testColumn.setCellValueFactory(new PropertyValueFactory<>("TestName"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("Time"));
        averageTimePerMilliColumn.setCellValueFactory(new PropertyValueFactory<>("AverageTimePerMilliVal"));
        typeOfComparedRunsColumn.setCellValueFactory(new PropertyValueFactory<>("TypeOfComparedRuns"));
        skippedRunColumn.setCellValueFactory(new PropertyValueFactory<>("SkippedRunVal"));
        bonferroniColumn.setCellValueFactory(new PropertyValueFactory<>("BonferroniVal"));
        comparedRunsColumn.setCellValueFactory(new PropertyValueFactory<>("ComparedRunsVal"));

        labelHeader.setText(this.job.toString());
        evalTable.setItems(FXCollections.observableArrayList(testEvals));
    }

    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/SteadyStateEval.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setMaxWidth(1200);
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Job Evaluation");
            stage.setScene(new Scene(root1));
            setLabeling();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLabeling() {
        labelHeader.setText("Job Evaluation | Job alpha: " + this.job.getAlpha() + " | Required accepted runs for steady state: " + settings.getRequiredRunsForSteadyState());
    }

}
