/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
@ManagedBean
public class ServiceManager implements Serializable{

    private static final String PERSISTENCE_UNIT_NAME = "HelloJPAPU";
    private static EntityManagerFactory factory;
    private EntityManager em;

    public ServiceManager() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
      
    /**
     * Inserts a new user in the database
     * @param username
     * @param password
     * @param experiment 
     */
    public void createUser(String username, String password, String experiment){
        SessionUser user = new SessionUser(username, password, experiment);
        
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
    }
    
    /**
     * Returns a list with all user in the database
     * @return 
     */
    private List getAllUser(){
        TypedQuery<SessionUser> query = em.createQuery("select user from SessionUser user", SessionUser.class);
        return query.getResultList();
    }
       
    /**
     * Inserts a new experiment into the database
     * @param name
     * @param nodes
     * @param datetime 
     */
    public void createExperiment(String name, ArrayList nodes, Date datetime){
        SessionExperiment experiment = new SessionExperiment(name, nodes, datetime);
        
        em.getTransaction().begin();
        em.persist(experiment);
        em.getTransaction().commit();
    }
    
    /**
     * Inserts an existing experiment into the database
     * @param experiment 
     */
    public void createExperiment(SessionExperiment experiment){
        
        SessionExperiment sessionExperiment = new SessionExperiment
                (experiment.getName(), experiment.getNodes(), experiment.getDatetime());
        
        em.getTransaction().begin();
        em.persist(sessionExperiment);
        em.getTransaction().commit();
    }
    
    /**
     * Returns a list with all experiments in the database
     * @return 
     */
    public List getAllExperiments(){
        TypedQuery<SessionExperiment> query = 
                em.createQuery("select experiment from SessionExperiment experiment", SessionExperiment.class);
        return query.getResultList();    
    }
    
}
