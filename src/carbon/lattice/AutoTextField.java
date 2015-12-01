/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Aniket
 */
public class AutoTextField implements EventHandler<KeyEvent> {

    private final TextField comboBox;
    private StringBuilder sb;
    private int lastLength;
    private final ObservableList list;

    public AutoTextField(TextField f, List lk) {
        comboBox = f;
        list = FXCollections.observableList(lk);
        sb = new StringBuilder();
        comboBox.setEditable(true);
        comboBox.setOnKeyReleased(AutoTextField.this);
        comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                //hasFocus
            } else {
                lastLength = 0;
                sb.delete(0, sb.length());
                selectClosestResultBasedOnTextFieldValue(false, false);
            }
        });
        comboBox.setOnMouseClicked((MouseEvent event) -> {
            selectClosestResultBasedOnTextFieldValue(true, true);
        });
    }

    @Override
    public void handle(KeyEvent event) {
        if (lastLength != (comboBox.getLength() - comboBox.getSelectedText().length())) {
            lastLength = comboBox.getLength() - comboBox.getSelectedText().length();
        }
        if (event.isControlDown() || event.getCode() == KeyCode.BACK_SPACE
                || event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                || event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.HOME
                || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
            return;
        }
        IndexRange ir = comboBox.getSelection();
        sb.delete(0, sb.length());
        sb.append(comboBox.getText());
        try {
            sb.delete(ir.getStart(), sb.length());
        } catch (Exception e) {
        }
        for (Object item : list) {
            if (item.toString().toLowerCase().startsWith(comboBox.getText().toLowerCase())) {
                try {
                    comboBox.setText(sb.toString() + item.toString().substring(sb.toString().length()));
                } catch (Exception e) {
                    comboBox.setText(sb.toString());
                }
                comboBox.positionCaret(sb.toString().length());
                comboBox.selectEnd();
                break;
            }
        }
    }

    private void selectClosestResultBasedOnTextFieldValue(boolean affect, boolean inFocus) {
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (comboBox.getText().toLowerCase().equals(list.get(i).toString().toLowerCase())) {
                try {
                    ListView lv = ((ComboBoxListViewSkin) comboBox.getSkin()).getListView();
                    lv.getSelectionModel().clearAndSelect(i);
                    lv.scrollTo(lv.getSelectionModel().getSelectedIndex());
                    found = true;
                    break;
                } catch (Exception e) {
                }
            }
        }
        String s = comboBox.getText();
        if (!found && affect) {
            comboBox.setText(s);
            comboBox.end();
        }
        if (!inFocus && comboBox.getText() != null && comboBox.getText().trim().length() > 0) {
            KeyEvent ke = new KeyEvent(comboBox, null, KeyEvent.KEY_RELEASED, KeyCode.ENTER.getName(), KeyCode.ENTER.getName(), KeyCode.ENTER, false, false, false, false);
            comboBox.fireEvent(ke);
        }
    }

}
