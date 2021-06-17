package View;

import Model.IModel;
import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("MyView.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Super Mazes");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.show();
        playSound(new File("resources/music/mazeMusic.wav"));


        IModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);
        View.MyViewController view = fxmlLoader.getController();
        view.setViewModel(viewModel);
        view.greet();
    }

    public static synchronized void playSound(final File url) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
                    clip.open(inputStream);
                    clip.loop(-1);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
