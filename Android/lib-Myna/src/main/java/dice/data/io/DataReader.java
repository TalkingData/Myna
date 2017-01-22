/**
  * Copy Right Information  : Zhang Xiatian
  * Project                 : Dice
  * JDK version used        : jdk1.6
  * Comments                : 
  * Version                 : 0.00
  * Modification history    : 2011.05.20
  **/
package dice.data.io;

import dice.data.Instances;

/**
 * Read data from disk to the memory as {@link Instances}
 * @author Zhang Xiatian
 * @see Instances
 *
 */
public interface DataReader {

	/**
	 * Get the data set object {@link Instances}.
	 * @return The {@link Instances} object.
	 */
	Instances getInstances();
	
}
