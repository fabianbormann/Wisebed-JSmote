/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import service.ServiceManagerTest;
import session.SessionImage;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
public class ImageTypeTest {

    private static final String PERSISTENCE_UNIT_NAME = "JSmotePU";
    private static EntityManagerFactory factory;
    private EntityManager em;

    @Before
    public void setUp() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void storeBinaryTest() {
        try {
            String relativeWebPath = "C:\\Users\\Fabian\\GSoC\\Wisebed-JSmote\\binaryImages\\remote_app.bin";
            File file = new File(relativeWebPath);

            Path path = Paths.get(file.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);

            SessionImage sessionImage = new SessionImage("iSense", data);

            em.getTransaction().begin();
            em.persist(sessionImage);
            em.getTransaction().commit();

        } catch (IOException e) {
            Logger.getLogger(ImageTypeTest.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    @Test
    public void getiSenseBinaryTest() {
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

            assertEquals(true, file.exists());
            
            System.out.println(file.getAbsolutePath());       
        } catch (IOException e) {
            Logger.getLogger(ImageTypeTest.class.getName()).log(Level.SEVERE, e.toString());
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger(ImageTypeTest.class.getName()).log(Level.SEVERE, e.toString());
            }
        }
    }
}