/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.05.19
 **/
package dice.tree.structure;

import java.util.Arrays;

import dice.util.BiArrays;


/**
 * Inner node of a tree.
 * @author Zhang Xiatian
 *
 */
public class InnerNode implements Node {


    private static final long serialVersionUID = -1715121901868539138L;

    /**
     * Split attribute id.
     */
    public int attr;

    /**
     * Split value of the split attribute.
     * It is only used for the numeric attribute.
     */
    public double split;

    /**
     * The index array of the children nodes.
     * For nominal split attribute, the number of the elements of this array
     * indicate the child node with the value of the attribute. For example,
     * the nominal split attribute have 0,1,2 three value, then the number
     * 0 means the child is the branch contains the value 0 of the attribute.
     * For numeric split attribute, there are only 3 possible number of the
     * array elements. 0, 1, and 2. 0 means missing value child, 1 indicate
     * the branch less or equals to the split value, and 2 indicate the
     * branch greater than the split value.
     */
    private int[] childrenIndex;

    /**
     * The array holds the children node of the node.
     * The corresponding value of these nodes can be found
     * in the {@link childrenIndex}.
     */
    public Node[] children;

    /**
     * Create an <code>InnerNode</code>
     */
    public InnerNode() {
        attr = -1;
    }

    @Override
    public void clear() {

        int c = 0;

        if (childrenIndex != null) {
            for (int index : childrenIndex) {
                if (index == -1) {
                    c++;
                } else {
                    break;
                }
            }

            int[] newIndex = new int[childrenIndex.length - c];
            System.arraycopy(childrenIndex, c, newIndex, 0, childrenIndex.length - c);

            childrenIndex = newIndex;

            Node[] newChildren = new Node[children.length - c];
            System.arraycopy(children, c, newChildren, 0, children.length - c);

            children = newChildren;

        }
    }

    /**
     * Create {@link #childrenIndex} and {@link #children} according
     * the assigned possible number of values of the split attribute.
     * This function not create the exactly the same number of
     * the parameter. It will claim a small array t avoid space waste.
     * In a deep branch, in some situation, the number of instances
     * may be less than the number of the possible values of the split
     * attributes. The array creation strategy is design to avoid that
     * situation.
     * @param attrVSize The possible number of values of the split attribute.
     */
    public void addChildren(int attrVSize) {

        int childrenSize = attrVSize < 3 ? 3 : (attrVSize / 10 + 3);

        children = new Node[childrenSize];
        childrenIndex = new int[childrenSize];
        for (int i = 0; i < childrenIndex.length; i++) {
            childrenIndex[i] = -1;
        }
    }

    /**
     * Add a child node for a split attribute value.
     * @param v The value of the split attribute.
     * @param cNode The child node.
     */
    public void addChild(int v, Node cNode) {

        if (childrenIndex[0] > -1) {//There are available position


            int newLength = (int) (childrenIndex.length / frac) + 1;
            int[] newIndex = new int[newLength];
            for (int i = 0; i < newLength - childrenIndex.length; i++) {
                newIndex[i] = -1;
            }
            System.arraycopy(childrenIndex, 0, newIndex, newLength - childrenIndex.length, childrenIndex.length);

            childrenIndex = newIndex;

            Node[] newChildren = new Node[newLength];
            System.arraycopy(children, 0, newChildren, newLength - children.length, children.length);

            children = newChildren;

        }

        childrenIndex[0] = v;
        children[0] = cNode;
        try {
            BiArrays.sort(childrenIndex, children);
        } catch (Exception e) {
            System.out.println(childrenIndex.length + ":" + children.length);
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Get the child node according the assigned
     * value of the split attribute.
     * @param v
     * @return
     */
    public Node getChild(int v) {
        int index = Arrays.binarySearch(childrenIndex, v);
        if (index < 0) {
            return null;
        }

        return children[index];
    }

}
