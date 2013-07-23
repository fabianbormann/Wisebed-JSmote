/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import session.SessionImage;

/**
 *
 * @author Fabian
 */
public enum EImageType {
    
    ISENSE, TELOSB;
    
    private static final String PERSISTENCE_UNIT_NAME = "JSmotePU";
    private static EntityManagerFactory factory;
    private EntityManager em;
    
    private String telosbBinary;

    public String getBinaryPath(){
        switch(this){
            case ISENSE:
                return getiSenseBinaryPath();
            case TELOSB:
                return telosbBinary;
            default:
                return "";
        }
    }
    
    public String getiSenseBinaryPath() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
        File file;
        FileOutputStream fileOutputStream = null;

        TypedQuery<SessionImage> query = em.createQuery("select image from SessionImage image WHERE image.type = :imageType", SessionImage.class)
                .setParameter("imageType", "iSense");

        SessionImage sessionImage = query.getResultList().get(0);

        byte[] image = sessionImage.getImage();

        try {
            file = new File("iSenseImage.bin");
            file.setReadable(true);
            fileOutputStream = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            }

            fileOutputStream.write(image);
            fileOutputStream.flush();
            fileOutputStream.close();
            
            return file.getAbsolutePath();       
        } catch (IOException e) {
            Logger.getLogger(EImageType.class.getName()).log(Level.SEVERE, e.toString());
            return "";
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger(EImageType.class.getName()).log(Level.SEVERE, e.toString());
            }
        }
    }
}