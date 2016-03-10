package feature_extraction;

import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import misc.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sotos on 3/9/16.
 */
public class BrownClustering {
    InstanceList instances;
    HashMap<Integer, ArrayList<Integer>> mapIDsToClusters; //a map from words ids to classes this words belongs to
    Config config;

    BrownClustering() {

    }

    /**
     * loads a file which contains the paths of the words as outputted by wcluster and
     * inserts them into the into mapIDsToClusters.
     *
     * @throws IOException
     */
    private void loadClusters() throws IOException {
        //open file
        BufferedReader clustersFile = new BufferedReader(new FileReader(config.getProperty("brown.clustersFile")));
        String line;
        while ((line = clustersFile.readLine()) != null) {
            String[] lineFields = line.split("[\\s]");
            String word = lineFields[0];
            String cluster = lineFields[1];
            for (int i = 1; i < cluster.length(); i++) {
                String subcluster = "1"; //add one because some clusters start with 0
                subcluster = subcluster + cluster.substring(0, i);
                //push the integer representation of the the cluster into the map
                Integer wordID = instances.getAlphabet().lookupIndex(word);
                mapIDsToClusters.get(wordID).add(Integer.parseInt(subcluster, 2));
            }
        }
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
            HashMap<Integer, Integer> features = new HashMap<Integer, Integer>(); //map from cluster to times of occurences for an instance
            for (k = 0; k < indices.length; k++) {
                ArrayList<Integer> clustersList = mapIDsToClusters.get(indices[k]);
                for (int j = 0; j < clustersList.size(); j++) {
                    features.put(clustersList.get(j), features.get(clustersList.get(j)) + (int) values[k]);
                }
            }

            outputFile.print("1 ");
            for (Map.Entry<Integer, Integer> entry : features.entrySet()) {
                outputFile.print(entry.getKey() + ":" + entry.getValue());
            }
            outputFile.println();
        }
        outputFile.close();
    }

}
