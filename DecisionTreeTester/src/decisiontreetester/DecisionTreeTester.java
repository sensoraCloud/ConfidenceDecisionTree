/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester;

import decisiontreetester.classifiers.CorrectHoeffTree;
import decisiontreetester.classifiers.FilteringHoeffdingTreeV1;
import decisiontreetester.classifiers.FilteringHoeffdingTreeV2;
import decisiontreetester.classifiers.HoeffCesaBoundTree;
import decisiontreetester.classifiers.McDiarmidBoundTree;
import decisiontreetester.classifiers.NaiveBayesLog;
import decisiontreetester.classifiers.SelectiveSamplingHoeffdingTree;
import decisiontreetester.classifiers.TransparentHoeffdingTree;
import decisiontreetester.eval.InterleavedTestThenTrainWithMatrix;
import decisiontreetester.eval.ModifiedInterleavedTestFilterThenTrainWithMatrix;
import decisiontreetester.gui.DrawableChart;
import decisiontreetester.gui.DrawableTree;
import decisiontreetester.settings.GS;
import decisiontreetester.utils.MyConfusionMatrix;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import moa.classifiers.Classifier;
import moa.classifiers.core.splitcriteria.AccuracySplitCriterion;
import moa.classifiers.core.splitcriteria.GiniSplitCriterion;
import moa.classifiers.trees.HoeffdingTree;
import moa.streams.ArffFileStream;

/**
 *
 * @author Rocco De Rosa
 */
public class DecisionTreeTester extends JFrame {

    /**
     * @param args the command line arguments
     */
    /*
     public static ArrayList<MyConfusionMatrix> currentStepsHoeffding = null;
     public static ArrayList<MyConfusionMatrix> currentStepsBayes = null;
     public static ArrayList<MyConfusionMatrix> currentStepsHoeffdingFiltered = null;
    
     public static ArrayList<ArrayList<MyConfusionMatrix>> currentStepsBayesVariableT = null;
     
     */
    public static int total_instances = 0;
    static String matrixToFile = "";
    //multiclasse
    //   public static String datasetName = "covtypeNorm";  //Nome del file
    //    public static String datasetName = "kddcup1999";
    //    public static String datasetName = "poker-lsn";
    //   public static String datasetName = "sensITvehicle";
    // public static String datasetName = "covtypeNorm_rand";  //Nome del file
    
    
     //binari
    //   public static String datasetName = "covtypeNorm_binario_rand"; //acc C_bound=0.05
    //   public static String datasetName = "airlines_rand"; //acc C_bound=0.45
      //  public static String datasetName = "elecNormNew_rand"; //acc C_bound=0.1
   //  public static String datasetName = "kddcup1999_binario_test"; //f-measure class 0  C_bound=0.65
   
    
    
    
    //Dimensioni
   // public static String datasetName = "Test_1_tree_2_cls_32_dim_1_bal_96_leaves_100_beta_0_hmm_9_max_dep_1_ril_feat"; 
   // public static String datasetName = "Test_1_tree_2_cls_64_dim_1_bal_192_leaves_100_beta_0_hmm_10_max_dep_1_ril_feat"; 
   // public static String datasetName = "Test_1_tree_2_cls_128_dim_1_bal_384_leaves_100_beta_0_hmm_11_max_dep_1_ril_feat"; 
       
    
    //Class prob
    //  public static String datasetName = "Test_1_tree_2_cls_64_dim_1_bal_1024_leaves_10_beta_0_hmm_15_max_dep_1_ril_feat_facile"; 
  //  public static String datasetName = "Test_1_tree_2_cls_64_dim_1_bal_1024_leaves_10_beta_0_hmm_16_max_dep_1_ril_feat_medio"; 
  //  public static String datasetName = "Test_1_tree_2_cls_64_dim_1_bal_1024_leaves_10_beta_0_hmm_15_max_dep_1_ril_feat_difficile"; 
   
    
    
    // public static String datasetName = "kddcup1999_train_test";
    //  public static String datasetName = "anno_10_11_12_no_ID";
    // public static String datasetName = "airlines";
    //   public static String datasetName = "anno_2011_rnd_converted_normalized";
//   public static String datasetName = "kddcup1999_binario_test";
    //   public static String datasetName = "kddcup1999_rand";
    // public static String datasetName = "kddcup1999";
    // public static String datasetName = "synthetic_tree_dataset_large";
    //   public static String datasetName = "elecNormNew";   
    //  public static String datasetName = "spamdataset";
    //public static String datasetName = "Test_6_tree_2_cls_150_dim_3_bal_160_leaves_0.85_beta_1_hmm_21_max_dep_1_ril_feat";
    // public static String datasetName = "covtypeNorm_binario";    
    // public static String datasetName = "covtypeNorm_binario_V2";
    // public static String datasetName = "Test_1_tree_2_cls_80_dim_1_bal_400_leaves_100_beta_0_hmm_11_max_dep_1_ril_feat";
    //   public static ArffFileStream stream = new ArffFileStream(GS.pathTree + datasetName + ".arff", -1);
    //  public static ArffFileStream stream = new ArffFileStream(GS.pathMulticlasse + datasetName + ".arff", -1);
    
    public static String datasetName=GS.datasetName;
    
    //public static ArffFileStream stream = new ArffFileStream(GS.path + datasetName + ".arff", -1);
 
    public static ArffFileStream stream = null;
    
    public static ArrayList<ClassifierTest> all_tests = new ArrayList<ClassifierTest>();
    private static int PLOT_TYPE = GS.PLOT_TYPE;

   static NumberFormat  nf = NumberFormat.getInstance();
    
