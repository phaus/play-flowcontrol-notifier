/**
 * ControllerNotifier
 * 04.08.2012
 * @author Philipp Haussleiter
 *
 */
package controllers;

import de.javastream.flowcontrol.notifier.FlowControlHelper;
import play.Play;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.results.Error;

public class ControllerNotifier extends Controller {

    private final static String API_KEY = Play.configuration.getProperty("flowcontrol.apikey");
    private final static String URL = Play.configuration.getProperty("flowcontrol.url");

    @Catch(Exception.class)
    public static void notify(Throwable e) {
        sendNotification(e);
        throw new Error(e.toString());
    }

    private static void sendNotification(Throwable e) {
        if (API_KEY != null && URL != null) {
            FlowControlHelper helper = FlowControlHelper.getInstance();
            helper.setApiKey(API_KEY);
            helper.setUrl(URL);
            helper.sendThrowable(e, request);
        }
    }
}
