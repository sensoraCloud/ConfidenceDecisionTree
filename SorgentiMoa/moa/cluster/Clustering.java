/*
 *    CFCluster.java
 *    Copyright (C) 2010 RWTH Aachen University, Germany
 *    @author Jansen (moa@cs.rwth-aachen.de)
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

package moa.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import moa.AbstractMOAObject;
import moa.core.AutoExpandVector;
import moa.gui.visualization.DataPoint;
import weka.core.Instance;

/**
 * Represents a collection of clusters.
 */
public class Clustering extends AbstractMOAObject{

    private AutoExpandVector<Cluster> clusters;

    public Clustering() {
        this.clusters = new AutoExpandVector<Cluster>();
    }

    public Clustering( Cluster[] clusters ) {
        this.clusters = new AutoExpandVector<Cluster>();
        for (int i = 0; i < clusters.length; i++) {
            this.clusters.add(clusters[i]);
        }
    }

    public Clustering(List<? extends Instance> points){
        HashMap<Integer, Integer> labelMap = classValues(points);
        int dim = points.get(0).dataset().numAttributes()-1;

        int numClasses = labelMap.size();

        ArrayList<Instance>[] sorted_points = (ArrayList<Instance>[]) new ArrayList[numClasses];
        for (int i = 0; i < numClasses; i++) {
            sorted_points[i] = new ArrayList<Instance>();
        }
        for (Instance point : points) {
            int clusterid = (int)point.classValue();
            if(clusterid == -1) continue;
            sorted_points[labelMap.get(clusterid)].add((Instance)point);
        }
        this.clusters = new AutoExpandVector<Cluster>();
        for (int i = 0; i < numClasses; i++) {
            if(sorted_points[i].size()>0){
                SphereCluster s = new SphereCluster(sorted_points[i],dim);
                s.setId(sorted_points[i].get(0).classValue());
                s.setGroundTruth(sorted_points[i].get(0).classValue());
                clusters.add(s);
            }
        }
    }

    public Clustering(ArrayList<DataPoint> points, double overlapThreshold, int initMinPoints){
        HashMap<Integer, Integer> labelMap = Clustering.classValues(points);
        int dim = points.get(0).dataset().numAttributes()-1;

        int numClasses = labelMap.size();
        int num = 0;

        ArrayList<DataPoint>[] sorted_points = (ArrayList<DataPoint>[]) new ArrayList[numClasses];
        for (int i = 0; i < numClasses; i++) {
            sorted_points[i] = new ArrayList<DataPoint>();
        }
        for (DataPoint point : points) {
            int clusterid = (int)point.classValue();
            if(clusterid == -1) continue;
            sorted_points[labelMap.get(clusterid)].add(point);
            num++;
        }

        clusters = new AutoExpandVector<Cluster>();
        int microID = 0;
        for (int i = 0; i < numClasses; i++) {
            ArrayList<SphereCluster> microByClass = new ArrayList<SphereCluster>();
            ArrayList<DataPoint> pointInCluster = new ArrayList<DataPoint>();
            ArrayList<ArrayList<Instance>> pointInMicroClusters = new ArrayList();

            pointInCluster.addAll(sorted_points[i]);
            while(pointInCluster.size()>0){
                ArrayList<Instance> micro_points = new ArrayList<Instance>();
                for (int j = 0; j < initMinPoints && !pointInCluster.isEmpty(); j++) {
                    micro_points.add((Instance)pointInCluster.get(0));
                    pointInCluster.remove(0);
                }
                if(micro_points.size() > 0){
                    SphereCluster s = new SphereCluster(micro_points,dim);
                    for (int c = 0; c < microByClass.size(); c++) {
                        if(((SphereCluster)microByClass.get(c)).overlapRadiusDegree(s) > overlapThreshold ){
                            micro_points.addAll(pointInMicroClusters.get(c));
                            s = new SphereCluster(micro_points,dim);
                            pointInMicroClusters.remove(c);
                            microByClass.remove(c);
                            //System.out.println("Removing redundant cluster based on radius overlap"+c);
                        }
                    }
                    for (int j = 0; j < pointInCluster.size(); j++) {
                        Instance instance = pointInCluster.get(j);
                        if(s.getInclusionProbability(instance) > 0.8){
                            pointInCluster.remove(j);
                            micro_points.add(instance);
                        }
                    }
                    s.setWeight(micro_points.size());
                    microByClass.add(s);
                    pointInMicroClusters.add(micro_points);
                    microID++;
                }
            }
            //
            boolean changed = true;
            while(changed){
                changed = false;
                for(int c = 0; c < microByClass.size(); c++) {
                    for(int c1 = c+1; c1 < microByClass.size(); c1++) {
                        double overlap = microByClass.get(c).overlapRadiusDegree(microByClass.get(c1));
//                        System.out.println("Overlap C"+(clustering.size()+c)+" ->C"+(clustering.size()+c1)+": "+overlap);
                        if(overlap > overlapThreshold){
                            pointInMicroClusters.get(c).addAll(pointInMicroClusters.get(c1));
                            SphereCluster s = new SphereCluster(pointInMicroClusters.get(c),dim);
                            microByClass.set(c, s);
                            pointInMicroClusters.remove(c1);
                            microByClass.remove(c1);
                            changed = true;
                            break;
                        }
                    }
                }
            }
            for (int j = 0; j < microByClass.size(); j++) {
                microByClass.get(j).setGroundTruth(sorted_points[i].get(0).classValue());
                clusters.add(microByClass.get(j));
            }

        }
        for (int j = 0; j < clusters.size(); j++) {
            clusters.get(j).setId(j);
        }

    }

