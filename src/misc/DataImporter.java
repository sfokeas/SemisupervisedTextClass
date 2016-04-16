package misc;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.InstanceList;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * The DataImporter class is important to parse data and create instances.
 * * Instances can be considered as tuples of (Name, Label, Data).
 * There is a one ot one relation between documents and instances. One instance corresponds to one document in the
 * data-set.
 */
public class DataImporter {
    Pipe pipe;
    Config config;

    public DataImporter(Config conf) {
        config = conf;
    }

    //TODO constructor with config argument?

    private Pipe buildPipePreprocessed() {
        ArrayList pipeList = new ArrayList();

        pipeList.add(new Input2CharSequence("UTF-8"));


        //define the pattern for tokenization. The files have been preprocessed by the
        //preprocess scripts so it just splits on whitespace
        Pattern tokenPattern = Pattern.compile("[\\S+]+");
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

    public InstanceList importRaw() {
        //TODO
        return null;
    }

    public InstanceList importPreprocessed() throws IOException {
        pipe = buildPipePreprocessed();
        InstanceList instances = readFile(new File(config.getProperty("data.input")));
        return instances;
    }

    private InstanceList readFile(File inputFile) throws IOException {
        //
        // Create the instance list and open the input file
        //
        String lineRegex = "^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$";
        Reader fileReader;
        fileReader = new InputStreamReader(new FileInputStream(inputFile));
        CsvIterator iterator = new CsvIterator(fileReader, Pattern.compile(lineRegex), 3, 2, 1);
        InstanceList instances = new InstanceList(pipe);
        instances.addThruPipe(iterator);
        System.out.println("finished creating instances");

        //serialize the InstanceList for later reference
        if (config.getProperty("data.saveInstances").trim().equalsIgnoreCase("true")) {
            Path p = Paths.get(config.getProperty("data.input"));
            String inputFilename = p.getFileName().toString();
            FileOutputStream fileOut = new FileOutputStream(config.getProperty("general.outputDir") + "/" + inputFilename + "_instances.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(instances);
            out.close();
            fileOut.close();
            System.out.println("finished Serializing instances");
        }
        return instances;
    }


}
