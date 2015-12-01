/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.MenuPane.ContactButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class LatticeStage extends Stage {

    private static final String OS_ARCH = System.getProperty("ensemble.os.arch", System.getProperty("os.arch"));
    private static final String OS_NAME = System.getProperty("ensemble.os.name", System.getProperty("os.name"));
    static final boolean IS_IOS = "iOS".equals(OS_NAME) || "iOS Simulator".equals(OS_NAME);
    static final boolean IS_ANDROID = "android".equals(System.getProperty("javafx.platform")) || "Dalvik".equals(System.getProperty("java.vm.name"));
    static final boolean IS_EMBEDDED = "arm".equals(OS_ARCH) && !IS_IOS && !IS_ANDROID;
    static final boolean IS_DESKTOP = !IS_EMBEDDED && !IS_IOS && !IS_ANDROID;
    static final boolean IS_MAC = OS_NAME.startsWith("Mac");

    public static final DimensionProperty dimension;
    public static ArrayList<LatticeStage> stages;

    static {
        Rectangle2D vb = Screen.getPrimary().getVisualBounds();
        double WIDTH = vb.getWidth();
        double HEIGHT = vb.getHeight();
        dimension = new DimensionProperty(WIDTH, HEIGHT);
        dimension.addDimensionListener((newDim) -> {
            System.out.println(newDim.getWidth() + " " +  newDim.getHeight());
            System.out.println(dimension.getWidth()+" " + dimension.getHeight() );
        });
        stages = new ArrayList<>();
    }

    public static final ObjectProperty<String> CSS;
    public static String NATIVE;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println(os);
        CSS = new SimpleObjectProperty<>();
        if (os.contains("win")) {
            if (os.contains("7")) {
                CSS.set(Lattice.class.getResource("win7.css").toExternalForm());
            } else {
//              CSS.set(Lattice.class.getResource("flatterfx.css").toExternalForm());
                CSS.set(Lattice.class.getResource("JMetroLightTheme.css").toExternalForm());
//                CSS.set(Lattice.class.getResource("JMetroDarkTheme.css").toExternalForm());
            }
        } else if (os.contains("mac")) {
            CSS.set(Lattice.class.getResource("mac_os.css").toExternalForm());
        } else {
            CSS.set(Lattice.class.getResource("flatterfx.css").toExternalForm());
        }
        NATIVE = CSS.get();
    }

    public static int PORT = 16384;
    private static String name;
    private MenuPane ms;
    final static ArrayList<Contact> contacts = new ArrayList<Contact>() {
        @Override
        public boolean add(Contact e) {
            if (contains(e)) {
                return false;
            }
            return super.add(e);
        }
    };

    public LatticeStage() {
        setTitle("Lattice");
        loadContacts();
        if (IS_DESKTOP) {
            setScene(new Scene(new LoginPane(this)));
        } else {
            setScene(new Scene(new LoginPane(this), dimension.getWidth(), dimension.getHeight()));
        }
        Preferences.getPref().nativeUI.addListener((ob, older, newer) -> {
            if (newer) {
                getScene().getStylesheets().add(NATIVE);
            } else {
                getScene().getStylesheets().remove(NATIVE);
            }
        });
        if (Preferences.getPref().useNativeUI()) {
            getScene().getStylesheets().add(NATIVE);
        }
        setOnCloseRequest((E) -> {
            save();
            try {
                SocketConnection.getConnection().close(false);
            } catch (Exception ex) {
            }
            Platform.exit();
            System.exit(0);
        });
        stages.add(this);
    }

    public final void save() {
        System.out.println("Saving Cache");
        if (LatticeStage.getName() != null) {
            File cache = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator);
            if (!cache.exists()) {
                cache.mkdirs();
            }
            if (ms != null) {
                for (ContactButton m : ms.getButtons()) {
                    Contact con = m.getMScene().getRecipient();
                    File fs = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + con.getUsername() + File.separator);
                    if (!fs.exists()) {
                        fs.mkdirs();
                    }
                    fs = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + con.getUsername() + File.separator + "messages.txt");
                    ArrayList<Message> messages = m.getMScene().getMessages();
                    if (ms.hash.containsKey(m.getContact().getUsername())) {
                        messages.addAll(0, ms.hash.get(m.getContact().getUsername()));
                    }
                    System.out.println("SIZE" + messages.size());
                    try (FileOutputStream fout = new FileOutputStream(fs); ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                        for (Message ma : messages) {
                            oos.writeObject(ma);
                        }
                    } catch (IOException ex) {
                    }
                    int r = messages.size() - 1 - m.getCurrentNotify();
                    if (r != -1) {
                        Message last = messages.get(r);
                        SocketConnection.getConnection().sendLastMessage(m.getContact().getUsername(), last, LatticeStage.getName());
                    }
                    File emoj = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + m.getMScene().getRecipient().getUsername() + File.separator + "emoji.txt");
                    Menu j = m.getMScene().getEmoji();
                    ArrayList<String> paht = new ArrayList<>();
                    for (MenuItem im : j.getItems()) {
                        if (im instanceof EmojiMenuItem) {
                            EmojiMenuItem ei = (EmojiMenuItem) im;
                            paht.add(ei.getPath());
                        }
                    }
                    try {
                        Files.write(emoj, paht);
                    } catch (IOException ex) {
                    }
                    int count = 1;
                    File del = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + m.getMScene().getRecipient().getUsername() + File.separator + count + ".wav");
                    while (del.exists()) {
                        del.delete();
                        count++;
                        System.out.println(count);
                        del = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + m.getMScene().getRecipient().getUsername() + File.separator + count + ".wav");
                    }
                }
            }
            saveContacts();
        }
    }

    private void loadContacts() {
        List<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + "contacts.txt")));
        } catch (IOException ex) {
        }
        contacts.addAll(Contact.getContacts(al));
    }

    private void saveContacts() {
        ArrayList<String> al = new ArrayList<>();
        for (Contact ac : contacts) {
            al.add(ac.getName());
            al.add(ac.getUsername());
        }
        try {
            Files.write(Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + "contacts.txt"), al);
        } catch (IOException ex) {
        }
    }

    public static void setName(String s) {
        name = s;
    }

    public static String getName() {
        return name;
    }

    public static void setPort(int p) {
        PORT = p;
    }

    public static int getPort() {
        return PORT;
    }

    public void toMessenger(Contact reci) {
        if (reci == null && !IS_DESKTOP) {
            getScene().setRoot(new GetContact(ms));
        } else {
            getScene().setRoot((new Messenger(this, ms, reci, true)));
        }
    }

    public void toMessenger(String s, Messenger mess) {
        getScene().setRoot((mess));
    }

