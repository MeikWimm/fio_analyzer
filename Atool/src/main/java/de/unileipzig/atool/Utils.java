package de.unileipzig.atool;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.*;

/**
 * @author meni1999
 */
public abstract class Utils {


    public static Callback<TableColumn<Section, Boolean>, TableCell<Section, Boolean>> getHypothesisCellFactory() {
        return (TableColumn<Section, Boolean> col) -> new TableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                    setStyle("");
                } else if (!item) {
                    setStyle("-fx-background-color: #fb7157;");
                    setText("Rejected");
                } else {
                    setStyle("-fx-background-color: #5fd85f;");
                    setText("Accepted");
                }
            }
        };
    }

    public static Callback<TableColumn<Section, Double>, TableCell<Section, Double>> getStatusCellFactory(double threshold) {
        return (TableColumn<Section, Double> col) -> new TableCell<>() {
            @Override
            public void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                    setStyle("");
                } else if (item > threshold) {
                    setStyle("-fx-background-color: #fb7157;");
                    setText(String.format(Locale.US,"%.2f", item));
                } else {
                    setStyle("-fx-background-color: #5fd85f;");
                    setText(String.format(Locale.US,"%.2f", item));
                }
            }
        };
    }

    public static Callback<TableColumn<Section, Boolean>, TableCell<Section, Boolean>> getBooleanCellFactory() {
        return (TableColumn<Section, Boolean> col) -> new TableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                    setStyle("");
                } else if (item) {
                    setText("Yes");
                } else {
                    setText("No");
                }
            }
        };
    }


    public static class CustomStringConverter extends StringConverter<Double> {
        private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

        {
            nf.setMaximumFractionDigits(Settings.FRACTION_DIGITS);
            nf.setMinimumFractionDigits(Settings.FRACTION_DIGITS);
        }

        @Override
        public String toString(final Double value) {
            if(value.equals(Section.UNDEFINED_DOUBLE_VALUE)){
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
                Logging.log(Level.WARNING, "Utils", "NumberFormatException: " + value);
            }
            return val;
        }
    }

    public static class SafeDoubleStringConverter extends DoubleStringConverter {
        @Override
        public Double fromString(String value) {
            Double val = Section.UNDEFINED_DOUBLE_VALUE;
            try {
                val = Double.valueOf(value);
            } catch (NumberFormatException e) {
                Logging.log(Level.WARNING, "Utils", "NumberFormatException: " + value);
            }
            return val;
        }
    }

    public static class SpeedComparator implements Comparator<DataPoint> {

        @Override
        public int compare(DataPoint lhs, DataPoint rhs) {
            return Double.compare(lhs.data, rhs.data);
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

        public ValidatedDoubleTableCell(Label labelLoadInfo, double minValue, double maxValue, double defaultValue, String info) {
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