    public DecisionTreeTester() {
        super("Classifier Comparisons");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocation(50, 50);

        //JPanel mainView = new JPanel(new BorderLayout());
        //mainView.setPreferredSize(new Dimension(800, 600));
        DrawableChart dc = new DrawableChart();
        dc.setPreferredSize(new Dimension(800, 600));
        //mainView.add(dc, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        //sidePanel.setPreferredSize(new Dimension(250, 600));

        for (ClassifierTest ct : all_tests) {
            JLabel testLabel = new JLabel(ct.name);

            testLabel.setPreferredSize(new Dimension(250, 30));
            //testLabel.setOpaque(true);
            //testLabel.setBackground(Color.RED);
            testLabel.setSize(250, 30);
            testLabel.setMinimumSize(new Dimension(250, 30));


            JPanel singleMatrix = new JPanel(new GridLayout(3, 3));
            //singleMatrix.setPreferredSize(new Dimension(250, 250));
            singleMatrix.setMaximumSize(new Dimension(250, 100));
            int[][] matrix = ct.results.get(ct.results.size() - 1).getMatrix();
            singleMatrix.add(new JLabel());
            singleMatrix.add(new JLabel("Real Neg"));
            singleMatrix.add(new JLabel("Real Pos"));
            singleMatrix.add(new JLabel("Pred Neg"));
            singleMatrix.add(new JLabel(Integer.toString(matrix[0][0])));
            singleMatrix.add(new JLabel(Integer.toString(matrix[0][1])));
            singleMatrix.add(new JLabel("Pred Pos"));
            singleMatrix.add(new JLabel(Integer.toString(matrix[1][0])));
            JLabel tpLabel = new JLabel(Integer.toString(matrix[1][1]));
            tpLabel.setOpaque(true);
            tpLabel.setBackground(Color.GREEN);
            singleMatrix.add(tpLabel);

            //  sidePanel.add(testLabel);
            // sidePanel.add(singleMatrix);
        }
        sidePanel.add(new JTextArea(matrixToFile));
        JScrollPane extSidePanel = new JScrollPane(sidePanel);
        extSidePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        extSidePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(dc, BorderLayout.CENTER);
        this.add(extSidePanel, BorderLayout.EAST);
        pack();
        dc.init();



    }
    
    
    public static void routine_generate_all_dataset_csv_performances(String direct) {
        
        //String experiment_name="Dataset_Reali";  
        String experiment_name=direct;
        String experiment_dir=experiment_name;   
        
        System.out.println(experiment_name);
        
        
        GS.BASE_IMAGE_PATH = GS.BASE_IMAGE_PATH + "\\" + experiment_name;
        //GS.BASE_IMAGE_PATH = GS.BASE_IMAGE_PATH + "/" + experiment_name;
        
        File dirFile = new File(GS.BASE_IMAGE_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        } 
        
        GS.set_GS_values=1;
        
        File folder = new File(GS.path+"\\"+experiment_dir);
        File[] datasets = folder.listFiles();
        
        for (File dataset : datasets) {
            
            System.gc();
            GS.print_file_window_performances = 0;
            GS.INSTANCE_LIMIT=10000000;
            datasetName = dataset.getName();
            
            if (datasetName.equals("kddcup1999_binario_test.arff"))            {
             
                GS.type_performances="fmeasure0";
                
            }else if  ((datasetName.contains("a9a"))||(datasetName.contains("w8a"))||(datasetName.contains("ijcnn1_rand"))||(datasetName.contains("cod-rna_rand")))  
            
                GS.type_performances="fmeasure1";
                
            else {
                
                GS.type_performances="acc";                
            }
            
            System.out.println(dataset.getPath());
            stream = new ArffFileStream(dataset.getPath(), -1);
            
            GS.completeDir=GS.BASE_IMAGE_PATH;
            routine_delta(0);
            routine_delta(1);
            routine_c_bound();
            
            //SS only for real dataset
           // if (experiment_name.equals("reali")){
            
                GS.print_file_window_performances = 1;
                routine_dataset_csv();
                //GS.print_file_window_performances = 0;            
                //routine_selective_sampling();
                
            //}
            
            
        }


    }
    
    
    

    public static void routine_all_dataset_csv(String[] args) {

        GS.print_file_window_performances = 1;

        File folder = new File(GS.pathTree);
        File[] datasets = folder.listFiles();

        String exp_name = "Dataset_Reali";

        for (int i = 0; i < datasets.length; i++) {

            datasetName = datasets[i].getName();

            stream = new ArffFileStream(datasets[i].getPath(), -1);

            //Hoeffding full
            TransparentHoeffdingTree hoeffdingClassifier = new TransparentHoeffdingTree();

            //McDiarmid full
            HoeffCesaBoundTree cesa = new HoeffCesaBoundTree();

            //Selective sampling McDiarmid
            //SelectiveSamplingHoeffdingTree ss_rocco = new SelectiveSamplingHoeffdingTree(GS.DEFAULT_SEED, GS.SS_C1);


            //Hoeff full
            all_tests.add(new ClassifierTest(
                    hoeffdingClassifier.getName(),
                    hoeffdingClassifier,
                    0.5,
                    0, 0, 255,
                    PLOT_TYPE, datasetName));

            //Full McDiarmid
            all_tests.add(new ClassifierTest(
                    cesa.getName(),
                    cesa,
                    0.5,
                    0, 246, 0,
                    PLOT_TYPE, datasetName));

            //SS
           /* all_tests.add(new ClassifierTest(
             ss_rocco.getName(),
             ss_rocco,
             0.5,
             230, 0, 0,
             PLOT_TYPE,datasetName));*/

            for (ClassifierTest test : all_tests) {

                prepareDir3(exp_name, test.name);

                test.datasetname = datasetName;

                test.doTest(stream);
            }


        }


    }

    public static void routine_dataset_csv() {

        
        GS.print_file_window_performances = 1;

        //Hoeffding full
        TransparentHoeffdingTree hoeffdingClassifier = null;

        //McDiarmid full
        HoeffCesaBoundTree cesa = null;
        
        //Correct Hoeff full
        CorrectHoeffTree  corrHoeff = null;

        
        //first lunch McDiarmid for correct calculate SS Istance Limit
        
           //Full McDiarmid
          
          cesa = new HoeffCesaBoundTree();
          
        ClassifierTest test2 = new ClassifierTest(
                cesa.getName(),
                cesa,
                0.5,
                0, 246, 0,
                PLOT_TYPE, datasetName);
       
      
        prepareDir3("Istances_Exp", test2.name);

        test2.doTest(stream);

        
          hoeffdingClassifier = new TransparentHoeffdingTree();
        
        ClassifierTest test1 = new ClassifierTest(
                hoeffdingClassifier.getName(),
                hoeffdingClassifier,
                0.5,
                0, 0, 255,
                PLOT_TYPE, datasetName);

        
        prepareDir3("Istances_Exp", test1.name);
        test1.doTest(stream);
        
        
        corrHoeff = new CorrectHoeffTree();
        
        ClassifierTest test3 = new ClassifierTest(
                corrHoeff.getName(),
                corrHoeff,
                0.5,
                255, 0, 0,
                PLOT_TYPE, datasetName);

        
        prepareDir3("Istances_Exp", test3.name);
        test3.doTest(stream);
        
        

    }

