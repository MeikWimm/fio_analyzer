package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class Bonferroni extends PostHocTest implements PostHocAnalyzer {

    public Bonferroni(Anova anova) {
        super(anova);
    }

    @Override
    public void apply(List<Run> sigRuns, List<List<Run>> anovaResult) {
            test.setAlpha(test.getAlpha() / this.test.groups.size());
            for(Run run: this.test.getJob().getRuns()){
                double fValue = run.getF();
                if (this.test.getCriticalValue() < fValue) {
                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
                } else {
                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                }
            }
        }
}
