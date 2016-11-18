/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.05.20
 **/
package dice.tree.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dice.data.Instance;
import dice.data.Instances;
import dice.tree.structure.InnerNode;
import dice.tree.structure.Leaf;
import dice.tree.structure.Node;
import dice.util.BiArrays;

/**
 * Build random decision trees. The usage of <code>TreeBuilder</code> is
 * demostrated in dice.examples package.
 *
 * @author Xiatian Zhang
 * @version 0.000
 * @date 2011-5-23 10:44:36
 * @project Dice
 *
 */
public class TreeBuilder {

    /**
     * @see #type
     */
    public static final byte CLASSIFICATION = 0;

    /**
     * @see #type
     */
    public static final byte REGRESSION = 1;

    /**
     * @see #type
     */
    public static final byte CBR_RDT = 2;

    /**
     * Random number generator.
     */
    private Random rd;

    /**
     * The type indicate what kind of tree will be built. 0: classification, 1:
     * regression, 2: CBR-RDT
     */
    private byte type;

    /**
     * The max depth of the tree. When an branch achieve the threshold, the
     * branch will stop grow.
     */
    private int maxDeep;

    /**
     * The max number of instances should be in a leaf node. It is not a hard
     * threshold. When a branch's number of instances are less than or equals to
     * the property, the branch will stop grow.
     */
    private int maxS;

    /**
     * The class number of the data. Note, the class attributes can only in the
     * tail of each instance attribute vector.
     */
    private int clsSize;

    /**
     * The training data set.
     */
    private Instances insts;

    /**
     * Current level of nodes of the trees.
     */
    private List<Node> level;

    /**
     * The instance ids belong to the nodes in current {@link #level}
     */
    private List<int[]> ions;

    /**
     * The parent nodes of the nodes in current {@link #level}
     */
    private Map<Node, Node> parents;

    /**
     * Create the object.
     *
     * @param randomSeed
     *            Random seed.
     * @param type
     *            The {@link #type} of the tree builder.
     *
     */
    public TreeBuilder(long randomSeed, byte type) {
        rd = new Random(randomSeed);
        this.type = type;
        this.clsSize = 1;// default = 1

    }

    /**
     * Set the number of classes.
     *
     * @param clsSize
     *            Number of classes.
     */
    public void setClsSize(int clsSize) {
        this.clsSize = clsSize;
    }

    /**
     * Get the training data set.
     *
     * @return the instances
     */
    public Instances getInstances() {
        return insts;
    }

    /**
     * Set the training data set.
     *
     * @param instances
     *            the instances to set
     */
    public void setInstances(Instances instances) {
        this.insts = instances;
    }

    /**
     * @return the maxDeep
     */
    public int getMaxDeep() {
        return maxDeep;
    }

    /**
     * Set the max depth of the tree to build. Should be used after set a
     * instances
     *
     * @param maxDeep
     *            the maxDeep to set
     */
    public void setMaxDeep(int maxDeep) {
        this.maxDeep = maxDeep < insts.getAttrSize() - clsSize + 1 ? maxDeep : insts.getAttrSize() - clsSize + 1;
    }

    /**
     * @see #maxS
     * @return the maxS
     */
    public int getMaxS() {
        return maxS;
    }

    /**
     * @see #maxS
     * @param maxS
     */
    public void setMaxS(int maxS) {
        this.maxS = maxS;
    }

    /**
     * @see #clsSize
     * @return The number of classes.
     */
    public int getClsSize() {
        return clsSize;
    }

    /**
     * @param randomSeed
     */
    public void setRandomSeed(long randomSeed) {
        rd = new Random(randomSeed);
    }

    /**
     * Initialize the tree builder.
     */
    public void init() {

        level = new LinkedList<Node>();
        ions = new LinkedList<int[]>();
        parents = new HashMap<Node, Node>();
    }

    /**
     * Clear the tree builder.
     */
    public void clear() {
        level = null;
        ions = null;
        parents = null;
    }