    public static void routine1(String[] args) {

        stream = new ArffFileStream(GS.path + datasetName + ".arff", -1);
        
        all_tests.clear();


        //double[] ts = new double[]{0.89, 0.915, 0.94, 0.965, 0.99};

        NaiveBayesLog bayesClassifier = new NaiveBayesLog(0.05);

        //Hoeffding full
        TransparentHoeffdingTree hoeffdingClassifier = new TransparentHoeffdingTree();

        //Nostro full
        HoeffCesaBoundTree cesa = new HoeffCesaBoundTree();

        //McDiarmid full
        McDiarmidBoundTree mcDiard = new McDiarmidBoundTree();

        //correct bound full
        CorrectHoeffTree corrHoeff = new CorrectHoeffTree();
        
        
        
        //Selective sampling McDiarmid
        SelectiveSamplingHoeffdingTree ss_rocco = new SelectiveSamplingHoeffdingTree(GS.DEFAULT_SEED, GS.SS_C1);

        //Selective sampling McDiarmid V3
        //SelectiveSamplingHoeffdingTreeV3 ss_rocco_v3 = new SelectiveSamplingHoeffdingTreeV3(GS.DEFAULT_SEED, GS.SS_C1);



        //Selective sampling McDiarmid Ver. 2 (Delta sensitive)
        //SelectiveSamplingHoeffdingTreeV2 ss_rocco_v2 = new SelectiveSamplingHoeffdingTreeV2(GS.DEFAULT_SEED, GS.SS_C1, GS.SS_C2);

        //filtering ha problemi di ordinamento di classe
        //Filtering V1   
        //FilteringHoeffdingTreeV1 ff_rocco_v1=new FilteringHoeffdingTreeV1(GS.DEFAULT_SEED, GS.SS_C1,GS.positive_class_index );

        //Filtering V2
        //FilteringHoeffdingTreeV2 ff_rocco_v2=new FilteringHoeffdingTreeV2(GS.DEFAULT_SEED, GS.SS_C1,GS.SS_C2,GS.positive_class_index);


      /*  all_tests.add(new ClassifierTest(
         bayesClassifier.getName(),
         bayesClassifier,
         0.5,
         100, 0, 250,
         PLOT_TYPE));
        */
        
        
       /* //SS
        all_tests.add(new ClassifierTest(
                ss_rocco.getName(),
                ss_rocco,
                0.5,
                230, 0, 0,
                PLOT_TYPE));
        */
        
       /* //McDiarmid full
        all_tests.add(new ClassifierTest(
                mcDiard.getName(),
                mcDiard,
                0.5,
                255, 0, 0,
                PLOT_TYPE));
        */
        
        //Full Nostro
        all_tests.add(new ClassifierTest(
                cesa.getName(),
                cesa,
                0.5,
                0, 246, 0,
                PLOT_TYPE));
        
        
       //Hoeff full
        all_tests.add(new ClassifierTest(
                hoeffdingClassifier.getName(),
                hoeffdingClassifier,
                0.5,
                0, 0, 255,
                PLOT_TYPE));

        //Corr Hoeff full
        all_tests.add(new ClassifierTest(
                corrHoeff.getName(),
                corrHoeff,
                0.5,
                255, 255, 0,
                PLOT_TYPE));
        

        

        /*
        
         //Full McDiarmid V3 SS
         all_tests.add(new ClassifierTest(
         ss_rocco_v3.getName(),
         ss_rocco_v3,
         0.5,
         100, 0, 250,
         PLOT_TYPE));
        */
      




        /*
         //SS Ver. 2
         all_tests.add(new ClassifierTest(
         ss_rocco_v2.getName(),
         ss_rocco_v2,
         0.5,
         240, 0, 0,
         PLOT_TYPE));

         */

        //Filtering V1
      /*  all_tests.add(new ClassifierTest(
         ff_rocco_v1.getName(),
         ff_rocco_v1,
         0.5,
         240, 100, 0,
         PLOT_TYPE));


         //Filtering V2
         all_tests.add(new ClassifierTest(
         ff_rocco_v2.getName(),
         ff_rocco_v2,
         0.5,
         0, 150, 255,
         PLOT_TYPE));
         * /

    
         /*        all_tests.add(new ClassifierTest(
         "bayes",
         bayesClassifier,
         0.5,
         246,0,0,
         PLOT_TYPE));
         /*
                
         for (int i = 0; i < 3; i++) {
         SelectiveSamplingHoeffdingTreeV2 selClassifierV2 = 
         new SelectiveSamplingHoeffdingTreeV2(
         new double[] {GS.SEL_HOEFF_C, (double)(i+3)});
         selClassifierV2.binarySplitsOption.setValue(true);
         selClassifierV2.leafpredictionOption.setValueViaCLIString("MC");
            
         all_tests.add(new ClassifierTest(
         selClassifierV2.getName(), 
         selClassifierV2, 
         0.5, -1, 
         0, 85*(i+1), 0, 
         1));
         }
         * 
         */


        /*
         double[] deltas = new double[] {0.9, 0.0001, 0.0000001, 0.0000000001};
        
         for (double delta : deltas) {
         MyHoeffdingTree ht = new MyHoeffdingTree();
         ht.splitConfidenceOption.setValue(delta);
         ht.prepareForUse();
            
         all_tests.add(new ClassifierTest(
         "Hoeffding Full Sampling", 
         ht, 
         0.5, 10000, 
         255, 0, 0));
         }
         */

        /*
                
         all_tests.add(new ClassifierTest(
         "Hoeffding Filtering (seed=100000)", 
         hoeffdingClassifier, 
         0.5, 100000, 
         200, 0, 255));
        
         * 
         */
        /*
         all_tests.add(new HoeffdingClassifierTest(
         "Hoeffding Filtering (seed=1000)", 
         hoeffdingClassifier, 
         0.5, 1000, 
         0, 255, 0)); 
         */

        /*
         for (double t : ts) {
         all_tests.add(new ClassifierTest(
         String.format("NaiveBayes (T = %1$.2f)", t), 
         bayesClassifier, 
         t, -1, 
         255.0f - 255.0f*(float)t, 0, 255.0f*(float)t));
         }
         */

        prepareDir1();

        for (ClassifierTest test : all_tests) {

            test.doTest(stream);

            System.out.println("Accuracy:" + test.results.get(test.results.size() - 1).getAccuracy());

            System.out.println("F_measure_class_0:" + test.results.get(test.results.size() - 1).getFMeasure(0));

            System.out.println("F_measure_class_1:" + test.results.get(test.results.size() - 1).getFMeasure(1));


            if (test.classifier instanceof TransparentHoeffdingTree) {
                //System.out.println(hoeffdingClassifier.measureByteSize());
                TransparentHoeffdingTree ht = (TransparentHoeffdingTree) test.classifier;

                System.out.println("Profondità albero:" + ht.getDepth());
                System.out.println("Nodi:" + ht.getNodeCount());
                System.out.println("Foglie:" + ht.getLeafCount());
                System.out.println("Query:" + ht.getQueryNumber());
                
                

            }else{
            
               NaiveBayesLog ht = (NaiveBayesLog) test.classifier; 
                
               System.out.println("Query:" + ht.getQueryNumber());
            }
        
        }
        

        //cesa.printTree();
        System.out.println("----------------------");
        //selClassifier.printTree();
        //selClassifier2.printTree();
        //selClassifier.printCounts();

        //PApplet.main(new String[] {"moaclassifierrocco.gui.DrawableChart"});

        //     System.out.println("Peso alle foglie: "+ hoeffdingClassifier.calcLeafWeight());

       // new DecisionTreeTester().setVisible(true);

    }

