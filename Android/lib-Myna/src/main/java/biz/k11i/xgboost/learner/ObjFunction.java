package biz.k11i.xgboost.learner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Objective function implementations.
 */
public class ObjFunction implements Serializable {
    private static final Map<String, ObjFunction> FUNCTIONS = new HashMap<>();

    static {
        register("binary:logistic", new RegLossObjLogistic());
        register("binary:logitraw", new ObjFunction());
        register("multi:softmax", new SoftmaxMultiClassObjClassify());
        register("multi:softprob", new SoftmaxMultiClassObjProb());
        register("reg:linear", new ObjFunction());
    }

    /**
     * Gets {@link ObjFunction} from given name.
     *
     * @param name name of objective function
     * @return objective function
     */
    public static ObjFunction fromName(String name) {
        ObjFunction result = FUNCTIONS.get(name);
        if (result == null) {
            throw new IllegalArgumentException(name + " is not supported objective function.");
        }
        return result;
    }

    /**
     * Register an {@link ObjFunction} for a given name.
     *
     * @param name name of objective function
     * @param objFunction objective function
     */
    public static void register(String name, ObjFunction objFunction) {
        FUNCTIONS.put(name, objFunction);
    }

    /**
     * Transforms prediction values.
     *
     * @param preds prediction
     * @return transformed values
     */
    public double[] predTransform(double[] preds) {
        // do nothing
        return preds;
    }

    /**
     * Transforms a prediction value.
     *
     * @param pred prediction
     * @return transformed value
     */
    public double predTransform(double pred) {
        // do nothing
        return pred;
    }

    /**
     * Logistic regression.
     */
    static class RegLossObjLogistic extends ObjFunction {
        @Override
        public double[] predTransform(double[] preds) {
            for (int i = 0; i < preds.length; i++) {
                preds[i] = sigmoid(preds[i]);
            }
            return preds;
        }

        @Override
        public double predTransform(double pred) {
            return sigmoid(pred);
        }

        double sigmoid(double x) {
            return (1 / (1 + Math.exp(-x)));
        }
    }

    /**
     * Multiclass classification.
     */
    static class SoftmaxMultiClassObjClassify extends ObjFunction {
        @Override
        public double[] predTransform(double[] preds) {
            int maxIndex = 0;
            double max = preds[0];
            for (int i = 1; i < preds.length; i++) {
                if (max < preds[i]) {
                    maxIndex = i;
                    max = preds[i];
                }
            }

            return new double[]{maxIndex};
        }

        @Override
        public double predTransform(double pred) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Multiclass classification (predicted probability).
     */
    static class SoftmaxMultiClassObjProb extends ObjFunction {
        @Override
        public double[] predTransform(double[] preds) {
            double max = preds[0];
            for (int i = 1; i < preds.length; i++) {
                max = Math.max(preds[i], max);
            }

            double sum = 0;
            for (int i = 0; i < preds.length; i++) {
                preds[i] = exp(preds[i] - max);
                sum += preds[i];
            }

            for (int i = 0; i < preds.length; i++) {
                preds[i] /= (float) sum;
            }

            return preds;
        }

        @Override
        public double predTransform(double pred) {
            throw new UnsupportedOperationException();
        }

        double exp(double x) {
            return Math.exp(x);
        }
    }
}
