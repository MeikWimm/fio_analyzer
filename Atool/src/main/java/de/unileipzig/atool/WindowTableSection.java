package de.unileipzig.atool;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WindowTableSection implements Initializable {
    private final List<Section> sectionList;
    public WindowTableSection(List<Section> sectionList) {
        this.sectionList = sectionList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