    public static void routine_selective_sampling() {

        String fileContent = "";
        
        String fileContent_2 = "";
        
        double acc_sat_95=GS.best_rocco_acc_SS*.98;
        double acc_sat_90=GS.best_rocco_acc_SS*.95;

        int num_test = 5;

        int test_count = 0;
        
        int saturation_count = 0;
        int saturation_count_95 = 0;
        int saturation_count_90 = 0;

        double saturation_100=0;
        double saturation_95=0;
        double saturation_90=0;
        
        double continutity_count_100=0;
        double continutity_count_95=0;
        double continutity_count_90=0;
        
      //  double[] c_vars={1,1.5,2,2.5,3,3.5,4,4.5,5,10,15,20,25,30,40,50};
        
      //  for (double c_var : c_vars){
        
       GS.INSTANCE_LIMIT=GS.INSTANCE_SS;
        
      int max_c=150;
       
       double best_mean_acc=0;
            
       double best_mean_nodes=0;
      
       for (double c_var = .25; c_var <= max_c; c_var +=.25) {

            all_tests.clear();

            for (int tt = 1; tt <= num_test; tt++) {

                SelectiveSamplingHoeffdingTree selClassifier = new SelectiveSamplingHoeffdingTree(GS.DEFAULT_SEED, (double) c_var);

                all_tests.add(new ClassifierTest(
                        selClassifier.getName(),
                        selClassifier,
                        0.5,
                        0, 0, 255,
                        PLOT_TYPE));

            }
            
            

            test_count++;

            double mean_acc=0;
            
            double mean_nodes=0;
            
            double current_saturation=0;
            
            for (ClassifierTest test : all_tests) {
                
                test.doTest(stream);
                SelectiveSamplingHoeffdingTree t = (SelectiveSamplingHoeffdingTree) test.classifier;
                                
                 String line = null;
                
                 current_saturation=(double) ( ((double)t.getQueryNumber())/((double)GS.INSTANCE_SS));
                 
                if (GS.type_performances.equals("acc")) {

                    line = String.format("%d;%.1f;%.4f;%.4f;%d;%d;%.4f;%.4f\n",
                        test_count,
                        t.getSelNumerator(),
                        test.results.get(test.results.size() - 1).getAccuracy(),                      
                        current_saturation,
                        t.getDepth(), (int) t.getNodeCount(),  GS.best_base_acc_SS,  GS.best_rocco_acc_SS);
                    
                    mean_acc=mean_acc+test.results.get(test.results.size() - 1).getAccuracy();

                    
                    
                } else if (GS.type_performances.equals("fmeasure0")) {

                    line = String.format("%d;%.1f;%.4f;%.4f;%d;%d;%.4f;%.4f\n",
                        test_count,
                        t.getSelNumerator(),                       
                       test.results.get(test.results.size() - 1).getFMeasure(0),
                        current_saturation,
                        t.getDepth(), (int) t.getNodeCount(), GS.best_base_acc_SS,GS.best_rocco_acc_SS);
                    
                    mean_acc=mean_acc+test.results.get(test.results.size() - 1).getFMeasure(0);
                    
                } else if (GS.type_performances.equals("fmeasure1")) {

                    line = String.format("%d;%.1f;%.4f;%.4f;%d;%d;%.4f;%.4f\n",
                        test_count,
                        t.getSelNumerator(),                       
                       test.results.get(test.results.size() - 1).getFMeasure(1),
                        current_saturation,
                        t.getDepth(), (int) t.getNodeCount(),GS.best_base_acc_SS,GS.best_rocco_acc_SS);

                     mean_acc=mean_acc+test.results.get(test.results.size() - 1).getFMeasure(1);
                    
                }          
                
                fileContent += line;  
                                
                mean_nodes=mean_nodes+(int) t.getNodeCount();
                
            }
            
            
           mean_acc = mean_acc / num_test;
           
           mean_nodes = mean_nodes / num_test;
                     
           //Saturation 95%
           if ((mean_acc >= acc_sat_90) && (saturation_count_90 <= 2)) {

               if (saturation_count_90 == 0) {
                   
                   saturation_90 = current_saturation;                   
                   saturation_count_90++;
                   
               } else {

                   if ((continutity_count_90 + 1) != test_count) {
                       saturation_count_90 = 1;
                       saturation_90 = current_saturation;
                       
                   } else {
                       saturation_count_90++;
                       
                   }

               }
               
               continutity_count_90 = test_count;

           }
           
           //Saturation 98%
           if ( (mean_acc >= acc_sat_95) && (saturation_count_95<=2) ) {
               
              if (saturation_count_95 == 0) {
                   
                   saturation_95 = current_saturation;                   
                   saturation_count_95++;
                   
               } else {

                   if ((continutity_count_95 + 1) != test_count) {
                       saturation_count_95 = 1;
                       saturation_95 = current_saturation;
                       
                   } else {
                       saturation_count_95++;
                       
                   }

               }
               
               continutity_count_95 = test_count;
               
           }
                     
           
           //LAST SATURATION
           if ( (mean_acc >= (GS.best_rocco_acc_SS)) || (c_var==max_c) ) {
            
               if (saturation_count == 0) {
                   
                   saturation_100 = current_saturation;      
                   best_mean_nodes = mean_nodes;                
                   saturation_count++;
                   
               } else {

                   if ((continutity_count_100 + 1) != test_count) {
                       saturation_count = 1;
                       saturation_100 = current_saturation;
                        best_mean_nodes = mean_nodes; 
                       
                   } else {
                       saturation_count++;
                       
                   }

               }
               
               continutity_count_100 = test_count;               
               
               if ((saturation_count>=3) || (c_var==max_c) )  {             
                
                   best_mean_acc = mean_acc;                    
                   
                   fileContent_2=String.format("%.4f;%.4f;%.4f;%d;%d;%d;%.4f;%.4f;%.4f\n",
                        GS.best_base_acc_SS,GS.best_rocco_acc_SS,best_mean_acc,(int)GS.best_base_nodes_SS,(int)GS.best_rocco_nodes_SS,(int)Math.floor(best_mean_nodes),saturation_90,saturation_95,saturation_100);
                   
                   break;
               }
               
               
               
           }
                       

        }

        

        try {
            
            prepareDir2();
            
            File file = new File(GS.completeDir + "/SS_istances_" + GS.INSTANCE_LIMIT + "_" + datasetName + "_seed_" + GS.DEFAULT_SEED + ".csv");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent.replace(".", ","));
            output.close();
            
            prepareDir2_bis();
            
            File file2 = new File(GS.completeDir + "/SS_saturation_" + GS.INSTANCE_LIMIT + "_" + datasetName + "_seed_" + GS.DEFAULT_SEED + ".csv");
            BufferedWriter output2 = new BufferedWriter(new FileWriter(file2));
            output2.write(fileContent_2);
            output2.close();
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void routine_delta(int typeclassifier) {

        String fileContent = "";
        
        String class_name="Hoeff";
              
        if (typeclassifier == 0)
            class_name="CorrHoeff";
        
        int num_test = 1;

        int test_count = 0;

        TransparentHoeffdingTree selClassifier = null;

        double[] deltas = {Math.pow(10, -90),Math.pow(10, -80),Math.pow(10, -70),Math.pow(10, -60),Math.pow(10, -50),Math.pow(10, -40),Math.pow(10, -30),Math.pow(10, -20),Math.pow(10, -19),Math.pow(10, -18),Math.pow(10, -17),Math.pow(10, -16),Math.pow(10, -15),Math.pow(10, -14),Math.pow(10, -13),Math.pow(10, -12), Math.pow(10, -11), Math.pow(10, -10), Math.pow(10, -9), Math.pow(10, -8), Math.pow(10, -7), Math.pow(10, -6), Math.pow(10, -5), Math.pow(10, -4), Math.pow(10, -3), Math.pow(10, -2), Math.pow(10, -1),.15,.16,.17,.18,.19,.2,.21,.22,.23,.24,.25,.26,.27,.28,.29,.3,.31,.32,.33,.34,.35,.36,.37,.38,.39,.4,.41,.42,.43,.44,.45,.46,.47,.48,.49,.5,.51,.52,.53,.54,.55,.56,.57,.58,.59,.6,.61,.62,.63,.64,.65,.66,.67,.68,.69,.7,.71,.72,.73,.74,.75,.76,.77,.78,.79,.8,.81,.82,.83,.84,.85,.86,.87,.88,.89,.9,.91,.92,.93,.94,.95,.96,.97,.98,.845,.855,.865,.875,.885,.895,.955,.915,.925,.935,.945,.955,.965,.975,.985,.99,.995,.999};
       
        //double[] deltas = {Math.pow(10, -40),Math.pow(10, -30),.965};
        
        //double[] deltas = {Math.pow(10, -60),Math.pow(10, -50)};
        
        //double[] deltas = {.1,.2,.3,-1,-2,-3,-4,-5,-6,-7,-8};
        
        double best_delta=1;
        double best_perf=-1;
        double current_perf=-1;
        
        //for (double delta = 0.7; delta <= 1; delta += 0.006) {
        for (double delta : deltas) {

            all_tests.clear();

            GS.delta = delta;

            for (int tt = 1; tt <= num_test; tt++) {

                
                if (typeclassifier == 0) {
                    //corrected hoeff tree
                    selClassifier = new CorrectHoeffTree();                    
                } else {
                    //hoeff tree
                    selClassifier = new TransparentHoeffdingTree();
                }

                all_tests.add(new ClassifierTest(
                        selClassifier.getName(),
                        selClassifier,
                        0.5,
                        0, 0, 255,
                        PLOT_TYPE));

            }

            test_count++;

            for (ClassifierTest test : all_tests) {
                
                test.doTest(stream);
                
                TransparentHoeffdingTree t = (TransparentHoeffdingTree) test.classifier;
                
                String line = null;
                
                if (GS.type_performances.equals("acc")) {

                    line = String.format("%d;%.50f;%.4f;%d;%d;%d;%.4f\n",
                            test_count,
                            GS.delta,
                            test.results.get(test.results.size() - 1).getAccuracy(),
                            t.getQueryNumber(),
                            t.getDepth(), (int) t.getLeafCount(),t.calculateError());
                    
                    current_perf= test.results.get(test.results.size() - 1).getAccuracy();

                } else if (GS.type_performances.equals("fmeasure0")) {

                    line = String.format("%d;%.50f;%.4f;%d;%d;%d;%.4f\n",
                            test_count,
                            GS.delta,
                            test.results.get(test.results.size() - 1).getFMeasure(0),
                            t.getQueryNumber(),
                            t.getDepth(), (int) t.getLeafCount(),t.calculateError());
                    
                    current_perf= test.results.get(test.results.size() - 1).getFMeasure(0);
                    
                } else if (GS.type_performances.equals("fmeasure1")) {

                    line = String.format("%d;%.50f;%.4f;%d;%d;%d;%.4f\n",
                            test_count,
                            GS.delta,
                            test.results.get(test.results.size() - 1).getFMeasure(1),
                            t.getQueryNumber(),
                            t.getDepth(), (int) t.getLeafCount(),t.calculateError());
                    
                    current_perf= test.results.get(test.results.size() - 1).getFMeasure(1);

                }          
                
                fileContent += line;               
                
                
                nf.setMaximumFractionDigits(2);
                nf.setGroupingUsed(false);
                nf.setRoundingMode(RoundingMode.FLOOR);

                current_perf = Double.parseDouble(nf.format(current_perf).replace(',','.'));
                                
                if (current_perf > best_perf) {

                    best_perf = current_perf;
                    if (typeclassifier == 0) {
                        best_delta = GS.delta_corrHoff;
                    } else {
                        best_delta = GS.delta;
                    }

                }
                
            }

        }
        
        
        //!!!! da rimettere
        prepareDir4(selClassifier.getName(),typeclassifier);
        
        
        try {
            //File file = new File(GS.completeDir + "/delta_" + best_delta + "_" + datasetName + ".csv");
            File file = new File(GS.completeDir + "\\delta_" + "type_" + class_name + "_" + best_delta + "_" + datasetName + ".csv");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent.replace(",", ".").replace(";", ","));            
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (GS.set_GS_values==1){
        
            GS.delta=best_delta;
            
            if (typeclassifier == 0) {
                GS.delta_corrHoff = best_delta;
            } else {
                GS.delta = best_delta;
            }            
            
        }
        
    }

    public static void routine_c_bound() {

        String fileContent = "";

        int num_test = 1;

        int test_count = 0;

        TransparentHoeffdingTree selClassifier = null;

        double best_c=1;
        double best_perf=-1;
        double current_perf=-1;
      
                
       for (double c_bound = 0.00005; c_bound <= .15; c_bound += 0.001) {

        //for (double c_bound = 0.05; c_bound <= .1; c_bound += 0.05) {
        
            all_tests.clear();

            GS.c_bound = c_bound;

            for (int tt = 1; tt <= num_test; tt++) {

                selClassifier = new HoeffCesaBoundTree();

                all_tests.add(new ClassifierTest(
                        selClassifier.getName(),
                        selClassifier,
                        0.5,
                        0, 0, 255,
                        PLOT_TYPE));

            }

            test_count++;

            for (ClassifierTest test : all_tests) {
                               
                test.doTest(stream);
                TransparentHoeffdingTree t = (TransparentHoeffdingTree) test.classifier;
                
                
                String line = null;
                
                if (GS.type_performances.equals("acc")) {

                    line = String.format("%d;%.50f;%.4f;%d;%d;%d;%.4f\n",
                            test_count,
                            GS.c_bound,
                            test.results.get(test.results.size() - 1).getAccuracy(),
                            t.getQueryNumber(),
                            t.getDepth(), (int) t.getLeafCount(),t.calculateError());
                    
                    current_perf= test.results.get(test.results.size() - 1).getAccuracy();

                } else if (GS.type_performances.equals("fmeasure0")) {

                    line = String.format("%d;%.50f;%.4f;%d;%d;%d;%.4f\n",
                            test_count,
                            GS.c_bound,
                            test.results.get(test.results.size() - 1).getFMeasure(0),
                            t.getQueryNumber(),
                            t.getDepth(), (int) t.getLeafCount(),t.calculateError());
                    
                    current_perf= test.results.get(test.results.size() - 1).getFMeasure(0);
                    
                } else if (GS.type_performances.equals("fmeasure1")) {

                    line = String.format("%d;%.50f;%.4f;%d;%d;%d;%.4f\n",
                            test_count,
                            GS.c_bound,
                            test.results.get(test.results.size() - 1).getFMeasure(1),
                            t.getQueryNumber(),
                            t.getDepth(), (int) t.getLeafCount(),t.calculateError());
                    
                    current_perf= test.results.get(test.results.size() - 1).getFMeasure(1);

                }          
                
                fileContent += line;               
               
                nf.setMaximumFractionDigits(2);
                nf.setGroupingUsed(false);
                nf.setRoundingMode(RoundingMode.FLOOR);

                current_perf = Double.parseDouble(nf.format(current_perf).replace(',','.'));
                
                if (current_perf >= best_perf) {

                    best_perf = current_perf;
                    best_c = GS.c_bound;

                }
                                
            }


        }

        //!!!! da rimettere
        prepareDir5(selClassifier.getName());

        try {
            //File file = new File(GS.completeDir + "/cbound_" + best_c + "_" + datasetName + ".csv");
            File file = new File(GS.completeDir + "\\cbound_" + best_c + "_" + datasetName + ".csv"); 
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent.replace(",", ".").replace(";", ","));            
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (GS.set_GS_values==1){
        
            GS.c_bound=best_c;
        
        }

    }

    
    public static void routine4(String[] args) {


        for (double c_var = 1.0; c_var <= 20.0; c_var += 0.5) {

            FilteringHoeffdingTreeV1 filtClassifier = new FilteringHoeffdingTreeV1(GS.DEFAULT_SEED, (double) c_var, GS.positive_class_index);

            all_tests.add(new ClassifierTest(
                    filtClassifier.getName(),
                    filtClassifier,
                    0.5,
                    0, 0, 255,
                    PLOT_TYPE));
        }

        prepareDir2();

        String fileContent = "";

        for (ClassifierTest test : all_tests) {
            test.doTest(stream);
            FilteringHoeffdingTreeV1 t = (FilteringHoeffdingTreeV1) test.classifier;
            String line = String.format("%.1f;%.4f;%d;%d;%d\n",
                    t.getSelNumerator(),
                    test.results.get(test.results.size() - 1).getMCC(1),
                    t.getQueryNumber(),
                    t.getDepth(), (int) t.getNodeCount());
            fileContent += line;
        }

        try {
            File file = new File(GS.completeDir + "\\" + datasetName + ".csv");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void routine5(String[] args) {

        for (double c_var = 1.0; c_var <= 20.0; c_var += 0.5) {
            for (double c2_var = 3.0; c2_var <= 20.0; c2_var += 0.5) {
                FilteringHoeffdingTreeV2 filtClassifier = new FilteringHoeffdingTreeV2(
                        GS.DEFAULT_SEED, (double) c_var, (double) c2_var, GS.positive_class_index);

                all_tests.add(new ClassifierTest(
                        filtClassifier.getName(),
                        filtClassifier,
                        0.5,
                        0, 0, 255,
                        PLOT_TYPE));
            }
        }

        prepareDir2();

        String fileContent = "";

        for (ClassifierTest test : all_tests) {
            test.doTest(stream);
            FilteringHoeffdingTreeV2 t = (FilteringHoeffdingTreeV2) test.classifier;
            String line = String.format("%.1f;%.1f;%.4f;%d;%d;%d\n",
                    t.getSelNumerator(),
                    t.getSelDenominator(),
                    test.results.get(test.results.size() - 1).getMCC(1),
                    t.getQueryNumber(),
                    t.getDepth(), (int) t.getNodeCount()).replace(",", ".");
            fileContent += line;
        }

        try {
            File file = new File(GS.completeDir + "\\" + datasetName + ".csv");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(fileContent);
            output.close();
        } catch (IOException e) {
        }
    }

    public static class ClassifierTest {

        public String name;
        public Classifier classifier;
        public double threshold;
        public int filter;
        public ArrayList<MyConfusionMatrix> results;
        public float r;
        public float g;
        public float b;
        public int lineModule;
        public int lineLength;
        public int chartType;
        public String datasetname;

        public ClassifierTest(
                String name,
                Classifier classifier,
                double threshold,
                float r, float g, float b,
                int chartType) {
            this.name = name;
            this.classifier = classifier;
            this.threshold = threshold;
            //this.filter = filter;
            this.r = r;
            this.g = g;
            this.b = b;
            this.lineModule = -1;
            this.lineLength = -1;
            this.chartType = chartType;
        }

        public ClassifierTest(
                String name,
                Classifier classifier,
                double threshold,
                float r, float g, float b,
                int chartType, String datasetname) {
            this.name = name;
            this.classifier = classifier;
            this.threshold = threshold;
            //this.filter = filter;
            this.r = r;
            this.g = g;
            this.b = b;
            this.lineModule = -1;
            this.lineLength = -1;
            this.chartType = chartType;
            this.datasetname = datasetname;

        }

        public ClassifierTest(
                String name,
                Classifier classifier,
                double threshold,
                int filter,
                float r, float g, float b,
                int lm,
                int ll,
                int chartType) {
            this.name = name;
            this.classifier = classifier;
            this.threshold = threshold;
            this.filter = filter;
            this.r = r;
            this.g = g;
            this.b = b;
            this.lineModule = lm;
            this.lineLength = ll;
            this.chartType = chartType;
        }

        public void doTest(ArffFileStream stream) {

            System.out.print(name + "... ");
            classifier.prepareForUse();
            classifier.resetLearning();
            stream.restart();
            String[] classi = new String[stream.getHeader().classAttribute().numValues()];
            int s = stream.getHeader().classAttribute().numValues();
            for (int i = 0; i < s; i++) {
                classi[i] = ((stream.getHeader().classAttribute().value(i)).toString());
            }

            InterleavedTestThenTrainWithMatrix teste = new InterleavedTestThenTrainWithMatrix(classifier, stream, threshold, chartType, datasetname);

            this.results = teste.mainTask();

            //     System.out.println(teste.evaluation.toMatrixString());
            //   System.out.println(classi.toString());
            matrixToFile += "\n";
            matrixToFile += name + "\n";
            matrixToFile += teste.evaluation.toMatrixString("ConfusionMatrix", classi);
            System.out.println("Done!");
            //System.out.println(classifier.trainingWeightSeenByModel());

           
            //PLOT TREE
            /*if (stream.getHeader().numAttributes() == 3 && classifier instanceof TransparentHoeffdingTree) {
                new SquarePlotWindow((TransparentHoeffdingTree) classifier, name).setVisible(true);
            }*/
           
           
        }

        public double[][] parseForDrawingGeneral() {
            double[][] arrayResult = new double[results.size()][];
            for (int i = 0; i < results.size(); i++) {
                arrayResult[i] = new double[]{
                    results.get(i).getAccuracy(),
                    classifier instanceof TransparentHoeffdingTree
                    ? ((TransparentHoeffdingTree) classifier).errorProgression.get(i)
                    : 0,
                    classifier instanceof TransparentHoeffdingTree
                    ? ((TransparentHoeffdingTree) classifier).queryProgression.get(i)
                    : 0,
                    classifier instanceof TransparentHoeffdingTree
                    ? ((TransparentHoeffdingTree) classifier).numNodesProgression.get(i)
                    : 0
                };
            }
            return arrayResult;
        }

        public double[][][] parseForDrawingWithClass() {
            int numClasses = results.get(0).getMatrix().length;
            double[][][] arrayResult = new double[numClasses][results.size()][];
            for (int c = 0; c < numClasses; c++) {
                for (int i = 0; i < results.size(); i++) {
                    arrayResult[c][i] = new double[]{
                        results.get(i).getMCC(c),
                        results.get(i).getPrecision(c),
                        results.get(i).getRecall(c),
                        results.get(i).getFMeasure(c)
                    };
                }
            }
            return arrayResult;
        }
    }

    public static class HoeffdingClassifierTest extends ClassifierTest {

        public HoeffdingClassifierTest(
                String name,
                Classifier classifier,
                double threshold,
                int filter,
                float r, float g, float b,
                int chartType) {
            super(name, classifier, threshold, r, g, b, chartType);
        }

        @Override
        public void doTest(ArffFileStream stream) {
            classifier.prepareForUse();
            classifier.resetLearning();
            stream.restart();
            this.results = (new ModifiedInterleavedTestFilterThenTrainWithMatrix((HoeffdingTree) classifier, stream, threshold, chartType)).mainTask();

        }
    }

    public void createDescriptionFile(ClassifierTest test) {
        String output = "";
        output += test.results.get(test.results.size() - 1).toString();

    }
    /*
     public class MyArffFileStream extends ArffFileStream {
        
     @Override
     public Instance nextInstance() {
            
     }
     }
     *
     */

    public static class SquarePlotWindow extends JFrame {

        public DrawableTree dt = null;

        public SquarePlotWindow(TransparentHoeffdingTree tree, String name) {
            super(name);

            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(false);
            setLocation(50, 50);

            //JPanel mainView = new JPanel(new BorderLayout());
            //mainView.setPreferredSize(new Dimension(800, 600));
            dt = new DrawableTree();
            dt.initDrawableTree(tree, name);
            dt.setPreferredSize(new Dimension(GS.RPLOT_WIDTH, GS.RPLOT_HEIGHT));

            this.add(dt, BorderLayout.CENTER);
            pack();

            dt.init();
        }
    }

    public static boolean prepareDir1() {

        ArrayList<ClassifierTest> tests = DecisionTreeTester.all_tests;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String datefolder = df.format(date);

        String classifierFolder = tests.get(0).name;

        for (int i = 1; i < tests.size(); i++) {
            classifierFolder = classifierFolder + "_vs_" + tests.get(i).name;
        }

        String typeID = String.format("_[type-%d]", tests.get(0).chartType);

        String delta = String.format("_[delta-%.8f]", GS.delta);
        
        String delta_CorrHoeff = String.format("_[deltaCorrHoeff-%.8f]", GS.delta_corrHoff);

        String c_bound = String.format("_[c_bound-%.8f]", GS.c_bound);

        GS.completeDir = GS.BASE_IMAGE_PATH + datefolder + "\\"
                + DecisionTreeTester.datasetName + "\\" + classifierFolder + typeID + delta + delta_CorrHoeff + c_bound;
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }
    }

    public static boolean prepareDir2() {

        ArrayList<ClassifierTest> tests = DecisionTreeTester.all_tests;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String datefolder = df.format(date);

        String classifierFolder = tests.get(0).name;

        /*
         for (int i = 1; i < tests.size(); i++) {
         classifierFolder = classifierFolder + "_vs_" + tests.get(i).name;
         }
        
         String typeID = String.format("_[type-%d]", tests.get(0).chartType);
         */


        GS.completeDir = GS.BASE_IMAGE_PATH + "/SS_C";
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }

    }
    
     public static boolean prepareDir2_bis() {

        ArrayList<ClassifierTest> tests = DecisionTreeTester.all_tests;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String datefolder = df.format(date);

        String classifierFolder = tests.get(0).name;

        /*
         for (int i = 1; i < tests.size(); i++) {
         classifierFolder = classifierFolder + "_vs_" + tests.get(i).name;
         }
        
         String typeID = String.format("_[type-%d]", tests.get(0).chartType);
         */


        GS.completeDir = GS.BASE_IMAGE_PATH + "/SS_Saturation";
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }

    }

