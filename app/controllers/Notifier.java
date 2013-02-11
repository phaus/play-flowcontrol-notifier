/**
 * Notifier 04.08.2012
 *
 * @author Philipp Haussleiter
 *
 */
package controllers;

import jobs.Notify;
import play.Invoker.Suspend;
import play.Logger;
import play.exceptions.PlayException;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Scope.Session;
import play.mvc.Util;

public class Notifier extends Controller {


    @Util
    @Catch(value = PlayException.class)
    public static void catchPlayExceptions(PlayException exception) {
        if (exception instanceof Suspend) {
            // thanks to Israel Tsadok. Could leed to blacklisting or performance issues on the mailserver.
            // see https://github.com/maklemenz/errorMailer/issues/2
            return;
        }
        new Notify(exception, request, Session.current()).now();
        Logger.info("Notifier catched a PlayExceptions!");
    }
    
    @Util
    @Catch(value = Exception.class)
    public static void catchExceptions(Exception exception) {
        if (exception instanceof Suspend) {
            // thanks to Israel Tsadok. Could leed to blacklisting or performance issues on the mailserver.
            // see https://github.com/maklemenz/errorMailer/issues/2
            return;
        }
        new Notify(exception, request, Session.current()).now();
        Logger.info("Notifier catched an Exception!");
    }
}
