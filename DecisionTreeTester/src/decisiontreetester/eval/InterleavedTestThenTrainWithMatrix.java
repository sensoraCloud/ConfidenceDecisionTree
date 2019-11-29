/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import moa.classifiers.Classifier;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import decisiontreetester.DecisionTreeTester;
import decisiontreetester.classifiers.HoeffCesaBoundTree;
import decisiontreetester.classifiers.TransparentHoeffdingTree;
import decisiontreetester.settings.GS;
import weka.core.Instance;
import weka.core.Utils;
import decisiontreetester.utils.MyConfusionMatrix;
import java.math.RoundingMode;
import java.text.NumberFormat;
import moa.classifiers.AbstractClassifier;

/**
 *
 * @author Rocco De Rosa
 */
public class InterleavedTestThenTrainWithMatrix {

    double[] prediction;
    protected Classifier learner;
    protected InstanceStream stream;
    protected Random rnd;
    public int filter;
    public int chartType;
    public String[] myclasses;
    public MyConfusionMatrix evaluation;
    protected double t;
    protected int type3window = 5;
    public static ClassificationPerformanceEvaluator evaluator =
            new BasicClassificationPerformanceEvaluator();
    //public static int INSTANCE_LIMIT = 200000;
    //public static int SAMPLE_FREQUENCY = 1000;
    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to learn from.", InstanceStream.class,
            "generators.RandomTreeGenerator");
    public String datasetname = "";
    public String old_perf = "";
    public double current_perf = 0;
    NumberFormat nf = NumberFormat.getInstance();

    public InterleavedTestThenTrainWithMatrix(
            Classifier learner,
            InstanceStream stream,
            double t,
            int chartType) {
        this.learner = learner;
        this.stream = stream;
        this.rnd = new Random();
        //this.filter = filter;
        this.t = t;
        this.chartType = chartType;
    }

    public InterleavedTestThenTrainWithMatrix(
            Classifier learner,
            InstanceStream stream,
            double t,
            int chartType, String datasetname) {
        this.learner = learner;
        this.stream = stream;
        this.rnd = new Random();
        //this.filter = filter;
        this.t = t;
        this.chartType = chartType;
        this.datasetname = datasetname;
    }

    public InterleavedTestThenTrainWithMatrix(
            Classifier learner,
            InstanceStream stream,
            int filter,
            int chartType) {
        this(learner, stream, 0.5, chartType);
    }

    public ArrayList<MyConfusionMatrix> mainTask() {

        String line_out = "";

        ArrayList<MyConfusionMatrix> steps = new ArrayList<MyConfusionMatrix>();
        ArrayList<MyConfusionMatrix> windowSteps = new ArrayList<MyConfusionMatrix>();
        int s = stream.getHeader().classAttribute().numValues();
        evaluation = new MyConfusionMatrix(s);

        learner.setModelContext(stream.getHeader());
        long instancesProcessed = 0;

        DecisionTreeTester.total_instances = 0;

        //int updates = 0;
        //ArrayList<Double> pos_probabilities = new ArrayList<Double>();

        //double[] pos_probabilities = new double[183066];

        ArrayList<Double> pos_probabilities_list = new ArrayList<Double>();

        boolean isTree = learner instanceof TransparentHoeffdingTree;

        while (stream.hasMoreInstances()
                && (instancesProcessed < GS.INSTANCE_LIMIT)) {

            DecisionTreeTester.total_instances++;
            Instance trainInst = stream.nextInstance();
            //System.out.println(trainInst);
            //System.out.println(trainInst.classValue());
            Instance testInst = (Instance) trainInst.copy();
            //int trueClass = (int) trainInst.classValue();

            prediction = learner.getVotesForInstance(testInst);
            //int beforeFilter = 0;

            if (prediction.length == 2) {

                double v = prediction[1] / Utils.sum(prediction);
                if (!Double.isNaN(v) && v >= t) {
                    pos_probabilities_list.add(v);
                }
            }

            int predResult;

            if (prediction.length == s) {
                /*    for (int i=0; i<s; i++){
                 System.out.println("predizione classe: "+i+"safa"+prediction[i]);
                 }*/
                predResult = (prediction[1] / Utils.sum(prediction)) >= t ? 1 : 0;
            } else {
                predResult = Utils.maxIndex(prediction);
            }

            //        System.out.println("\n"+testInst.toString()+",      ");




            //System.out.println(learner.correctlyClassifies(testInst));
            // evaluator.addResult(testInst, prediction);
            /*
             if ((Utils.maxIndex(prediction) == 1) && (trueClass == 0))
             trainInst.setClassValue(1.0);
            
             */
            //  System.out.println(testInst.toString() +",      "+predResult);

            learner.trainOnInstance(trainInst);

            //if (instancesProcessed == filter)
            //    System.out.println(evaluation.getMatrix()[1][1]);


            instancesProcessed++;

            //if seed is over, get peroformances
            if (instancesProcessed > GS.DEFAULT_SEED) {


                evaluation.addPrediction(testInst, predResult);


                if (isTree) {

                    TransparentHoeffdingTree treeLearner =
                            (TransparentHoeffdingTree) learner;
                } else {

                    AbstractClassifier treeLearner = (AbstractClassifier) learner;

                }

                if ((instancesProcessed % GS.SAMPLE_FREQUENCY == 0)
                        || stream.hasMoreInstances() == false) {


                    if (GS.print_file_window_performances == 1) {

                        if (GS.type_performances.equals("acc")) {

                            line_out += ("\n" + DecisionTreeTester.total_instances + ";" + evaluation.getAccuracy() + ";" + ((TransparentHoeffdingTree) learner).getNodeCount());

                            current_perf = evaluation.getAccuracy();

                        } else if (GS.type_performances.equals("fmeasure0")) {

                            line_out += ("\n" + DecisionTreeTester.total_instances + ";" + evaluation.getFMeasure(0) + ";" + ((TransparentHoeffdingTree) learner).getNodeCount());

                            current_perf = evaluation.getFMeasure(0);                                                      
                            
                        } else if (GS.type_performances.equals("fmeasure1")) {

                            line_out += ("\n" + DecisionTreeTester.total_instances + ";" + evaluation.getFMeasure(1) + ";" + ((TransparentHoeffdingTree) learner).getNodeCount());

                            current_perf = evaluation.getFMeasure(1);
                        }

                        if (GS.set_GS_values == 1) {

                            //truncate 4 digit performances                            
                            nf.setMaximumFractionDigits(4);
                            nf.setGroupingUsed(false);
                            nf.setRoundingMode(RoundingMode.FLOOR);

                            //for selective sampling saturation
                            if (!nf.format(current_perf).equals(old_perf)) {

                                if (learner instanceof HoeffCesaBoundTree) {

                                    GS.INSTANCE_SS = (int) instancesProcessed;
                                    GS.best_rocco_acc_SS = current_perf;
                                    
                                    if (isTree) {
                                        GS.best_rocco_nodes_SS =  ((TransparentHoeffdingTree) learner).getNodeCount();
                                    }
                                }

                            }

                            old_perf = nf.format(current_perf);

                            if ((!(learner instanceof HoeffCesaBoundTree)) && (instancesProcessed <= GS.INSTANCE_SS)) {

                                GS.best_base_acc_SS = current_perf;
                                if (isTree) {
                                    GS.best_base_nodes_SS = ((TransparentHoeffdingTree) learner).getNodeCount();
                                }
                            }



                        }



                    }



                    //double acc = evaluation.getAccuracy();
                    //double prec = evaluation.getPrecision(1);
                    //double rec = evaluation.getRecall(1);
                    //double fmeas = evaluation.getFMeasure(1);
                    //System.out.println(String.format("%5$d Accuracy: %1$.2f; Prec.: %2$.2f; Rec.: %3$.2f; F-m.: %4$.2f.", acc, prec, rec, fmeas, instancesProcessed));
                    //System.out.println(((HoeffdingTree)learner).measureTreeDepth());
                    //System.out.println(Arrays.toString(prediction));
                    //System.out.println(learner.measureTreeDepth());
                    steps.add(evaluation.duplicate());
                    //evaluation.addPrediction(testInst, trueClass);
                    //System.out.println(testInst.classValue());
                    //System.out.println(Arrays.toString(prediction));
                    if (chartType == 2) {
                        if (steps.size() > 1) {
                            windowSteps.add(evaluation.difference(steps.get(steps.size() - 2)));
                        } else {
                            windowSteps.add(evaluation.duplicate());
                        }
                    } else if (chartType == 3) {
                        if (steps.size() > type3window) {
                            windowSteps.add(evaluation.difference(steps.get(steps.size() - type3window - 1)));
                        } else {
                            windowSteps.add(evaluation.duplicate());
                        }
                    }

                    if (isTree) {


                    //    treeLearner.errorProgression.add(treeLearner.calculateError());

                      //  treeLearner.numNodesProgression.add(treeLearner.getNodeCount());

                       // treeLearner.queryProgression.add((treeLearner.getQueryNumber()) / (treeLearner.trainingWeightSeenByModel()));

                    }
                }

            }

        }

        double[] pos_probabilities = new double[pos_probabilities_list.size()];
        int i = 0;
        for (Double d : pos_probabilities_list) {
            pos_probabilities[i++] = d.doubleValue();
        }

        //System.out.println(Arrays.toString(pos_probabilities));
        //System.out.println("Media delle proporzioni: " + Utils.mean(pos_probabilities));
        //System.out.println("varianza delle proporzioni: " + Utils.variance(pos_probabilities));


        //System.out.println(updates);
        //learner.

        if (GS.print_file_window_performances == 1) {
            printLineOut(line_out, ((TransparentHoeffdingTree) learner).getName());
        }


        if (chartType == 2 || chartType == 3) {
            return windowSteps;
        } else {
            return steps;
        }
    }

    private void printLineOut(String line_out, String classifierName) {
        try {


            File file = new File(GS.completeDir + "\\" + this.datasetname.replace(".arff", "") + "_performances.csv");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(line_out.replace(".", ","));
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
