/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.core.Service;
import carbon.lattice.core.Contact;
import carbon.lattice.Messenger.MessageBox;
import java.io.File;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Aniket
 */
public class InfoPane extends BorderPane {

    private final Messenger current;
    private final Button back, message;
    private final Text title, name;
    private final BorderPane top;
    private final VBox box;
    
    private Contact contact;

    public InfoPane(Messenger m, ArrayList<Message> meas) {
        current = m;
        top = new BorderPane(title = new Text("Details"), null, null, null, back = new Button("<Back"));
        setTop(top);
        title.setFill(Color.WHITE);
        BorderPane.setAlignment(top, Pos.TOP_LEFT);
        box = new VBox(15);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        ScrollPane bar;
        setCenter(bar = new ScrollPane(box));

        bar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        box.setMinWidth(450);
        message = new Button("Send Message");
        top.getChildren().add(message);
        contact = getContact(m.getRecipient().getUsername());
        if (contact == null) {
            contact = new Contact(m.getRecipient().getName(), m.getRecipient().getUsername());
            Button a;
            box.getChildren().addAll(name = new Text(m.getRecipient().getUsername()), a = new Button("Create a new Contact"));
            a.setOnAction((e) -> {
                getScene().setRoot(new ContactPane(this, m.getRecipient()));
            });
            a.setFont(new Font(16));
        } else {
            Button a;
            box.getChildren().addAll(name = new Text(contact.getName().isEmpty() ? m.getRecipient().getUsername() : contact.getName()), a = new Button("Edit Contact"));
            a.setOnAction((e) -> {
                getScene().setRoot(new ContactPane(this, contact));
            });
            a.setFont(new Font(16));
        }
        box.getChildren().addAll(new Text("Total " + (m.getMessages().size() + meas.size()) + " Messages"));
        ArrayList<Button> ab = new ArrayList<>();
        for (Node ma : m.getMessage().getChildren()) {
            if (ma instanceof MessageBox) {
                MessageBox mb = (MessageBox) ma;
                for (Node n : mb.getChildren()) {
                    if (n instanceof ImageButton) {
                        ImageButton ib = (ImageButton) n;
                        ab.add(m.thumbnail(ib.getView().getImage(), ib.getName(), 100));

                    }
                }
            }
        }
        for (Message ma : meas) {
            if (ma.isImage()) {
                ab.add(getButton(m, ma));
            }
        }
        if (!ab.isEmpty()) {
            box.getChildren().addAll(new Text("Shared Images"));
            box.getChildren().addAll(ab);
        }
        ab = new ArrayList<>();
        for (Node ma : m.getMessage().getChildren()) {
            if (ma instanceof MessageBox) {
                MessageBox mb = (MessageBox) ma;
                for (Node n : mb.getChildren()) {
                    if (n instanceof AudioButton) {
                        AudioButton ib = (AudioButton) n;
                        ab.add(ib.copy());
                    }
                }
            }
        }
        for (Message ma : meas) {
            if (ma.isAudio()) {
                ab.add(getButton(m, ma));
            }
        }
        if (!ab.isEmpty()) {
            box.getChildren().add(new Text("Shared Audio Files"));
            box.getChildren().addAll(ab);
        }
        ab = new ArrayList<>();
        for (Node ma : m.getMessage().getChildren()) {
            if (ma instanceof MessageBox) {
                MessageBox mb = (MessageBox) ma;
                for (Node n : mb.getChildren()) {
                    if (n instanceof FileButton) {
                        FileButton ib = (FileButton) n;
                        ab.add(ib.copy());
                    }
                }
            }
        }
        for (Message ma : meas) {
            if (ma.isFile()) {
                ab.add(getButton(m, ma));
            }
        }
        if (!ab.isEmpty()) {
            box.getChildren().add(new Text("Shared Files"));
            box.getChildren().addAll(ab);
        }
        name.setFont(new Font(16));
        back.setFont(new Font(16));
        top.setPadding(new Insets(5, 10, 5, 10));
        title.setFont(new Font(16));
        back.setOnAction((e) -> {
            getScene().setRoot(current);
        });
    }
    
    public Contact getContact() {
        return contact;
    }

    public static Button getButton(Messenger ms, Message mesa) {
        if (mesa.isImage()) {
            File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + ms.getRecipient().getUsername() + File.separator + mesa.getMetadata());
            if (f.exists()) {
                Image ima = new Image(f.toURI().toString());
                return ms.thumbnail(ima, mesa.getMetadata(), 100);
            } else {
                Image ima = ms.getImage(mesa.getData());
                return ms.thumbnail(ima, mesa.getMetadata(), 100);
            }
        } else if (mesa.isAudio()) {
            return new AudioButton(ms.getRecipient(), mesa.getData());
        } else if (mesa.isFile()) {
            return new FileButton(ms.getRecipient(), mesa.getMetadata(), mesa.getData());
        }
        return null;
    }

    public void setName(String s) {
        if (!s.isEmpty()) {
            name.setText(s);
        } else {
            if (current.getRecipient().getName().isEmpty()) {
                name.setText(current.getRecipient().getUsername());
            } else {
                name.setText(current.getRecipient().getName());
            }
        }
        current.setName(s);
    }

    static Contact getContact(String uer) {
        for (Contact c : LatticeStage.contacts) {
            if (c.getUsername().equals(uer)) {
                return c;
            }
        }
        return null;
    }
}