    /**
     * Build trees.
     *
     * @param treeNum
     *            The number of trees to build.
     * @return
     */
    public Node[] buildTrees(int treeNum) {
        // setInstances(insts);
        Node[] trees = new Node[treeNum];
        for (int i = 0; i < trees.length; i++) {
            trees[i] = build();
        }
        clear();
        return trees;
    }

    /**
     * Build a tree.
     *
     * @return
     */
    private Node build() {
        init();

        InnerNode root = null;
        if (maxDeep == 1 || insts.size() <= maxS) {
            return null;
        }

        root = new InnerNode();

        int[] ion = new int[insts.size()];
        for (int i = 0; i < insts.size(); i++) {
            ion[i] = i;
        }
        level.add(root);
        ions.add(ion);

        for (int i = 0; i < maxDeep; i++) {
            incLevel(root);
            if (level.isEmpty()) {
                break;
            }
        }

        Iterator<int[]> itor = ions.iterator();
        for (Node node : level) {
            ion = itor.next();
            InnerNode parent = (InnerNode) parents.get(node);
            for (int i = 0; i < parent.children.length; i++) {
                if (parent.children[i].equals(node)) {
                    parent.children[i] = closeNode(node, ion);
                    parents.remove(node);
                    break;
                }
            }
        }
        return root;
    }

    /**
     * Build a level of a tree.
     *
     * @param root
     *            The root of the tree. It is just used for attribute selection.
     * @return
     */

