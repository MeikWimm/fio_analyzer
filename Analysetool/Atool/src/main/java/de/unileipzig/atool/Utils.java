/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author meni1999
 */
public abstract class Utils {

    public static Callback<TableColumn<Run, Byte>, TableCell<Run, Byte>> getHypothesisCellFactory() {
        return (TableColumn<Run, Byte> col) -> new TableCell<Run, Byte>() {
            @Override
            public void updateItem(Byte item, boolean empty) {
                super.updateItem(item, empty);
                if (Objects.equals(item, Run.UNDEFIND_NULLHYPOTHESIS) || item == null) {
                    setText("");
                    setStyle("");
                } else if (item.equals(Run.REJECTED_NULLHYPOTHESIS)) {
                    setStyle("-fx-background-color: tomato;");
                    setText("Rejected");
                } else {
                    setStyle("-fx-background-color: green;");
                    setText("Accepted");
                }
            }
        };
    }

    /**
     * Formatter for Logger
     *
     * @author meni1999
     */
    public static class CustomFormatter extends Formatter {

        String stageName;

        public CustomFormatter(String stageName) {
            super();
            this.stageName = stageName;
        }

        @Override
        public String format(LogRecord record) {
            return String.format("[LOG, %s, %s] ", this.stageName, record.getLevel()) +
                    record.getMessage() +
                    "\n";
        }

    }

    public static class CustomStringConverter extends StringConverter<Double> {
        private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

        {
            nf.setMaximumFractionDigits(Settings.FRACTION_DIGITS);
            nf.setMinimumFractionDigits(Settings.FRACTION_DIGITS);
        }

        @Override
        public String toString(final Double value) {
            return nf.format(value);
        }

        @Override
        public Double fromString(final String s) {
            // Don't need this, unless table is editable, see DoubleStringConverter if needed
            return null;
        }
    }

    public static class SpeedComparator implements Comparator<DataPoint> {

        @Override
        public int compare(DataPoint lhs, DataPoint rhs) {
            return Double.compare(lhs.getSpeed(), rhs.getSpeed());
        }
    }
}
