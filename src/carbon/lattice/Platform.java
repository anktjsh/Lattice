/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.File;
import java.net.URL;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Window;

/**
 *
 * @author Aniket
 */
public abstract class Platform {

    public abstract void exit();

    public abstract File getFile(String s);

    public abstract void play(URL url);

    public abstract void showMessage(String mess, String title, Window w, AlertType aler);

    public abstract byte[] getBytes(Object ob);

    public abstract void takeCameraImage(ObjectProperty prop);

    public abstract void restart(Window w);

    public abstract void findImage(ObjectProperty op);

    public abstract void saveImage(Image im, String name, Contact con);

    public abstract Image loadImage(File F);

    public abstract void setMenuBar();

    public abstract void startRecording();

    public abstract void stopRecording();

    public abstract void playback();
}
