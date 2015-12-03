/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.MenuPane.ContactButton;
import carbon.lattice.Messenger.MessageBox;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 *
 * @author Aniket
 */
public class Messenger extends BorderPane {

    private Image currentI;

    private Contact recipient;
    private final TextField text;
    private final VBox message;
    private final BorderPane bottom;
    private BorderPane top;
    private Button back, info;
    private final Button plus;
    private final MenuItem image, keyboard, audio, camera, file;
    private final Menu emoji;
    private Label name;
    private final MenuPane menu;
    private final HBox deliver = new HBox(new Text("Delivered"));
    private final HBox sent = new HBox(new Text("Sent"));

    {
        ((Text) deliver.getChildren().get(0)).setFill(Color.BLACK);
        deliver.setAlignment(Pos.CENTER_RIGHT);
        ((Text) sent.getChildren().get(0)).setFill(Color.BLACK);
        sent.setAlignment(Pos.CENTER_RIGHT);
    }
    private final static Image on = new Image(Messenger.class.getResourceAsStream("online.png"), 30, 30, true, true);
    private final static Image off = new Image(Messenger.class.getResourceAsStream("offline.png"), 30, 30, true, true);

    public Button getBack() {
        return back;
    }

    Messenger(LatticeStage stage, MenuPane ms, Contact reci, boolean b) {
        menu = ms;
        setPadding(new Insets(5, 10, 5, 10));
        setMinWidth(425);
        text = new TextField();
        text.setPromptText("Message");
        image = new MenuItem();
        camera = new MenuItem();
        file = new MenuItem();
        emoji = new Menu();
        keyboard = new MenuItem("", new ImageView(new Image(getClass().getResourceAsStream("keyboard.png"), 35, 35, true, true)));
        emoji.getItems().add(keyboard);
        audio = new MenuItem();
        if (reci == null) {
            System.out.println("No Contact");
            noContact(stage, ms);
            System.out.println("No Contact");
        } else {
            recipient = reci;
            top = new BorderPane();
            Contact c = InfoPane.getContact(recipient.getUsername());
            if (c == null) {
                top.setCenter(name = new Label(recipient.getUsername()));
                name.setStyle("-fx-text-fill:white");
            } else {
                recipient = c;
                top.setCenter(name = new Label(c.getName().isEmpty() ? recipient.getUsername() : c.getName()));
                name.setStyle("-fx-text-fill:white");
            }
            ContactButton mb = ms.getMButton(recipient);
            if (mb == null && b) {
                ms.create(Messenger.this, recipient);
            }

            recipient.online.addListener((ob, older, newer) -> {
                if (newer) {
                    setGraphic(name, (new ImageView(on)));
                } else {
                    setGraphic(name, (new ImageView(off)));
                }
            });
            if (recipient.isOnline()) {
                setGraphic(name, (new ImageView(on)));
            } else {
                setGraphic(name, (new ImageView(off)));
            }
            name.setFont(new Font(16));
            top.setLeft(back = new Button("<Back" + ((ms.getNotifications() == 0) ? "" : " (" + ms.getNotifications() + ")")));
            back.setFont(new Font(16));
            back.setOnAction((ge) -> {
                back.setText("<Back");
                stage.toMenu(Messenger.this);
            });
            setTop(top);
            top.setRight(info = new Button("Info"));
            info.setFont(new Font(16));
            info.setOnAction((ke) -> {
                InfoPane ip;
                if (menu.hash.containsKey(recipient.getUsername())) {
                    ip = new InfoPane(this, menu.hash.get(recipient.getUsername()));
                } else {
                    ip = new InfoPane(this, new ArrayList<>());
                }
                getScene().setRoot(ip);
            });
            text.setOnAction((e) -> {
                if (!text.getText().isEmpty()) {
                    send();
                } else {
                    sendImage();
                }
            });
        }
        bottom = new BorderPane();
        text.focusedProperty().addListener((ob, older, newer) -> {

        });

        bottom.setCenter(new BorderPane(text));
        setBottom(bottom);
        image.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("image.png"), 35, 35, true, true)));
        camera.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("camera.png"), 35, 35, true, true)));
        file.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("file.png"), 25, 25, true, true)));
        audio.setText("");
        audio.setGraphic(new Circle(15, Color.RED));
        plus = new Button("+");
        plus.setFont(new Font(16));
        plus.setContextMenu(new ContextMenu());
        plus.setOnAction((e) -> {
            plus.getContextMenu().show(plus, Side.TOP, 0, 0);
        });
        plus.setStyle("-fx-text-fill:black;");
        BorderPane.setMargin(plus, new Insets(5));
        plus.getContextMenu().getItems().addAll(camera, image, audio, file, emoji);
        image.setOnAction((e) -> {
            ObjectProperty op = new SimpleObjectProperty();
            op.addListener((ob, older, newer) -> {
                if (newer instanceof Image) {
                    currentI = (Image) newer;
                    Label l = new Label(getRandomImageName());
                    bottom.setRight(l);
                    l.setMaxWidth(60);
                    send();
                } else {
                    Platform.runLater(() -> {
                        byte[] bytes = Service.get().getBytes(newer);
                        Image im = (getImage(bytes));
                        Label l = new Label(getRandomImageName());
                        currentI = (Image) im;
                        bottom.setRight(l);
                        l.setMaxWidth(60);
                        send(bytes);
                    });
                }
            });
            Service.get().findImage(op);
        });
        camera.setOnAction((E) -> {
            ObjectProperty op = new SimpleObjectProperty();
            op.addListener((ob, older, newer) -> {
                if (newer instanceof String) {

                } else if (newer instanceof Image) {
                    Image ima = (Image) newer;
                    Label l = new Label(getRandomImageName());
                    currentI = ima;
                    bottom.setRight(l);
                    l.setMaxWidth(60);
                    send();
                } else {
                    Platform.runLater(() -> {
                        byte[] bytes = Service.get().getBytes(newer);
                        Image im = (getImage(bytes));
                        Label l = new Label(getRandomImageName());
                        currentI = (Image) im;
                        bottom.setRight(l);
                        l.setMaxWidth(60);
                        send(bytes);
                    });
                }
            });
            op.set("");
            Service.get().takeCameraImage(op);
        });
        file.setOnAction((e) -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose a File");
            File f = fc.showOpenDialog(null);
            if (f != null) {
                sendFile(f);
            }
        });
        audio.setOnAction((E) -> {
            if (!(bottom.getBottom() instanceof Recording)) {
                bottom.setBottom(new Recording(this));
            } else {
                bottom.setBottom(null);
            }
        });
        emoji.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("emoji/emoji/499.png"), 32.5, 32.5, true, true)));
        bottom.setRight(null);
        keyboard.setOnAction((e) -> {
            if (!(bottom.getBottom() instanceof EmojiKeyboard)) {
                bottom.setBottom(EmojiKeyboard.getKeyboard(Messenger.this));
            } else {
                bottom.setBottom(null);
            }
        });

        bottom.setLeft(plus);
        text.setFont(new Font(16));
        message = new VBox();
        message.setStyle("-fx-background-color:white;");
        message.setMinWidth(400);
        message.setMaxWidth(400);
        message.setMinHeight(500);
        ScrollPane scr = new ScrollPane(message);
        BorderPane cent = new BorderPane();
        scr.vvalueProperty().addListener((ob, older, newr) -> {
            if (newr.doubleValue() <= 0.05) {
                if (menu.hash.containsKey(recipient.getUsername()) && !menu.hash.get(recipient.getUsername()).isEmpty()) {
                    Button but;
                    cent.setTop(but = new Button("Load Older Messages"));
                    BorderPane.setAlignment(cent.getTop(), Pos.CENTER);
                    BorderPane.setMargin(cent.getTop(), new Insets(5));
                    but.setOnAction((e) -> {
                        if (menu.hash.containsKey(recipient.getUsername())) {
                            ArrayList<Message> mes = menu.hash.get(recipient.getUsername());
                            if (mes.size() >= 50) {
                                for (int x = 0; x < 50; x++) {
                                    addMessage(0, mes.remove(mes.size() - 1));
                                }
                            } else {
                                System.out.println(mes.size());
                                Collections.reverse(mes);
                                int n = mes.size();
                                for (int x = 0; x < n; x++) {
                                    addMessage(0, mes.remove(0));
                                    System.out.println(x);
                                    System.out.println(n);
                                }
                            }
                        }
                        cent.setTop(null);
                    });
                }
            } else {
                cent.setTop(null);
            }
        });
        //scr.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        BorderPane.setMargin(scr, new Insets(5, 0, 5, 0));
        message.heightProperty().addListener((ob, older, newer) -> {
            scr.setVvalue(newer.doubleValue());
        });
        cent.setCenter(scr);

        setCenter(cent);

        if (SocketConnection.getConnection().connected.get()) {
            if (reci != null) {
                text.setDisable(false);
                image.setDisable(false);
                camera.setDisable(false);
                file.setDisable(false);
                audio.setDisable(false);
                emoji.setDisable(false);
            }
        } else {
            text.setDisable(true);
            image.setDisable(true);
            audio.setDisable(true);
            emoji.setDisable(true);
            camera.setDisable(true);
            file.setDisable(true);
        }
        SocketConnection.getConnection().connected.addListener((ob, older, newer) -> {
            if (newer) {
                text.setDisable(false);
                image.setDisable(false);
                audio.setDisable(false);
                emoji.setDisable(false);
                camera.setDisable(false);
                file.setDisable(false);
            } else {
                text.setDisable(true);
                image.setDisable(true);
                camera.setDisable(true);
                file.setDisable(true);
                audio.setDisable(true);
                emoji.setDisable(true);
            }
        });
        System.out.println("Complete");
    }

    private void noContact(LatticeStage stage, MenuPane ms) {

        top = new BorderPane();
        BorderPane topTop = new BorderPane();
        Text newMess;
        topTop.setCenter(newMess = new Text("New Message"));
        newMess.setFill(Color.WHITE);
        newMess.setFont(new Font(16));
        Button canc;
        topTop.setRight(canc = new Button("Cancel"));
        topTop.setPadding(new Insets(5));
        canc.setFont(new Font(16));
        canc.setOnAction((e) -> {
            stage.toMenu(Messenger.this);
        });
        top.setTop(topTop);

        Text h;
        top.setLeft(h = new Text("To:"));
        h.setFill(Color.WHITE);
        BorderPane.setMargin(h, new Insets(5));
        h.setFont(new Font(16));
        HBox topCenter = new HBox(10);
        TextField tf;
        top.setCenter(topCenter);
        Button sel = new Button("Enter");
        Button add = new Button("+");
        sel.setFont(new Font(16));
        add.setFont(new Font(16));
        topCenter.getChildren().addAll(tf = new TextField(""), sel, add);
        topCenter.setAlignment(Pos.CENTER);
        tf.setPromptText("Username");
        AutoTextField autoTextField = new AutoTextField(tf, ms.getContacts());
        tf.setFont(new Font(16));
        image.setDisable(true);
        camera.setDisable(true);
        file.setDisable(true);
        audio.setDisable(true);
        emoji.setDisable(true);
        add.setOnAction((e) -> {
            if (!tf.getText().isEmpty()) {
                Text te;
                topCenter.getChildren().add(0, te = new Text(tf.getText()));
                te.setFill(Color.WHITE);
                tf.setText("");
                tf.setMaxWidth(tf.getWidth() - 5 * te.getText().length() - 10);
            }
        });
        tf.setOnAction((e) -> {
            if (topCenter.getChildren().size() == 3 || topCenter.getChildren().size() == 1) {
                if (!tf.getText().isEmpty()) {
                    if (tf.getText().equals(LatticeStage.getName())) {
                        Service.get().showMessage("Cannot send message to yourself!", "Message Error", stage, AlertType.ERROR);
                        return;
                    }
                    SocketConnection.getConnection().usernameExists(tf.getText());
                    if (!SocketConnection.getConnection().getReader().exists()) {
                        Service.get().showMessage("Username does not exist!", "Message Error", stage, AlertType.ERROR);
                        return;
                    }
                    recipient = new Contact("", tf.getText());
                    ContactButton mb = ms.getMButton(recipient);
                    if (mb != null) {
                        mb.transfer(getScene());
                    } else {
                        Contact c = InfoPane.getContact(recipient.getUsername());
                        if (c == null) {
                            top.setCenter(name = new Label(recipient.getUsername()));
                        } else {
                            recipient = c;
                            top.setCenter(name = new Label(c.getName().isEmpty() ? recipient.getUsername() : c.getName()));
                        }
                        recipient.online.addListener((ob, older, newer) -> {
                            if (newer) {
                                setGraphic(name, (new ImageView(on)));
                            } else {
                                setGraphic(name, (new ImageView(off)));
                            }
                        });
                        if (recipient.isOnline()) {
                            setGraphic(name, (new ImageView(on)));
                        } else {
                            setGraphic(name, (new ImageView(off)));
                        }

                        name.setFont(new Font(16));
                        top.setTop(null);
                        ms.create(Messenger.this, recipient);
                        top.setLeft(back = new Button("<Back" + ((ms.getNotifications() == 0) ? "" : " (" + ms.getNotifications() + ")")));
                        back.setFont(new Font(16));
                        back.setOnAction((ge) -> {
                            back.setText("<Back");
                            stage.toMenu(this);
                        });
                        top.setRight(info = new Button("Info"));
                        info.setFont(new Font(16));
                        emoji.setDisable(false);
                        image.setDisable(false);
                        camera.setDisable(false);
                        file.setDisable(false);
                        audio.setDisable(false);
                        text.setOnAction((fe) -> {
                            if (!text.getText().isEmpty()) {
                                send();
                            } else {
                                sendImage();
                            }
                        });
                        info.setOnAction((ke) -> {
                            InfoPane ip;
                            if (menu.hash.containsKey(recipient.getUsername())) {
                                ip = new InfoPane(this, menu.hash.get(recipient.getUsername()));
                            } else {
                                ip = new InfoPane(this, new ArrayList<>());
                            }
                            getScene().setRoot(ip);
                        });
                    }
                }
            }
        });
        Platform.runLater(() -> {
            setTop(top);
        });
        sel.setOnAction(tf.getOnAction());

    }

    private void setGraphic(Label n, Node t) {
        if (Platform.isFxApplicationThread()) {
            n.setGraphic(t);
        } else {
            Platform.runLater(() -> {
                n.setGraphic(t);
            });
        }
    }

    public long randomLong() {
        long lk = new Random().nextLong();
        lk = Math.abs(lk);
        String as = lk + "";
        while (as.length() < 18) {
            as += (int) (9 * Math.random());
        }
        return Long.parseLong(as);
    }

    private String getRandomImageName() {
        return randomLong() + ".png";
    }

    ImageButton thumbnail(Image im, String name, double width) {
        ImageView iv = new ImageView(im);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitWidth(width);
        ImageButton ib = new ImageButton(this, iv, name);
        return ib;
    }

    public VBox getMessage() {
        return message;
    }

    public Contact getRecipient() {
        return recipient;
    }

    public Menu getEmoji() {
        return emoji;
    }

    public void setName(String s) {
        if (!s.isEmpty()) {
            name.setText(s);
        } else {
            if (!recipient.getName().isEmpty()) {
                name.setText(recipient.getName());
            } else {
                name.setText(recipient.getUsername());
            }
        }
        button.setName(s);
    }

    public ArrayList<Message> getMessages() {
        ArrayList<Message> str = new ArrayList<>();
        for (Node n : message.getChildren()) {
            if (n instanceof MessageBox) {
                MessageBox mb = (MessageBox) n;
                str.add(mb.getMessage());
            }
        }
        return str;
    }
    private ContactButton button;

    void setMButton(ContactButton j) {
        button = j;
    }

    ContactButton getMButton() {
        return button;
    }

    public final void addMessage(int n, Message m) {
        MessageBox h = null;
        Text l = null;
        if (m.isImageText()) {
            Message ma = new Message(m.getTo(), m.getFrom(), Message.IMAGE, null, m.getData(), m.getMetadata(), m.getTimeSent());
            Message mg = new Message(m.getTo(), m.getFrom(), Message.TEXT, m.getText(), null, null, m.getTimeSent());
            addMessage(n, ma);
            addMessage(n, mg);
            return;
        }
        if (m.isImage()) {
            File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + recipient.getUsername() + File.separator + m.getMetadata());
            System.out.println("Image Exists : " + f.exists());
            if (f.exists()) {
                Image ima = Service.get().loadImage(f);
                h = new MessageBox(m, recipient, 5, "New Image", true, l = new Text(m.getTimeSent()), thumbnail(ima, m.getMetadata(), 100));
            } else {
                Image ima = getImage(m.getData());
                h = new MessageBox(m, recipient, 5, "New Image", true, l = new Text(m.getTimeSent()), thumbnail(ima, m.getMetadata(), 100));
            }
        } else if (m.isText()) {
            if (!m.getText().isEmpty()) {
                Label lk = new Label(" " + m.getText() + " ");
                h = new MessageBox(m, recipient, 5, m.getText(), true, l = new Text(m.getTimeSent()), lk);
                lk.setWrapText(true);
                lk.setFont(new Font(16));
                if (m.getFrom().equals(LatticeStage.getName())) {
                    lk.setStyle("-fx-background-color:blue;-fx-text-fill:white;");
                } else {
                    lk.setStyle("-fx-background-color:gray;-fx-text-fill:white;");
                }
                lk.setStyle(lk.getStyle() + "-fx-background-radius: 10em; ");
                lk.setMaxWidth(270);
            }
        } else if (m.isEmoji()) {
            ImageLabel lk;
            lk = new ImageLabel(emoji(m.getMetadata()), m.getMetadata());
            h = new MessageBox(m, recipient, 5, "emoji", true, l = new Text(m.getTimeSent()), lk);
        } else if (m.isAudio()) {
            h = new MessageBox(m, recipient, 5, "Audio File", true, l = new Text(m.getTimeSent()), new AudioButton(recipient, m.getData()));
        } else if (m.isFile()) {
            h = new MessageBox(m, recipient, 5, "New File", true, l = new Text(m.getTimeSent()), new FileButton(recipient, m.getMetadata(), m.getData()));
        }
        if (h != null) {
            if (m.getFrom().equals(LatticeStage.getName())) {
                h.setAlignment(Pos.CENTER_RIGHT);
                addMessage(n, h, true);
            } else {
                h.setAlignment(Pos.CENTER_LEFT);
                addMessage(n, h, false);
            }
        }
        if (l != null) {
            l.setFont(new Font(12));
        }
    }

    public void addMessage(Message m, boolean b, boolean c) {
        MessageBox h = null;
        Text l = null;
        if (m.isImageText()) {
            Message ma = new Message(m.getTo(), m.getFrom(), Message.IMAGE, null, m.getData(), m.getMetadata(), m.getTimeSent());
            Message mg = new Message(m.getTo(), m.getFrom(), Message.TEXT, m.getText(), null, null, m.getTimeSent());
            addMessage(ma, b, c);
            addMessage(mg, b, c);
            return;
        } else if (m.isImage()) {
            Image ima = getImage(m.getData());
            h = new MessageBox(m, recipient, 5, "New Image", true, l = new Text(m.getTimeSent()), thumbnail(ima, m.getMetadata(), 100));
            if (!b) {
                button.setLast("New Image", true, c);
            }
            saveImage(ima, m.getMetadata());
        } else if (m.isText()) {
            if (!m.getText().isEmpty()) {
                Label lk = new Label(" " + m.getText() + " ");
                h = new MessageBox(m, recipient, 5, m.getText(), true, l = new Text(m.getTimeSent()), lk);
                lk.setWrapText(true);
                lk.setFont(new Font(16));
                if (b) {
                    lk.setStyle("-fx-background-color:blue;-fx-text-fill:white;");
                } else {
                    lk.setStyle("-fx-background-color:gray;-fx-text-fill:white;");
                }
                lk.setStyle(lk.getStyle() + "-fx-background-radius: 10em; ");
                lk.setMaxWidth(270);
                if (!b) {
                    button.setLast(m.getText(), true, c);
                }
            }
        } else if (m.isAudio()) {
            h = new MessageBox(m, recipient, 5, "Audio File", true, l = new Text(m.getTimeSent()), new AudioButton(recipient, m.getData()));
            if (!b) {
                button.setLast("Audio File", true, c);
            }
        } else if (m.isVideo()) {

        } else if (m.isEmoji()) {
            ImageLabel lk;
            lk = new ImageLabel(emoji(m.getMetadata()), m.getMetadata());
            h = new MessageBox(m, recipient, 5, "emoji", true, l = new Text(m.getTimeSent()), lk);
            if (!b) {
                button.setLast("emoji", true, c);
            }
        } else if (m.isFile()) {
            h = new MessageBox(m, recipient, 5, "emoji", true, l = new Text(m.getTimeSent()), new FileButton(recipient, m.getMetadata(), m.getData()));
            if (!b) {
                button.setLast("New File", true, c);
            }
        }
        if (l != null) {
            l.setFill(Color.WHITE);
            l.setFont(new Font(12));
        }
        if (h != null) {
            if (b) {
                h.setAlignment(Pos.CENTER_RIGHT);
            } else {
                h.setAlignment(Pos.CENTER_LEFT);
            }
            addMessage(h, b);
            if (b) {
                menu.undelivered.add(h);
            }
        }
    }

    public void setDeliver() {
        int x = 0;
        for (int y = 0; y < message.getChildren().size(); y++) {
            if (message.getChildren().get(y) instanceof MessageBox) {
                if (((MessageBox) message.getChildren().get(y)).getChildren().size() > 2) {
                    x = y;
                    break;
                }
            }
        }
        if (x != 0) {
            message.getChildren().remove(deliver);
            message.getChildren().add(x, deliver);
        }
    }

    void addMessage(int b, MessageBox mb, boolean ba) {
        if (Platform.isFxApplicationThread()) {
            message.getChildren().add(b, mb);
        } else {
            Platform.runLater(() -> {
                message.getChildren().add(b, mb);
            });
        }
    }

    void addMessage(MessageBox mb, boolean b) {
        if (Platform.isFxApplicationThread()) {
            message.getChildren().add(mb);
            if (b) {
                message.getChildren().remove(deliver);
                message.getChildren().add(deliver);
            }
        } else {
            Platform.runLater(() -> {
                message.getChildren().add(mb);
                if (b) {
                    message.getChildren().remove(deliver);
                    message.getChildren().add(deliver);
                }
            });
        }
    }

    public void removal() {
        for (int y = 0; y < message.getChildren().size(); y++) {
            if (message.getChildren().get(y) instanceof MessageBox) {
                if (((MessageBox) message.getChildren().get(y)).getChildren().size() > 2) {
                    ((MessageBox) message.getChildren().get(y)).getChildren().remove(2);
                }
            }
        }
    }

    public ImageView emoji(String path) {
        if (path.contains("emoji")) {
            return new ImageView(new Image(getClass().getResourceAsStream("emoji/" + path), 40, 40, true, true));
        } else {
            return new ImageView(new Image(getClass().getResourceAsStream("emoji/emoji/" + path), 40, 40, true, true));
        }
    }

    public void saveImage(Image im, String name) {
        File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + recipient.getUsername() + File.separator);
        if (!f.exists()) {
            f.mkdirs();
        }
        Service.get().saveImage(im, name, recipient);
    }

    public final Image getImage(byte[] b) {
        InputStream in = new ByteArrayInputStream(b);
        return new Image(in);
    }

    public String getFileName(String s) {
        return s.substring(s.lastIndexOf(File.separator) + 1);
    }

    public File getFile(String s) {
        File f = Service.get().getFile(s);
        return f;
    }

    private void send(byte... b) {
        Message m;
        if (currentI != null) {
            String yedj = ((Label) bottom.getRight()).getText();
            m = new Message(recipient.getUsername(), LatticeStage.getName(), Message.IMAGETEXT, text.getText(), b.length == 0 ? getBytes(currentI, yedj.substring(yedj.indexOf('.') + 1)) : b, yedj, LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 5));
        } else {
            m = new Message(recipient.getUsername(), LatticeStage.getName(), Message.TEXT, text.getText(), null, null, LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 5));
        }
        if (SocketConnection.getConnection().connected.get()) {
            if (currentI != null) {
                addMessage(m, true, false);
                text.setText("");
                bottom.setRight(null);
                currentI = null;
            } else {
                addMessage(m, true, false);
                text.setText("");
            }
        } else {
            if (currentI != null) {
                bottom.setRight(null);
                currentI = null;
            }
        }
        if (!SocketConnection.getConnection().sendMessage(m)) {
            message.getChildren().remove(message.getChildren().size() - 1);
        }

    }

    public void sendEmoji(String path) {
        Message m = new Message(recipient.getUsername(), LatticeStage.getName(), Message.EMOJI, null, null, path, LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 5));
        addMessage(m, true, false);
        if (!SocketConnection.getConnection().sendMessage(m)) {
            message.getChildren().remove(message.getChildren().size() - 1);
        }
    }

    private void sendFile(File f) {
        byte[] b;
        try {
            b = getBytes(f);
        } catch (IOException ex) {
            b = new byte[]{};
        }
        Message m = new Message(recipient.getUsername(), LatticeStage.getName(), Message.FILE, "", b, f.getName(), LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 5));
        addMessage(m, true, false);
        if (!SocketConnection.getConnection().sendMessage(m)) {
            message.getChildren().remove(message.getChildren().size() - 1);
        }
    }

    public byte[] getBytes(File f) throws IOException {
        byte[] bFile = new byte[(int) f.length()];
        try (FileInputStream fileInputStream = new FileInputStream(f)) {
            fileInputStream.read(bFile);
        }
        return bFile;
    }

    private void sendImage() {
        if (currentI != null) {
            String ashj = ((Label) bottom.getRight()).getText();
            Message m;
            m = new Message(recipient.getUsername(), LatticeStage.getName(), Message.IMAGE, null, getBytes(currentI, ashj.substring(ashj.indexOf('.') + 1)), ashj, LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 5));
            addMessage(m, true, false);
            bottom.setRight(null);
            currentI = null;
            if (!SocketConnection.getConnection().sendMessage(m)) {
                message.getChildren().remove(message.getChildren().size() - 1);
            }
        }
    }

    public void sendAudio(byte[] b) {
        Message m = new Message(recipient.getUsername(), LatticeStage.getName(), Message.AUDIO, null, b, null, LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 5));
        addMessage(m, true, false);
        if (!SocketConnection.getConnection().sendMessage(m)) {
            message.getChildren().remove(message.getChildren().size() - 1);
        }
    }

    public byte[] getBytes(Image io, String format) {
        BufferedImage bImage = javafx.embed.swing.SwingFXUtils.fromFXImage(io, null);
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(bImage, format, s);
        } catch (IOException ex) {
        }
        byte[] res = s.toByteArray();
        return res;
    }

    class ImageLabel extends Label {

        private final String name;

        public ImageLabel(ImageView im, String name) {
            this.name = name;
            setGraphic(im);
        }

        public String getPath() {
            return name;
        }
    }

    class MessageBox extends HBox {

        private final Contact recipi;
        private final String la;
        private final boolean b;
        private boolean delivered;
        private final Message mess;

        public MessageBox(Message m, Contact recipient, double spacing, String last, boolean k, Node... children) {
            super(spacing, children);
            recipi = recipient;
            la = last;
            b = k;
            mess = m;
        }

        public Message getMessage() {
            return mess;
        }

        public boolean getNotify() {
            return b;
        }

        public String getLast() {
            return la;
        }

        public Contact getRecipient() {
            return recipi;
        }

        public boolean isDelivered() {
            return delivered;
        }

        public void setDeliver(boolean b) {
            delivered = b;
        }
    }

    public boolean equals(Object ij) {
        if (ij instanceof Messenger) {
            Messenger mess = (Messenger) ij;
            if (mess.getRecipient() != null && getRecipient() != null) {
                if (mess.getRecipient().equals(getRecipient())) {
                    return true;
                }
            }
        }
        return false;
    }
}
