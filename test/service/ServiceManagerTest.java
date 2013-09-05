/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabaseUserDuplicationException;
import exceptions.DatabaseUserNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
public class ServiceManagerTest {

    private static final String PERSISTENCE_UNIT_NAME = "JSmotePU";
    
    private static EntityManagerFactory factory;
    private EntityManager em;
    private SessionUser sessionUser;
    private ServiceManager serviceManager;
    private UserService userService;

    @Before
    public void setUp() {
        
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();

        sessionUser = new SessionUser("DeleteIt", "NotImportant");
        
        userService = new UserService();
        serviceManager = new ServiceManager();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testRemoveUser() { 
        
        String username = sessionUser.getName();
        
        TypedQuery<SessionUser> query = em.createQuery("select user from SessionUser user", SessionUser.class);
        int userCount = query.getResultList().size();
        
        query = em.createQuery("select user from SessionUser user WHERE user.name = :username", SessionUser.class)
                .setParameter("username", username);
        
        em.getTransaction().begin();
        
        em.remove(query.getResultList().get(0));
        
        em.getTransaction().commit();
        
        query = em.createQuery("select user from SessionUser user", SessionUser.class);
        int updatedUserCount = query.getResultList().size();
        
        assertEquals(userCount-1,  updatedUserCount);
    }
    
    @Test
    public void testCreateUser(){
        
        TypedQuery<SessionUser> query = em.createQuery("select user from SessionUser user", SessionUser.class);
        int userCount = query.getResultList().size();
        
        this.sessionUser.addExperiment(new SessionExperiment());
        
        em.getTransaction().begin();
        em.persist(this.sessionUser);
        em.getTransaction().commit();
        
        query = em.createQuery("select user from SessionUser user", SessionUser.class);
        int updatedUserCount = query.getResultList().size();
        
        assertEquals(userCount+1,  updatedUserCount);
    }
    
    @Test
    public void testSaveAndGetExperiments(){
        
        int userCountBefore = serviceManager.getAllUser().size();
        int experimentCountBefore = serviceManager.getAllExperiments().size();
        
        SessionUser testUser = new SessionUser("testUserWithExperiments", "1234");
        
        Date date = new java.util.Date();
        ArrayList<Node> nodeUrns = new ArrayList<Node>();
        nodeUrns.add(new Node("urn:wisebed:uzl1:0x2005"));
        nodeUrns.add(new Node("urn:wisebed:uzl1:0x2008"));
        
        SessionExperiment experiment = new SessionExperiment("NewExperiment001", nodeUrns, date, testUser);
        
        testUser.addExperiment(experiment);
        
        serviceManager.createExperiment(experiment);
        serviceManager.createUser(testUser);
        
        try {
            SessionUser loadedUser = userService.getSessionUser(testUser.getName());
            
            SessionExperiment latestUserExperiment = loadedUser.getExperiments().get(0);
            
            assertEquals(experiment.getName(), latestUserExperiment.getName());
            assertEquals(experiment.getDatetime(), latestUserExperiment.getDatetime());
            assertEquals(experiment.getSessionUser(), latestUserExperiment.getSessionUser());
            
        } catch (Exception e) {
            Logger.getLogger(ServiceManagerTest.class.getName()).log(Level.SEVERE, e.toString());
        }
        try {
            serviceManager.removeUser(testUser);
            serviceManager.removeExperiment(experiment);
        } catch (Exception e) {
            Logger.getLogger(ServiceManagerTest.class.getName()).log(Level.SEVERE, e.toString());
        }
        
        assertEquals(userCountBefore, serviceManager.getAllUser().size());
        assertEquals(experimentCountBefore, serviceManager.getAllExperiments().size());
 
    }
}