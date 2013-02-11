package controllers;

import play.mvc.Controller;
import play.mvc.With;

@With(Notifier.class)
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void error() {
        String[] arr = {"foo", "bar", "212312", "123123"};
        String value = arr[arr.length + 1];
        render(value);
    }
}
