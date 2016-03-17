import classifiers.SVM;
import feature_extraction.BOW;
import feature_extraction.BrownClustering;
import libsvm.svm_problem;
import misc.Config;
import misc.DataImporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by sotos on 3/9/16.
 */
public class Textclassification {
    private static Textclassification ourInstance = new Textclassification();

    private Textclassification() {
    }

    public static Textclassification getInstance() {
        return ourInstance;
    }

    public static void main(String[] args) {
        try {
            //read config file
            //svm paremeters
            //cross-validation parameters
            //model parameters like min frequency
            //model to use
            //read args
            //config has some defaults, but they can be overridden by args
            //output file
            //input file
            //raw
            //specify a saved file
            //option compare?//compare many models together

            svm_problem problem = null;
            Config config = new Config(args[0]);
            DataImporter importer = new DataImporter(config);
            if (config.getProperty("data.model").trim().equals("bow")) {
                BOW bow = new BOW(importer.importPreprocessed(), config);
                problem = bow.formatSparseMatrix();
                System.out.println("finished transforming features from bow to sparse matrix");
                //bow.saveSparceMatrixBinary();
            } else if (config.getProperty("data.model").trim().equals("brown")) {
                BrownClustering brown = new BrownClustering(importer.importPreprocessed(), config);
                problem = brown.formatSparseMatrix();
                System.out.println("finished transforming features from brown to sparse matrix");
            }
            // problem = FeatureScaling(problem)

            FileOutputStream fileOut = new FileOutputStream("problem.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(problem);
            out.close();
            fileOut.close();
            System.out.printf("finished Serializing problem object");

            SVM svm = new SVM(config, problem);
            svm.runSVM(); //saves output to the specified file in config

        } catch (IOException e) {
            System.err.print(e.getMessage());
        }
    }
}
