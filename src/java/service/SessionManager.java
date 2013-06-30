/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.Experiment;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
public class SessionManager {
    final static EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("$objectdb/db/jsmote.odb");
    final static EntityManager em = emf.createEntityManager();  
    
    public static void createUser(String username, String password, String experiment){
        SessionUser user = new SessionUser(username, password, experiment);
        
        em.persist(user);
        em.getTransaction().commit();
    }
    
    private static List findUser(String username, String password){
        return em.createQuery(
        "SELECT user FROM SessionUser user WHERE user.name = :username AND user.password = :password")
        .setParameter("name", username)
        .setParameter("password", password)
        .getResultList();
    }
    
    public static SessionUser getUser(String username, String password){
        SessionUser user = (SessionUser) findUser(username,password).get(0);
        return user;
    }
    
    public static void updateUserExperiments(Experiment experiment, SessionUser user){
        ArrayList experiments = user.getExperiments();
        experiments.add(experiment);
        
        user.setExperiments(experiments);
 
        em.createQuery("UPDATE SessionUser user SET user.experiments = :experiments "
                + "WHERE user.name = :user")
        .setParameter("name", user.getName())
        .setParameter("experiments", user.getExperiments());
    }
    
    public static void createExperiment(String name, ArrayList nodes, Date datetime){
        SessionExperiment experiment = new SessionExperiment(name, nodes, datetime);
        
        em.persist(experiment);
        em.getTransaction().commit();
    }
    
    public static void saveExperiment(Experiment experiment){
        
        SessionExperiment session_experiment = new SessionExperiment
                (experiment.getName(), experiment.getNodes(), experiment.getDatetime());
        
        em.persist(session_experiment);
        em.getTransaction().commit();
    }
    
    private static List findExperiment(String name){
        return em.createQuery(
        "SELECT experiment FROM SessionExperiment experiment WHERE experiment.name = :name")
        .setParameter("name", name)
        .getResultList();
    }
    
    public static SessionExperiment getExperiment(String name){
        SessionExperiment experiment = (SessionExperiment) findExperiment(name).get(0);
        return experiment;
    }
}
