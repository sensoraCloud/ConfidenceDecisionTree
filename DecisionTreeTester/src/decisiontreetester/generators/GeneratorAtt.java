/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import moa.streams.generators.RandomTreeGenerator;
import decisiontreetester.DecisionTreeTester;
import decisiontreetester.settings.GS;
import weka.core.Instance;

/**
 *
 * @author Marco
 */
public class GeneratorAtt {
    
    public static void main (String[] args){
        RandomTreeGenerator generator = new RandomTreeGenerator();
        // utilizzo le impostazioni di default
        //generator.generateRandomTree();
        // valori per il l'albero
        //generator.generateRandomTree(treeRandomSeed, instanceRandomSeed, classNumber, numNominals, numNumeric, valNominals, maxDept, firstLeaf, leafFraction);     
        int classNumber=2;
        int numNominals;
        int numNumeric;
        int valNominals=5;
        int maxDepth=10;
        int  firstLeaf=2;
        double leafFraction=0.15;
     try { 
         for(numNominals=5; numNominals<250; numNominals+=10){
          int   attTot=2*numNominals;         
                 generator.generateRandomTree(1, 1, classNumber, numNominals, numNominals, valNominals, maxDepth, firstLeaf, leafFraction);  
                generator.restart();
                System.out.println(generator.toString());
                   generator.generateHeader();
                 prepareDir1();
                System.out.println("inizio la scrittura"+attTot);
                 File file = new File("C:/Users/Marco/Dropbox/Hoeff_Tree_Project/dataset/binari/randomtree/att/"+
                            classNumber+"_"+maxDepth+"_"+attTot+".arff");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(generator.getHeader().toString());
              System.out.println("scritto l'header e comincio con le istanze");
            while(generator.countTot<100000){
            Instance inst=generator.nextInstance();
            if(inst!=null){
            output.write("\n"+inst.toString());
            }
        }
            output.close();
         }
        } catch (IOException e) {
            e.printStackTrace();
        }  
     
       /* System.out.println();
        for (int i=0; i<90000; i++){
            Instance inst=generator.nextInstance();
            System.out.println(inst.toString());
        }
        */
   //    System.out.print("numero di nodi"+generator.nodeCount());
        
    }
    public static boolean prepareDir1() {        
          
        GS.completeDir = "C:/Users/Marco/Dropbox/Hoeff_Tree_Project/dataset/binari/randomtree/att";
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }
    }
}
