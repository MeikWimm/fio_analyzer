package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;

import java.util.List;

public interface PostHocAnalyzer {
    void apply(List<Run> resultWithRuns, List<List<Run>> resultWithGroups);
}
