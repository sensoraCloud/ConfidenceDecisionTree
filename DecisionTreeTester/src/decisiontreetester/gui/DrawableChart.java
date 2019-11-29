/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.gui;

import java.awt.event.MouseEvent;
import decisiontreetester.DecisionTreeTester;
import decisiontreetester.DecisionTreeTester.ClassifierTest;
import decisiontreetester.classifiers.TransparentHoeffdingTree;
import decisiontreetester.settings.GS;
import moa.tasks.Plot;
import processing.core.*;

/**
 *
 * @author Rocco De Rosa
 */
public class DrawableChart extends PApplet{

    // 0 = acc, 1 = prec, 2 = rec, 3 = fmeas
    
    /*
    private double[][] v_data_hoeff = null;
    private double[][] v_data_hoeff_filter = null;
    private double[][] v_data_bayes = null;
    
    private double[][][] v_data_bayes_t = null;
    
    * 
    */
    
    private double[][][][] v_data_stats_with_class = null;
    private double[][][] v_data_stats_general = null;
    
    private static final int CHART_HEIGHT = 100;
    private static final int CHART_WIDTH = 300;
    
    private static final int MCC_I = 0;
    private static final int PRECISION_I = 1;
    private static final int RECALL_I = 2;
    private static final int FMEASURE_I = 3;
    
    //TODO: Automatizzare
    private static int xFreq = 0;  
    
    private int screen_page = 0;
    
    private static int selected_class = 0;
    
    //private String baseImagePath = "C:\\Users\\Dominic\\Dropbox\\Hoeff_Tree_Project\\Rilevazioni\\";
    
    
    //public static String completeDir = "";
    
    
    private static int     chartT = GS.V_PAD, 
                chartB = GS.V_SPLIT - GS.V_PAD, 
                chartL = GS.H_PAD, 
                chartR = GS.H_SIZE - GS.H_PAD,
                chartHSize = GS.H_SIZE - 2 * GS.H_PAD,
                chartVSize = GS.V_SPLIT - 2 * GS.V_PAD;
    
    
    
    private String[] classPerfStrings = new String[] {
        "MCC (%d)", "PRECISION (%d)", "RECALL (%d)", "FMEASURE (%d)"
    };
    
    private String[] generalPerfStrings = new String[] {
        "ACCURACY", "LEAF_ERROR", "QUERY %","NUMBER OF NODES"
    };
    
    private String[] classPerfShortStrings = new String[] {
        "mcc", "precision", "recall", "fmeasure"
    };
    
    private String[] generalPerfShortStrings = new String[] {
        "accuracy", "leaf_error", "q_perc", "num_nodes"
    };
    
    private double[][] classPerfRanges = new double[][] {
        new double[] {-1.0, 1.0},
        new double[] {0.0, 1.0},
        new double[] {0.0, 1.0},
        new double[] {0.0, 1.0}
    };
    
    
    
    @Override
    public void setup() {
        size(GS.H_SIZE, GS.V_SIZE);
        smooth();
        parseData();
        calcXFreq();
        
        
        for (int p = 0 ; p  < classPerfStrings.length; p++) {
            for (int c = 0; c < v_data_stats_with_class[0].length; c++) {
                drawBigChartWithClass(String.format(classPerfStrings[p], c), c, p);
                save(GS.completeDir+"\\performance_with_class\\"+ c +"_" + classPerfShortStrings[p] + ".png");
            }
        }
        
        for (int p = 0; p < generalPerfStrings.length; p++) {
            drawBigChartGeneral(generalPerfStrings[p], p);
            save(GS.completeDir+"\\general_performance\\"+ generalPerfShortStrings[p] + ".png");
        }
        voidScreen();
        //noLoop();
    }
    