    private void incLevel(Node root) {

        int[] attrs = insts.getAttributes();

        List<Node> nextLevel = new LinkedList<Node>();
        List<int[]> nextions = new LinkedList<int[]>();

        Iterator<int[]> itor = ions.iterator();
        for (Node e : level) {

            InnerNode node = (InnerNode) e;
            int[] ion = itor.next();
            if (ion.length <= maxS) {
                InnerNode parent = (InnerNode) parents.get(e);
                for (int i = 0; i < parent.children.length; i++) {
                    if (parent.children[i].equals(e)) {
                        parent.children[i] = closeNode(node, ion);
                        parents.remove(e);
                        break;
                    }
                }
                node.clear();
                continue;
            }

            int attr = selectAttr(node, (InnerNode) root, insts.get(ion[0]));

            if (attr == -1) {
                InnerNode parent = (InnerNode) parents.get(e);
                for (int i = 0; i < parent.children.length; i++) {
                    if (parent.children[i].equals(e)) {
                        parent.children[i] = closeNode(node, ion);
                        parents.remove(e);
                        break;
                    }
                }
                node.clear();
                continue;
            }

            node.attr = attr;
            if (attrs[attr] > 0) {// nominal attribute
                node.addChildren(attrs[attr]);
                int[] tmp = new int[ion.length];
                for (int i = 0; i < ion.length; i++) {
                    tmp[i] = (int) insts.get(ion[i]).getValue(attr);
                }
                BiArrays.sort(tmp, ion);
                List<Integer> splits = new ArrayList<Integer>();
                double t = tmp[0];
                for (int i = 0; i < tmp.length; i++) {
                    if (tmp[i] != t) {
                        t = tmp[i];
                        splits.add(i);
                    }
                }
                splits.add(tmp.length);

                int psp = 0;
                for (int sp : splits) {
                    int[] ioc = new int[sp - psp];
                    System.arraycopy(ion, psp, ioc, 0, sp - psp);
                    if (ioc.length <= maxS) {
                        Leaf leaf = new Leaf();
                        closeNode(leaf, ioc);
                        node.addChild(tmp[sp - 1], leaf);
                    } else {
                        InnerNode nNode = new InnerNode();
                        // nNode.parent = node;
                        node.addChild(tmp[sp - 1], nNode);
                        nextLevel.add(nNode);
                        nextions.add(ioc);
                        parents.put(nNode, e);
                    }
                    psp = sp;
                }

                node.clear();
            } else {// numerical attribute

                node.addChildren(2);
                double[] tmp = new double[ion.length];
                int c = 0;
                Set<Double> values = new HashSet<Double>();
                for (int i = 0; i < ion.length; i++) {
                    double value = insts.get(ion[i]).getValue(attr);
                    if (value == Double.MAX_VALUE) {
                        c++;
                    }
                    tmp[i] = value;
                    values.add(value);
                }

                if (values.size() == 1) {
                    if (ion.length <= maxS) {
                        InnerNode parent = (InnerNode) parents.get(e);
                        if (parent == null) {
                            root = closeNode(node, ion);
                        } else {
                            for (int i = 0; i < parent.children.length; i++) {
                                if (parent.children[i].equals(e)) {
                                    parent.children[i] = closeNode(node, ion);
                                    parents.remove(e);
                                    break;
                                }
                            }
                        }
                        node.clear();
                        continue;
                    } else {
                        node.attr = -1;
                        nextLevel.add(node);
                        nextions.add(ion);
                        continue;
                    }
                }

                List<Double> valueList = new ArrayList<Double>(values);
                Collections.sort(valueList);
                if (values.size() <= 2) {
                    node.split = valueList.get(0);
                } else {
                    node.split = valueList.get(rd.nextInt(valueList.size() - 2) + 1);
                }

                BiArrays.sort(tmp, ion);
                int splitInst = Arrays.binarySearch(tmp, node.split);
                while (splitInst < tmp.length
                        && tmp[splitInst] == tmp[splitInst + 1]) {
                    splitInst++;
                }

                int[] ioc = new int[splitInst + 1];
                System.arraycopy(ion, 0, ioc, 0, splitInst + 1);
                if (ioc.length <= maxS) {
                    Leaf leaf = new Leaf();
                    closeNode(leaf, ioc);
                    node.addChild(1, leaf);
                } else {
                    InnerNode nNode = new InnerNode();
                    // nNode.parent = node;
                    node.addChild(1, nNode);
                    nextLevel.add(nNode);
                    nextions.add(ioc);
                    parents.put(nNode, e);
                }

                int t = tmp.length - c - splitInst - 1;
                if (t > 0) {
                    ioc = new int[t];
                    System.arraycopy(ion, splitInst + 1, ioc, 0, t);
                    if (ioc.length <= maxS) {
                        Leaf leaf = new Leaf();
                        closeNode(leaf, ioc);
                        node.addChild(2, leaf);
                    } else {
                        InnerNode nNode = new InnerNode();
                        // nNode.parent = node;
                        node.addChild(2, nNode);
                        nextLevel.add(nNode);
                        nextions.add(ioc);
                        parents.put(nNode, e);
                    }
                }

                if (c > 0) {
                    ioc = new int[c];
                    System.arraycopy(ion, tmp.length - c, ioc, 0, c);
                    if (ioc.length <= maxS) {
                        Leaf leaf = new Leaf();
                        closeNode(leaf, ioc);
                        node.addChild(0, leaf);
                    } else {
                        InnerNode nNode = new InnerNode();
                        // nNode.parent = node;
                        node.addChild(0, nNode);
                        nextLevel.add(nNode);
                        nextions.add(ioc);
                        parents.put(nNode, e);
                    }
                }

                node.clear();
            }
            parents.remove(e);
        }
        level = nextLevel;
        ions = nextions;
    }

    /**
     * Close a node to the leaf node.
     *
     * @param node
     *            The node to be closed.
     * @param ion
     *            The instance ids on the node.
     * @return Leaf node.
     */
    private Leaf closeNode(Node node, int[] ion) {
        switch (type) {
            case CLASSIFICATION:
                return closeClassificationNode(node, ion);
            case REGRESSION:
                return closeRegressionNode(node, ion);
            case CBR_RDT:
                return closeCBRNode(node, ion);

        }
        return null;
    }

