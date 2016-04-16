package feature_extraction;

import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import libsvm.svm_node;
import libsvm.svm_problem;
import misc.Config;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by sotos on 3/8/16.
 */
public class BOW {

    InstanceList instances;
    Config config;

    public BOW(InstanceList inst, Config conf) {
        instances = inst;
        config = conf;
    }

    /**
     * Saves instances to disk, in a format which libSVM can easily handle
     */
    public void saveSparceMatrixBinary() throws IOException {
        PrintWriter outputFile = new PrintWriter(config.getProperty("data.output_file")); //this is the file libSVM will use.
        int i = 0;
        for (i = 0; i < instances.size(); i++) {
            Instance inst = instances.get(i);
            FeatureVector tokens = (FeatureVector) inst.getData();
            int[] indices = tokens.getIndices();
            double[] values = tokens.getValues();
            int k;
            for (k = 0; k < indices.length; k++) {
                int value = 0;
                if (values[k] > 0) {
                    value = 1;
                }
                outputFile.print("1 " + indices[k] + ":" + value + " ");
            }
            outputFile.println();
        }
        outputFile.close();
    }

    public void saveSparseMatrix() throws IOException {
        PrintWriter outputFile = new PrintWriter(config.getProperty("data.output_file")); //this is the file libSVM will use.
        int i = 0;
        for (i = 0; i < instances.size(); i++) {
            Instance inst = instances.get(i);
            FeatureVector tokens = (FeatureVector) inst.getData();
            int[] indices = tokens.getIndices();
            double[] values = tokens.getValues();
            int k;
            for (k = 0; k < indices.length; k++) {
                outputFile.print((String) inst.getLabeling().getBestLabel().getEntry() + " " + indices[k] + ":" + values[k] + " ");
            }
            outputFile.println();
        }
        outputFile.close();
    }

    public svm_problem formatSparseMatrix() throws IOException {
        svm_problem problem = new svm_problem();
        problem.l = instances.size();
        problem.y = new double[problem.l];
        problem.x = new svm_node[problem.l][];
        int i = 0;
        for (i = 0; i < instances.size(); i++) { //each instance corresponds to a document
            Instance inst = instances.get(i);
            FeatureVector tokens = (FeatureVector) inst.getData();
            int[] indices = tokens.getIndices();
            double[] values = tokens.getValues();
            int k;

            svm_node[] x = new svm_node[indices.length];
            for (k = 0; k < indices.length; k++) {
                x[k] = new svm_node();
                x[k].index = indices[k];
                x[k].value = values[k];

            }
            problem.y[i] = Double.parseDouble((String) inst.getTarget());
            problem.x[i] = x;
        }
        return problem;
    }

    /**
     * similar to formatSparseMatrix, but has only 1 or 0 as features, corresponding to the presence or not of the
     * @return
     * @throws IOException
     */
    public svm_problem formatSparseMatrixBinary() throws IOException {
        svm_problem problem = new svm_problem();
        problem.l = instances.size();
        problem.y = new double[problem.l];
        problem.x = new svm_node[problem.l][];
        int i = 0;
        for (i = 0; i < instances.size(); i++) { //each instance corresponds to a document
            Instance inst = instances.get(i);
            FeatureVector tokens = (FeatureVector) inst.getData();
            int[] indices = tokens.getIndices();
            double[] values = tokens.getValues();
            int k;

            svm_node[] x = new svm_node[indices.length];
            for (k = 0; k < indices.length; k++) {
                x[k] = new svm_node();
                x[k].index = indices[k];
                x[k].value = values[k];

            }
            problem.y[i] = Double.parseDouble((String) inst.getTarget());
            problem.x[i] = x;
        }
        return problem;
    }
}



