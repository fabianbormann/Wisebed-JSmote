/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
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

    @Before
    public void setUp() {
        
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();

        sessionUser = new SessionUser("DeleteIt", "NotImportant");
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
}