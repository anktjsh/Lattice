/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.core.Contact;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Aniket
 */
public class ContactPane extends BorderPane {

    private final Button cancel, save;
    private final BorderPane top;
    private final Text title;
    private final InfoPane ipa;
    private final VBox box;
    private final TextField name, username;

    public ContactPane(InfoPane ip, Contact c) {
        setPadding(new Insets(5, 10, 5, 10));
        ipa = ip;
        top = new BorderPane();
        box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);
        setCenter(box);
        BorderPane.setAlignment(box, Pos.TOP_CENTER);
        Text g, p;
        box.getChildren().addAll(g = new Text("Name : "), name = new TextField(c.getName()), p = new Text("Username : "), username = new TextField(c.getUsername()));
        username.setEditable(false);
        g.setFill(Color.WHITE);
        p.setFill(Color.WHITE);
        
        g.setFont(new Font(16));
        p.setFont(new Font(16));
        title = new Text();
        title.setFill(Color.WHITE);
        name.setFont(new Font(16));
        username.setFont(new Font(16));
        title.setFont(new Font(16));
        if (!c.getName().isEmpty()) {
            title.setText(c.getName());
        } else {
            title.setText(c.getUsername());
        }
        top.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        cancel = new Button("Cancel");
        save = new Button("Save");
        cancel.setOnAction((e) -> {
            if (cancel.getText().equals("Cancel")) {
                name.setText(c.getName());
                username.setText(c.getUsername());
                name.setEditable(false);
                username.setEditable(false);
                cancel.setText("<Back");
                save.setText("Edit");
            } else {
                getScene().setRoot(ipa);
            }
        });
        save.setOnAction((e) -> {
            if (save.getText().equals("Save")) {
                c.setName(name.getText());
                c.setUsername(username.getText());
                cancel.setText("<Back");
                save.setText("Edit");
                name.setEditable(false);
                username.setEditable(false);
                Contact ca = InfoPane.getContact(username.getText());
                if (ca == null) {
                    LatticeStage.contacts.add(c);
                    ipa.setName(c.getName());
                    if (!c.getName().isEmpty()) {
                        title.setText(c.getName());
                    } else {
                        title.setText(c.getUsername());
                    }
                } else {
                    ca.setName(c.getName());
                    ca.setUsername(username.getText());
                    ipa.setName(ca.getName());
                    if (!ca.getName().isEmpty()) {
                        title.setText(ca.getName());
                    } else {
                        title.setText(ca.getUsername());
                    }
                }
            } else {
                cancel.setText("Cancel");
                save.setText("Save");
                name.setEditable(true);
                username.setEditable(true);
            }
        });
        top.setLeft(cancel);
        cancel.setFont(new Font(16));
        top.setRight(save);
        save.setFont(new Font(16));
        setTop(top);
    }
}
