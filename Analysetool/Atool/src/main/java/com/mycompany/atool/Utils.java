/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author meni1999
 */
public abstract class Utils {
    
    /**
 * Formatter for Logger
 * @author meni1999
 */
public static class CustomFormatter extends Formatter{

    String stageName;
    
    public CustomFormatter(String stageName){
        super();
        this.stageName = stageName;
    }
    
    @Override
    public String format(LogRecord record) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("[LOG, %s, %s] ", this.stageName, record.getLevel()));
        msg.append(record.getMessage());
        msg.append("\n");
        return msg.toString();
    }
    
}

public static class CustomStringConverter extends StringConverter<Double>{
        private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

        {
             nf.setMaximumFractionDigits(3);
             nf.setMinimumFractionDigits(3);
        }

        @Override public String toString(final Double value) {
            return nf.format(value);
        }

        @Override public Double fromString(final String s) {
            // Don't need this, unless table is editable, see DoubleStringConverter if needed
            return null; 
        }
}

    public static class SpeedComparator implements Comparator<DataPoint>{

        @Override
        public int compare(DataPoint lhs, DataPoint rhs) {
            if(lhs.getSpeed() > rhs.getSpeed()){
                return 1;
            } else if(lhs.getSpeed() < rhs.getSpeed()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    
    public static Callback<TableColumn<Run, Boolean>, TableCell<Run, Boolean>> getHypothesisCellFactory(){
        return (TableColumn<Run, Boolean> col) -> new TableCell<Run, Boolean>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                    setStyle("");
                } else if (!item) {
                    setStyle("-fx-background-color: tomato;");
                    setText("Rejected");
                } else {
                    setStyle("-fx-background-color: green;");
                    setText("Accepted");
                }
            }
        };
    }
}
