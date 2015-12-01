/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Aniket
 */
public class Contact {

    public BooleanProperty online;
    private String name;
    private String username;

    public Contact(String nam, String a) {
        name = nam;
        username = a;
        online = new SimpleBooleanProperty(false);
    }

    public void setOnline(boolean b) {
        online.set(b);
    }

    public boolean isOnline() {
        return online.get();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        username = name;
    }

    public static ArrayList<Contact> getContacts(List<String> al) {
        if (al.size() % 2 == 0) {
            ArrayList<Contact> ac = new ArrayList<>();
            for (int x = 0; x < al.size(); x += 2) {
                ac.add(new Contact(al.get(x), al.get(x + 1)));
            }
            return ac;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof Contact) {
                Contact c = (Contact) obj;
                if (c.getUsername().equals(getUsername())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (!getName().isEmpty()) {
            return getName();
        } else {
            return getUsername();
        }
    }

}
