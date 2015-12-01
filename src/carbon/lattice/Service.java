/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.File;
import static java.lang.String.format;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.stage.Window;

/**
 *
 * @author Aniket
 */
public class Service {

    public static final String DESKTOP = "Desktop";
    public static final String ANDROID = "Android";
    public static final String IOS = "iOS";

    private static final Logger LOG = Logger.getLogger(Service.class.getName());

    private static Service instance;

    public static synchronized Service get() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    private final ServiceLoader<Platform> serviceLoader;
    private Platform provider;

    private Service() {
        serviceLoader = ServiceLoader.load(Platform.class);
        try {
            Iterator<Platform> iterator = serviceLoader.iterator();
            while (iterator.hasNext()) {
                if (provider == null) {
                    provider = iterator.next();
                    LOG.info(format("Using Provider: %s", provider.getClass().getName()));
                } else {
                    LOG.info(format("This Provider is ignored: %s", iterator.next().getClass().getName()));
                }
            }
        } catch (Exception e) {
            throw new ServiceConfigurationError("Failed to access + ", e);
        }
        if (provider == null) {
            LOG.severe("No Provider implementation could be found!");
        }
    }

    public File getFile(String s) {
        if (provider != null) {
            return provider.getFile(s);
        } 
        return null;
    }

    public void play(URL ul) {
        if (provider != null) {
            provider.play(ul);
        }
    }

    public void showMessage(String mess, String ti, Window w) {
        if (provider != null) {
            provider.showMessage(mess, ti, w);
        }
    }

    public Platform platform() {
        return provider;
    }

    public void takeCameraImage(ObjectProperty op) {
        if (provider != null) {
            provider.takeCameraImage(op);
        }
    }

    public void findImage(ObjectProperty op) {
        if (provider != null) {
            provider.findImage(op);
        }
    }

    public void restart(Window w) {
        if (provider != null) {
            provider.restart(w);
        }
    }

    public byte[] getBytes(Object ob) {
        if (provider != null) {
            return provider.getBytes(ob);
        }
        return null;
    }
    
    public void saveImage(Image im, String n, Contact con){
        if (provider!=null) {
            provider.saveImage(im, n, con);
        }
    }
    
    public Image loadImage(File f){
        if (provider!=null) {
            return provider.loadImage(f);
        }
        return null;
    }

    public void setMenuBar() {
        if (provider!=null){
            provider.setMenuBar();
        }
    }
    
    public void startRecord() {
        
    }
    
    public void stopRecord() {
        
    }
    public void playback() {
        
    }
}
