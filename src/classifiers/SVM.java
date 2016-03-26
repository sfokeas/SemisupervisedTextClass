package classifiers;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import misc.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by sotos on 3/9/16.
 */
public class SVM {
    Config config;
    svm_problem prob;
    svm_parameter param;

    public SVM(Config conf, svm_problem prob) {
        config = conf;
        this.prob = prob;
        param = new svm_parameter();
    }

    private static void GenerateLogSpace(int min, int max, int logBins) {
        double logarithmicBase = Math.E;
        double logMin = Math.log(min);
        double logMax = Math.log(max);
        double delta = (logMax - logMin) / logBins;
        int[] indexes = new int[logBins + 1];
        double accDelta = 0;
        float[] v = new float[logBins];
        for (int i = 0; i <= logBins; ++i) {
            v[i] = (float) Math.pow(logarithmicBase, logMin + accDelta);
            accDelta += delta;// accDelta = delta * i
        }
    }

    private void writeResults(double[] predicted) {
        try {

            Path p = Paths.get(config.getProperty("data.input"));
            String inputFilename = p.getFileName().toString();
            BufferedWriter rawResultsWriter = new BufferedWriter(new FileWriter(config.getProperty("general.outputDir") + "/"
                    + inputFilename
                    + "_" + config.getProperty("data.model")
                    + "_C_" + param.C
                    + "_predictedValues"));
            BufferedWriter measurementsWriter = new BufferedWriter(new FileWriter(config.getProperty("general.outputDir") + "/"
                    + inputFilename
                    + "_" + config.getProperty("data.model")
                    + "_C_" + param.C
                    + "_statistics"));

            //calculate and write quality of predictions measurements
            ArrayList<Double> classes = findClasess(); //TODO if number of classes in config file is not set
            double[][] confusionMatrix = new double[classes.size()][classes.size()]; //rows are the predictions, columns are true values
            //initialize confusionMatrix with zero
            for (int i = 0; i < classes.size(); i++) {
                for (int j = 0; j < classes.size(); j++) {
                    confusionMatrix[i][j] = 0;
                }
            }
            for (int i = 0; i < prob.l; i++) {
                confusionMatrix[classes.indexOf(predicted[i])][classes.indexOf(prob.y[i])]++;
            }

            //calculate precision, recall and F-score, BAC for in class X / not in class X binary decisions.
            for (int c = 0; c < classes.size(); c++) {
                double truePos = confusionMatrix[c][c]; //true positives
                double falsePos = 0, falseNeg = 0;
                for (int i = 0; i < classes.size(); i++) {
                    if (i == c) continue;
                    falsePos += confusionMatrix[c][i];
                    falseNeg += confusionMatrix[i][c];
                }
                double trueNeg = prob.l - truePos - falsePos - falseNeg; //tn = total - tp - fp - fn
                double prec = truePos / (truePos + falsePos);
                double recall = truePos / (truePos + falseNeg);
                double fscore = (2 * prec * recall) / (prec + recall);
                double specificity = trueNeg / (trueNeg + falsePos);
                double sensitivity = truePos / (truePos + falseNeg);
                double bac = (specificity + sensitivity) / 2;

                NumberFormat formatter = new DecimalFormat("#.###");
                measurementsWriter.write("Class " + classes.get(c) + "\t"
                        + "precision: " + formatter.format(prec) + "\t"
                        + "recall: " + formatter.format(recall) + "\t"
                        + "F-Score: " + formatter.format(fscore) + "\t"
                        + "specificity: " + formatter.format(specificity) + "\t"
                        + "sensitivity: " + formatter.format(sensitivity) + "\t"
                        + "BAC: " + formatter.format(bac) + "\n"
                );
            }
//            precision
//                    Precision = true_positive / (true_positive + false_positive)
//            recall
//
//                    Recall = true_positive / (true_positive + false_negative)
//
//            fscore
//
//            F-score = 2 * Precision * Recall / (Precision + Recall)
//
//            bac
//
//            BAC (Balanced ACcuracy) = (Sensitivity + Specificity) / 2,
//                    where Sensitivity = true_positive / (true_positive + false_negative)
//            and   Specificity = true_negative / (true_negative + false_positive)
//

            //calculate accuracy
            int total_correct = 0;
            for (int i = 0; i < prob.l; i++)
                if (predicted[i] == prob.y[i])
                    ++total_correct;
            measurementsWriter.write("General Cross Validation Accuracy = " + 100.0 * total_correct / prob.l + "%\n");
            double accuracy = total_correct / prob.l;

            //write predicted values
            rawResultsWriter.write("real\tpredicted\n");
            for (int i = 0; i < predicted.length; i++) {
                rawResultsWriter.write(Double.toString(prob.y[i]) + "\t" + Double.toString(predicted[i]) + "\n");
            }
            rawResultsWriter.flush();
            rawResultsWriter.close();
            measurementsWriter.flush();
            measurementsWriter.close();
        } catch (IOException e) {
            System.err.println(e.fillInStackTrace());
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param nr_fold (numbeer of folds)
     * @return
     */
    private void do_cross_validation(int nr_fold) {

        double[] target = new double[prob.l];
        svm.svm_cross_validation(prob, param, nr_fold, target); //the results of cv are return in the target variable
        writeResults(target);
        //TODO we shouldn't measure accuracy like this in multiclass cases. should use Balanced Error Rate (BER).
        //http://icapeople.epfl.ch/mekhan/pcml15/project-2/objectDetection.html
    }

    private ArrayList<Double> findClasess() {
        ArrayList<Double> classes = new ArrayList<Double>();
        for (int i = 0; i < prob.l; i++) {
            if (!classes.contains(prob.y[i])) {
                classes.add(prob.y[i]);
            }
        }
        return classes;
    }

    public void loadFile() {

    }


    //tunes C and g and returns the maximum
    //you can call it inside runSVM
    //notes: linear SVM has only one parameter, C.
    //
//    public  tuneParameters(){
//        //call docrossvalidation
//    }

//    public tuneSVM(){
//
//    }

    public void loadFile(String filename) {

    }


//
    //check this in svm_predict private static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException

    // check svm_train.readProblem() for a function to insert a problem from a file

    public void runSVM() {

        ///svm train parameters
        //default parameters
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.degree = 3;
        param.gamma = 0;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 40;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        //get rest parameters from config file
        //TODO
        //-----

        svm_model model = null;

        // train model
        if (config.getProperty("clf.crossValidation").trim().equalsIgnoreCase("true")) {
            do_cross_validation(Integer.parseInt(config.getProperty("clf.crossValidation.folds")));
        } else {
            model = svm.svm_train(prob, param);
        }

        // if config tuneParameters == true then call tune parameters
        //

        //use svm savemodel to save the model

        //this is the test data
        //how to output accuracy?


        //predict!
        //use svm loadmodel to load a saved model if needed

        //use predict


//        x[0] = new svm_node();
//        x[1] = new svm_node();
//        x[0].index = 1;
//        x[1].index = 2;
//
//        Graphics window_gc = getGraphics();
//        for (int i = 0; i < XLEN; i++)
//            for (int j = 0; j < YLEN ; j++) {
//                x[0].value = (double) i / XLEN;
//                x[1].value = (double) j / YLEN;
//                double d = svm.svm_predict(model, x);
//                if (param.svm_type == svm_parameter.ONE_CLASS && d<0) d=2;
//                buffer_gc.setColor(colors[(int)d]);
//                window_gc.setColor(colors[(int)d]);
//                buffer_gc.drawLine(i,j,i,j);
//                window_gc.drawLine(i,j,i,j);
//            }
//
//        double d = svm.svm_predict(model, x);
    }
}
