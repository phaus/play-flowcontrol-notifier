/**
 * FlowControlHelper
 * 12.05.2012
 * @author Philipp Haussleiter
 *
 */
package de.javastream.flowcontrol.notifier;

import play.mvc.Http.Request;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;

public class FlowControlHelper extends Controller {

    private final static FlowControlHelper INSTANCE = new FlowControlHelper();
    private String url;
    private String apiKey;
    private String version = "2.2";
    private String hostname = null;

    public static FlowControlHelper getInstance() {
        return INSTANCE;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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

    public void sendThrowable(Throwable throwable, Request request) {
        sendException((Exception) throwable, request);
    }

    public void sendException(Exception exeption, Request request) {
        String notice = getNotice(apiKey, exeption, request);
        F.Promise<WS.HttpResponse> r1 = WS.url(url).setHeader("content-type", "text/xml; charset=utf-8").body(notice).timeout("60s").postAsync();
        F.Promise<List<WS.HttpResponse>> promises = F.Promise.waitAll(r1);
        await(promises, new F.Action<List<WS.HttpResponse>>() {

            public void invoke(List<WS.HttpResponse> httpResponses) {
                WS.HttpResponse response = httpResponses.get(0);
                if (response != null && Http.StatusCode.OK == response.getStatus()) {
                    Logger.info(response.getString() + " was sucessfully send!");
                } else {
                    Logger.warn("Notification was not send! Error: " + response.getStatus());
                }
            }
        });
    }

    public void send(Exception exeption) {
        sendException(exeption, null);
    }

    public String getNotice(String apiKey, Exception ex, Request request) {
        StringBuilder notice = new StringBuilder();
        notice.append("<notice version=\"").append(version).append("\">");
        notice.append("<api-key>").append(apiKey).append("</api-key>");
        notice.append("<notifier>");
        notice.append("<name>" + Version.NAME + "</name>");
        notice.append("<version>" + Version.VERSION + "</version>");
        notice.append("<url>" + Version.URL + "</url>");
        notice.append("</notifier>");
        notice.append("<error>");
        notice.append("<class>").append(ex.getClass().getName()).append("</class>");
        notice.append("<message>").append(ex.getLocalizedMessage()).append("</message>");
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
        req.append("<params>");
        if (request.params != null) {
            Map<String, String> data = request.params.allSimple();
            req.append(getValues(data));
        }
        req.append("</params>");
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
