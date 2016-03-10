import feature_extraction.BOW;
import misc.Config;
import misc.DataImporter;

import java.io.IOException;
import java.util.Properties;

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

            Config config = new Config("config.conf");
            DataImporter importer = new DataImporter(config);
            BOW bow = new BOW(importer.importPreprocessed(),config);
            bow.saveLibSVMFormat();




        } catch (IOException e) {
            System.err.print(e.getMessage());
        }
    }
}
