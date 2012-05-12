/**
 * FlowControlHelper
 * 12.05.2012
 * @author Philipp Haussleiter
 *
 */
package de.javastream.flowcontrol.notifier;

import play.mvc.Http.Request;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlowControlHelper {

    private final static FlowControlHelper INSTANCE = new FlowControlHelper();
    private URL url;
    private String apiKey;
    private String version = "2.2";
    private String env = "TestEnv";
    private String hostname = null;

    public static FlowControlHelper getInstance() {
        return INSTANCE;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(url + " is not a valid URL!");
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    private String getHostName() {
        if (hostname == null) {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                byte[] ipAddr = addr.getAddress();
                hostname = addr.getHostName();
            } catch (UnknownHostException ex) {
                hostname = "N/A";
            }
        }
        return hostname;
    }

    public boolean send(Exception exeption, Request request) {
        OutputStreamWriter wr = null;
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("content-type", "application/xml; charset=utf-8");

            wr = new OutputStreamWriter(httpConnection.getOutputStream());
            String notice = getNotice(apiKey, exeption, request);
            Logger.getLogger(FlowControlHelper.class.getName()).log(Level.INFO, "sending:\n{0}\n", notice);
            wr.write(notice);
            wr.flush();

            int status = httpConnection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                Logger.getLogger(FlowControlHelper.class.getName()).log(Level.WARNING, "status was: {0}", status);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(FlowControlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FlowControlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                wr.close();
            } catch (IOException ex) {
                Logger.getLogger(FlowControlHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public boolean send(Exception exeption) {
        return send(exeption, null);
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
        notice.append("<project-root>").append(getHostName()).append("</project-root>");
        notice.append("<environment-name>").append(env).append("</environment-name>");
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
            req.append("</request>");
        }
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
