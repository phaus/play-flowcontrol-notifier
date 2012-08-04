package controllers;

import play.exceptions.ActionNotFoundException;

public class Application extends ControllerNotifier {

    public static void index() {
        error("fooo");
    }
}