    /**
     * Close a node for classification.
     *
     * @param node
     *            The node to be closed.
     * @param ion
     *            The instance ids on the node.
     * @return Leaf node.
     */
    private Leaf closeClassificationNode(Node node, int[] ion) {
        Leaf leaf;
        if (node instanceof Leaf) {
            leaf = (Leaf) node;
        } else {
            leaf = new Leaf();
        }

        leaf.addDists(insts.getAttributes()[insts.getAttrSize() - clsSize]);//clsSize should be 1
        for (int i = 0; i < ion.length; i++) {
            int instId = ion[i];
            for (int j = 0; j < clsSize; j++) {
                leaf.incDist((int) insts.get(instId).getValue(
                        insts.getAttrSize() - 1));
            }
        }
        leaf.clear();
        for (int i = 0; i < leaf.dist.length; i++) {
            leaf.dist[i] /= ion.length;
        }
        leaf.size = ion.length;
        node = null;
        return leaf;
    }


    private Leaf closeRegressionNode(Node node, int[] ion) {

        Leaf leaf;
        if (node instanceof Leaf) {
            leaf = (Leaf) node;
        } else {
            leaf = new Leaf();
        }


        for (int i = 0; i < ion.length; i++) {
            leaf.addValue(insts.get(ion[i]).getValue(insts.getAttrSize() - clsSize));//clsSize should be 1

        }
        leaf.clear();

        leaf.v /= ion.length;
        leaf.size = ion.length;
        node = null;

        return leaf;
    }

    /**
     * Close a node for multi-label classification.
     *
     * @param node
     *            The node to be closed.
     * @param ion
     *            The instance ids on the node.
     * @return Leaf node.
     */
    private Leaf closeCBRNode(Node node, int[] ion) {
        Leaf leaf;
        if (node instanceof Leaf) {
            leaf = (Leaf) node;
        } else {
            leaf = new Leaf();
        }

        leaf.addDists(clsSize);
        for (int i = 0; i < ion.length; i++) {
            int instId = ion[i];
            int lnum = 0;
            for (int j = 0; j < clsSize; j++) {
                double t = insts.get(instId).getValue(
                        insts.getAttrSize() - clsSize + j);
                if (t == 1.0) {
                    leaf.incDist(j);
                    lnum++;
                }
            }
            leaf.addValue(lnum);
        }
        leaf.clear();
        for (int i = 0; i < leaf.dist.length; i++) {
            leaf.dist[i] /= ion.length;
        }
        leaf.v /= ion.length;
        leaf.size = ion.length;
        node = null;
        return leaf;
    }

    /**
     * Randomly select a attribute.
     *
     * @param node
     * @param root
     * @param inst
     * @return
     */
    private int selectAttr(InnerNode node, InnerNode root, Instance inst) {

        int[] attributes = insts.getAttributes();
        int attrSize = insts.getAttrSize() - clsSize;
        int t0 = rd.nextInt(attrSize);

        Node tNode = root;
        Set<Integer> usedAttrs = new HashSet<Integer>();
        while (tNode instanceof InnerNode) {
            InnerNode tn = (InnerNode) tNode;
            if (tn.attr == -1) {
                break;
            }
            if (attributes[tn.attr] > 0) {// nonominal attribute
                usedAttrs.add(tn.attr);
                tNode = tn.getChild((int) inst.getValue(tn.attr));
            } else {// numerical attribute
                double t = inst.getValue(tn.attr);
                if (t == Double.NaN) {
                    tNode = tn.getChild(0);
                } else if (t <= tn.split) {
                    tNode = tn.getChild(1);
                } else if (t > tn.split) {
                    tNode = tn.getChild(2);
                }
            }
        }

        boolean flag = true;
        int t1 = t0;
        while (usedAttrs.contains(t0)) {
            t0++;
            t0 %= attrSize;
            if (t0 == t1) {
                flag = false;
                break;
            }
        }

        return flag ? t0 : -1;
    }

}
