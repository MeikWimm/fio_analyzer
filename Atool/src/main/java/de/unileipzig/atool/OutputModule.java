package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.GenericTest;
import de.unileipzig.atool.Analysis.SteadyStateEval;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Paths;
import java.util.logging.Level;

public class OutputModule {
    private SteadyStateEval eval;
    private final DirectoryChooser directoryChooser;
    private boolean isAlreadyOpen;
    private File selectedDirectory;
    private final StringBuilder stringBuilder;
    private File path;
    private final String className = "OutputModule";

    public OutputModule() {
        directoryChooser = new DirectoryChooser();
        stringBuilder = new StringBuilder();
    }

    public void openDirectoryChooser(Window ownerWindow) {
        isAlreadyOpen = false;
        if(ownerWindow != null) {
            selectedDirectory = directoryChooser.showDialog(ownerWindow);
        } else {
            selectedDirectory = directoryChooser.showDialog(null);
        }
    }

    public STATUS saveEval(SteadyStateEval eval) {
        this.eval = eval;

        if(selectedDirectory == null) {
            Logging.log(Level.WARNING, className, "Cannot save eval to null directory");
            return STATUS.NO_DIR_SET;
        } else {
            Logging.log(Level.INFO,  className,"Saving eval to "+selectedDirectory.getAbsolutePath());
        }

        if(!selectedDirectory.canWrite()) {
            Logging.log(Level.WARNING, className, "Is selected directory writable: " + selectedDirectory.canWrite());
            return STATUS.DIR_NOT_WRITEABLE;
        } else {
            Logging.log(Level.INFO, className, "Is selected directory writable: " + selectedDirectory.canWrite());
        }

        if(!isOpen()) {
            isAlreadyOpen = true;
            path = new File(selectedDirectory + "/Job_" + eval.getJob().getID());
            Logging.log(Level.INFO, className, "created new directory: " + "\\Job_" + eval.getJob().getID());
            writeEval();
            saveEvalToFile();
        } else {
            Logging.log(Level.WARNING, className, "Directory Chooser already open!");
            return STATUS.DIR_CHOOSER_ALREADY_OPEN;
        }

        return STATUS.SUCCESS;
    }

    private void saveEvalToFile() {
        try {
            FileWriter fileWriter = new FileWriter(path + "/evaluation.txt");
            // Creates a BufferedWriter
            BufferedWriter output = new BufferedWriter(fileWriter);

            // Writes the string to the file
            output.append(stringBuilder);

            // Closes the writer
            output.close();
        } catch (IOException e) {
            Logging.log(Level.SEVERE, className, e.getMessage());
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
        stringBuilder.append("Unit used: ").append(Settings.getConversion()).append("\n");
        stringBuilder.append("Standard Deviation: ").append(job.getStandardDeviation()).append('\n');
        stringBuilder.append("Runs: ").append(job.getRuns().size()).append('\n');
        stringBuilder.append("Alpha: ").append(job.getAlpha()).append('\n');
        stringBuilder.append("\n");
        boolean isPathCreated = false;
        if (!path.exists()){
            isPathCreated = path.mkdirs();
        }

        if(isPathCreated){
            eval.getScene();
            stringBuilder.append("[Evaluation]").append("\n");
            saveTable("Evaluation",eval.getEvalTable());

            for (GenericTest test : eval.getTests()) {
                test.getScene();
                saveTestValues(test);
                saveTable(test.getTestName(), test.getTable());

                // Saving graphs of all Tests
                Scene testCharterScene = test.getCharterScene();
                if(testCharterScene != null) {
                    testCharterScene.snapshot(img);
                    File graphFile = new File(Paths.get(path.toString(), "/" + test.getTestName() + "_graph"  + ".png").toString());
                    saveSnapshot(img, graphFile);
                } else {
                    Logging.log(Level.INFO, className, "No charter scene found for test: " + test.getTestName());
                }
            }
        } else {
            Logging.log(Level.WARNING, className,"Directory already exist!");
        }

        Logging.log(Level.INFO, className,"Done!");
    }


    private void saveTestValues(GenericTest test) {
        stringBuilder.append("[").append(test.getTestName()).append("]").append("\n");
        double criticalValue = test.getCriticalValue();
        if(criticalValue == Run.UNDEFINED_DOUBLE_VALUE){
            stringBuilder.append("Critical Value: ").append("UNDEFINED").append("\n").append("\n");
        } else {
            stringBuilder.append("Critical Value: ").append(criticalValue).append("\n").append("\n");
        }
    }

    private void saveTable(String title, TableView<?> table) {
        int width = 35;
        String space = String.format("%%-%ds", width);
        String line = "-".repeat(width);

        if(table == null) {
            Logging.log(Level.WARNING, className,"TableView equals null for test " + title);
            return;
        }

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

    private void saveSnapshot(WritableImage img, File file) {
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
            Logging.log(Level.INFO, className,"Saved image to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Logging.log(Level.SEVERE, className, e.getMessage());
        }
    }

    public String getInfo(OutputModule.STATUS state) {
        return switch (state) {
            case NO_DIR_SET -> "No directory set!";
            case DIR_CHOOSER_ALREADY_OPEN -> "Directory chooser already open!";
            case DIR_NOT_WRITEABLE -> "Directory is not writable!";
            case SUCCESS -> "All files loaded!";
            default -> "Unknown state!";
        };
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
