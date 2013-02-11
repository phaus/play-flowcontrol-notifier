/**
 * Notify 11.02.2013
 *
 * @author Philipp Haussleiter
 *
 */
package jobs;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.libs.WS;
import play.modules.flowcontrol.notifier.FlowcontrolHelper;
import play.mvc.Http;
import play.mvc.Scope;

public class Notify extends Job {

    private final static String API_KEY = Play.configuration.getProperty("flowcontrol.apikey");
    private final static String URL = Play.configuration.getProperty("flowcontrol.url");
    private final static FlowcontrolHelper HELPER = FlowcontrolHelper.getInstance();
    private Exception exception;
    private Http.Request request;
    private Scope.Session session;
    public Notify(Exception exception, Http.Request request, Scope.Session session){
        this.exception = exception;
        this.request = request;
        this.session = session;
    }

    @Override
    public void doJob() {
        if (API_KEY != null && URL != null) {
            String notice = HELPER.getNotice(API_KEY, exception, request, session);
            WS.HttpResponse r1 = WS.url(URL).setHeader("content-type", "text/xml; charset=utf-8").body(notice).timeout("60s").post();
            if (r1 != null && Http.StatusCode.OK == r1.getStatus()) {
                String noteId = r1.getString();
                Logger.info(noteId + " was sucessfully send!");
            } else {
                Logger.warn("Notification was not send! Error: " + r1.getStatus());
            }
            Logger.error(exception, "Notifier catched an exception");
        }
    }
}