    @Override
    public void draw() {
        switch(screen_page) {
            case 0: homeScreen();
                break;
            case 1: drawBigChartWithClass("MCC", selected_class, 0);
                break;
            case 2: drawBigChartWithClass("Precision (pos)", selected_class, 1);
                break;
            case 3: drawBigChartWithClass("Recall (pos)", selected_class, 2);
                break;
            case 4: drawBigChartWithClass("F-Measure (pos)", selected_class, 3);
                break;
            default: voidScreen();
                break;
        }
        noLoop();
    }
    
    public void calcXFreq() {
        xFreq =  DecisionTreeTester.total_instances / GS.CHART_H_TICKS;
    }
    
    public void homeScreen() {
        background(255);
        textAlign(CENTER);
        textSize(14);
        fill(0);
        stroke(0);
        line(400, 0, 400, 600);
        line(0, 300, 800, 300);
        
        drawChart("MCC", MCC_I, selected_class, 0, -1);
        drawChart("Precision (positive)", PRECISION_I, selected_class, 400, 0);
        drawChart("Recall (positive)", RECALL_I, selected_class, 0, 300);
        drawChart("F-Measure (positive)", FMEASURE_I, selected_class, 400, 300);
    }
    
    public void voidScreen() {
        background(255);
    }

    public void parseData() {
        v_data_stats_with_class = new double[DecisionTreeTester.all_tests.size()][][][];
        
        for (int i = 0; i < v_data_stats_with_class.length; i++) {
            v_data_stats_with_class[i] = DecisionTreeTester.all_tests.get(i).parseForDrawingWithClass();
        }
        
        v_data_stats_general = new double[DecisionTreeTester.all_tests.size()][][];
        
        for (int i = 0; i < v_data_stats_general.length; i++ )
            v_data_stats_general[i] = DecisionTreeTester.all_tests.get(i).parseForDrawingGeneral();
        
    }
    
    
    /**
     * Converts a value from a range of double to another of int (useful for pixel
     * representation)
     * 
     * @param value Value to be converted
     * @param min Old range minimum
     * @param max Old range maximum
     * @param newmin New range minimum
     * @param newmax New range maximum
     * @return New calculated value
     */
    public static int convertRange(double value, double min, double max, int newmin, int newmax ) {
        return (int)((((value - min) * (newmax - newmin)) / (max - min)) + newmin);
    }
    
    public void drawChart(String name, int index, int actualClass, float c_x, float c_y) {
        pushMatrix();
        pushStyle();
        translate(c_x, c_y);
        stroke(220);
        line(50, 50, 350, 50);
        line(350, 50, 350, 150);
        stroke(0);
        fill(0);
        textAlign(CENTER);
        textSize(14);
        text(name, 200, 30);
        line(50, 50, 50, 152);
        line(45, 50, 50, 50);
        line(45, 150, 350, 150);
        line(350, 150, 350, 152);
        textSize(12);
        text("0", 50, 170);
        text(Integer.toString(DecisionTreeTester.total_instances), 350, 170);
        textAlign(RIGHT);
        text("1.0", 40, 55);
        text("0.0", 40, 155);
        
        textAlign(LEFT);
        
        textSize(12);
        
        fill(0.0f, 0.0f);
        
        for (int i = 0; i < v_data_stats_with_class.length; i++) {
            ClassifierTest ct = DecisionTreeTester.all_tests.get(i);
            stroke(ct.r, ct.g, ct.b);
            line(50, 200 + 15*i, 75, 200 + 15*i);
            drawChartLine(v_data_stats_with_class[i][actualClass], index);
        }
        
        fill(0);
        
        for (int i = 0; i < v_data_stats_with_class.length; i++) {
            ClassifierTest ct = DecisionTreeTester.all_tests.get(i);
            text(String.format("= %1$s: %2$.4f", ct.name, v_data_stats_with_class[i][actualClass][v_data_stats_with_class[i][actualClass].length -1][index]), 80, 200+15*i);
        }
        popStyle();
        popMatrix();
    }
    
