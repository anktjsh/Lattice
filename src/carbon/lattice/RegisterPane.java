/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import static carbon.lattice.LatticeStage.IS_DESKTOP;
import carbon.lattice.LoginPane.SocketBar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 *
 * @author Aniket
 */
public class RegisterPane extends BorderPane implements EventHandler<ActionEvent> {

    private final VBox box;
    private final Label title, user, pass;
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
        if (IS_DESKTOP) {
            box.setAlignment(Pos.CENTER);
        } else {
            box.setAlignment(Pos.TOP_CENTER);
        }
        setCenter(box);
        box.getChildren().addAll(title = new Label("Register"), user = new Label("Enter a username : "), username = new TextField(), pass = new Label("Enter a password : "), password = new PasswordField(),
                new Label("Enter a Security Question"), question = new TextField(), new Label("Enter the answer to the Security Question"), answer = new TextField(),
                enter = new Button("Confirm"), back = new Button("Back"));
        enter.setOnAction(RegisterPane.this);
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
            //st.centerOnScreen();
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
//            Alert al = new Alert(AlertType.CONFIRMATION);
//            al.initOwner(getScene().getWindow());
//            al.setTitle("Registered");
//            al.setHeaderText("Enter your Username and Password to Continue");
//            al.showAndWait();
            Service.get().showMessage("Enter your Username and Password to Continue", "Registered", getScene().getWindow());
            Window st = getScene().getWindow();
            log.restore();
            st.sizeToScene();
            //st.centerOnScreen();
        }
    }

    public void showError(String mess, String titl) {
//        Alert al = new Alert(AlertType.ERROR);
//        al.initOwner(getScene().getWindow());
//        al.setHeaderText(mess);
//        al.setContentText(null);
//        al.setTitle(titl);
//        al.showAndWait();
        Service.get().showMessage(mess, titl, getScene().getWindow());
    }

}
