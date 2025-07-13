package de.unileipzig.atool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SectionWindow implements Initializable {
    @FXML private TableView<Section> sectionsTable;
    @FXML private TableColumn<Section, Integer> sectionsIDColumn;
    @FXML private TableColumn<Section, Integer> groupColumn;
    @FXML private TableColumn<Section, Double> FColumn;
    @FXML private TableColumn<Section, Double> CVColumn;
    @FXML private TableColumn<Section, Double> TColumn;
    @FXML private TableColumn<Section, Double> ZColumn;
    @FXML private TableColumn<Section, Double> QColumn;
    @FXML private TableColumn<Section, Double> averageSpeedColumn;
    @FXML private TableColumn<Section, Double> intervalFromColumn;
    @FXML private TableColumn<Section, Double> intervalToColumn;
    @FXML private TableColumn<Section, Double> plusMinusValueColumn;
    @FXML private TableColumn<Section, Double> standardDeviationColumn;
    @FXML private TableColumn<Section, Boolean> hypothesisColumn;

    private boolean showIntervalFromColumn = false;
    private boolean showIntervalToColumn = false;
    private boolean showPlusMinusValueColumn = false;
    private boolean showGroup = true;
    private boolean showFColumn = false;
    private boolean showCVColumn = false;
    private boolean showTColumn = false;
    private boolean showZColumn = false;
    private boolean showQColumn = false;
    private boolean showStandardDeviationColumn = false;
    private final ObservableList<Section> sections;
    private final Run run;

    public SectionWindow(Run run) {
        this.run = run;
        this.sections = FXCollections.observableList(this.run.getResultSections());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sectionsIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        groupColumn.setVisible(showGroup);

        averageSpeedColumn.setText("Average Speed" + Settings.getConversion());
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        intervalFromColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalFrom"));
        intervalFromColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        intervalFromColumn.setVisible(showIntervalFromColumn);

        intervalToColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalTo"));
        intervalToColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        intervalToColumn.setVisible(showIntervalToColumn);

        plusMinusValueColumn.setCellValueFactory(new PropertyValueFactory<>("PlusMinusValue"));
        plusMinusValueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        plusMinusValueColumn.setVisible(showPlusMinusValueColumn);

        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
        standardDeviationColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        standardDeviationColumn.setVisible(showStandardDeviationColumn);

        FColumn.setCellValueFactory(new PropertyValueFactory<>("F"));
        FColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        FColumn.setVisible(showFColumn);

        CVColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));
        CVColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        CVColumn.setVisible(showCVColumn);

        TColumn.setCellValueFactory(new PropertyValueFactory<>("T"));
        TColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        TColumn.setVisible(showTColumn);

        ZColumn.setCellValueFactory(new PropertyValueFactory<>("Z"));
        ZColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        ZColumn.setVisible(showZColumn);

        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        QColumn.setVisible(showQColumn);

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getSectionHypothesisCellFactory());


        sectionsTable.setItems(this.sections);
    }

    public void setShowFColumn(boolean showFColumn) {
        this.showFColumn = showFColumn;
    }

    public void setShowCVColumn(boolean showCVColumn) {
        this.showCVColumn = showCVColumn;
    }

    public void setShowTColumn(boolean showTColumn) {
        this.showTColumn = showTColumn;
    }

    public void setShowZColumn(boolean showZColumn) {
        this.showZColumn = showZColumn;
    }

    public void setShowPlusMinusValueColumn(boolean showPlusMinusValueColumn) {
        this.showPlusMinusValueColumn = showPlusMinusValueColumn;
    }

    public void setShowQColumn(boolean b) {
        this.showQColumn = b;
    }

    public void setShowIntervalFromColumn(boolean showIntervalFromColumn) {
        this.showIntervalFromColumn = showIntervalFromColumn;
    }

    public void setShowIntervalToColumn(boolean showIntervalToColumn) {
        this.showIntervalToColumn = showIntervalToColumn;
    }

    public void setShowGroup(boolean b) {
        this.showGroup = b;
    }

    public void openWindow() {
        Scene scene = getScene();
        Stage stage = new Stage();
        stage.setMaxWidth(1200);
        stage.setMaxHeight(700);
        stage.setMinHeight(700);
        stage.setMinWidth(800);
        stage.setTitle("Calculated sections");
        stage.setScene(scene);
        stage.show();

    }

    public Scene getScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/Sections.fxml"));
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Scene(root);
    }



}
