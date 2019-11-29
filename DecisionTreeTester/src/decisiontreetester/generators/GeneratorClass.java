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
public class GeneratorClass {
    
    public static void main (String[] args){
        RandomTreeGenerator generator = new RandomTreeGenerator();
        // utilizzo le impostazioni di default
        //generator.generateRandomTree();
        // valori per il l'albero
        //generator.generateRandomTree(treeRandomSeed, instanceRandomSeed, classNumber, numNominals, numNumeric, valNominals, maxDept, firstLeaf, leafFraction);     
        int classNumber;
        int numNominals=25;
        int numNumeric=25;
        int attTot=numNominals+numNumeric;
        int valNominals=5;
        int maxDept=10;
        int  firstLeaf=2;
        double leafFraction=0.15;
       
     try {    
          for(classNumber=2; classNumber<30; classNumber++){
                 generator.generateRandomTree(1, 1, classNumber, numNominals, numNumeric, valNominals, maxDept, firstLeaf, leafFraction);  
                generator.restart();
                System.out.println(generator.toString());
                   generator.generateHeader();
                 prepareDir1();
                 System.out.println("inizio la scrittura"+classNumber);
                 File file = new File("C:/Users/Marco/Dropbox/Hoeff_Tree_Project/dataset/binari/randomtree/class/"+
                         +attTot+"_"+maxDept+"_"+classNumber+".arff");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(generator.getHeader().toString());
              System.out.println("scritto l'header e comincio con le istanze");
            for (int i=0; i<100000; i++){
            Instance inst=generator.nextInstance();
            output.write("\n"+inst.toString());
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
          
        GS.completeDir = "C:/Users/Marco/Dropbox/Hoeff_Tree_Project/dataset/binari/randomtree/class";
        File dirFile = new File(GS.completeDir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        } else {
            return true;
        }
    }
}
