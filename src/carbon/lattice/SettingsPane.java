/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Aniket
 */
public class SettingsPane extends BorderPane {

    private final VBox center;
    private final ArrayList<CheckBox> boxes;
    private final Button back;
    private final Text title;

    public SettingsPane(BorderPane main) {
        setPadding(new Insets(5, 10, 5, 10));
        setTop(new BorderPane(title = new Text("Settings"), null, null, null, back = new Button("<Back")));
        back.setOnAction((e) -> {
            getScene().setRoot(main);
        });
        title.setFill(Color.WHITE);
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
        }
        center.getChildren().addAll(boxes);
    }
}
