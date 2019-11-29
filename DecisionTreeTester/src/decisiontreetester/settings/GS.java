/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.settings;

/**
 * This class defines many common fields to all classes
 *
 * @author Rocco De Rosa
 */
public class GS {
    
    
    // CLASSIFICATION PARAMETERS
    

    
    //data
  
    
    
    
    // GUI PARAMETERS
    
    public static final int H_SIZE = 1600;
    public static final int V_SIZE = 900;
    
    public static final int H_PAD = 100;
    public static final int V_PAD = 100;
    public static final int V_SPLIT = 700;    
    public static final int CHART_H_TICKS = 50;
    public static final int CHART_V_TICKS = 20;
    
    
    // RECTANGLE PLOT
    public static final int RPLOT_WIDTH = 800;
    public static final int RPLOT_HEIGHT = 800;
    public static final int RPLOT_LEFT = 100;
    public static final int RPLOT_RIGHT = 700;
    public static final int RPLOT_TOP = 100;
    public static final int RPLOT_BOTTOM = 700;
    
    
    //Parametri out
    public static String BASE_IMAGE_PATH = 
      "/home/ruego/Dropbox/Hoeff_Tree_Project/Codici/Rilevazioni/" ;
     //   "C:\\Users\\ruego\\Desktop\\Codici\\Rilevazioni\\";
    public static String completeDir = "";
    //if print_file_window_performances=1 make .csv with plot performances
    public static int print_file_window_performances =0; //for routine_all_dataset_csv
    public static int set_GS_values =0; //for routine_generate_all_dataset_csv_performances
       
    
      //binari
    //  public static String datasetName = "covtypeNorm_binario_rand";  //w8a_rand cod-rna_rand  airlines_rand
      public static String datasetName = "medium";  //SyntheticUnBallancedTree_Gaus SyntheticUnBallancedTree_Unif Synthetic_UnBallanced_Tree_HMM
    // public static String datasetName = "synthetic_tree_dataset_large"; 
    // public static String datasetName = "airlines_rand"; 
   //  public static String datasetName = "Test_1_tree_2_cls_16_dim_1_bal_64_leaves_100_beta_0_hmm_8_max_dep_8192_m_min_leaf_0.85_class_prob_1_ril_feat"; 
   //public static String datasetName = "Test_1_tree_2_cls_8_dim_1_bal_32_leaves_100_beta_0_hmm_7_max_dep_16384_m_min_leaf_0.65_class_prob"; 
          
   
      //Filtering
    public static int positive_class_index = 1;   
 
   
    //Parametri albero
    public static double SS_C1 = 20;
    public static double SS_C2 = 3.0;
    public static String type_performances="acc";//acc fmeasure0 fmeasure1 
    public static int INSTANCE_LIMIT = 1000000000; //10000000  
    public static int INSTANCE_SS = 1000000000;
    public static final boolean binarySplit= true;
    public static double delta = 0.9; //Math.pow(10, -1) 0.0000001   .003
    public static double delta_corrHoff = 0.9; 
    public static double delta_McD = 1;
    public static double c_bound = 0.001; //0.3859
    public static final String leafPrediction= "MC"; //"MC", "NB", "NBAdaptive" 
    public static final boolean poorAtt= false;
    public static final int grace = 100;
    public static final double tieThreshold = 0;
    public static final String splitCriterionOption = "GiniSplitCriterion"; //GiniSplitCriterion  InfoGainSplitCriterion KearnsSplitCriterion
    public static final double only_consistence = 0; //for BayesOptimalFollowerTree (0: split if best split function and consistent child. 1: split if consistent child)
    public static final int SAMPLE_FREQUENCY = 200; // windows per il plot 2 e 3 e la frequenza del plot 1
    public static final int DEFAULT_SEED = 0;
    public static final int TYPE_BOUND = 2; //1 Entropy; 2 Giny; 3 Kearns
           
    
    //Array dataset
    public static final String[] datasetBinari= {"anno_10_11_12_no_ID","kddcup1999_binario","elecNormNew"}; 
    public static final String[] datasetSintetici={"SmallComplex","SmallComplex","SmallSimple"};
    public static final String[] datasetMulticlasse={"kddcup1999","sensITvehicle","covtype","poker-lsn"};
    
    // path 
    public static final String pathTree= "C:\\Users\\ruego\\Desktop\\Codici\\dataset\\binari\\randomtree\\dim_150\\";
    public static final String pathMulticlasse= "C:\\Users\\ruego\\Desktop\\Codici\\dataset\\multiclasse\\";
    public static final String pathProva= "C:\\Users\\ruego\\Desktop\\Codici\\dataset\\prova\\";
    //public static  String path= "/home/ruego/Dropbox/Hoeff_Tree_Project/Codici/dataset/binari";
    public static  String path= "C:\\Users\\ruego\\Desktop\\Codici\\dataset\\binari\\";
    //public static final String path= "C:\\Users\\ruego\\Desktop\\Codici\\dataset\\";
    //public static final String pathMarco="C:\\Users\\ruego\\Desktop\\Codici\\dataset\\binari\\randomtree\\depth\\";
    //parametri plot
    public static final int PLOT_TYPE = 1;
    
    //support variables
    public static double best_base_acc_SS = 0;
    public static double best_rocco_acc_SS = 0;
    public static double best_base_nodes_SS= 0;
    public static double best_rocco_nodes_SS = 0;
    
}
