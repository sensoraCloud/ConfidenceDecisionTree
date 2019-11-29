/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.utils;

import java.util.Arrays;
import weka.core.Instance;
import weka.core.Utils;

/**
 * This class defines a confusion matrix with all its inherent methods
 * 
 * 
 * @author Rocco De Rosa
 */
public class MyConfusionMatrix {
    ///String[] classes = {"-1","1"};
    private int[][] matrix;
    
    public MyConfusionMatrix(int numClasses) {
        this.matrix = new int[numClasses][numClasses];
    }
    
    private MyConfusionMatrix(int[][] matrix) {
        this.matrix = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            this.matrix[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
    }
    /*
    public MyConfusionMatrix(Instances testSet, FastVector predictions) {
        int l = testSet.numClasses();
        this.matrix = new int[l][l];
        
        
         *
         * Righe: classi predette
         * Colonne: classi reali
         *
        
        for (int i = 0; i < testSet.numInstances(); i++) {
            this.matrix[(int)((NominalPrediction)predictions.elementAt(i)).predicted()]
                    [(int)testSet.instance(i).classValue()]++;
        }
    }
    */
    
    public MyConfusionMatrix duplicate() {
        return new MyConfusionMatrix(matrix);
    }
    
    public MyConfusionMatrix difference(MyConfusionMatrix other) {
        MyConfusionMatrix res = new MyConfusionMatrix(matrix.length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                res.getMatrix()[i][j] = matrix[i][j] - other.getMatrix()[i][j];
            }
        }
        return res;
    }
    
    public void addPrediction(Instance i, double prediction) {///rivedere da questo pezzo in poi 
        this.matrix[(int)i.classValue()][(int)prediction]++;
    }
     
    public double tp(int classIndex) {
        return this.matrix[classIndex][classIndex];
    }
    
    public double tn(int classIndex) {
        double tn = 0;
        for (int i = 0; i < this.matrix.length; i++) {
            if (i != classIndex)
                tn += this.matrix[i][i];
        }
        return tn;
    }
    
    public double fp(int classIndex) {
        double fp = 0;
        for (int i = 0; i < this.matrix.length; i++) {
            if (i != classIndex)
                fp += this.matrix[i][classIndex];
        }
        return fp;
    }
    
    public double fn(int classIndex) {
        double fn = 0;
        for (int i = 0; i < this.matrix.length; i++) {
            if (i != classIndex)
                fn += this.matrix[classIndex][i];
        }
        return fn;
    }
    
    
    
    
    
    public double getPrecision(int classIndex) {
        double tp = tp(classIndex);
        double tp_fp = tp + fp(classIndex);
         if (tp_fp!=0){
        return tp / tp_fp;
         }else{
         return   0;
        }
    }
    
    public double getRecall(int classIndex) {
        double tp = tp(classIndex);
        double tp_fn = tp + fn(classIndex);
        if (tp_fn!=0){
        return tp / tp_fn;
        }else{
         return   0;
        }
    }
    
    public double getAccuracy(){
        double tp_tn = tp(0) + tn(0);
        double tp_tn_fp_fn = 0;
        
        for (int i = 0; i < this.matrix.length; i++) {
            tp_tn_fp_fn += fp(i);
        }
        /*
        for (int i = 0; i < this.matrix.length; i++) {
            for (int j = 0; j < this.matrix.length; j++) {
                tp_tn_fp_fn += this.matrix[i][j];
                if (i == j)
                    tp_tn += this.matrix[i][j];
            }
        }*/
        tp_tn_fp_fn += tp_tn;
         double accuracy = tp_tn / tp_tn_fp_fn;
        if(tp_tn_fp_fn!=0){
        return accuracy;
       }else{
           return 0.0;
       }
    }
    public double getAccuracy(int classIndex) {
        double tp_tn = tp(classIndex) + tn(classIndex);
        double tp_tn_fp_fn =  tp(classIndex) + tn(classIndex)+fp(classIndex)+fn(classIndex);
        
       double accuracy = tp_tn / tp_tn_fp_fn;
       if(tp_tn_fp_fn!=0){
        return accuracy;
       }else{
           return 0.0;
       }
    }
    
    public double getFMeasure(int classIndex) {
        double p = getPrecision(classIndex);
        double r = getRecall(classIndex);
        double fmeasure =2 * (p * r) / (p + r);
        if(p+r!=0){
                return fmeasure;
        }else{
            return 0;
        }
    }
    
