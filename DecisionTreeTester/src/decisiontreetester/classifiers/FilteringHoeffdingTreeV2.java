/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package decisiontreetester.classifiers;

import weka.core.Utils;

/**
 *
 * @author Rocco De Rosa
 */
public class FilteringHoeffdingTreeV2 extends FilteringHoeffdingTreeV1 {

    protected double selDenominator;

    public FilteringHoeffdingTreeV2(int seed, double c1, double c2,int pos_class_index) {
        super(seed, c1, pos_class_index);
        this.selNumerator = c1;
        this.selDenominator = c2;
    }

    public double getSelDenominator() {
        return selDenominator;
    }

    @Override
    public double calculateTrainingBound(Node node) {
        double[] dis = node.getObservedClassDistribution();
        double delta = 2 * (dis[1] / Utils.sum(dis)) - 1.0;

        // (n)^( 1/(c-|delta|*(c-2)) )        
        return this.selNumerator / Math.pow(Utils.sum(dis),
                1.0 / (this.selDenominator - Math.abs(delta) * (this.selDenominator - 2.0)));
    }

    @Override
    public String getName() {
        return String.format("FHTV2[C=%.1f,c=%.1f,s=%d]",
                this.selNumerator,
                this.selDenominator,
                this.seed);

    }
}
