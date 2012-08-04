
import de.javastream.flowcontrol.notifier.FlowControlHelper;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.junit.BeforeClass;
import play.test.UnitTest;

/**
 * TestPost
 * 12.05.2012
 * @author Philipp Haussleiter
 *
 */
public class TestPost extends UnitTest {

    private static String url;
    private static String apiKey;
    
    public static void main(String args[]) throws MalformedURLException {
        TestPost tp = new TestPost();
        setup();
        tp.testPost();
        tp.testExtendedException();
        tp.testCustomException();
    }

    @BeforeClass
    public static void setup(){
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File("test/test.properties")));
            url = props.getProperty("flowcontrol.url");
            apiKey = props.getProperty("flowcontrol.apikey");
        } catch (IOException ex) {
            throw new RuntimeException("cannot find test.properties!");
        }
    }

    @Test
    public void testPost() {
        FlowControlHelper helper = FlowControlHelper.getInstance();

    }

    @Test
    public void testExtendedException() {
        FlowControlHelper helper = FlowControlHelper.getInstance();
        try {
            String word = null;
            word.length();
        } catch (NullPointerException npe) {

        }

        try {
            String arr[] = {"1", "2"};
            for (int i = 0; i < 4; i++) {
                arr[i].length();
            }
        } catch (IndexOutOfBoundsException ioobe) {

        }
    }

    @Test
    public void testCustomException() {
        FlowControlHelper helper = FlowControlHelper.getInstance();
    }
}
