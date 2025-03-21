package com.mycompany.atool;


import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;



public class PrimaryController implements Initializable{
    InputModule inputModule;
    Tester tester;
    
    @FXML public MenuItem menuItem_open;
    @FXML public MenuItem menuItem_ANOVA;
    
    @FXML public TableView<Job> table;
    @FXML public TableColumn<Job,String> fileNameColumn;
    @FXML public TableColumn<Job,Integer> runsColumn;
    @FXML public TableColumn<Job,String> speedColumn;
    @FXML public TableColumn<Job,String> timeColumn;
    @FXML public TableColumn<Job,String> lastModifiedColumn;
    @FXML public TableColumn<Job,String> fileCreatedColumn;
    
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputModule = new InputModule();
        tester = new Tester();
        
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("File"));
        lastModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("FileLastModifiedDate"));
        fileCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("FileCreationDate"));
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("Runs"));
        
        runsColumn.setCellFactory(cel -> new EditingCell());
        prepareTable();

        
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("TimeInSec"));
    }
    
    private void prepareTable(){
        runsColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Integer> t) -> {
            System.out.println("Commiting last name change. Previous: " + t.getOldValue() + "   New: " + t.getNewValue());
            System.out.println(t.getRowValue().toString());
            System.err.println("--------->");
            t.getRowValue().setRuns(t.getNewValue());
            System.out.println(t.getRowValue().toString());
        });

        table.setOnKeyReleased((KeyEvent t)-> {
            KeyCode key=t.getCode();
            if (key==KeyCode.DELETE){
                int pos=table.getSelectionModel().getSelectedIndex();
                Job remove = table.getItems().remove(pos);
                System.out.println("Removed: " + remove.toString());
            }
            for (Job item : table.getItems()) {
                System.out.println("Jobs left: " + item.toString());
            }
        });
        
        table.setRowFactory((TableView<Job> tableView) -> {
            final TableRow<Job> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            MenuItem applyTestItem = new MenuItem("Draw Job");
            applyTestItem.setOnAction((ActionEvent event) -> {
                drawJob(row.getItem());
            });            MenuItem removeItem = new MenuItem("Delete");
            removeItem.setOnAction((ActionEvent event) -> {
                table.getItems().remove(row.getItem());
            });
            rowMenu.getItems().addAll(applyTestItem, removeItem);

            // only display context menu for non-null items:
            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu)null));
            return row;
        });
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void openLogfile() {
        inputModule.loadFile();
        table.setItems(inputModule.getJobs());
        for (Job job : inputModule.getJobs()) {
            System.out.println("File loaded:");
            System.out.println(job.toString());
        }
            System.out.println("----------------------------------");
    }
    
    @FXML
    private void executeANOVA(){
        tester.executeANOVA(table.getSelectionModel().getSelectedItem());
    }

    private void drawJob(Job job) {
        Stage stage = new Stage();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        XYChart.Series series = new XYChart.Series();
        lineChart.setTitle("Job");
        Map<Integer, Double> data = job.getData();
        for (Map.Entry<Integer, Double> entry : data.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
            series.getData().add(new XYChart.Data<>(key, value));
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
       
        stage.setScene(scene);
        stage.show();
    }
    
}

class EditingCell extends TableCell<Job, Integer> {
private TextField textField;

public EditingCell() {
}

@Override
public void startEdit() {
    super.startEdit();

    if( textField == null ) {
        createTextField();
    }
    setText(null);
    setGraphic(textField);
    textField.selectAll();
}

@Override
public void cancelEdit() {
    super.cancelEdit();
    setText(Integer.toString(getItem()));
    setGraphic(null);
}

@Override
public void updateItem(Integer item, boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
        setText(null);
        setGraphic(null);
    } else {
        if (isEditing()) {
            if (textField != null) {
                textField.setText(getString());
            }
            setText(null);
            setGraphic(textField);
        } else {
            setText(getString());
            setGraphic(null);
        }
    }
}

private int checkTextfieldInput(String input){
    try {
        return Integer.parseInt(input);
    } catch (NumberFormatException e) {
        System.err.println("Input in TextField was not a number!");
    }
    return 1; //Default Value if Input was wrong
}

private void createTextField() {
    textField = new TextField(getString());
    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
    textField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
        if (!arg2) { commitEdit(checkTextfieldInput(textField.getText())); }
    });

    textField.setOnKeyReleased((KeyEvent t) -> {
        if (t.getCode() == KeyCode.ENTER) {
            String value = textField.getText();
            if (value != null) { commitEdit(checkTextfieldInput(value)); } else { commitEdit(1); }
        } else if (t.getCode() == KeyCode.ESCAPE) {
            cancelEdit();
        }
    });
}

private String getString() {
    return getItem() == null ? "" : getItem().toString();
}
}