    public void drawChartLine(double[][] data, int index) {
        beginShape();
        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i][index])){
                vertex((float)(50 + (300.0 * ((i+1.0) / data.length))),
                       (float)(150 - (100.0f * (float)data[i][index])));
        }else{
                double a=2;
           vertex((float)(50 + (300.0 * ((i+1.0) / data.length))),
                     (float)0);
        }
        }
        endShape();
    }
    
    public void drawBigChartGrid(String name, double yMax, double yMin) {
        background(255);
        
        stroke(0);
        fill(0);
        textAlign(CENTER);
        textSize(14);
        text(name, GS.H_SIZE / 2, GS.V_PAD / 2);
        //int zeroLine = (int)((((0.0 - yMin) * (double)chartVSize ) / (yMax-yMin)) + chartT);
                // chartB + (int)((double)chartVSize * (yMin * (yMax - yMin)));
        int zeroLine = convertRange(0, yMin, yMax, chartT, chartB);
        
        strokeWeight(2);
        
        line(chartL, chartT, chartL, chartB);
        line(chartL, zeroLine, chartR, zeroLine);
        
        strokeWeight(1);
      
        textSize(12);
        text("0", chartL, chartB + 20);
        
        pushStyle();
        stroke(220);
        textAlign(RIGHT);
        
        double step = (yMax - yMin) / GS.CHART_V_TICKS;
        
        for (double d = yMin; d <= yMax + 0.01; d += step) 
            text(String.format("%1$.2f", d), chartL - 20, 5 + convertRange(d, yMax, yMin, chartT, chartB));
        for (int i = chartT; i <= chartB; i+= chartVSize / GS.CHART_V_TICKS)
            line(chartL, i, chartR, i);
        
        stroke(0);
        
        textAlign(CENTER);
        int j = 0;
        
        text(String.format("n. istanze (x %1$d)", xFreq), GS.H_SIZE / 2, GS.V_SPLIT - 60);
        
        for (int i = xFreq; i <= DecisionTreeTester.total_instances; i+= xFreq) {
            j++;
            float grid_x = ((float)i/(float)DecisionTreeTester.total_instances)*(float)chartHSize + GS.H_PAD;
            line(grid_x, chartT, grid_x, chartB);
            text(j, grid_x, chartB + 20);
        }
        popStyle();
        
    }
    
    public void drawBigChartGeneral(String name, int index) {
        pushMatrix();
        pushStyle();
        
        //CALCULATE MIN MAX RAGE NODES
        double max_value=Double.MIN_VALUE;
        double min_value=Double.MAX_VALUE;
        
        double val;
        
        for (int c = 0; c < v_data_stats_general.length; c++) {
            for (int i = 0; i < v_data_stats_general[c].length; i++) {

                val = (double) v_data_stats_general[c][i][index];
                if (val > max_value) {
                    max_value = val;
                }
                if (val < min_value) {
                    min_value = val;
                }
            }
        }
        
         
     //range of Y axis
   /*  double[][] generalPerfRanges = new double[][] {
        new double[] {0.0, 1.0},
        new double[] {0.0, 0.5},
        new double[] {0.0, 1.0},
        new double[] {0.0, 60.0}
    };*/
        
      //  drawBigChartGrid(name, 
        //        generalPerfRanges[index][1], generalPerfRanges[index][0]);
        
        if (Math.abs(min_value)>1)
            min_value=min_value-1;
        else
            min_value=min_value-0.1;
        
        if (Math.abs(max_value)>1)
            max_value=max_value+1;
        else
            max_value=max_value+0.1;     
        
        
        drawBigChartGrid(name, 
                max_value , min_value);
        
        
        textAlign(LEFT);
        textSize(12);
        fill(0.0f, 0.0f);
        
        for (int i = 0; i < v_data_stats_general.length; i++) {
            ClassifierTest ct = DecisionTreeTester.all_tests.get(i);
            stroke(ct.r, ct.g, ct.b);
            line(chartL, GS.V_SPLIT + 15*i, chartL + 50, GS.V_SPLIT + 15*i);
            //drawBigChartLine(v_data_stats_general[i], index, 
              //  generalPerfRanges[index][1], generalPerfRanges[index][0]);
            
            drawBigChartLine(v_data_stats_general[i], index, 
                 max_value , min_value);
            
            
        }
        
        fill(0);
        
        int initial_line = GS.V_SPLIT + 15* (v_data_stats_with_class.length + 1);
        
        for (int i = 0; i < v_data_stats_general.length; i++) {
            ClassifierTest ct = DecisionTreeTester.all_tests.get(i);
            text(String.format("= %1$s (final value: %2$.4f)", ct.name, v_data_stats_general[i][v_data_stats_general[i].length -1][index]), 
                    chartL + 60, GS.V_SPLIT+15*i);
            
            if (ct.classifier instanceof TransparentHoeffdingTree)
                text(ct.name + "\n\n" + ((TransparentHoeffdingTree)ct.classifier).printTree(), 
                        GS.H_PAD + ((GS.H_SIZE - 2*GS.H_PAD) / v_data_stats_general.length) * i, initial_line);
        }
        popStyle();
        popMatrix();
    }
    
    public void drawBigChartWithClass(String name, int actualClass, int index) {
        pushMatrix();
        pushStyle();
        
        drawBigChartGrid(name, 
                classPerfRanges[index][1], classPerfRanges[index][0]);
        
        textAlign(LEFT);
        textSize(12);
        
        fill(0.0f, 0.0f);
        
        for (int i = 0; i < v_data_stats_with_class.length; i++) {
            ClassifierTest ct = DecisionTreeTester.all_tests.get(i);
            stroke(ct.r, ct.g, ct.b);
            line(chartL, GS.V_SPLIT + 15*i, chartL + 50, GS.V_SPLIT + 15*i);
            drawBigChartLine(v_data_stats_with_class[i][actualClass], index, 
                classPerfRanges[index][1], classPerfRanges[index][0]);
        }
        
        fill(0);
        
        int initial_line = GS.V_SPLIT + 15* (v_data_stats_with_class.length + 1);
        
        for (int i = 0; i < v_data_stats_with_class.length; i++) {
            ClassifierTest ct = DecisionTreeTester.all_tests.get(i);
            text(String.format("= %1$s (final value: %2$.4f)", ct.name, v_data_stats_with_class[i][actualClass][v_data_stats_with_class[i][actualClass].length -1][index]), 
                    chartL + 60, GS.V_SPLIT+15*i);
            
      
         //       text(ct.name + "\n\n" + ((TransparentHoeffdingTree)ct.classifier).printTree(), 
           //             GS.H_PAD + ((GS.H_SIZE - 2*GS.H_PAD) / v_data_stats_with_class.length) * i, initial_line);
        }
        
        
        
        popStyle();
        popMatrix();
    }
    
    public void drawBigChartLine(double[][] data, int index, double yMax, double yMin) {
        
        strokeWeight(4);
        beginShape();
        for (int i = 0; i < data.length; i++) {
            
            
            if (!Double.isNaN(data[i][index])){
                vertex((float)(chartL + (chartHSize * ((i+1.0) / data.length))),
                       (float)(convertRange(data[i][index], yMax, yMin, chartT, chartB)));
          }else{
            vertex((float)(chartL + (chartHSize * ((i+1.0) / data.length))),
                     (float)(convertRange(0, yMax, yMin, chartT, chartB)));
        }
            }
            
        endShape();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (screen_page == 0) {
            if (mouseX < 400) {
                if (mouseY < 300)
                    screen_page = 1;
                else
                    screen_page = 3;
            } else if (mouseY < 300)
                screen_page = 2;
            else
                screen_page = 4;
        } else 
            screen_page = 0;
        loop();
    }
}
