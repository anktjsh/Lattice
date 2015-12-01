/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.Messenger.MessageBox;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Menu;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class MenuPane extends BorderPane {

    private int lastLoaded;
    private final BorderPane top, topTop;
    private final Button edit, write, signout, settings;
    private final Text status, title, time, server, name;
    private final VBox container;
    final ArrayList<ContactButton> conf = new ArrayList<>();
    private final LatticeStage stage;
    private final HBox topTopCenter;
    final TreeMap<String, ArrayList<Message>> hash = new TreeMap<>();

    private final ChangeListener<Boolean> conn;

    private final IntegerProperty notify = new SimpleIntegerProperty(0);
    final ObservableList<MessageBox> undelivered;

    public MenuPane(LatticeStage hj) {
        setStyle("-fx-background-color:white;");
        setPadding(new Insets(5, 10, 5, 10));
        setMinWidth(450);
        undelivered = FXCollections.observableArrayList();
        stage = hj;
        top = new BorderPane();
        top.setStyle("-fx-background-color:white;");
        top.setPadding(new Insets(0, 2, 5, 2));
        topTop = new BorderPane();
        topTop.setPadding(new Insets(0, 2, 5, 2));
        top.setTop(topTop);
        BorderPane.setAlignment(topTop, Pos.CENTER);
        status = new Text(SocketConnection.getConnection().connected.get() ? "Connected" : "Offline");
        status.setFont(new Font(16));
        settings = new Button("", new ImageView(new Image(getClass().getResourceAsStream("settings.png"), 25, 25, true, true)));

        settings.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        settings.setMaxSize(30, 30);
        settings.setOnAction((e) -> {
            getScene().setRoot(new SettingsPane(this));
        });
        topTopCenter = new HBox(15);
        topTopCenter.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(topTopCenter, Pos.CENTER);
        topTopCenter.getChildren().add(status);
        topTop.setCenter(topTopCenter);
        topTopCenter.getChildren().add(0, name = new Text(LatticeStage.getName()));
        name.setFont(new Font(16));
        time = new Text("");
        time.setFont(new Font(16));
        topTopCenter.getChildren().add(1, time);
        topTopCenter.getChildren().add(0, signout = new Button("Sign Out"));
        topTopCenter.getChildren().add(settings);
        signout.setFont(new Font(16));
        (new Thread(new TimeThread())).start();
        BorderPane.setAlignment(name, Pos.CENTER);

        top.setRight(write = new Button("New"));
        write.setContentDisplay(ContentDisplay.TOP);
        write.setFont(new Font(10));
        write.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("write.png"), 30, 30, true, true)));
        conn = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    status.setText("Connected");
                    write.setDisable(false);
                } else {
                    Service.get().showMessage("Connection from Server Severed", "Server Error", stage);
                    write.setDisable(true);
                    SocketConnection.getConnection().connected.removeListener(conn);
                    status.setText("Offline");
                    Lattice.signOut(stage);
                }
            }
        };
        SocketConnection.getConnection().connected.addListener(conn);
        signout.setOnAction((e) -> {
            write.setDisable(true);
            status.setText("Offline");
            SocketConnection.getConnection().connected.removeListener(conn);
            Lattice.signOut(stage);
        });
        server = new Text();
        server.setOnMouseClicked((E) -> {
//            double width = server.getWidth();
//            TextField tt;
//            topTopCenter.getChildren().set(topTopCenter.getChildren().indexOf(server), tt = new TextField());
//            tt.setMaxWidth(width);
//            tt.setText(server.getText());
//            tt.setOnAction((gE) -> {
//                if (tt.getText().length() > 0) {
//                    topTopCenter.getChildren().set(topTopCenter.getChildren().indexOf(tt), server);
//                }
//            });
        });
        server.setFont(new Font(16));
        title = new Text("Messages");
        notify.addListener((ob, older, newer) -> {
            if (newer.intValue() == 0) {
                title.setText("Messages");
                for (ContactButton mb : conf) {
                    mb.getMScene().getBack().setText("<Back");
                }
            } else {
                title.setText("Message (" + newer.intValue() + ")");
                for (ContactButton mb : conf) {
                    mb.getMScene().getBack().setText("<Back (" + newer.intValue() + ")");
                }
            }
        });
        title.setFont(new Font(16));
        BorderPane.setAlignment(top, Pos.CENTER);
        BorderPane.setAlignment(title, Pos.CENTER);
        top.setCenter(title);
        top.setLeft(edit = new Button("Edit"));
        edit.setContentDisplay(ContentDisplay.TOP);
        edit.setFont(new Font(10));
        edit.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("delete.png"), 30, 30, true, true)));
        edit.setOnAction((e) -> {
            if (!conf.isEmpty()) {
                if (conf.get(0).getAction().getText().equals(">")) {
                    for (ContactButton k : conf) {
                        k.getAction().setStyle("-fx-text-fill:white;-fx-background-color:red");
                        k.getAction().setText("x");
                    }
                } else {
                    for (ContactButton k : conf) {
                        k.getAction().setText(">");
                        k.getAction().setStyle("");
                    }
                }
            }
        });
        setTop(top);
        container = new VBox();
        setCenter(container);
        container.setMinHeight(600);
        write.setOnAction((e) -> {
            stage.toMessenger(null);
        });

        ArrayList<String> str = SocketConnection.getConnection().getRecipients(LatticeStage.getName());
        System.out.println(str);
        for (String s : str) {
            File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + s + File.separator);
            if (!f.exists()) {
                System.out.println(f.mkdirs());
            }
        }
