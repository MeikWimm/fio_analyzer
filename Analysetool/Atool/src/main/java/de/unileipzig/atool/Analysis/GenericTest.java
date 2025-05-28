package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericTest {
    private static final Logger LOGGER = Logger.getLogger(GenericTest.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    protected Job job;
    protected List<List<Run>> groups;
    List<List<Run>> resultGroups;
    List<Run> resultRuns;
    double alpha;

    public GenericTest(Job job, int skipFirstRun, boolean skipGroup, int groupSize, double alpha){
        this.job = new Job(job);
        Job.prepareSkippedData(this.job, skipFirstRun);
        this.groups = Job.setupGroups(this.job, skipGroup, groupSize);
        this.resultGroups = new ArrayList<>();
        this.resultRuns = new ArrayList<>();
        this.alpha = alpha;
    }

    public void calculate(){}

    public void calculatePostHoc(PostHocAnalyzer postHocAnalyzer){
        if(this.resultGroups.size() < 2) {
            LOGGER.log(Level.WARNING, String.format("%s group size of test result is 1", this.getClass().getName()));
            return;
        }

        for (List<Run> runs: this.resultGroups){
            for (Run run: runs){
                if(run.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
                    this.resultRuns.add(run);
                }
            }
        }

        for (List<Run> runs: this.groups){
            if(runs.getFirst().getNullhypothesis() == Run.REJECTED_NULLHYPOTHESIS){
                for (Run run: runs){
                    run.setGroup("");
                }
            }
        }

        for (int i = 0; i < resultGroups.size() - 1; i++) {
            var currentGroup = resultGroups.get(i).getFirst();
            var nextGroup = resultGroups.get(i + 1).getFirst();
            if (currentGroup != null && nextGroup != null) {
                String currentGroupValue = currentGroup.getGroup();
                String nextGroupValue = nextGroup.getGroup();
                currentGroup.setGroup(currentGroupValue + " | " + nextGroupValue);
            }
            for (int j = 1; j < resultGroups.get(i).size(); j++) {
                resultGroups.get(i).get(j).setGroup("");
            }
        }
        resultGroups.getLast().getFirst().setGroup("");


        job.resetRuns();
        postHocAnalyzer.apply(this.resultRuns, this.resultGroups);
    }

    protected void draw(){

    }

    public Job getJob() {
        return this.job;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getCriticalValue(){
        return Run.UNDEFINED_DOUBLE_VALUE;
    }
    
    public static double average(Run run) {
    	double sum = 0;
    	double n = run.getData().size();
    	for (DataPoint dp : run.getData()) {
			sum += dp.getSpeed();
		}
    	return sum / n;
    }
    
    public static double average(List<Run> group) {
    	double sum = 0;
    	double n = 0;
    	for (Run run : group) {
			for (DataPoint dp: run.getData()) {
				sum += dp.getSpeed();
				n++;
			}
		}
    	return sum / n;
    }
    
    // Calculate the median of a sorted list
    public static double median(Run run) {
    	ArrayList<DataPoint> sorted = new ArrayList<DataPoint>(run.getData()); 
    	sorted.sort(new Utils.SpeedComparator());
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2).getSpeed();
        } else {
            return (sorted.get(n / 2 - 1).getSpeed() + sorted.get(n / 2).getSpeed()) / 2.0;
        }
    }
    
    // Calculate the median of a sorted list
    public static double median(List<DataPoint> data) {
    	ArrayList<DataPoint> sorted = new ArrayList<DataPoint>(data); 
    	sorted.sort(new Utils.SpeedComparator());
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2).getSpeed();
        } else {
            return (sorted.get(n / 2 - 1).getSpeed() + sorted.get(n / 2).getSpeed()) / 2.0;
        }
    }
    
    // Calculate MAD
    public static double mad(Run run, double median) {
        List<DataPoint> deviations = new ArrayList<>();
        for (DataPoint dp : run.getData()) {
            deviations.add(new DataPoint(Math.abs(dp.getSpeed() - median), dp.getTime()));
        }
        deviations.sort(new Utils.SpeedComparator());
        return median(deviations);
    }
    
    public static double variance(Run run) {
    	double sum = 0;
    	double average = average(run);
    	double n = run.getData().size();
    	for (DataPoint dp : run.getData()) {
			sum += Math.pow(dp.getSpeed() - average, 2);
		}
    	return sum / (n - 1);
    }

    public static double variance(List<Double> list, double average) {
        double sum = 0;
        double n = list.size();
        for (Double d : list) {
            sum += Math.pow(d - average, 2);
        }
        return sum / (n - 1);
    }

    
    public static double standardError(Run run1, Run run2) {
    	double std = 0;
    	std = Math.sqrt(0.5 * (GenericTest.variance(run1) / run1.getData().size() + GenericTest.variance(run2) / run2.getData().size()));
    	return std;
    }

    public static double calcualteCoV(double std,List<Run> group) {
        return std / average(group);
    }

    
}
