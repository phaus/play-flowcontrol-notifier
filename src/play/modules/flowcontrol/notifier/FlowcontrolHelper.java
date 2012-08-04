/**
 * FlowcontrolHelper
 * 12.05.2012
 * @author Philipp Haussleiter
 *
 */
package play.modules.flowcontrol.notifier;

import play.mvc.Http.Request;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import play.Play;
import play.exceptions.PlayException;
import play.mvc.Http.Header;
import play.mvc.Scope.Session;

public class FlowcontrolHelper {

    private final static FlowcontrolHelper INSTANCE = new FlowcontrolHelper();
    private String hostname = null;

    public static FlowcontrolHelper getInstance() {
        return INSTANCE;
    }

    private String getHostName() {
        if (hostname == null) {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                hostname = addr.getHostName();
            } catch (UnknownHostException ex) {
                hostname = "N/A";
            }
        }
        return hostname;
    }

    public String getNotice(String apiKey, Exception ex, Request request) {
        StringBuilder notice = new StringBuilder();
        notice.append("<notice version=\"").append(Version.API_VERSION).append("\">");
        notice.append("<api-key>").append(apiKey).append("</api-key>");
        notice.append("<notifier>");
        notice.append("<name>" + Version.NAME + "</name>");
        notice.append("<version>" + Version.VERSION + "</version>");
        notice.append("<url>" + Version.URL + "</url>");
        notice.append("</notifier>");
        notice.append("<error>");
        notice.append("<class>").append(ex.getClass().getName()).append("</class>");
        notice.append("<message>");
        if (ex instanceof PlayException) {
            PlayException pe = (PlayException) ex;
            notice.append(pe.getId()).append(" ");
        }
        notice.append(ex.getLocalizedMessage());
        notice.append("</message>");
        notice.append("<backtrace>");
        for (StackTraceElement ele : ex.getStackTrace()) {
            notice.append("<line method=\"").append(ele.getMethodName()).append("\" file=\"").append(ele.getFileName()).append("\" number=\"").append(ele.getLineNumber()).append("\" />");
        }
        notice.append("</backtrace>");
        notice.append("</error>");
        if (request != null) {
            notice.append(getRequest(request));
        }
        notice.append("<server-environment>");
        notice.append("<project-root>").append(Play.applicationPath).append("</project-root>");
        notice.append("<environment-name>").append(Play.id).append("</environment-name>");
        notice.append("<hostname>").append(getHostName()).append("</hostname>");
        notice.append("<app-version>").append(Play.version).append("</app-version>");
        notice.append("</server-environment>");
        notice.append("</notice>");
        return notice.toString();
    }

    public String getRequest(Request request) {
        StringBuilder req = new StringBuilder();
        req.append("<request>");
        req.append("<url>").append(request.url).append("</url>");
        req.append("<component>").append(request.controller).append("</component>");
        req.append("<action>").append(request.action).append("</action>");

        if (request.params != null) {
            req.append("<params>");
            Map<String, String> data = request.params.allSimple();
            req.append(getValues(data));
            req.append("</params>");
        }

        req.append("<cgi-data>");
        req.append("<var key=\"headers\">");
        if (request.headers != null) {
            Header header;
            for (String key : request.headers.keySet()) {
                header = request.headers.get(key);
                req.append("<var key=\"").append(key).append("\">").append(header.value()).append("</var>");
            }
        }
        req.append("</var>");
        req.append("<var key=\"HTTPS\">").append(request.secure ? "ON" : "OFF").append("</var>");
        req.append("<var key=\"SERVER_PORT\">").append(request.port).append("</var>");
        req.append("<var key=\"SERVER_NAME\">").append(request.host).append("</var>");
        req.append("<var key=\"PATH_INFO\">").append(request.path).append("</var>");

        req.append("</cgi-data>");
        Map<String, String> sessionData = Session.current().all();
        if (sessionData != null) {
            req.append("<session>");
            for (String key : sessionData.keySet()) {
                req.append("<var key=\"").append(key).append("\">").append(sessionData.get(key)).append("</var>");
            }
            req.append("</session>");
        }
        req.append("</request>");

        return req.toString();
    }

    public String getValues(Map<String, String> data) {
        StringBuilder d = new StringBuilder();
        if (data != null) {
            for (String key : data.keySet()) {
                d.append("<var key=\"").append(key).append("\">").append(data.get(key)).append("</var>");
            }
        }
        return d.toString();
    }
}
