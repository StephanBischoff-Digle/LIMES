package org.aksw.limes.core.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.aksw.limes.core.gui.controller.MainController;
import org.aksw.limes.core.gui.view.MainView;
import org.apache.log4j.BasicConfigurator;

import java.util.Locale;

/**
 * Starts the LinkDiscovery Application, with Extra
 *
 * @author Daniel Obraczka {@literal <} soz11ffe{@literal @}
 *         studserv.uni-leipzig.de{@literal >}
 */
public class LimesGUI extends Application {

    /**
     * Main function Entry Point for the Application
     *
     * @param args
     *         optional arguments on StartUp, No Options implemented yet
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        BasicConfigurator.configure();
        launch(args);
    }

    /**
     * Opens a new Window for the Application
     * @param primaryStage View to initialize Application
     * @exception Exception Thrown if initialization didn't work properly
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainView mainView = new MainView(primaryStage);
        MainController mainController = new MainController(mainView);
        mainView.setController(mainController);
    }
}