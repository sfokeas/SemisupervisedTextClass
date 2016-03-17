package feature_extraction;

import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import libsvm.svm_node;
import libsvm.svm_problem;
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

    public BrownClustering(InstanceList inst, Config conf) throws IOException {
        instances = inst;
        config = conf;
        mapIDsToClusters = new HashMap<Integer, ArrayList<Integer>>();
        loadClusters();
    }

    /**
     * loads a file which contains the paths of the words as outputted by wcluster and
     * inserts them into the into mapIDsToClusters.
     *
     * @throws IOException
     */
    private void loadClusters() throws IOException {
        //open file
        BufferedReader clustersFile = new BufferedReader(new FileReader(config.getProperty("data.brown.paths")));
        String line;
        while ((line = clustersFile.readLine()) != null) {
            String[] lineFields = line.split("[\\s]");
            String word = lineFields[1];
            String cluster = lineFields[0];
            for (int i = 1; i < cluster.length() + 1; i++) {
                String subcluster = "1"; //add one because some clusters start with 0
                subcluster = subcluster + cluster.substring(0, i);
                //push the integer representation of the the cluster into the map
                Integer wordID = instances.getAlphabet().lookupIndex(word); //	/** Returns -1 if entry isn't present. */
                if (wordID != -1) {
                    if (!mapIDsToClusters.containsKey(wordID)) {
                        mapIDsToClusters.put(wordID, new ArrayList<Integer>());
                    }
                    mapIDsToClusters.get(wordID).add(Integer.parseInt(subcluster, 2)); //parse the binary representation(2) as int.
                }

            }
        }
    }

    /**
     * Saves instances into a format which libSVM can easily handle
     */
    public void saveSparseMatrix() throws IOException {
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

            outputFile.print((String) inst.getLabeling().getBestLabel().getEntry() + " ");
            for (Map.Entry<Integer, Integer> entry : features.entrySet()) {
                outputFile.print(entry.getKey() + ":" + entry.getValue());
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
        for (int i = 0; i < instances.size(); i++) {
            Instance inst = instances.get(i);
            FeatureVector tokens = (FeatureVector) inst.getData();
            int[] indices = tokens.getIndices();
            double[] values = tokens.getValues();
            HashMap<Integer, Integer> features = new HashMap<Integer, Integer>(); //map from cluster to times of occurences for an instance
            for (int k = 0; k < indices.length; k++) {
                if (mapIDsToClusters.containsKey(indices[k])) {
                    ArrayList<Integer> clustersList = mapIDsToClusters.get(indices[k]);
                    for (int j = 0; j < clustersList.size(); j++) {
                        if (!features.containsKey(clustersList.get(j))) {
                            features.put(clustersList.get(j), (int) values[k]);
                        } else {
                            features.put(clustersList.get(j), features.get(clustersList.get(j)) + (int) values[k]);
                        }
                    }
                }
            }
            svm_node[] x = new svm_node[features.size()];
            int k = 0;
            for (Map.Entry<Integer, Integer> entry : features.entrySet()) {
                x[k] = new svm_node();
                x[k].index = entry.getKey(); //cluster
                x[k].value = entry.getValue(); //number of occurrences of this cluster.
                k++;
            }
            problem.y[i] = Double.parseDouble((String) inst.getTarget());
            problem.x[i] = x;
        }
        return problem;
    }


}
