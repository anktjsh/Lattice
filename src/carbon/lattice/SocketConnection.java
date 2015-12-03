/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Aniket
 */
public class SocketConnection {

    public final BooleanProperty connected;
    private Socket socket;
    private String ip;
    private int port;
    private MenuPane menu;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private SocketConnection(int po, String i) {
        port = po;
        ip = i;
        connect();
        connected = new SimpleBooleanProperty(socket != null);
        connected.addListener((ob, odler, newer) -> {
            if (newer) {
                try {
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ex) {
                }
            }
        });
        if (connected.get()) {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
            }
        }
    }

    private static SocketConnection connection;

    public static SocketConnection getConnection() {
        if (connection == null) {
            connection = new SocketConnection(16384, "p.grappl.io");
        }
        return connection;
    }

    private Socket getSocket(int port, String host) {
        Socket s;
        try {
            s = new Socket(host, port);
            return s;
        } catch (IOException e) {
        }
        return null;
    }

    private void connect() {
        socket = getSocket(port, ip);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean reconnect(int po, String i) {
        if (!connected.get()) {
            port = po;
            ip = i;
            connect();
            boolean c = socket != null;
            if (c != isConnected()) {
                connected.set(c);
            }
            return c;
        }
        return false;
    }

    public void close(boolean b) {
        try {
            socket.close();
        } catch (IOException ex) {
        }
        socket = null;
        oos = null;
        ois = null;
        if (b && connected.get()) {
            connected.set(false);
            connection = new SocketConnection(16384, "grappl.io");
        }
    }

    public String getSecurityQuestion(String user) {
        if (isConnected()) {
            String st[] = new String[]{"Security", user};
            try {
                oos.writeObject(st);
                return (String) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                connected.set(false);
                return "";
            }
        }
        return "";
    }

    public String getRecoveredPassword(String user, String question, String answer) {
        if (isConnected()) {
            String st[] = new String[]{"Recover", user, question, answer};
            try {
                oos.writeObject(st);
                return (String) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                connected.set(false);
                return "";
            }
        }
        return "";
    }

    public boolean verifyRegister(String a, String b, String c, String d) {
        if (isConnected()) {
            String[] ver = new String[]{"Register", a, b, c, d};
            try {
                oos.writeObject(ver);
                return (Boolean) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                connected.set(false);
                return false;
            }
        }
        return false;
    }

    public boolean verifyLogin(String a, String b) {
        if (isConnected()) {
            String[] ver = new String[3];
            ver[0] = "Login";
            ver[1] = a;
            ver[2] = b;
            try {
                oos.writeObject(ver);
                return (Boolean) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                connected.set(false);
                return false;
            }
        }
        return false;
    }

    public void usernameExists(String s) {
        if (isConnected()) {
            try {
                oos.writeObject(s);
            } catch (IOException e) {
                connected.set(false);
            }
        }
    }

    public void sendLastMessage(String to, Message m, String from) {
        if (isConnected()) {
            Object[] p = new Object[]{to, m, from};
            try {
                oos.writeObject(p);
            } catch (IOException ex) {
                connected.set(false);
            }
        }
    }

    public Message getLastMessage(String to, String from) {
        if (isConnected()) {
            String l[] = new String[]{to, from};
            try {
                oos.writeObject(l);
                return (Message) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                connected.set(false);
                return null;
            }
        }
        return null;
    }

    public List<Message> retrieveMessages(boolean b, String per, Message last) {
        if (isConnected()) {
            Object[] s = new Object[4];
            s[0] = "Messages";
            if (b) {
                s[1] = LatticeStage.getName();
                s[2] = per;
            } else {
                s[1] = per;
                s[2] = LatticeStage.getName();
            }
            s[3] = last;
            try {
                oos.writeObject(s);
                Object oi = ois.readObject();
                System.out.println(oi.getClass().getName());
                if (oi instanceof List) {
                    return (List<Message>) oi;
                } else {
                    return new ArrayList<>();
                }
            } catch (IOException | ClassNotFoundException ex) {
                connected.set(false);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public ArrayList<String> getRecipients(String s) {
        if (isConnected()) {
            String[] l = new String[1];
            l[0] = s;
            try {
                oos.writeObject(l);
                return (ArrayList<String>) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                connected.set(false);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
    private Reader read;

    Reader getReader() {
        return read;
    }

    public void startMessenger(MenuPane mp) {
        menu = mp;
        (new Thread(read = new Reader(ois))).start();
    }

    public MenuPane getMessenger() {
        return menu;
    }

    class Reader implements Runnable {

        private final ObjectInputStream ois;
        private Boolean usernameExists;

        public Reader(ObjectInputStream jk) {
            ois = jk;
        }

        @Override
        public void run() {
            try {
                Object oj;
                while (isConnected() && (oj = ois.readObject()) != null) {
                    if (oj instanceof Message || oj instanceof Integer) {
                        if (menu != null) {
                            menu.update(oj);
                        }
                    } else if (oj instanceof Boolean) {
                        usernameExists = (Boolean) oj;
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
            }
        }

        public boolean exists() {
            if (usernameExists == null) {
                while (usernameExists == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                    }
                }
            }
            Boolean b = usernameExists;
            usernameExists = null;
            return b;
        }
    }

    public boolean sendMessage(Message m) {
        try {
            oos.writeObject(m);
            return true;
        } catch (IOException e) {
            connected.set(false);
            return false;
        }
    }
}
