package komickeeper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

public class Main extends Application {
    public static Ini config;
    public static String databaseName;
    public static PrintWriter out;


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("jkomickeeper.fxml"));
        primaryStage.setTitle("KomicKeeper");
        primaryStage.setScene(new Scene(root, 815, 725));
        primaryStage.show();

    }


    public static void main(String[] args) {
        configHelper.makeConfigDirs();
        try {
            out = new PrintWriter("jkomickeeper.log");
        } catch (IOException e) {
            out = null;
        }
        config = configHelper.getSettingFile();

        databaseName = configHelper.getSetting(Objects.requireNonNull(config), "database", "comics.sqlite");
        dbHelper.makeDBIfNotExist(databaseName);


        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
        launch(args);
    }
}
