/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.eval;

import java.util.ArrayList;
import moa.classifiers.trees.HoeffdingTree;
import moa.streams.InstanceStream;
import decisiontreetester.DecisionTreeTester;
import decisiontreetester.settings.GS;
import weka.core.Instance;
import weka.core.Utils;
import decisiontreetester.utils.MyConfusionMatrix;

/**
 *
 * @author Rocco De Rosa
 */
public class ModifiedInterleavedTestFilterThenTrainWithMatrix extends
        InterleavedTestThenTrainWithMatrix {
    
    
    public ModifiedInterleavedTestFilterThenTrainWithMatrix(
            HoeffdingTree learner,
            InstanceStream stream, 
            double t,
            int chartType) {
        super(learner, stream, t, chartType);
        learner.leafpredictionOption.setChosenLabel("MC");
    }
    
    public ModifiedInterleavedTestFilterThenTrainWithMatrix(
            HoeffdingTree learner,
            InstanceStream stream,
            int filter,
            int chartType) {
        this(learner, stream, 0.5, chartType);
    }
    
    @Override
    public ArrayList<MyConfusionMatrix> mainTask() {
        ArrayList<MyConfusionMatrix> steps = new ArrayList<MyConfusionMatrix>();
        ArrayList<MyConfusionMatrix> windowSteps = new ArrayList<MyConfusionMatrix>();
        
        MyConfusionMatrix evaluation = new MyConfusionMatrix(2);
        
        learner.setModelContext(stream.getHeader());
        long instancesProcessed = 0;
        
        DecisionTreeTester.total_instances = 0;
        
        int updates = 0;
        //ArrayList<Double> pos_probabilities = new ArrayList<Double>();
          
        //double[] pos_probabilities = new double[183066];
        
        ArrayList<Double> pos_probabilities_list = new ArrayList<Double>();
        
        while (stream.hasMoreInstances() && 
                (instancesProcessed < GS.INSTANCE_LIMIT)) {
            DecisionTreeTester.total_instances++;
            Instance trainInst = stream.nextInstance();
            //System.out.println(trainInst);
            //System.out.println(trainInst.classValue());
            Instance testInst = (Instance) trainInst.copy();
            int trueClass = (int) trainInst.classValue();
            
            double[] prediction = learner.getVotesForInstance(testInst);
            
            double delta = 0;
            double epsilon = 0;
            
            if (prediction.length == 2) {
                double instancesPerLeaf = prediction[0] + prediction[1];
                double positiveInstances = prediction[1];
                
                delta = (2.0 * (positiveInstances / instancesPerLeaf) - 1.0);
                
                epsilon = Math.sqrt((2.0 / (instancesPerLeaf + 1)) * Math.log((instancesProcessed + 1)^2));
                
                //System.out.println("Delta: " + delta);
                //System.out.println("Epsilon: " + epsilon);
                
                
            }
            //int beforeFilter = 0;
            
            
            
            if ((filter < 0) || instancesProcessed <= filter || (delta + epsilon) >= 0) {
                    learner.trainOnInstance(trainInst);
                    updates++;
            }
            
            if (prediction.length == 2) {
                double v = prediction[1] / Utils.sum(prediction);
                if (!Double.isNaN(v) && v >= t) {
                    pos_probabilities_list.add(v);
                }
            } 
            int predResult;
            if (prediction.length == 2) 
                predResult = (prediction[1] / Utils.sum(prediction)) >= t ? 1 : 0;
            else 
                predResult = Utils.maxIndex(prediction);
            
            evaluation.addPrediction(testInst, predResult);
            
            //System.out.println(learner.correctlyClassifies(testInst));
            // evaluator.addResult(testInst, prediction);
            /*
            if ((Utils.maxIndex(prediction) == 1) && (trueClass == 0))
                trainInst.setClassValue(1.0);
            
            */ 
            
            
            if (instancesProcessed == filter)
                System.out.println(evaluation.getMatrix()[1][1]);
            
            instancesProcessed++;
            if ((instancesProcessed % GS.SAMPLE_FREQUENCY == 0) || 
                    stream.hasMoreInstances() == false) {
                double mcc = evaluation.getMCC(1);
                double prec = evaluation.getPrecision(1);
                double rec = evaluation.getRecall(1);
                double fmeas = evaluation.getFMeasure(1);
                System.out.println(String.format("%5$d MCC: %1$.2f; Prec.: %2$.2f; Rec.: %3$.2f; F-m.: %4$.2f.", mcc, prec, rec, fmeas, instancesProcessed));
                //System.out.println(Arrays.toString(prediction));
                //System.out.println(learner.measureTreeDepth());
                steps.add(evaluation.duplicate());
                //evaluation.addPrediction(testInst, trueClass);
                //System.out.println(testInst.classValue());
                //System.out.println(Arrays.toString(prediction));
                if (chartType == 2) {
                    if (steps.size() > 1) 
                        windowSteps.add(evaluation.difference(steps.get(steps.size() - 2)));
                    else
                        windowSteps.add(evaluation.duplicate());
                } else if (chartType == 3) {
                    if (steps.size() > type3window)
                        windowSteps.add(evaluation.difference(steps.get(steps.size() - type3window - 1)));
                    else
                        windowSteps.add(evaluation.duplicate());
                }
            }
        }
        
        double[] pos_probabilities = new double[pos_probabilities_list.size()];
        int i= 0;
        for (Double d : pos_probabilities_list)
            pos_probabilities[i++] = d.doubleValue();
        
        //System.out.println(Arrays.toString(pos_probabilities));
        System.out.println("Media delle proporzioni: " + Utils.mean(pos_probabilities));
        System.out.println("varianza delle proporzioni: " + Utils.variance(pos_probabilities));
        
        
        System.out.println(updates);
        //learner.
        if (chartType == 2 || chartType == 3)
            return windowSteps;
        else
            return steps;
    }
    
}
