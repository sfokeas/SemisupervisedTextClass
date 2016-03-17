package classifiers;

import libsvm.*;
import misc.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by sotos on 3/9/16.
 */
public class SVM {
    Config config;
    svm_problem prob;
    svm_parameter param;

    /**
     *
     * @param nr_fold
     * @return accuracy percentage (e.g. 0.6)
     */
    private double do_cross_validation(int nr_fold)
    {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

        svm.svm_cross_validation(prob,param,nr_fold,target); //the results are return in the target variable?
        for(i=0;i<prob.l;i++)
            if(target[i] == prob.y[i])
                ++total_correct;
        System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
        System.out.println(target);
        System.out.print("-_-_-_-");
        System.out.println(target.toString());
        //TODO we shouldn't measure accuracy like this in multiclass cases. should use Balanced Error Rate (BER).
        //http://icapeople.epfl.ch/mekhan/pcml15/project-2/objectDetection.html
        return total_correct/prob.l;
    }

    public void loadFile(){

    }

    public void loadFile(String filename){

    }

    public SVM(Config conf,svm_problem prob){
        config = conf;
        this.prob = prob;
        param = new svm_parameter();
    }

    public void runSVM(){

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
        if(config.getProperty("clf.crossValidation").trim().equals("true")){
            do_cross_validation(10);
        }
        else{
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


    //tunes C and g and returns the maximum
    //you can call it inside runSVM
//    public  tuneParameters(){
//        //call docrossvalidation
//    }

//
    //check this in svm_predict private static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException

    // check svm_train.readProblem() for a function to insert a problem from a file
}
