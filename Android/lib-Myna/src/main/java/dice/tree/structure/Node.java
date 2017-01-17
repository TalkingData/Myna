/**
 * Copy Right Information  : Zhang Xiatian
 * Project                 : Dice
 * JDK version used        : jdk1.6
 * Comments                :
 * Version                 : 0.00
 * Modification history    : 2011.5.19
 **/
package dice.tree.structure;

import java.io.Serializable;

/**
 *
 * Node constructing a decision tree.
 * @author Zhang Xiatian
 *
 */
public interface Node extends Serializable {

    /**
     * An parameter for claiming or reclaiming the
     * space of the arrays may be used in <code>Node</code>
     */
    double frac = 0.75;

    /**
     * Clear the node to reduce the space consumption.
     */
    void clear();

}
