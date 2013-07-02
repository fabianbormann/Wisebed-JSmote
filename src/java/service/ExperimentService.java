/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
public class ExperimentService {
    
    private static final String PERSISTENCE_UNIT_NAME = "HelloJPAPU";
    private static EntityManagerFactory factory;
    private EntityManager em;

    public ExperimentService() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
    
    public void updateUserExperiments(SessionExperiment experiment, SessionUser user){
        ArrayList experiments = user.getExperiments();
        experiments.add(experiment);
        
        user.setExperiments(experiments);
 
        em.createQuery("UPDATE SessionUser user SET user.experiments = :experiments "
                + "WHERE user.name = :user")
        .setParameter("name", user.getName())
        .setParameter("experiments", user.getExperiments());
    }

    private List findExperiment(String name){
        return em.createQuery(
        "SELECT experiment FROM SessionExperiment experiment WHERE experiment.name = :name")
        .setParameter("name", name)
        .getResultList(); 
    }
    
    public SessionExperiment getExperiment(String name){
        SessionExperiment experiment = (SessionExperiment) findExperiment(name).get(0);
        return experiment;
    }
    
}
