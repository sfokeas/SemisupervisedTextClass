import cc.mallet.types.InstanceList;
import classifiers.SVM;
import feature_extraction.BOW;
import feature_extraction.BrownClustering;
import libsvm.svm_problem;
import misc.Config;
import misc.DataImporter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            //parse config file
            //config has some defaults, but they can be overridden by args
            final long startTime = System.currentTimeMillis();
            svm_problem problem = null;
            Config config = new Config(args[0]);
            //parse arguments
            // currently: application <config file location> <C> //C is the cost parameter of SVM
            if (args.length > 1) {
                config.setProperty("clf.svm.C", args[1]);
            }
            //Only one of the following four inputs are used each run. The priority goes like this: predicted > problem > instances > input.
            if (!config.getProperty("data.predictionsFile").trim().equalsIgnoreCase("")) {
                //take a file with predictions and extract measurements
                SVM svm = new SVM(config);
                svm.predFileToMeasurements();
            }
            else { //if the predictions file is not available use the the serialized problem object
                if (!config.getProperty("data.problem").trim().equalsIgnoreCase("")) {
                    FileInputStream fileIn = new FileInputStream(config.getProperty("data.problem"));
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    problem = (svm_problem) in.readObject();
                    in.close();
                    fileIn.close();
                } else { //if the problem object is not available use the serialized instances object
                    DataImporter importer = new DataImporter(config);
                    InstanceList instances = null;
                    if (!config.getProperty("data.instances").trim().equalsIgnoreCase("")) {
                        FileInputStream fileIn = new FileInputStream(config.getProperty("data.instances"));
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        instances = (InstanceList) in.readObject();
                        in.close();
                        fileIn.close();
                    } else { //if the serialized instances are not available extract them from input file
                        instances = importer.importPreprocessed();
                    }
                    if (config.getProperty("data.model").trim().equals("bow")) {
                        BOW bow = new BOW(instances, config);
                        problem = bow.formatSparseMatrix();
                        System.out.println("finished transforming features from bow to sparse matrix");
                        //bow.saveSparceMatrixBinary();
                    } else if (config.getProperty("data.model").trim().equals("brown")) {
                        BrownClustering brown = new BrownClustering(instances, config);
                        problem = brown.formatSparseMatrix();
                        System.out.println("finished transforming features from brown to sparse matrix");
                    }
                    // problem = FeatureScaling(problem) //TODO
                    if (config.getProperty("data.saveProblem").trim().equalsIgnoreCase("true")) {
                        //serialize problem object
                        Path pData = Paths.get(config.getProperty("data.input"));
                        String inputFilename = pData.getFileName().toString();
                        Path pBrown = Paths.get(config.getProperty("data.brown.paths"));
                        String brownPathsName = pBrown.getName(pBrown.getNameCount() - 1).toString();
                        FileOutputStream fileOut = new FileOutputStream(config.getProperty("general.outputDir") + "/"
                                + inputFilename
                                + "_" + config.getProperty("data.model")
                                + "_" + brownPathsName
                                + "_problem.ser");
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(problem);
                        out.close();
                        fileOut.close();
                        System.out.printf("finished Serializing problem object");
                    }
                }
                SVM svm = new SVM(config, problem);
                svm.runSVM(); //saves output to the specified file in config
                final long endTime = System.currentTimeMillis();
                System.out.println("Total execution time: " + (endTime - startTime));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            System.err.println(e.getCause());
        }
    }
}
