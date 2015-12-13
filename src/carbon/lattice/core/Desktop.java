/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice.core;

import carbon.lattice.Lattice;
import carbon.lattice.LatticeStage;
import carbon.lattice.WebCamApp;
import carbon.lattice.core.Service;
import carbon.lattice.core.Platform;
import carbon.lattice.core.Contact;
import de.codecentric.centerdevice.platform.osx.NSMenuBarAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;

/**
 *
 * @author Aniket
 */
public class Desktop extends Platform {

    @Override
    public void exit() {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @Override
    public File getFile(String s) {
        return new File(s);
    }

    @Override
    public void play(URL url) {

    }

    @Override
    public void showMessage(String mess, String title, Window w, AlertType ajl) {
        Alert al = new Alert(ajl);
        al.initOwner(w);
        al.setHeaderText(mess);
        al.setContentText(null);
        al.setTitle(title);
        al.showAndWait();
    }

    @Override
    public byte[] getBytes(Object ob) {
        if (ob instanceof Image) {
            Image im = (Image) ob;
            BufferedImage bImage = SwingFXUtils.fromFXImage(im, null);
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            try {
                ImageIO.write(bImage, "png", s);
            } catch (IOException ex) {
            }
            byte[] res = s.toByteArray();
            return res;
        }
        return null;
    }

    @Override
    public void takeCameraImage(ObjectProperty op) {
        WebCamApp ap = new WebCamApp(null);
        Optional<Image> sh = ap.showAndWait();
        if (sh.isPresent()) {
            op.set(ap.getCameraImage());
        }
    }

    @Override
    public void restart(Window w) {
        Lattice.newMessenger();
        ((Stage) w).close();
    }

    @Override
    public void findImage(ObjectProperty op) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose an image");
        ArrayList<String> al = new ArrayList<>();
        for (String s : javax.imageio.ImageIO.getReaderFileSuffixes()) {
            al.add("*" + s);
        }
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", al));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            op.set(new Image(f.toURI().toString()));
        }
    }

    @Override
    public void saveImage(Image im, String name, Contact con) {
        try {
            ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(im, null), "png", Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + con.getUsername() + File.separator + name));
        } catch (IOException ex) {
        }
    }

    @Override
    public Image loadImage(File F) {
        return new Image(F.toURI().toString());
    }

    @Override
    public void setMenuBar() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("mac")) {
            NSMenuBarAdapter adapter = new NSMenuBarAdapter();
            MenuBar menuBar = new MenuBar();
            MenuItem about = new MenuItem("Close Lattice");
            about.setOnAction((event) -> {
                javafx.application.Platform.exit();
                System.exit(0);
            });
            menuBar.getMenus().add(new Menu("Lattice"));
            menuBar.getMenus().get(0).getItems().add(0, about);
            adapter.setMenuBar(menuBar);
        }
    }

    @Override
    public void startRecording() {
    }

    @Override
    public void stopRecording() {
    }

    @Override
    public void playback() {
    }

}
