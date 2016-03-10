package misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by sotos on 3/9/16.
 */
public class Config extends Properties {
    //add functionality for configuration sanity checks

    //the constructor can check if the file is properly formatted and throw an exception. built your own exception?
    public Config(String configFile) throws IOException {

        //load?
        this.load(new InputStreamReader(new FileInputStream(configFile)));
        checkFormat();
    }

    private boolean checkFormat() {
        // if config.get property is not set ...
        // throw new ConfigFileException();
        //TODO
        return true;
    }
}
