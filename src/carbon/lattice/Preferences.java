/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class Preferences {

    private static Preferences pref;

    public static Preferences getPref() {
        if (pref == null) {
            pref = new Preferences();
        }
        return pref;
    }

    public final BooleanProperty soundOn;
    public final BooleanProperty showNotifications;
    public final BooleanProperty nativeUI;
    public final BooleanProperty showErrorSend;

    private Preferences() {
        soundOn = new SimpleBooleanProperty(this, "Sound On", true);
        showNotifications = new SimpleBooleanProperty(this, "Show Notifications", true);
        nativeUI = new SimpleBooleanProperty(this, "Use Native UI", true);
        showErrorSend = new SimpleBooleanProperty(this, "Show User Offline Message", true);
        load();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
        }));
    }

    private void save() {
        File fi = Service.get().getFile("cache" + File.separator + "preferences.txt");
        ArrayList<String> str = new ArrayList<>();

        str.add("" + soundOn.get());
        str.add("" + showNotifications.get());
        str.add("" + nativeUI.get());
        str.add("" + showErrorSend.get());
        try {
            Files.write(fi, str);
        } catch (IOException ex) {
        }
    }

    private void load() {
        File fi = Service.get().getFile("cache" + File.separator + "preferences.txt");
        if (fi.exists()) {
            try {
                List<String> str = Files.readAllLines(fi);
                if (str.size() == allProperties().size()) {
                    soundOn.set(Boolean.parseBoolean(str.get(0)));
                    showNotifications.set(Boolean.parseBoolean(str.get(1)));
                    nativeUI.set(Boolean.parseBoolean(str.get(2)));
                    showErrorSend.set(Boolean.parseBoolean(str.get(3)));
                }
            } catch (IOException ex) {
            }
        }
    }

    public boolean isSoundOn() {
        return soundOn.get();
    }

    public boolean showNotifications() {
        return showNotifications.get();
    }

    public ObservableList<BooleanProperty> allProperties() {
        return FXCollections.observableArrayList(soundOn, showNotifications, nativeUI, showErrorSend);
    }

    public boolean useNativeUI() {
        return nativeUI.get();
    }

    public boolean showErrorSend() {
        return showErrorSend.get();
    }

}