    public static boolean prepareDir3(String exp_name, String classifier_name) {

        GS.completeDir = "";

        ArrayList<ClassifierTest> tests = DecisionTreeTester.all_tests;

        GS.completeDir = GS.BASE_IMAGE_PATH + "\\"
                + exp_name + "\\" + classifier_name + "\\";
        File dirFile = new File(GS.completeDir);

        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }

    }

    public static boolean prepareDir4(String classifier_name,int type) {

        ArrayList<ClassifierTest> tests = DecisionTreeTester.all_tests;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String datefolder = df.format(date);

        String classifierFolder = tests.get(0).name;

        if (type == 1) {
            GS.completeDir = GS.BASE_IMAGE_PATH + "/Delta_Hoeff_Exp";
        } else {
            GS.completeDir = GS.BASE_IMAGE_PATH + "/Delta_CorrHoff_Exp";
        }
            
        
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }

    }

    public static boolean prepareDir5(String classifier_name) {

        ArrayList<ClassifierTest> tests = DecisionTreeTester.all_tests;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String datefolder = df.format(date);

        String classifierFolder = tests.get(0).name;

        GS.completeDir = GS.BASE_IMAGE_PATH + "/C_Bound_Exp";
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }

    }

    public static void main(String[] args) {
       
        /*
        final int trials = 161750;
        final int successes = 10007;
        final double alpha = 0.05d;

// the supplied precision is the default precision according to the source code
        BetaDistribution betaDist = new BetaDistribution(successes + 1, trials - successes + 1, 1e-9);

        System.out.println("2.5 percentile :" + betaDist.inverseCumulativeProbability(alpha / 2d));
        System.out.println("mean: " + betaDist.getNumericalMean());
        System.out.println("median: " + betaDist.inverseCumulativeProbability(0.5));
        System.out.println("97.5 percentile :" + betaDist.inverseCumulativeProbability(1 - alpha / 2d));
        */
                
        String exp_dir = null;

        if (args.length == 0) {
            exp_dir = "reali";
        } else {
            exp_dir = args[0];
        }
        
        routine_generate_all_dataset_csv_performances(exp_dir);
        
        
        
        
              
        //routine1(args);
        // print();

        
        //stream = new ArffFileStream(GS.path + datasetName + ".arff", -1);
        //GS.completeDir = GS.path;
        //routine_delta(1);
        //routine_delta(0);
        //routine_c_bound();
        
        
       //routine_delta(1);
     
  //    routine_c_bound();
        
     // routine_dataset_csv();
 
   //  routine_selective_sampling();
      
        
       /*   
         try {
         Thread.sleep(1000);

         } catch (InterruptedException ex) {
      
         }
         routine_delta(0);
     
         */

                //SS V1
        
        //complexity dimension
        //  routine_all_dataset_csv(args);



    }

    public static void print() {
        try {

            File file = new File(GS.completeDir + "/Confusion_Matrix.csv");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(matrixToFile);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
