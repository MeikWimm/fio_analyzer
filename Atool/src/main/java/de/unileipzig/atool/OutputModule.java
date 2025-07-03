package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.GenericTest;
import de.unileipzig.atool.Analysis.PostHocTest;
import de.unileipzig.atool.Analysis.SteadyStateEval;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Paths;
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
    private final StringBuilder stringBuilder;
    private String selfCreatedPath = "/" + "Job_";

    public OutputModule() {
        directoryChooser = new DirectoryChooser();
        stringBuilder = new StringBuilder();
    }

    public void openDirectoryChooser(Window ownerWindow) {
        isAlreadyOpen = false;
        selectedDirectory = directoryChooser.showDialog(ownerWindow);
    }

    public STATUS saveEval(SteadyStateEval eval) {
        this.eval = eval;

        if(selectedDirectory == null) {
            LOGGER.log(Level.WARNING, "Cannot save eval to null directory");
            return STATUS.NO_DIR_SET;
        } else {
            LOGGER.log(Level.INFO, "Saving eval to "+selectedDirectory.getAbsolutePath());
        }

        LOGGER.log(Level.WARNING, "Is selected directory writable: " + selectedDirectory.canWrite());
        if(!selectedDirectory.canWrite()) {
            return STATUS.DIR_NOT_WRITEABLE;
        }

        if(!isOpen()) {
            isAlreadyOpen = true;
            writeEval();
            saveEvalToFile();
        } else {
            LOGGER.log(Level.WARNING, "Directory Chooser already open!");
            return STATUS.DIR_CHOOSER_ALREADY_OPEN;
        }

        return STATUS.SUCCESS;
    }

    private void saveEvalToFile() {
        try {
            FileWriter fileWriter = new FileWriter(selectedDirectory + selfCreatedPath + eval.getJob().getID() + "/evaluation.txt");
            // Creates a BufferedWriter
            BufferedWriter output = new BufferedWriter(fileWriter);

            // Writes the string to the file
            output.append(stringBuilder);

            // Closes the writer
            output.close();
        } catch (IOException e) {
            Logger.getLogger(OutputModule.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }

    private boolean isOpen() {
        return isAlreadyOpen;
    }

    private void writeEval() {
        WritableImage img = new WritableImage(800, 600);
        Job job = eval.getJob();
        stringBuilder.append("Job: ").append(job.getFile().toString()).append("\n");
        stringBuilder.append("Averaged Speed: ").append(job.getAverageSpeed()).append('\n');
        stringBuilder.append("Runs: ").append(job.getRuns().size()).append('\n');
        stringBuilder.append("Alpha: ").append(job.getAlpha()).append('\n');
        stringBuilder.append("\n");
        File path = new File(selectedDirectory + selfCreatedPath + "_eval");
        boolean isPathCreated = false;
        if (!path.exists()){
            isPathCreated = path.mkdirs();
        }

        if(isPathCreated){
            eval.getScene();
            saveTable("Evaluation",eval.getEvalTable());

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
                    testCharterScene.snapshot(img);
                    File graphFile = new File(Paths.get(path.toString(), "/" + test.getTestName() + "_graph"  + ".png").toString());
                    saveSnapshot(img, graphFile);
                    savePostHocGraphTests(img, path, test);
                } else {
                    Logger.getLogger(OutputModule.class.getName()).log(Level.WARNING, "Charter scene equals null! Test: " + test.getTestName());
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "Directory already exist!");
        }

        Logger.getLogger(OutputModule.class.getName()).log(Level.INFO, "Saved!");
    }

        private void saveTable(String title, TableView<?> table) {
        String space = "%-26s";
        String line = "--------------------------";

        if(table == null) {
            LOGGER.log(Level.WARNING, "TableView equals null for test " + title);
            return;
        }
            stringBuilder.append("[").append(title).append("]").append("\n").append("\n");
        for (int i = 0; i < table.getColumns().size(); i++) {
            String columnText = table.getColumns().get(i).getText();
            stringBuilder.append(String.format(space, columnText));
        }
            stringBuilder.append("\n");
            stringBuilder.append(line.repeat(table.getColumns().size())).append("\n"); // 30 characters

            for(int i= 0; i < table.getItems().size(); i++){ // rows
            for(int j = 0; j < table.getColumns().size() ; j++){ //columns
                String value = getValueAt(table, i, j); // table cell value
                stringBuilder.append(String.format(space, value));
            }
            stringBuilder.append("\n");
        }
            stringBuilder.append("\n");
            stringBuilder.append("\n");
    }

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
        DIR_NOT_WRITEABLE,
        ERROR_WHILE_READING_FILE,
        DIR_CHOOSER_ALREADY_OPEN,
        FAILURE
    }

}
