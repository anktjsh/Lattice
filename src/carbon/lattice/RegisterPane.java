/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.LoginPane.SocketBar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Window;

/**
 *
 * @author Aniket
 */
public class RegisterPane extends BorderPane implements EventHandler<ActionEvent> {

    private final VBox box;
    private final Text title, user, pass, sec1, sec2;
    private final TextField username, question, answer;
    private final PasswordField password;
    private final Button enter, back;
    private final LoginPane log;
    private final SocketConnection sock;
    private final SocketBar bar;

    RegisterPane(LoginPane lp, SocketConnection sco, SocketBar b) {
        log = lp;
        bar = b;
        sock = sco;
        setPadding(new Insets(5, 10, 5, 10));
        box = new VBox(10);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        setCenter(box);
        box.getChildren().addAll(title = new Text("Register"), 
                user = new Text("Enter a username : "), 
                username = new TextField(), 
                pass = new Text("Enter a password : "), 
                password = new PasswordField(),
                sec1 = new Text("Enter a Security Question"), 
                question = new TextField(), 
                sec2 = new Text("Enter the answer to the Security Question"), 
                answer = new TextField(),
                enter = new Button("Confirm"), 
                back = new Button("Back"));
        enter.setOnAction(RegisterPane.this);
        title.setFill(Color.WHITE);
        user.setFill(Color.WHITE);
        pass.setFill(Color.WHITE);
        sec1.setFill(Color.WHITE);
        sec2.setFill(Color.WHITE);
        answer.setOnAction((E) -> {
            enter.fire();
        });
        back.setOnAction(RegisterPane.this);
    }

    @Override
    public void handle(ActionEvent e) {
        if (e.getSource() == back) {
            Window st = getScene().getWindow();
            log.restore();
            st.sizeToScene();
        }
        if (e.getSource() == enter) {
            if (username.getText().isEmpty()) {
                showError("Username cannot be empty", "Login");
                return;
            }
            if (username.getText().contains("/")) {
                showError("Username cannot contain '/'", "Login");
                return;
            }
            if (password.getText().isEmpty()) {
                showError("Password cannot be empty", "Login");
                return;
            }
            if (password.getText().contains("/")) {
                showError("Password cannot contain '/'", "Login");
                return;
            }
            if (question.getText().isEmpty()) {
                showError("Security Question cannot be empty", "Login");
                return;
            }
            if (question.getText().contains("/")) {
                showError("Security Question cannot contain '/'", "Login");
                return;
            }
            if (answer.getText().isEmpty()) {
                showError("Answer cannot be empty", "Login");
                return;
            }
            if (answer.getText().contains("/")) {
                showError("Answer cannot contain '/'", "Login");
                return;
            }
            if (!sock.isConnected()) {
                sock.reconnect(bar.getPort(), bar.getServer());
                if (!sock.isConnected()) {
                    showError("Not connected to server", "Login");
                    return;
                }
            }
            if (!sock.verifyRegister(username.getText(), password.getText(), question.getText(), answer.getText())) {
                showError("Username has already been taken", "Login");
                return;
            }
            Service.get().showMessage("Enter your Username and Password to Continue", "Registered", getScene().getWindow(), AlertType.CONFIRMATION);
            Window st = getScene().getWindow();
            log.restore();
            st.sizeToScene();
        }
    }

    public void showError(String mess, String titl) {
        Service.get().showMessage(mess, titl, getScene().getWindow(), AlertType.ERROR);
    }

}
