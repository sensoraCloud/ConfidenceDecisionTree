/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.eval;

import java.util.ArrayList;
import java.util.Arrays;
import moa.classifiers.Classifier;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import decisiontreetester.settings.GS;
import weka.core.Instance;
import weka.core.Utils;
import decisiontreetester.utils.MyConfusionMatrix;

/**
 *
 * @author Rocco De Rosa
 */
public class InterleavedTestThenTrainWithMatrixOld {
    
    private Classifier learner;
    private InstanceStream stream;
    private double t;
    
    public static ClassificationPerformanceEvaluator evaluator = 
            new BasicClassificationPerformanceEvaluator();
    //public static int INSTANCE_LIMIT = 200000;
    //public static int SAMPLE_FREQUENCY = 1000;
    
    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to learn from.", InstanceStream.class,
            "generators.RandomTreeGenerator");
    
    public InterleavedTestThenTrainWithMatrixOld(
            Classifier learner,
            InstanceStream stream) {
        this.learner = learner;
        this.stream = stream;
        this.t = 0.5;
    }
    
    public InterleavedTestThenTrainWithMatrixOld(
            Classifier learner, 
            InstanceStream stream,
            double t) {
        this.learner = learner;
        this.stream = stream;
        this.t = t;
    }
    
    public ArrayList<MyConfusionMatrix> mainTask() {
        ArrayList<MyConfusionMatrix> steps = new ArrayList<MyConfusionMatrix>();
        
        MyConfusionMatrix evaluation = new MyConfusionMatrix(stream.getHeader().numClasses());
        
        learner.setModelContext(stream.getHeader());
        long instancesProcessed = 0;
        
        while (stream.hasMoreInstances() && 
                (instancesProcessed < GS.INSTANCE_LIMIT)) {
            Instance trainInst = stream.nextInstance();
            //System.out.println(trainInst);
            //System.out.println(trainInst.classValue());
            Instance testInst = (Instance) trainInst.copy();
            int trueClass = (int) trainInst.classValue();
            
            
            
            double[] prediction = learner.getVotesForInstance(testInst);
            
            int predResult;
            if (prediction.length == 2) 
                predResult = (prediction[1] / Utils.sum(prediction)) >= t ? 1 : 0;
            else 
                predResult = Utils.maxIndex(prediction);
            
            evaluation.addPrediction(testInst, predResult);
            
            //System.out.println(learner.correctlyClassifies(testInst));
            // evaluator.addResult(testInst, prediction);
            learner.trainOnInstance(trainInst);
            instancesProcessed++;
            if ((instancesProcessed % GS.SAMPLE_FREQUENCY == 0) || 
                    stream.hasMoreInstances() == false) {
                double acc = evaluation.getAccuracy();
                double prec = evaluation.getPrecision(1);
                double rec = evaluation.getRecall(1);
                double fmeas = evaluation.getFMeasure(1);
                System.out.println(String.format("%5$d Accuracy: %1$.2f; Prec.: %2$.2f; Rec.: %3$.2f; F-m.: %4$.2f.", acc, prec, rec, fmeas, instancesProcessed));
                steps.add(evaluation.duplicate());
                //evaluation.addPrediction(testInst, trueClass);
                //System.out.println(testInst.classValue());
                System.out.println(predResult);
                System.out.println(Arrays.toString(prediction));
            }
        }
        //learner.
        
        return steps;
    }
    
}
