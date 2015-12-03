/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.ProgressNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Simple Preloader Using the ProgressBar Control
 *
 * @author Aniket
 */
public class LatticeLoader extends Preloader {

    private ProgressBar bar;
    private Stage stage;

    private Scene createPreloaderScene() {
        bar = new ProgressBar();
        VBox p = new VBox(10);
        p.setPadding(new Insets(5, 10, 5, 10));
        p.setAlignment(Pos.CENTER);
        p.setStyle("-fx-background-color:lightblue");
        
        Text l, lk;
        p.getChildren().add(l = new Text("Welcome to Lattice Messenger"));
        ImageView im;
        bar.setPrefWidth(400);
        bar.setStyle("-fx-text-box-border: palegreen;\n"
                + "  -fx-control-inner-background: darkblue;");
        
        p.getChildren().add(im = new ImageView(stage.getIcons().get(0)));
        im.setFitHeight(150);
        im.setFitWidth(150);
        im.setPreserveRatio(true);
        p.getChildren().add(lk = new Text("Loading..."));
        
        l.setFont(new Font(14));
        lk.setFont(new Font(14));
        
        p.getChildren().add(bar);
        Scene s = new Scene(p, 500, 250);
        s.getStylesheets().add(LatticeStage.NATIVE);
        return s;
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("messenger.png")));
        stage.setScene(createPreloaderScene());
        stage.setAlwaysOnTop(true);
        stage.setTitle("Lattice");
        stage.setOnCloseRequest((e) -> {
            e.consume();
        });
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification scn) {
        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {
            ((new Thread(() -> {
                Platform.runLater(() -> {
                    bar.setProgress(1);
                    stage.hide();
                });
            }))).start();
        }
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        if (pn.getProgress() > 0.1) {
            bar.setProgress(pn.getProgress() - 0.1);
        } else {
            bar.setProgress(pn.getProgress());
        }
    }

}
