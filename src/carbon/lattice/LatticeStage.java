/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.MenuPane.ContactButton;
import carbon.lattice.core.Contact;
import carbon.lattice.core.Service;
import carbon.lattice.core.SocketConnection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class LatticeStage extends Stage {

    public static String NATIVE;

    static {
        NATIVE = Lattice.class.getResource("css/flatterfx.css").toExternalForm();
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
        setScene(new Scene(new LoginPane(this)));
        getScene().getStylesheets().add(NATIVE);
        setOnCloseRequest((E) -> {
            E.consume();
            if (getScene().getRoot() instanceof LoginPane||((getScene().getRoot() instanceof SettingsPane)&&((SettingsPane)getScene().getRoot()).getLastNode() instanceof LoginPane)) {
                end();
            } else {
                Alert al = new Alert(Alert.AlertType.CONFIRMATION);
                al.setTitle("Logout");
                al.setHeaderText("Are you sure you want to Logout?");
                al.initOwner(this);
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                        end();
                    }
                }
            }
        });
    }

    public void end() {
        //save();
        try {
            SocketConnection.getConnection().close(false);
        } catch (Exception ex) {
        }
        Platform.exit();
        System.exit(0);
    }
/*
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
*/
    private void loadContacts() {
        List<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + "contacts.txt").toPath()));
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
            Files.write(Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + "contacts.txt").toPath(), al);
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
        getScene().setRoot((new Messenger(this, ms, reci, true)));
    }

    public void toMessenger(String s, Messenger mess) {
        getScene().setRoot((mess));
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
