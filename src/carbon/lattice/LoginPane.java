/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import carbon.lattice.core.Service;
import carbon.lattice.core.SocketConnection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *
 * @author Aniket
 */
public class LoginPane extends BorderPane implements EventHandler<ActionEvent> {

    private static String receivedIp;

    public static String getReceivedIp() {
        return receivedIp;
    }

    public static void setReceivedIp(String s) {
        receivedIp = s;
    }

    private final VBox box;
    private final Text title, user, pass;
    private final TextField username;
    private final PasswordField password;
    private final Button enter;
    private final Hyperlink register, recover;
    private final LatticeStage stage;
    private final SocketConnection conn;
    private final SocketBar bar;

    public LoginPane(LatticeStage msts) {
        conn = SocketConnection.getConnection();
        Service.get().setMenuBar();
        setTop(bar = new SocketBar());
        stage = msts;

        setPadding(new Insets(5, 10, 5, 10));
        box = new VBox(10);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        setCenter(box);
        box.getChildren().addAll(title = new Text("Login"),
                user = new Text("Username : "),
                username = new TextField(),
                pass = new Text("Password : "),
                password = new PasswordField(),
                enter = new Button("Enter"),
                register = new Hyperlink("Register"),
                recover = new Hyperlink("Forgot my Password?"));
        title.setFill(Color.WHITE);
        user.setFill(Color.WHITE);
        pass.setFill(Color.WHITE);
        enter.setOnAction(LoginPane.this);
        password.setOnAction((E) -> {
            enter.fire();
        });
        setBottom(new ImageView(new Image(getClass().getResourceAsStream("images/messenger.png"))));
        BorderPane.setAlignment(getBottom(), Pos.CENTER);
        register.setOnAction(LoginPane.this);
        recover.setOnAction(LoginPane.this);
    }

    public void restore() {
        setCenter(box);
    }

    @Override
    public void handle(ActionEvent e) {
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
            if (!conn.isConnected()) {
                conn.reconnect(bar.getPort(), bar.getServer());
                if (!conn.isConnected()) {
                    showError("Not connected to server", "Login");
                    return;
                }
            }
            if (!conn.verifyLogin(username.getText(), password.getText())) {
                showError("Incorrect Username or Password", "Login");
                return;
            }
            LatticeStage.setName(username.getText());
            stage.toMenu(null);
            stage.sizeToScene();
        }
        if (e.getSource() == register) {
            setCenter(new RegisterPane(this, conn, bar));
            stage.sizeToScene();
        }
        if (e.getSource() == recover) {
            setCenter(new RecoverPane(this, conn, bar));
            stage.sizeToScene();
        }
    }

    public void showError(String mess, String titl) {
        Alert al = new Alert(AlertType.ERROR);
        al.initOwner(stage);
        al.setHeaderText(mess);
        al.setContentText(null);
        al.setTitle(titl);
        al.showAndWait();
    }

    class SocketBar extends HBox {

        private final Text po, ser;
        private final TextField port, server;
        private final Button settings;

        public SocketBar() {
            super(10);
            po = new Text("Port : ");
            po.setFill(Color.WHITE);
            ser = new Text("Server : ");
            ser.setFill(Color.WHITE);
            List<String> al = get();
            port = new TextField(al.get(0));
            server = new TextField(getReceivedIp() != null ? getReceivedIp() : al.get(1));
            settings = new Button("", new ImageView(new Image(getClass().getResourceAsStream("images/settings.png"), 25, 25, true, true)));
            settings.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            settings.setMaxSize(30, 30);
            settings.setOnAction((e) -> {
                getScene().setRoot(new SettingsPane(LoginPane.this));
            });
            getChildren().addAll(po, port, ser, server, settings);
            setAlignment(Pos.CENTER);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                File f = Service.get().getFile("cache" + File.separator);
                if (!f.exists()) {
                    f.mkdir();
                }
                f = Service.get().getFile("cache" + File.separator + "server.txt");
                try {
                    Files.write(f.toPath(), FXCollections.observableArrayList(port.getText(), server.getText()));
                } catch (IOException ex) {
                }
            }));
        }

        private List<String> get() {
            File f = Service.get().getFile("cache" + File.separator + "server.txt");
            if (f.exists()) {
                try {
                    List<String> al = Files.readAllLines(f.toPath());
                    if (al.size() == 2) {
                        return al;
                    } else {
                        return FXCollections.observableArrayList("16384", "localhost");
                    }
                } catch (IOException ex) {
                    return FXCollections.observableArrayList("16384", "localhost");
                }
            } else {
                return FXCollections.observableArrayList("16384", "localhost");
            }
        }

        public int getPort() {
            try {
                return Integer.parseInt(port.getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public String getServer() {
            return server.getText();
        }
    }
}
