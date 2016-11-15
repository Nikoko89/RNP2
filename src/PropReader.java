import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropReader {

    InputStream inputStream;
    Properties prop;

    public PropReader(String fileName) {
        try {
            prop = new Properties();

            inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                prop = null;
                throw new FileNotFoundException("property file '" + fileName + "' not found in the classpath");
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            System.err.println("Inputstream could not be closed");
        }

    }

    public Properties getProp() {
        return prop;
    }
}
