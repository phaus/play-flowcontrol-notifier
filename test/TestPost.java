
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Test;
import de.javastream.flowcontrol.notifier.FlowControlHelper;
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

    private String url;
    private String apiKey;
    
    public static void main(String args[]) throws MalformedURLException {
        TestPost tp = new TestPost();
        tp.setup();
        tp.testPost();
        tp.testExtendedException();
        tp.testCustomException();
    }

    @BeforeClass
    public void setup(){
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
        helper.setUrl(url);
        helper.setApiKey(apiKey);
        boolean result = helper.send(new RuntimeException("fooooo"));
        assertTrue(result);
    }

    @Test
    public void testExtendedException() {
        FlowControlHelper helper = FlowControlHelper.getInstance();
        helper.setUrl(url);
        helper.setApiKey(apiKey);
        try {
            String word = null;
            word.length();
        } catch (NullPointerException npe) {
            boolean result = helper.send(npe);
            assertTrue(result);
        }

        try {
            String arr[] = {"1", "2"};
            for (int i = 0; i < 4; i++) {
                arr[i].length();
            }
        } catch (IndexOutOfBoundsException ioobe) {
            boolean result = helper.send(ioobe);
            assertTrue(result);
        }
    }

    @Test
    public void testCustomException() {
        FlowControlHelper helper = FlowControlHelper.getInstance();
        helper.setUrl(url);
        helper.setApiKey(apiKey);
        boolean result = helper.send(new TestException("this is a custom Test Exception"));
        assertTrue(result);
    }
}
