package assignment1;

import assignment1.controller.Controller;
import assignment1.model.ModelImpl;
import assignment1.view.InputUI;
import assignment1.view.SwingView;
import assignment1.view.WebView;

public class App {

    public static void main(String[] args) throws Exception {
        ModelImpl model = new ModelImpl();
        SwingView view = new SwingView(model);
        InputUI inputUI = new InputUI();
        Controller controller = new Controller(model);
        inputUI.addObserver(controller);
        view.display();
        inputUI.display();

        new WebView(model);
    }
}
