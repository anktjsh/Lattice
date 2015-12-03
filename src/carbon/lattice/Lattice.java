/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.File;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class Lattice extends Application {

    @Override
    public void start(Stage m) {
        File f = Service.get().getFile("cache" + File.separator);
        System.out.println(f.exists());
        if (f.exists()) {
            recurse(f);
        }

        EmojiKeyboard.create();
        m = new LatticeStage();
        m.setResizable(false);
        m.getIcons().add(new Image(Lattice.class.getResourceAsStream("messenger.png")));
        m.show();

    }

    public void recurse(File f) {
        for (File fl : f.listFiles()) {
            if (fl.isDirectory()) {
                recurse(fl);
            } else {
                System.out.println(fl.getName() + " " + fl.getAbsolutePath());
            }
        }
    }

    public static void newMessenger() {
        SocketConnection.getConnection().close(true);
        Stage m = new LatticeStage();
        m.setResizable(false);
        m.getIcons().add(new Image(Lattice.class.getResourceAsStream("messenger.png")));
        m.show();
    }

    public static void signOut(LatticeStage ls) {
        ls.toMenu(null);
        ls.save();
        Service.get().restart(ls);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }
}
