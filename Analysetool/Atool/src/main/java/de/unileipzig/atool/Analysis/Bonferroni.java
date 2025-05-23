package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;

import java.util.List;

public class Bonferroni extends PostHocTest implements PostHocAnalyzer{

    public Bonferroni(Anova anova) {
        super(anova);
    }

    @Override
    public void apply(List<Run> sigRuns, List<List<Run>> anovaResult) {
            for (Run run : sigRuns){
                System.out.println("Run " + run.getID() + " p-value: " + run.getP());
            }
        }
}
