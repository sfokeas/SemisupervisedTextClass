package feature_extraction;

import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import misc.Config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

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
     * Saves instances into a format which libSVM can easily handle
     */
    public void saveLibSVMFormat() throws IOException {
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

}