    public double getMCC(int classIndex) {
        double tp = tp(classIndex);
        double tn = tn(classIndex);
        double fn = fn(classIndex);
        double fp = fp(classIndex);
        double mcc=(tp*tn - fp*fn) / Math.sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn));
        if((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn)!=0){
        return mcc;
        }else{
            return 0.0;
        }
    }

    public void printMatrix() {
        try {
            int rows = matrix.length;
            int columns = matrix[0].length;
            String str = "|\t";

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    str += matrix[i][j] + "\t";
                }

                System.out.println(str + "|");
                str = "|\t";
            }

        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
        
        
        for (int i = 0; i < this.matrix.length; i++) {
            System.out.println("Accuracy: " + this.getAccuracy(i));
            System.out.println("[ClassIndex = " + i + "]");
            System.out.println("Precision: " + this.getPrecision(i));
            System.out.println("Recall: " + this.getRecall(i));
            System.out.println("F-Measure: " + this.getFMeasure(i));
            System.out.println("MCC: " + this.getMCC(i));
        }
        System.out.println();
    }
    
    private String repeatSymbol(char symbol, int n) {
        String res = "";
        for (int i = 0; i < n; i++)
            res += symbol;
        return res;
    }
    
    @Override
     public String toString() {
        int rows = matrix.length;
        int columns = matrix[0].length;
        
        String[][] valueStrings = new String[rows][columns];
        
        int maxLength = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                valueStrings[i][j] = Integer.toString(matrix[i][j]);
                if (valueStrings[i][j].length() > maxLength)
                    maxLength = valueStrings[i][j].length();
            }
        }
        
        String line = repeatSymbol('-', maxLength+2);
        String mHSep = "+";
        for (int i = 0; i < columns; i++) 
            mHSep += line + "+";
        mHSep += "\n";
        
        String mString = mHSep;
        
        for (int i = 0; i < rows; i++) {
            mString += "|";
            for (int j = 0; j < columns; j++) {
                mString +="Classe "+(i-1)+ repeatSymbol(' ', maxLength - valueStrings[i][j].length() - 1) +  valueStrings[i][j] + " |";
            }
            
            mString += "\n" + mHSep;
        }
        
        mString += "\n" + repeatSymbol('=', 10) + "\n";
        
        
        for (int i = 0; i < this.matrix.length; i++) {
            mString += "\n[ClassIndex = " + i + "]";
            mString += "\nAccuracy: " + this.getAccuracy(i);
            mString += "\nPrecision: " + this.getPrecision(i);
            mString += "\nRecall: " + this.getRecall(i);
            mString += "\nF-Measure: " + this.getFMeasure(i);
            mString += "\nMCC: " + this.getMCC(i);
        }
        mString += "\n";
        
        return mString;
    }
    
    public int[][] getMatrix() {
        return this.matrix;
    }
    public String toMatrixString(String title, String[] classi) {

    StringBuffer text = new StringBuffer();
    char[] IDChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
        'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
        'z' };
    int IDWidth;
    boolean fractional = false;
    int rows = matrix.length;
    int columns = matrix[0].length;
   

    // Find the maximum value in the matrix
    // and check for fractional display requirement
    double maxval = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < rows; j++) {
        double current = matrix[i][j];
        if (current < 0) {
          current *= -10;
        }
        if (current > maxval) {
          maxval = current;
        }
        double fract = current - Math.rint(current);
        if (!fractional && ((Math.log(fract) / Math.log(10)) >= -2)) {
          fractional = true;
        }
      }
    }

    IDWidth = 1 + Math.max(
        (int) (Math.log(maxval) / Math.log(10) + (fractional ? 3 : 0)),
        (int) (Math.log(rows) / Math.log(IDChars.length)));
    text.append(title).append("\n");
    for (int i = 0; i < rows; i++) {
      if (fractional) {
        text.append(" ").append(num2ShortID(i, IDChars, IDWidth - 3))
            .append("   ");
      } else {
        text.append(" ").append(num2ShortID(i, IDChars, IDWidth));
      }
    }
    text.append("   <-- classified as\n");
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j <rows; j++) {
        text.append(" ").append(
            Utils.doubleToString(matrix[i][j], IDWidth,
                (fractional ? 2 : 0)));
      }
      text.append(" | ").append(num2ShortID(i, IDChars, IDWidth)).append(" = ")
          .append(classi[i]).append("\n");
    }
     text.append("\nAccuracy: " + this.getAccuracy());
        for (int i = 0; i < this.matrix.length; i++) {
           text.append( "\n[ClassIndex = " + classi[i] + "]");
    //         text.append("\nAccuracy: " + this.getAccuracy(i));
             text.append("\nPrecision: " + this.getPrecision(i));
            text.append("\nRecall: " + this.getRecall(i));
             text.append("\nF-Measure: " + this.getFMeasure(i));
            text.append("\nMCC: " + this.getMCC(i));
        }
        text.append("\n");
    return text.toString();
  }
    protected String num2ShortID(int num, char[] IDChars, int IDWidth) {

    char ID[] = new char[IDWidth];
    int i;

    for (i = IDWidth - 1; i >= 0; i--) {
      ID[i] = IDChars[num % IDChars.length];
      num = num / IDChars.length - 1;
      if (num < 0) {
        break;
      }
    }
    for (i--; i >= 0; i--) {
      ID[i] = ' ';
    }

    return new String(ID);
  }
}
