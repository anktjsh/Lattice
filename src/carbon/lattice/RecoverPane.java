/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
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
public class RecoverPane extends BorderPane implements EventHandler<ActionEvent> {

    private final Text password, question;
    private final Text one, two, thr;
    private final VBox box;
    private final TextField user, answer;
    private final Button back;
    private final LoginPane log;
    private final SocketConnection sock;
    private final LoginPane.SocketBar bar;

    RecoverPane(LoginPane lp, SocketConnection sco, LoginPane.SocketBar b) {
        log = lp;
        bar = b;
        sock = sco;

        setTop(one = new Text("Password Recovery"));
        BorderPane.setAlignment(getTop(), Pos.CENTER);
        setCenter(box = new VBox(10));
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(two = new Text("Enter Your Username : "),
                user = new TextField(),
                question = new Text("Your Security Question Is : "),
                thr = new Text("Enter the Answer"),
                answer = new TextField(""),
                password = new Text("Your Password is : "),
                back = new Button("Back"));
        user.setOnAction(RecoverPane.this);
        answer.setOnAction(RecoverPane.this);
        answer.setDisable(true);
        question.setDisable(true);
        password.setDisable(true);
        back.setOnAction(RecoverPane.this);
        password.setFill(Color.WHITE);
        question.setFill(Color.WHITE);
        one.setFill(Color.WHITE);
        two.setFill(Color.WHITE);
        thr.setFill(Color.WHITE);
    }
    private String ques;

    @Override
    public void handle(ActionEvent e) {
        if (e.getSource() == back) {
            Window st = getScene().getWindow();
            log.restore();
            st.sizeToScene();
        }
        if (e.getSource() == user) {
            if (user.getText().isEmpty()) {
                showError("Username cannot be empty", "Recovery");
                return;
            }
            if (!sock.isConnected()) {
                sock.reconnect(bar.getPort(), bar.getServer());
                if (!sock.isConnected()) {
                    showError("Not connected to server", "Recovery");
                    return;
                }
            }
            String s = sock.getSecurityQuestion(user.getText());
            if (s.isEmpty()) {
                showError("Incorrect Username", "Recovery");
                return;
            } else {
                ques = s;
                question.setText(question.getText() + s);
                user.setDisable(true);
                answer.setDisable(false);
                question.setDisable(false);
                password.setDisable(false);
            }
        }
        if (e.getSource() == answer) {
            if (answer.getText().isEmpty()) {
                showError("Answer cannot be empty", "Recovery");
                return;
            }
            if (!sock.isConnected()) {
                sock.reconnect(bar.getPort(), bar.getServer());
                if (!sock.isConnected()) {
                    showError("Not connected to server", "Recovery");
                    return;
                }
            }
            String ps = sock.getRecoveredPassword(user.getText(), ques, answer.getText());
            if (ps.isEmpty()) {
                showError("Incorrect Answer", "Recovery");
                return;
            }
            password.setText(password.getText() + ps + "\nGo Back and Enter your Username and Password to Login");
            getScene().getWindow().sizeToScene();
        }
    }

    public void showError(String mess, String titl) {
        Service.get().showMessage(mess, titl, getScene().getWindow(), AlertType.ERROR);
    }
}
