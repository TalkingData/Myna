/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.05.20
 **/
package dice.tree.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dice.data.Instance;
import dice.tree.structure.InnerNode;
import dice.tree.structure.Leaf;
import dice.tree.structure.Node;

/**
 * The Random Decision Trees Model for multi-label classification prediction.
 * CBRRDT is Calibrated Binary Relevance Random Decision Tree.
 * In this model, it will predicate scores/probablities for each label,
 * and the number of the positive labels for the given instance.
 * The usage can of it can be found in dice.examples package.
 * @author Xiatian Zhang
 * @version 0.000
 * @date 2011-5-21 09:37:46 p.m.
 * @project Dice
 *
 */
public class CBRRDTModel {

    public class Prediction {

        double labelNum;

        public Map<Integer, Double> dist;

    }

    /**
     * An parameter for claiming or reclaiming the
     * space of the arrays may be used in <code>Model</code>
     */
    public static double frac = 0.75;

    /**
     * The array stores the decision trees.
     */
    private Node[] trees;

    /**
     * If the number of data instances in a leaf node is
     * leass than <code>minS<code>, <code>Model</code>
     * don't use that leaf node's class distribution to
     * predict, while it will use the class distribution
     * on the parent node to predict.
     */
    private int minS;


    /**
     * The attribute type array.
     */
    private int[] attrs;


    /**
     * Random number generator.
     */
    private Random rd;

    public void init(Node[] trees, int[] attrs, int minS) {
        this.trees = trees;
        this.minS = minS;
        this.attrs = attrs;
        rd = new Random(0);
    }

    public void clear() {
        trees = null;
        attrs = null;
    }

    /**
     * Predict the probabilities of classes of an instance.
     * @param inst
     * @return
     */
    public Prediction estimate(Instance inst) {

        Prediction prediction = new Prediction();

        double v = 0;
        Map<Integer, Double> dist = new HashMap<Integer, Double>();

        for (int i = 0; i < trees.length; i++) {
            Node node = findLeaf(trees[i], inst);
            if (node instanceof Leaf) {
                Leaf leaf = (Leaf) node;
                v += leaf.v;
                for (int j = 0; j < leaf.distIndex.length; j++) {
                    Double t = dist.get(leaf.distIndex[j]);
                    if (t == null) {
                        dist.put(leaf.distIndex[j], leaf.dist[j]);
                    } else {
                        dist.put(leaf.distIndex[j], t + leaf.dist[j]);
                    }
                }
            } else {
                Prediction cPrediction = getChildrenPrediction(node);
                Map<Integer, Double> cDist = cPrediction.dist;
                v += cPrediction.labelNum;
                for (Integer k : cDist.keySet()) {
                    Double t = dist.get(k);
                    if (t == null) {
                        dist.put(k, cDist.get(k));
                    } else {
                        dist.put(k, cDist.get(k) + t);
                    }
                }

            }


        }

        for (Integer k : dist.keySet()) {
            dist.put(k, dist.get(k) / trees.length);
        }
        v /= trees.length;
        prediction.labelNum = v;
        prediction.dist = dist;
        return prediction;

    }

    /**
     * Find the leaf node the given instance belong to.
     * @param tree
     * @param inst
     * @return
     */
    private Node findLeaf(Node tree, Instance inst) {
        Node nd = tree;
        while (nd instanceof InnerNode) {
            InnerNode node = (InnerNode) nd;
            if (attrs[node.attr] > 0) {//nonominal attribute
                int v = (int) inst.getValue(node.attr);
                Node tn = node.getChild(v);
                if (tn != null) {
                    if (tn instanceof InnerNode) {
                        nd = (InnerNode) tn;
                    } else {
                        return tn;
                    }
                } else {
                    return node;
                }
            } else {//numerical, real attribute
                double v = inst.getValue(node.attr);
                if (Double.isNaN(v)) {
                    Node tn = node.getChild(0);
                    if (tn != null) {
                        if (tn instanceof InnerNode) {
                            nd = (InnerNode) tn;
                        } else {
                            return tn;
                        }
                    } else {
                        return node;
                    }
                } else if (v <= node.split) {
                    Node tn = node.getChild(1);
                    if (tn != null) {
                        if (tn instanceof InnerNode) {
                            nd = (InnerNode) tn;
                        } else {
                            return tn;
                        }
                    } else {
                        return node;
                    }
                } else {
                    Node tn = node.getChild(2);
                    if (tn != null) {
                        if (tn instanceof InnerNode) {
                            nd = (InnerNode) tn;
                        } else {
                            return tn;
                        }
                    } else {
                        return node;
                    }
                }
            }
        }
        return tree;
    }

    /**
     * Get the distribution of an inner node.
     * @param node
     * @return
     */
    private Prediction getChildrenPrediction(Node node) {
        Prediction prediction = new Prediction();

        Map<Integer, Double> dist = new HashMap<Integer, Double>();
        double v = 0;


        List<Node> level = new LinkedList<Node>();
        level.add(node);
        List<Node> nextLevel = new LinkedList<Node>();

        int c = 0;
        while (level.size() > 0) {
            for (Node nd : level) {
                if (nd instanceof Leaf) {
                    Leaf leaf = (Leaf) nd;
                    v += leaf.size * leaf.v;
                    for (int i = 0; i < leaf.distIndex.length; i++) {
                        Double t = dist.get(leaf.distIndex[i]);
                        if (t == null) {
                            dist.put(leaf.distIndex[i], leaf.dist[i] * leaf.size);
                        } else {
                            dist.put(leaf.distIndex[i], t + leaf.dist[i] * leaf.size);
                        }
                    }
                    c += leaf.size;
                    continue;
                } else {
                    InnerNode tn = (InnerNode) nd;
                    for (int i = 0; i < tn.children.length; i++) {
                        nextLevel.add(tn.children[i]);
                    }
                }
            }
            level = nextLevel;
            nextLevel = new LinkedList<Node>();
        }

        v /= c;
        for (Integer k : dist.keySet()) {
            dist.put(k, dist.get(k) / c);
        }

        prediction.labelNum = v;
        prediction.dist = dist;

        return prediction;
    }

}
