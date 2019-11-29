/*
 *    RandomTreeGenerator.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.streams.generators;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import moa.core.InstancesHeader;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;

/**
 * Stream generator for a stream based on a randomly generated tree..
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public class RandomTreeGenerator extends AbstractOptionHandler implements
        InstanceStream {

    @Override
    public String getPurposeString() {
        return "Generates a stream based on a randomly generated tree.";
    }
    public int[] countClass;
    public int countTot;
    private static final long serialVersionUID = 1L;

    public IntOption treeRandomSeedOption = new IntOption("treeRandomSeed",
            'r', "Seed for random generation of tree.", 1);

    public IntOption instanceRandomSeedOption = new IntOption(
            "instanceRandomSeed", 'i',
            "Seed for random generation of instances.", 1);

    public IntOption numClassesOption = new IntOption("numClasses", 'c',
            "The number of classes to generate.", 2, 2, Integer.MAX_VALUE);

    public IntOption numNominalsOption = new IntOption("numNominals", 'o',
            "The number of nominal attributes to generate.", 10, 0,
            Integer.MAX_VALUE);

    public IntOption numNumericsOption = new IntOption("numNumerics", 'u',
            "The number of numeric attributes to generate.", 10, 0,
            Integer.MAX_VALUE);

    public IntOption numValsPerNominalOption = new IntOption(
            "numValsPerNominal", 'v',
            "The number of values to generate per nominal attribute.", 5, 2,
            Integer.MAX_VALUE);

    public IntOption maxTreeDepthOption = new IntOption("maxTreeDepth", 'd',
            "The maximum depth of the tree concept.", 5, 0, Integer.MAX_VALUE);

    public IntOption firstLeafLevelOption = new IntOption(
            "firstLeafLevel",
            'l',
            "The first level of the tree above maxTreeDepth that can have leaves.",
            3, 0, Integer.MAX_VALUE);

    public FloatOption leafFractionOption = new FloatOption("leafFraction",
            'f',
            "The fraction of leaves per level from firstLeafLevel onwards.",
            0.15, 0.0, 1.0);

    public  class Node implements Serializable {

        private static final long serialVersionUID = 1L;

        public int classLabel;

        public int splitAttIndex;

        public double splitAttValue;
        
        public double[] distribution;
          public double[] distributionFinal;
          public Random rand;

        public RandomTreeGenerator.Node[] children;
        
        public Node(){};
        public Node (int n){
            this.rand= new Random();
        this.distribution= new double[n];
        this.distributionFinal = normalize(distribution);
        }
        
        public int getNodes(){
            return children.length;
        }
         public double[] normalize (double[] distribution){
         for (int i=0; i<distribution.length;i++ ){
                distribution[i]= 1;              
         }
         Random rand = new Random();
         
         distribution[rand.nextInt(this.distribution.length)]+=1;
                  
         double somma =0.0;
         for (double i: distribution){
             somma+=i;
         }
        for (int i=0; i<distribution.length;i++ ){
                distribution[i]=   distribution[i]/somma;              
         }
 //       Arrays.sort(distribution);
        for (int i=1;i<distribution.length; i++){
           
                
                distribution[i]+=distribution[i-1];
            
        }
 //       Arrays.sort(distribution);
        double[] distributionF= new double[distribution.length+1];
        distributionF[0]=0;
        for (int i=0; i<distribution.length; i++){
            distributionF[i+1]= distribution[i];
        }
        return distributionF;
        }
         
        public int calcClassInd(){
           int classe =0;
       //    int[] valori = new int[3];
        //   for(int j=0; j<1000;j++){
            double value = rand.nextDouble();
            
                for (int i=distributionFinal.length-2; i>=0; i--){
                if (value>distributionFinal[i]){
                   classe=i; 
       //            valori[i]++;
                   break;
                }
                
            }
          //  classe++;
           
        //} 
           
           return classe;
        }
    }

    public RandomTreeGenerator.Node treeRoot;

    protected InstancesHeader streamHeader;

    public Random instanceRandom;

    @Override
    public void prepareForUseImpl(TaskMonitor monitor,
            ObjectRepository repository) {
        monitor.setCurrentActivity("Preparing random tree...", -1.0);
        generateHeader();
        generateRandomTree();
        restart();
    }

    @Override
    public long estimatedRemainingInstances() {
        return -1;
    }

    @Override
    public boolean isRestartable() {
        return true;
    }

    @Override
    public void restart() {
        this.instanceRandom = new Random(this.instanceRandomSeedOption.getValue());
    }

    @Override
    public InstancesHeader getHeader() {
        return this.streamHeader;
    }

    @Override
    public boolean hasMoreInstances() {
        return true;
    }

    @Override
    public Instance nextInstance() {
      //  this.instanceRandom= new Random();
        double[] attVals = new double[this.numNominalsOption.getValue()
                + this.numNumericsOption.getValue()];
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        for (int i = 0; i < attVals.length; i++) {
         //  attVals[i]=Math.random();
        //  this.instanceRandom= new Random();
             attVals[i] = i < this.numNominalsOption.getValue() ? this.instanceRandom.nextInt(this.numValsPerNominalOption.getValue())
                    : this.instanceRandom.nextDouble();
            inst.setValue(i, attVals[i]);
        }
        inst.setDataset(header);
       inst.setClassValue(classifyInstance(this.treeRoot, attVals));
        int i=(int) inst.classValue();
        double k=(1/(double)numClassesOption.getValue())+0.05;
     if(prob(i)<=k){
       countClass[i]++;
       countTot++;
       return inst;
        }else{
            return null;
        }
     // countTot++;
       //return inst;
    }
    
    public double prob(int i){
        if (countTot!=0){
        return (countClass[i]/(double)countTot);
        }else{
            return 0;
        }
    }
   

    protected int classifyInstance(RandomTreeGenerator.Node node, double[] attVals) {
        if (node.children == null) {
             
            return node.calcClassInd();
        }
        if (node.splitAttIndex < this.numNominalsOption.getValue()) {
            return classifyInstance(
                    node.children[(int) attVals[node.splitAttIndex]], attVals);
        }
        return classifyInstance(
                node.children[attVals[node.splitAttIndex] < node.splitAttValue ? 0
                : 1], attVals);
    }

    public void generateHeader() {
        FastVector attributes = new FastVector();
        FastVector nominalAttVals = new FastVector();
        for (int i = 0; i < this.numValsPerNominalOption.getValue(); i++) {
            nominalAttVals.addElement("v" + (i + 1));
        }
        for (int i = 0; i < this.numNominalsOption.getValue(); i++) {
            attributes.addElement(new Attribute("nom" + (i + 1),
                    nominalAttVals));
        }
        for (int i = 0; i < this.numNumericsOption.getValue(); i++) {
            attributes.addElement(new Attribute("num" + (i + 1)));
        }
        FastVector classLabels = new FastVector();
        for (int i = 0; i < this.numClassesOption.getValue(); i++) {
            classLabels.addElement("c" + (i + 1));
        }
        attributes.addElement(new Attribute("c", classLabels));
        this.streamHeader = new InstancesHeader(new Instances(
                getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);
    }

    public void generateRandomTree() {
        Random treeRand = new Random(this.treeRandomSeedOption.getValue());
        ArrayList<Integer> nominalAttCandidates = new ArrayList<Integer>(
                this.numNominalsOption.getValue());
        for (int i = 0; i < this.numNominalsOption.getValue(); i++) {
            nominalAttCandidates.add(i);
        }
        double[] minNumericVals = new double[this.numNumericsOption.getValue()];
        double[] maxNumericVals = new double[this.numNumericsOption.getValue()];
        for (int i = 0; i < this.numNumericsOption.getValue(); i++) {
            minNumericVals[i] = 0.0;
            maxNumericVals[i] = 1.0;
        }
        this.treeRoot = generateRandomTreeNode(0, nominalAttCandidates,
                minNumericVals, maxNumericVals, treeRand);
    }
    
    
    protected RandomTreeGenerator.Node generateRandomTreeNode(int currentDepth,
            ArrayList<Integer> nominalAttCandidates, double[] minNumericVals,
            double[] maxNumericVals, Random treeRand) {
        if ((currentDepth >= this.maxTreeDepthOption.getValue())
                || ((currentDepth >= this.firstLeafLevelOption.getValue()) && (this.leafFractionOption.getValue() >= (1.0 - treeRand.nextDouble())))) {
          Node leaf = new Node(this.numClassesOption.getValue());
                     
          //  System.out.println(leaf.hashCode()+" con etichetta: "+leaf.classLabel);
           /*  Node leaf = new Node();
            leaf.classLabel = treeRand.nextInt(this.numClassesOption.getValue());
          */  return leaf;
        }
        RandomTreeGenerator.Node node = new RandomTreeGenerator.Node(this.numClassesOption.getValue());
        int chosenAtt = treeRand.nextInt(nominalAttCandidates.size()
                + this.numNumericsOption.getValue());
        if (chosenAtt < nominalAttCandidates.size()) {
            node.splitAttIndex = nominalAttCandidates.get(chosenAtt);
            node.children = new RandomTreeGenerator.Node[this.numValsPerNominalOption.getValue()];
            ArrayList<Integer> newNominalCandidates = new ArrayList<Integer>(
                    nominalAttCandidates);
            newNominalCandidates.remove(new Integer(node.splitAttIndex));
            newNominalCandidates.trimToSize();
            for (int i = 0; i < node.children.length; i++) {
                node.children[i] = generateRandomTreeNode(currentDepth + 1,
                        newNominalCandidates, minNumericVals, maxNumericVals,
                        treeRand);
            }
        } else {
            int numericIndex = chosenAtt - nominalAttCandidates.size();
            node.splitAttIndex = this.numNominalsOption.getValue()
                    + numericIndex;
            double minVal = minNumericVals[numericIndex];
            double maxVal = maxNumericVals[numericIndex];
            node.splitAttValue = ((maxVal - minVal) * treeRand.nextDouble())
                    + minVal;
            node.children = new RandomTreeGenerator.Node[2];
            double[] newMaxVals = maxNumericVals.clone();
            newMaxVals[numericIndex] = node.splitAttValue;
            node.children[0] = generateRandomTreeNode(currentDepth + 1,
                    nominalAttCandidates, minNumericVals, newMaxVals, treeRand);
            double[] newMinVals = minNumericVals.clone();
            newMinVals[numericIndex] = node.splitAttValue;
            node.children[1] = generateRandomTreeNode(currentDepth + 1,
                    nominalAttCandidates, newMinVals, maxNumericVals, treeRand);
        }
        return node;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }
    public int nodeCount(){
        return nodeCount(this.treeRoot);
    }
            
    public int nodeCount(RandomTreeGenerator.Node node){
        int count=1;
        if (node.children!=null){
            count++;
            for (int i=0; i<node.getNodes(); i++){
                count+=nodeCount(node.children[i]);
            }
        }
        return count;
    }
    public void generateRandomTree( int treeRandomSeed, int instanceRandomSeed, int classNumber, int numNominals, int numNumeric, int valNominals,
            int maxDept, int firstLeaf, double leafFraction){
      this.treeRandomSeedOption.setValue(treeRandomSeed);
      this.instanceRandomSeedOption.setValue(instanceRandomSeed);
      this.numClassesOption.setValue(classNumber);
      this.numNominalsOption.setValue(numNominals);
      this.numNumericsOption.setValue(numNumeric);
      this.numValsPerNominalOption.setValue(valNominals);
      this.maxTreeDepthOption.setValue(maxDept);
      this.firstLeafLevelOption.setValue(firstLeaf);
      this.leafFractionOption.setValue(leafFraction);
      this.countClass= new int[classNumber];
      this.countTot=0;
      generateRandomTree();
    }
}

