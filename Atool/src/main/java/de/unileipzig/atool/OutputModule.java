package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.GenericTest;
import de.unileipzig.atool.Analysis.PostHocTest;
import de.unileipzig.atool.Analysis.SteadyStateEval;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import net.sourceforge.jdistlib.F;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutputModule {
    private static final Logger LOGGER = Logger.getLogger(OutputModule.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Output Module"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }
    private SteadyStateEval eval;
    private final DirectoryChooser directoryChooser;
    private boolean isAlreadyOpen;
    private File selectedDirectory;

    public OutputModule() {
        directoryChooser = new DirectoryChooser();
    }

    public void openDirectoryChooser(Window ownerWindow) {
        isAlreadyOpen = false;
        selectedDirectory = directoryChooser.showDialog(ownerWindow);
    }

    public STATUS saveEval(SteadyStateEval eval) {
        this.eval = eval;

        if(selectedDirectory == null) {
            LOGGER.log(Level.WARNING, "Cannot save eval to null directory");
            return STATUS.DIR_NOT_READABLE;
        }

        if(!isOpen()) {
            isAlreadyOpen = true;
            saveEvalAsImage();
        } else {
            LOGGER.log(Level.WARNING, "Directory Chooser already open!");
            return STATUS.DIR_CHOOSER_ALREADY_OPEN;
        }

        return STATUS.SUCCESS;
    }

    private boolean isOpen() {
        return isAlreadyOpen;
    }

    private void saveEvalAsImage() {
        Platform.runLater(() -> {
            WritableImage img = new WritableImage(1000, 500);
            WritableImage imgGraph = new WritableImage(800, 600);
            Job job = eval.getJob();
            File path = new File(InputModule.SELECTED_DIRECTORY.toString() + "/" + "TEST" + "_eval");
            boolean isPathCreated = false;
            if (!path.exists()){
                isPathCreated = path.mkdirs();
            }

            if(isPathCreated){
                //Saving table evaluation
                Scene scene = eval.getScene();
                if(scene != null) {
                    scene.snapshot(img);
                    File evalFile = new File(Paths.get(path.toString(), "/Evaluation.png").toString());
                    saveSnapshot(img, evalFile);
                } else {
                    Logger.getLogger(OutputModule.class.getName()).log(Level.WARNING, "Eval scene equals null!");
                }


                for (GenericTest test : eval.getTests()) {
                    test.getScene();
                    saveTable(test.getTestName(), test.getTable());
                    PostHocTest postHocTest = test.getPostHocTest();
                    if(postHocTest != null){
                        postHocTest.getScene();
                        saveTable(postHocTest.getTestName(), postHocTest.getTable());
                    }

                    // Saving graphs of all Tests
                    Scene testCharterScene = test.getCharterScene();
                    if(testCharterScene != null) {
                        scene = test.getCharterScene();
                        scene.snapshot(imgGraph);
                        File graphFile = new File(Paths.get(path.toString(), "/" + test.getTestName() + "_graph"  + ".png").toString());
                        saveSnapshot(imgGraph, graphFile);
                        savePostHocGraphTests(imgGraph, path, test);
                    } else {
                        Logger.getLogger(OutputModule.class.getName()).log(Level.WARNING, "Charter scene equals null! Test: " + test.getTestName());
                    }
                }
            } else {
                LOGGER.log(Level.WARNING, "Directory already exist!");
            }

            Logger.getLogger(OutputModule.class.getName()).log(Level.INFO, "Saved!");
        });
    }

        private void saveTable(String title, TableView<?> table) {
        if(table == null) {
            LOGGER.log(Level.WARNING, "TableView equals null for test " + title);
            return;
        }
        StringBuilder sb = new StringBuilder();
        //sb.append("<table border=\"1\">\n");
            sb.append("[").append(title).append("]").append("\n").append("\n");
        for (int i = 0; i < table.getColumns().size(); i++) {
            String columnText = table.getColumns().get(i).getText();
            sb.append(String.format("%-30s", columnText));
        }
            sb.append("\n");
            sb.append("------------------------------".repeat(table.getColumns().size())).append("\n"); // 30 characters

            for(int i= 0; i < table.getItems().size(); i++){ // rows
            for(int j = 0; j < table.getColumns().size() ; j++){ //columns
                String value = getValueAt(table, i, j); // table cell value
                sb.append(String.format("%-30s", value));
            }
            sb.append("\n");
        }
        System.out.println(sb);
    }

//    private void saveTableAsImage(TableView<?> table) {
//        GridPane gPane = new GridPane();
//        gPane.setSnapToPixel(true);
//        String style =  "-fx-background-color: WHITE; -fx-padding: 5;"+
//                        "-fx-hgap: 5; -fx-vgap: 5;";
//        gPane.setStyle(style);
//
//        int columnCounter = 0;
//        boolean isColumnSet = false;
//        for(int i= 0; i < table.getItems().size(); i++){ // rows
//            for(int j = 0; j < table.getColumns().size(); j++){ //columns
//                if(!isColumnSet){
//                    String columnName = table.getColumns().get(columnCounter).getText();
//                    Label label = new Label(columnName);
//                    label.setStyle("-fx-border-color: black;");
//                    gPane.add(label, i, j);
//                    isColumnSet = true;
//                }
//                    String value = getValueAt(table, i, j);
//                    Label label = new Label(value);
//                label.setStyle("-fx-border-color: black;");
//                gPane.add(label, i, j + 1);
//            }
//
//            if(columnCounter < table.getColumns().size()) {
//                columnCounter++;
//                isColumnSet = false;
//            }
//        }
//        new Scene(gPane);
//        WritableImage image = gPane.snapshot(new SnapshotParameters(), null);
//        saveSnapshot(image, new File(InputModule.SELECTED_DIRECTORY.toString() + "/" + "TEST" + "_table.png"));
//    }

    private String getValueAt(TableView<?> table, int row, int column) {
        var r = table.getColumns().get(column).getCellObservableValue(row);
        if(r == null) {
            return "";
        }
        if(r.getValue() != null) {
            return r.getValue().toString();
        }
        return "";
    }

    private void savePostHocTests(WritableImage img, File path, GenericTest test) {
        if(test.getPostHocTest() != null) {
            PostHocTest postHocTest = test.getPostHocTest();
            Scene scene = postHocTest.getScene();
            scene.snapshot(img);
            File postHocGraphFile = new File(Paths.get(path.toString(), "/" + postHocTest.getTestName()  + ".png").toString());
            saveSnapshot(img, postHocGraphFile);
        }
    }

    private void savePostHocGraphTests(WritableImage img, File path, GenericTest test) {
        if(test.getPostHocTest() != null) {
            PostHocTest postHocTest = test.getPostHocTest();
            Scene scene = postHocTest.getCharterScene();
            scene.snapshot(img);
            File postHocGraphFile = new File(Paths.get(path.toString(), "/" + postHocTest.getTestName() + "_graph"  + ".png").toString());
            saveSnapshot(img, postHocGraphFile);
        }
    }

    private void saveSnapshot(WritableImage img, File file) {
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
            Logger.getLogger(OutputModule.class.getName()).log(Level.INFO, "Saved image to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Logger.getLogger(OutputModule.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public enum STATUS {
        SUCCESS,
        NO_FILES_FOUND,
        NO_DIR_SET,
        DIR_NOT_READABLE,
        ERROR_WHILE_READING_FILE,
        DIR_CHOOSER_ALREADY_OPEN,
        FAILURE
    }

}
