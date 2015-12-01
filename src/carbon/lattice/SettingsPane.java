/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import static carbon.lattice.LatticeStage.NATIVE;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 *
 * @author Aniket
 */
public class SettingsPane extends BorderPane {

    private final VBox center;
    private final ArrayList<CheckBox> boxes;
    private final Button back;
    private final Label title;

    public SettingsPane(BorderPane main) {
        setPadding(new Insets(5, 10, 5, 10));
        setTop(new BorderPane(title = new Label("Settings"), null, null, null, back = new Button("<Back")));
        back.setOnAction((e) -> {
            getScene().setRoot(main);
        });
        title.setFont(new Font(16));
        back.setFont(new Font(16));
        center = new VBox(10);
        setCenter(center);
        center.setAlignment(Pos.CENTER);
        boxes = new ArrayList<>();
        for (BooleanProperty bp : Preferences.getPref().allProperties()) {
            CheckBox cb;
            boxes.add(cb = new CheckBox(bp.getName()));
            cb.setSelected(bp.get());
            cb.selectedProperty().addListener((ob, older, newer) -> {
                bp.set(newer);
            });
            if (bp.getName().contains("UI")) {
                CustomMenuItem cm;
                TextField tf;
                cb.setContextMenu(new ContextMenu(cm = new CustomMenuItem(tf = new TextField())));
                cm.setHideOnClick(false);
                tf.setOnAction((e) -> {
                    String st = tf.getText();
                    if (st.contains("Macintosh")) {
                        cb.setSelected(false);
                        NATIVE = Lattice.class.getResource("mac_os.css").toExternalForm();
                        cb.setSelected(true);
                    } else if (st.contains("Windows 7")) {
                        cb.setSelected(false);
                        NATIVE = Lattice.class.getResource("win7.css").toExternalForm();
                        cb.setSelected(true);
                    } else if (st.contains("Windows 8")) {
                        cb.setSelected(false);
                        NATIVE = Lattice.class.getResource("JMetroLightTheme.css").toExternalForm();
                        cb.setSelected(true);
                    } else if (st.contains("Flatter")) {
                        cb.setSelected(false);
                        NATIVE = Lattice.class.getResource("flatterfx.css").toExternalForm();
                        cb.setSelected(true);
                    } else if (st.equals("Native")) {
                        cb.setSelected(false);
                        NATIVE = carbon.lattice.LatticeStage.CSS.get();
                        cb.setSelected(true);
                    }
                });

            }
        }
        center.getChildren().addAll(boxes);
    }
}