//        ZCHANGE
//        if (LatticeStage.IS_DESKTOP) {
        Stage al = new Stage();
        al.setResizable(false);
        al.getIcons().addAll(stage.getIcons());
        al.setWidth(stage.getWidth());
        al.setHeight(stage.getHeight() / 2);
        BorderPane bor;
        al.setScene(new Scene(bor = new BorderPane(new ProgressIndicator(-1)), Color.DARKBLUE));
        bor.setStyle("-fx-background-color:darkblue;");
        al.initOwner(stage);
        al.initModality(Modality.APPLICATION_MODAL);
        al.setOnCloseRequest((e) -> {
            e.consume();
        });

        new Thread(() -> {
            load(al);
        }).start();
        al.showAndWait();
//        } else {
////            new Thread(() -> {
//            load(null);
////            }).start();
//        }

    }

    public int getLastLoaded() {
        return lastLoaded;
    }

    public void setLastLoaded(int n) {
        lastLoaded = n;
    }

    private void load(Stage al) {
        File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator);
        System.out.println(f.exists());
        ArrayList<File> fil = new ArrayList<>();
        if (f.exists()) {
            File[] lis = f.listFiles();
            for (File fa : lis) {
                System.out.println(fa.getName());
                if (fa.isDirectory()) {
                    fil.add(fa);
                }
            }
        }
        System.out.println(fil.size());
        for (File fl : fil) {
            if (!fl.getName().equals(LatticeStage.getName())) {
                ArrayList<Message> messages = new ArrayList<>();
                File mes = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + fl.getName() + File.separator + "messages.txt");
                System.out.println(mes.exists());
                if (mes.exists()) {
                    try (FileInputStream fout = new FileInputStream(mes); ObjectInputStream oos = new ObjectInputStream(fout)) {
                        Object in;
                        while ((in = oos.readObject()) != null) {
                            System.out.println("Load Message");
                            messages.add((Message) in);
                        }
                    } catch (FileNotFoundException ex) {
                    } catch (IOException | ClassNotFoundException ex) {
                    }
                }
                Message last = SocketConnection.getConnection().getLastMessage(fl.getName(), LatticeStage.getName());
                ContactButton mb = new ContactButton(new Contact("", fl.getName()), "", null);
                conf.add(mb);
                System.out.println("Load : " + messages.size());
                List<Message> retret = SocketConnection.getConnection().retrieveMessages(true, fl.getName(), messages.isEmpty() ? new Message(null, null, Message.TEXT, null, null, null, null) : messages.get(messages.size() - 1));
                for (Message js : retret) {
                    messages.add(js);
                }
                System.out.println("received " + retret.size());
                if (!messages.isEmpty()) {
                    int notif = 0;// = retret.size();

                    int message = getMessage(messages, last);
                    System.out.println(message);
                    System.out.println(messages.size());
                    if (message != -1) {
                        notif += ((messages.size() - (message + 1)));
                    }
                    System.out.println(notif);
                    if (notif != -1 && notif != 0) {
                        for (int x = messages.size() - notif; x < messages.size(); x++) {
                            mb.setLast(messages.get(x), true, false);
                        }
                    } else {
                        mb.setLast(messages.get(messages.size() - 1), false, false);
                    }
                    if (messages.size() >= 50) {
                        for (int x = 0; x < 50; x++) {
                            mb.add(0, messages.remove(messages.size() - 1));
                        }
                    } else {
                        for (int x = messages.size() - 1; x >= 0; x--) {
                            mb.add(0, messages.remove(messages.size() - 1));
                        }
                    }
                    hash.put(mb.getContact().getUsername(), messages);

                } else {
                    conf.remove(mb);
                }
                List<String> em = null;
                try {
                    em = Files.readAllLines(Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + fl.getName() + File.separator + "emoji.txt"));
                } catch (IOException fe) {
                }
                if (em != null) {
                    Menu cm = mb.getMScene().getEmoji();
                    for (String sa : em) {
                        cm.getItems().add(new EmojiMenuItem(mb.getMScene(), sa));
                    }
                }
                //removeDuplicates(mb.getMScene().getMessage().getChildren());
            }
        }

        Platform.runLater(() -> {
            container.getChildren().clear();
            Collections.sort(conf);
            container.getChildren().addAll(conf);
            if (al != null) {
                al.close();
            }
        });
    }

    public int getMessage(List<Message> mes, Message m) {
        if (m != null) {
            for (int x = 0; x < mes.size(); x++) {
                if (mes.get(x).getTo().equals(m.getTo())) {
                    if (mes.get(x).getFrom().equals(m.getFrom())) {
                        if (mes.get(x).getText().equals(m.getText())) {
                            if (mes.get(x).getMetadata().equals(m.getMetadata())) {
                                if (mes.get(x).getTimeSent().equals(m.getTimeSent())) {
                                    return x;
                                }
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    static ArrayList<Node> removeDuplicates(List<Node> list) {
        ArrayList<Node> result = new ArrayList<>();
        HashSet<Node> set = new HashSet<>();
        for (Node item : list) {
            if (!set.contains(item)) {
                result.add(item);
                set.add(item);
            }
        }
        return result;
    }

    ArrayList<String> getContacts() {
        ArrayList<String> temp = new ArrayList<>();
        File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator);
        if (f.exists()) {
            File[] li = f.listFiles();
            for (File fa : li) {
                if (fa.isDirectory()) {
                    temp.add(fa.getName());
                }
            }
        }
        return temp;
    }

    ArrayList<ContactButton> getButtons() {
        return conf;
    }

    public void create(Messenger ms, Contact recipient) {
        ContactButton temp;
        container.getChildren().add(0, temp = new ContactButton(recipient, "", ms));
        conf.add(temp);
    }

    public void update(Object obj) {
        if (obj instanceof Message) {
            update((Message) obj);
        } else if (obj instanceof Integer) {
            update((Integer) obj);
        }
    }

    private void update(Message message) {
        String s = message.getFrom();
        ContactButton ch = getMButton(new Contact("", s));
        if (ch != null) {
            ch.update(message);
            if (!ch.getContact().isOnline()) {
                ch.getContact().setOnline(true);
            }
        } else {
            ch = new ContactButton(new Contact("", s), "", null);
            final ContactButton jk = ch;
            conf.add(jk);
            Platform.runLater(() -> {
                container.getChildren().add(0, jk);
            });
            if (!ch.getContact().isOnline()) {
                ch.getContact().setOnline(true);
            }
            jk.update(message);
        }
    }

    private void update(Integer i) {
        if (i == -1) {
            if (!undelivered.isEmpty()) {
                Platform.runLater(() -> {
                    for (int x = undelivered.size() - 1; x >= 0; x--) {
                        MessageBox lastSentMessage = undelivered.get(x);
                        Node j = (Node) lastSentMessage.getChildren().get(lastSentMessage.getChildren().size() - 1);
                        if (!(j instanceof Button) || ((Button) j).getGraphic() != null) {
                            if (Preferences.getPref().showErrorSend()) {
//                                PopOver pop;
                                Button canc;
                                lastSentMessage.getChildren().add(canc = new Button("User Offline"));
//                                pop = new PopOver(canc);
//                                Button but;
//                                VBox vb;
//                                pop.setContentNode(vb = new VBox(5, new Text("User will still receive message"), but = new Button("Remove this Message")));
//                                vb.setPadding(new Insets(5, 10, 5, 10));
                                canc.setStyle("-fx-text-fill:red;");
                                canc.setFont(new Font(16));
                                canc.setWrapText(true);
                                Messenger ms = getMButton(lastSentMessage.getRecipient()).getMScene();
                                ms.setDeliver();
//                                but.setOnAction((e) -> {
//                                    lastSentMessage.getChildren().remove(canc);
//                                    lastSentMessage.getChildren().add(new Button(""));
//                                    if (pop.isShowing()) {
//                                        pop.hide();
//                                    }
//                                });
                                canc.setOnAction((e) -> {
                                    Service.get().showMessage("User will still receive message", "User Offline", stage);
                                });
//                                canc.setOnAction((e) -> {
//                                    pop.show(canc);
//                                });
                            } else {
                                Button b;
                                lastSentMessage.getChildren().add(b = new Button(""));
                                Messenger ms = getMButton(lastSentMessage.getRecipient()).getMScene();
                                ms.setDeliver();
                            }
                        }
                    }
                    MessageBox remove = undelivered.remove(0);
                    ContactButton mb = getMButton(remove.getRecipient());
                    mb.setLast(remove.getLast(), remove.getNotify(), false);
                    if (remove.getRecipient().isOnline()) {
                        remove.getRecipient().setOnline(false);
                    }
                });
            }
        } else {
            if (!undelivered.isEmpty()) {
                Platform.runLater(() -> {
                    MessageBox remove = undelivered.remove(0);
                    ContactButton mb = getMButton(remove.getRecipient());
                    if (!remove.getRecipient().isOnline()) {
                        remove.getRecipient().setOnline(true);
                    }
                    mb.setLast(remove.getLast(), remove.getNotify(), false);
                    mb.getMScene().removal();
                });
            }
        }
    }

    public int getNotifications() {
        return notify.get();
    }

    ContactButton getMButton(Contact cont) {
        for (ContactButton fsdfs : conf) {
            if (fsdfs.getContact().equals(cont)) {
                return fsdfs;
            }
        }
        return null;

    }

    class ContactButton extends BorderPane implements Comparable<ContactButton> {

        private Contact contact;
        private String last;
        private final Messenger connt;
        private final Button go, audio;
        private final Text tes;
        private final Text tem;
        private final IntegerProperty currentNotify;
        private final HBox right, left;
        private boolean connectionSent;
//
//        public ContactButton(Contact r, String l) {
//            this(r, l, null);
//        }

        public ContactButton(Contact r, String l, Messenger jk) {
            contact = r;
            last = l;
            if (jk == null) {
                connt = new Messenger(stage, MenuPane.this, contact, false);
            } else {
                connt = jk;
            }
            connt.setMButton(ContactButton.this);
            Contact c = InfoPane.getContact(contact.getUsername());
            if (c == null) {
                setTop(tem = new Text(contact.getName().isEmpty() ? contact.getUsername() : contact.getName()));
            } else {
                contact = c;
                setTop(tem = new Text(contact.getName().isEmpty() ? contact.getUsername() : contact.getName()));
            }
            BorderPane.setAlignment(tem, Pos.CENTER);
            tem.setFont(new Font(16));
            setRight(right = new HBox(go = new Button(">")));
            left = new HBox(audio = new Button(""));
            currentNotify = new SimpleIntegerProperty(0);
            audio.setDisable(true);
            audio.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("phone.png"), 30, 30, true, true)));

            go.setFont(new Font(16));
            audio.setOnAction((e) -> {
            });
            go.setOnAction((e) -> {
                if (go.getText().equals(">")) {
                    stage.toMessenger("", connt);
                    currentNotify.set(currentNotify.get() + 1);
                    notify.set(notify.get() - currentNotify.get());
                    notify.set(notify.get() + 1);
                    setStyle("");
                    currentNotify.set(0);
                } else {
                    container.getChildren().remove(ContactButton.this);
                    File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + contact.getUsername() + File.separator);
                    deleteDir(f);
                    conf.remove(ContactButton.this);
                }
            });
            setCenter(tes = new Text(last));
            tes.setFont(new Font(16));
        }

//        private class Notification {
//
//            private final Notifications not;
//
//            public Notification(Scene sc, ContactButton cb) {
//                not = Notifications.create().darkStyle().title("Notification").text("New Message from : " + contact.getUsername() + "!\n" + last).position(Pos.BOTTOM_CENTER).owner(stage).onAction((e) -> {
//                    if (sc != null) {
//                        sc.setRoot(connt);
//                        currentNotify.set(currentNotify.get() + 1);
//                        notify.set(notify.get() - currentNotify.get());
//                        notify.set(notify.get() + 1);
//                        cb.setStyle("");
//                        currentNotify.set(0);
//                    }
//                });
//            }
//
//            public void show() {
//                not.showInformation();
//            }
//        }
        public boolean connectionSent() {
            return connectionSent;
        }

        public Button getCall() {
            return audio;
        }

        public void setName(String s) {
            if (!s.isEmpty()) {
                tem.setText(s);
            } else {
                if (contact.getName().isEmpty()) {
                    tem.setText(contact.getUsername());
                } else {
                    tem.setText(contact.getName());
                }
            }
        }

        public void transfer(Scene s) {
            s.setRoot(connt);
        }

        public Button getAction() {
            return go;
        }

        public Messenger getMScene() {
            return connt;
        }

        public Contact getContact() {
            return contact;
        }

        public void setLast(Message m, boolean b, boolean c) {
            if (m.isImage()) {
                setLast("New Image", b, c);
            } else if (m.isText()) {
                setLast(m.getText(), b, c);
            } else if (m.isEmoji()) {
                setLast("emoji", b, c);
            } else if (m.isAudio()) {
                setLast("Audio File", b, c);
            } else if (m.isFile()) {
                setLast("New File", b, c);
            }
        }
        String[] format = new String[]{
            "jpg", "bmp", "jpeg", "wbmp", "png", "gif"};

        public void setLast(String s, boolean b, boolean c) {
            if (s.endsWith(".wav") || s.endsWith(".mp3")) {
                s = "Audio File";
            } else {
                for (String as : format) {
                    if (s.endsWith(as)) {
                        s = "New Image";
                    }
                }
            }
            last = s;
            Platform.runLater(() -> {
                if (last.length() > 25) {
                    tes.setText(last.substring(0, 25) + "...");
                } else {
                    tes.setText(last);
                }
                if (b && (stage.getScene().getRoot() instanceof MenuPane || !stage.getScene().getRoot().equals(connt) /*|| !stage.isFocused()*/)) {
                    notify.set(notify.get() + 1);
                    if (c) {
                        if (LatticeStage.IS_DESKTOP) {
//                            if (Preferences.getPref().showNotifications()) {
////                            (new Notification(getScene(), this)).show();
//                            }
//                            if (Preferences.getPref().isSoundOn()) {
//                                try {
//                                    tone(1000, 100);
//                                } catch (LineUnavailableException ex) {
//                                }
//                            }
                        }
                    }
                    setStyle("-fx-background-color:lightblue;");
                    currentNotify.set(currentNotify.get() + 1);
                } else if (stage.getScene().getRoot().equals(connt) && !stage.isFocused()) {
                    if (c) {
                        if (LatticeStage.IS_DESKTOP) {
                            if (Preferences.getPref().showNotifications()) {
//                            (new Notification(getScene(), this)).show();
                            }
//                            if (Preferences.getPref().isSoundOn()) {
//                                try {
//                                    tone(1000, 100);
//                                } catch (LineUnavailableException ex) {
//                                }
//                            }
                        }
                    }
                }
                container.getChildren().remove(this);
                container.getChildren().add(0, this);
            });
        }

        public int getCurrentNotify() {
            return currentNotify.get();
        }

        public void update(Message obj) {
            connt.addMessage(obj, false, true);
        }

        public void add(int n, Message m) {
            connt.addMessage(n, m);
        }

        private boolean deleteDir(File directory) {
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (null != files) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteDir(file);
                        } else {
                            file.delete();
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public int compareTo(ContactButton o) {
            if (connt == null) {
                if (o.getMScene() == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                ArrayList<Message> al = o.getMScene().getMessages();
                ArrayList<Message> ala = getMScene().getMessages();
                if (ala.isEmpty() && al.isEmpty()) {
                    return 0;
                } else if (ala.isEmpty()) {
                    return -1;
                } else if (al.isEmpty()) {
                    return 1;
                } else {
                    Message sg = al.get(al.size() - 1);
                    Message gg = ala.get(ala.size() - 1);
                    String s = sg.getTimeSent();
                    String g = gg.getTimeSent();

                    String spl[] = s.split(" ");
                    String sp[] = g.split(" ");
                    LocalDate one = LocalDate.parse(spl[0]);
                    LocalDate two = LocalDate.parse(sp[0]);
                    int r = two.compareTo(one);
                    if (r == 0) {
                        LocalTime ti = LocalTime.parse(spl[1] + ":00");
                        LocalTime til = LocalTime.parse(sp[1] + ":00");
                        return til.compareTo(ti);
                    } else {
                        return r;
                    }
                }
            }
        }
    }

    private class TimeThread implements Runnable {

        @Override
        public void run() {
            boolean b = true;
            while (b) {
                String s = LocalTime.now().toString().substring(0, 5);
                String hour = s.substring(0, s.indexOf(':'));
                String string;
                int j = Integer.parseInt(hour);
                if (j < 13) {
                    string = (s + " AM");
                } else {
                    s = s.replace(hour, "");
                    hour = (j - 12) + "";
                    string = (hour + s + " PM");
                }
                Platform.runLater(() -> {
                    time.setText(string);
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        }

    }

    public static float SAMPLE_RATE = 8000f;
//
//    public static void tone(int hz, int msecs)
//            throws LineUnavailableException {
//        tone(hz, msecs, 1.0);
//    }
//
//    public static void tone(int hz, int msecs, double vol)
//            throws LineUnavailableException {
//        byte[] buf = new byte[1];
//        AudioFormat af
//                = new AudioFormat(
//                        SAMPLE_RATE, // sampleRate
//                        8, // sampleSizeInBits
//                        1, // channels
//                        true, // signed
//                        false);      // bigEndian
//        try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
//            sdl.open(af);
//            sdl.start();
//            for (int i = 0; i < msecs * 8; i++) {
//                double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
//                buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
//                sdl.write(buf, 0, 1);
//            }
//            sdl.drain();
//            sdl.stop();
//        }
//    }

}
