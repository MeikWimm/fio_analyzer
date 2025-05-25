/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.beans.binding.Bindings;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiConsumer;
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
            if(value.equals(Run.UNDEFINED_DOUBLE_VALUE)){
                return "";
            }
            return nf.format(value);
        }

        @Override
        public Double fromString(final String s) {
            // Don't need this, unless table is editable, see DoubleStringConverter if needed
            return null;
        }
    }

    public static class SafeIntegerStringConverter extends IntegerStringConverter {
        @Override
        public Integer fromString(String value) {
            Integer val = Job.DEFAULT_RUN_COUNT;
            try {
                val = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return val;
        }
    }

    public static class SafeDoubleStringConverter extends DoubleStringConverter {
        @Override
        public Double fromString(String value) {
            Double val = Run.UNDEFINED_DOUBLE_VALUE;
            try {
                val = Double.valueOf(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return val;
        }
    }

    public static class SpeedComparator implements Comparator<DataPoint> {

        @Override
        public int compare(DataPoint lhs, DataPoint rhs) {
            return Double.compare(lhs.getSpeed(), rhs.getSpeed());
        }
    }

    public static class ValidatedIntegerTableCell<S> extends TextFieldTableCell<S, Integer> {
        private final int maxValue;
        private final int minValue;
        private final int defaultValue;
        private final Label labelLoadInfo;
        private final String info;

        public ValidatedIntegerTableCell(Label labelLoadInfo, int minValue, int maxValue, int defaultValue, String info) {
            super(new SafeIntegerStringConverter());
            this.labelLoadInfo = labelLoadInfo;
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.defaultValue = defaultValue;
            this.info = info;

        }

        @Override
        public void commitEdit(Integer newValue) {
            if (newValue > maxValue) {
                labelLoadInfo.setText(info);
                super.commitEdit(defaultValue);
            } else if (newValue < minValue) {
                labelLoadInfo.setText(info);
                super.commitEdit(defaultValue);
            } else {
                super.commitEdit(newValue);
            }
        }
    }

    public static class ValidatedDoubleTableCell<S> extends TextFieldTableCell<S, Double> {
        private final double maxValue;
        private final double minValue;
        private final double defaultValue;
        private final Label labelLoadInfo;
        private final String info;

        public ValidatedDoubleTableCell(Label labelLoadInfo, double maxValue, double minValue, double defaultValue, String info) {
            super(new SafeDoubleStringConverter());
            this.labelLoadInfo = labelLoadInfo;
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.defaultValue = defaultValue;
            this.info = info;

        }

        @Override
        public void commitEdit(Double newValue) {
            if (newValue > maxValue) {
                labelLoadInfo.setText(info);
                super.commitEdit(defaultValue);
            } else if (newValue < minValue) {
                labelLoadInfo.setText(info);
                super.commitEdit(defaultValue);
            } else {
                super.commitEdit(newValue);
            }
        }
    }

    public static class CustomTableRowFactory implements Callback<TableView<Job>, TableRow<Job>> {

        // A descriptor class for each menu item to be added later
        private static class MenuItemDescriptor {
            String name;
            BiConsumer<TableRow<Job>, TableView<Job>> handler;

            MenuItemDescriptor(String name, BiConsumer<TableRow<Job>, TableView<Job>> handler) {
                this.name = name;
                this.handler = handler;
            }
        }

        private final List<MenuItemDescriptor> menuItemDescriptors = new ArrayList<>();

        public void addMenuItem(String name, BiConsumer<TableRow<Job>, TableView<Job>> handler) {
            menuItemDescriptors.add(new MenuItemDescriptor(name, handler));
        }

        @Override
        public TableRow<Job> call(TableView<Job> table) {
            final TableRow<Job> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();

            // Build actual MenuItems using row and table
            for (MenuItemDescriptor descriptor : menuItemDescriptors) {
                MenuItem item = new MenuItem(descriptor.name);
                item.setOnAction(e -> descriptor.handler.accept(row, table));
                rowMenu.getItems().add(item);
            }

            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(rowMenu)
            );

            return row;
        }
    }

    public static class CustomLineChart<X, Y> extends LineChart<X, Y> {


        public CustomLineChart(Axis<X> axis, Axis<Y> axis1) {
            super(axis, axis1);
            //axis.setAutoRanging(false);
            //axis1.setAutoRanging(false);
        }

        @Override
        protected void dataItemAdded(Series series, int i, Data data) {
            //no-op
        }

    }
}