//    
    private class GetContact extends BorderPane {

        private final TextField name;
        private final Button ent, cancel;
        private final BorderPane top;

        public GetContact(MenuPane mp) {
            setTop(top = new BorderPane());
            top.setLeft(new Label("To : "));
            HBox box;
            top.setRight(box = new HBox(5, ent = new Button("Enter"), cancel = new Button("Cancel")));
            setPadding(new Insets(5));
            box.setPadding(new Insets(5));
            cancel.setOnAction((e) -> {
                LatticeStage.this.getScene().setRoot(mp);
            });
            top.setCenter(name = new TextField(""));
            name.setOnAction((e) -> {
                if (!name.getText().isEmpty()) {
                    if (name.getText().equals(LatticeStage.getName())) {
                        Service.get().showMessage("Cannot send message to yourself!", "Message Error", LatticeStage.this);
                        return;
                    }
                    SocketConnection.getConnection().usernameExists(name.getText());
                    if (!SocketConnection.getConnection().getReader().exists()) {
                        Service.get().showMessage("Username does not exist!", "Message Error", LatticeStage.this);
                        return;
                    }
                    Contact recipient = new Contact("", name.getText());

                    ContactButton mb = ms.getMButton(recipient);
                    if (mb != null) {
                        mb.transfer(getScene());
                    } else {
                        toMessenger(recipient);
                    }

                }
            });
            ent.setOnAction(name.getOnAction());
        }
    }

    public void toMenu(Messenger m) {
        if (m != null) {
            if (m.getMessages().isEmpty()) {
                ContactButton jk = m.getMButton();
                if (jk != null) {
                    VBox jka = (VBox) jk.getParent();
                    jka.getChildren().remove(jk);
                    ms.conf.remove(jk);
                }
            }
        }
        if (ms == null) {
            getScene().setRoot(ms = new MenuPane(this));
        } else {
            getScene().setRoot(ms);
        }
        if (SocketConnection.getConnection().getMessenger() == null) {
            SocketConnection.getConnection().startMessenger(ms);
        }
    }
}