    /**
     * @param points 
     * @return an array with the min and max class label value
     */
    public static HashMap<Integer, Integer> classValues(List<? extends Instance> points){
        HashMap<Integer,Integer> classes = new HashMap<Integer, Integer>();
        int workcluster = 0;
        boolean hasnoise = false;
        for (int i = 0; i < points.size(); i++) {
            int label = (int) points.get(i).classValue();
            if(label == -1){
                hasnoise = true;
            }
            else{
                if(!classes.containsKey(label)){
                    classes.put(label,workcluster);
                    workcluster++;
                }
            }
        }
        if(hasnoise)
            classes.put(-1,workcluster);
        return classes;
    }

    public Clustering(AutoExpandVector<Cluster> clusters) {
        this.clusters = clusters;
    }


    /**
     * add a cluster to the clustering
     */
    public void add(Cluster cluster){
        clusters.add(cluster);
    }

    /**
     * remove a cluster from the clustering
     */
    public void remove(int index){
        if(index < clusters.size()){
            clusters.remove(index);
        }
    }

    /**
     * remove a cluster from the clustering
     */
    public Cluster get(int index){
        if(index < clusters.size()){
            return clusters.get(index);
        }
        return null;
    }

    /**
     * @return the <code>Clustering</code> as an AutoExpandVector
     */
    public AutoExpandVector<Cluster> getClustering() {
        return clusters;
    }

    /**
     * @return A deepcopy of the <code>Clustering</code> as an AutoExpandVector
     */
    public AutoExpandVector<Cluster> getClusteringCopy() {
        return (AutoExpandVector<Cluster>)clusters.copy();
    }


    /**
     * @return the number of clusters
     */
    public int size() {
	return clusters.size();
    }

    /**
     * @return the number of dimensions of this clustering
     */
    public int dimension() {
	assert (clusters.size() != 0);
	return clusters.get(0).getCenter().length;
    }

    @Override
    public void getDescription(StringBuilder sb, int i) {
        sb.append("Clustering Object");
    }



    public double getMaxInclusionProbability(Instance point) {
        double maxInclusion = 0.0;
        for (int i = 0; i < clusters.size(); i++) {
            maxInclusion = Math.max(clusters.get(i).getInclusionProbability(point),
                    maxInclusion);
        }
        return maxInclusion;
    }





}
