package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public abstract class PostHocTest {
    protected GenericTest test;

    public PostHocTest(GenericTest test){
        this.test = test;
    }
}
