/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import static carbon.lattice.LatticeStage.IS_DESKTOP;
import static carbon.lattice.LatticeStage.dimension;
import java.io.InputStream;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author Aniket
 */
public class EmojiKeyboard extends BorderPane {

    int[] index = {116, 346, 447, 636, 845};
    private Messenger mess;
    private final ArrayList<ScrollPane> panes;
    private final ArrayList<GridPane> grids;
    private int current;
    private final HBox controls;
    private final Label page1, page2;
    private final Button left, right;

    private EmojiKeyboard(Messenger ms) {
        grids = new ArrayList<>();
        panes = new ArrayList<>();
        System.out.println(dimension.getHeight() + " " + dimension.getWidth());
        double lower = dimension.getHeight() > dimension.getWidth() ? dimension.getWidth() : dimension.getHeight();
        System.out.println(lower);
        setMinSize(440, IS_DESKTOP ? 185 : lower / 2 - 20);
        setMaxSize(IS_DESKTOP ? 440 : dimension.getWidth() - 100, IS_DESKTOP ? 185 : lower / 2 - 50);

        controls = new HBox(10, left = new Button("<"), page1 = new Label("Page"), page2 = new Label(""), right = new Button(">"));
        controls.setPadding(new Insets(5, 10, 5, 10));
        setTop(controls);
        controls.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(controls, Pos.CENTER);
        for (int x = 0; x < 5; x++) {
            grids.add(new GridPane());
            grids.get(grids.size() - 1).setHgap(5);
            grids.get(grids.size() - 1).setVgap(5);
            panes.add(new ScrollPane(grids.get(x)));
        }
        left.setOnAction((E) -> {
            toIndex(current - 1);
        });
        right.setOnAction((e) -> {
            toIndex(current + 1);
        });
        ArrayList<EmojiButton> img = new ArrayList<>();
        for (int count = 1; count <= 845; count++) {
            img.add(new EmojiButton(count + ".png"));
        }
        for (EmojiButton eb : img) {
            eb.setMinWidth(IS_DESKTOP ? 25 : 60);
            eb.setMaxWidth(IS_DESKTOP ? 25 : 60);
            eb.setMinHeight(IS_DESKTOP ? 25 : 60);
            eb.setMaxHeight(IS_DESKTOP ? 25 : 60);
            eb.setStyle("-fx-background-radius : 5em;");
        }
        for (int x = 0; x < grids.size(); x++) {
            if (x == 0) {
                GridPane gp = grids.get(x);
                int count = index[x] / (IS_DESKTOP ? 6 : 6);
                int row = 0;
                int g = 0;
                for (int y = 0; y < index[x]; y++) {
                    if (IS_DESKTOP){
                        gp.add(img.get(y), row, g);
                    } else {
                        gp.add(img.get(y), g, row);
                    }
                    g++;
                    if (g == count) {
                        g = 0;
                        row++;
                    }
                }
            } else {
                GridPane gp = grids.get(x);
                int count = (index[x] - index[x - 1]) / (IS_DESKTOP ? 6 : 6);
                int row = 0;
                int g = 0;
                for (int y = index[x - 1]; y < index[x]; y++) {
                    if (IS_DESKTOP){
                        gp.add(img.get(y), row, g);
                    } else {
                        gp.add(img.get(y), g, row);
                    }
                    g++;
                    if (g == count) {
                        g = 0;
                        row++;
                    }
                }
            }
        }
        mess = ms;
        toIndex(0);
    }

    private void toIndex(int n) {
        if (n < 0 || n > 4) {
            return;
        }
        current = n;
        setCenter(panes.get(current));
        page2.setText("" + (1 + n));
    }

    public void setWindow(Messenger ms) {
        mess = ms;
    }
    private static EmojiKeyboard ins;

    public static EmojiKeyboard getKeyboard(Messenger ms) {
        if (ins == null) {
            ins = new EmojiKeyboard(ms);
        } else {
            ins.setWindow(ms);
        }
        return ins;
    }

    private class EmojiButton extends Button {

        public EmojiButton(String path) {
            InputStream im = getClass().getResourceAsStream("emoji/emoji/" + path);
            double size = IS_DESKTOP ? 20 : 50;
            Image i = new Image(im, size, size, true, true);
            setGraphic(new ImageView(i));
            setOnAction((e) -> {
                if (mess != null) {
                    int g = mess.getEmoji().getItems().size();
                    EmojiMenuItem ei = new EmojiMenuItem(mess, path, i);
                    if (mess.getEmoji().getItems().contains(ei)) {
                        mess.getEmoji().getItems().remove(mess.getEmoji().getItems().indexOf(ei));
                    } else {
                        if (g == 6) {
                            mess.getEmoji().getItems().remove(5);
                        }
                    }
                    mess.getEmoji().getItems().add(1, ei);
                    mess.sendEmoji(path);
                }
            });
        }
    }
}
