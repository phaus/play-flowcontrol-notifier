/**
 * Notifier
 * 04.08.2012
 * @author Philipp Haussleiter
 *
 */
package controllers;

import play.modules.flowcontrol.notifier.FlowcontrolHelper;
import java.util.List;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.WS;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Util;

public class Notifier extends Controller {

    private final static String API_KEY = Play.configuration.getProperty("flowcontrol.apikey");
    private final static String URL = Play.configuration.getProperty("flowcontrol.url");
    private final static FlowcontrolHelper HELPER = FlowcontrolHelper.getInstance();

    @Util
    @Catch(value = Exception.class)
    public static void catchExceptions(Exception exception) {
        if (API_KEY != null && URL != null) {
            Logger.error(exception, "Notifier catched an exception");
            String notice = HELPER.getNotice(API_KEY, exception, request);
            F.Promise<WS.HttpResponse> r1 = WS.url(URL).setHeader("content-type", "text/xml; charset=utf-8").body(notice).timeout("60s").postAsync();
            F.Promise<List<WS.HttpResponse>> promises = F.Promise.waitAll(r1);
            await(promises, new F.Action<List<WS.HttpResponse>>() {
                public void invoke(List<WS.HttpResponse> httpResponses) {
                    WS.HttpResponse response = httpResponses.get(0);
                    String noteId = "";
                    if (response != null && Http.StatusCode.OK == response.getStatus()) {
                        noteId = response.getString();
                        Logger.info(noteId + " was sucessfully send!");
                    } else {
                        Logger.warn("Notification was not send! Error: " + response.getStatus());
                    }
                }
            });
        } else {
            error(exception);
        }
        renderTemplate("errors/500"+request.format, exception);
    }
}
