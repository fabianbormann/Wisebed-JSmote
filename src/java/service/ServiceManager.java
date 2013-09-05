/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabaseExperimentNotFoundException;
import exceptions.DatabaseUserNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class ServiceManager implements Serializable {

    private static final String PERSISTENCE_UNIT_NAME = "JSmotePU";
    private static EntityManagerFactory factory;
    private EntityManager em;

    public ServiceManager() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }

    /**
     * Inserts a existing SessionUser in the database and add it's experiment
     *
     * @param username
     * @param password
     * @param experiment
     */
    public void createUser(SessionUser user, SessionExperiment experiment) {
        user.setName(user.getName().toLowerCase());
        user.addExperiment(experiment);

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
    }

    /**
     * Inserts a existing SessionUser in the database
     *
     * @param username
     * @param password
     * @param experiment
     */
    public void createUser(SessionUser user) {
        user.setName(user.getName().toLowerCase());
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
    }

    /**
     * Returns a list with all user in the database
     *
     * @return
     */
    public List getAllUser() {
        TypedQuery<SessionUser> query = em.createQuery("select user from SessionUser user", SessionUser.class);
        return query.getResultList();
    }

    /**
     * Inserts a new experiment into the database
     *
     * @param name
     * @param nodes
     * @param datetime
     */
    public void createExperiment(String name, ArrayList nodes, Date datetime, SessionUser sessionUser, String reservationKey) {
        SessionExperiment experiment = new SessionExperiment(name, nodes, datetime, sessionUser, reservationKey);

        em.getTransaction().begin();
        em.persist(experiment);
        em.getTransaction().commit();
    }

    /**
     * Inserts an existing experiment into the database
     *
     * @param experiment
     */
    public void createExperiment(SessionExperiment experiment) {
        em.getTransaction().begin();
        em.persist(experiment);
        em.getTransaction().commit();
    }

    /**
     * Returns a list with all experiments in the database
     *
     * @return
     */
    public List getAllExperiments() {
        TypedQuery<SessionExperiment> query =
                em.createQuery("select experiment from SessionExperiment experiment", SessionExperiment.class);
        return query.getResultList();
    }

    public void updateUser(SessionUser sessionUser) {
        em.getTransaction().begin();    
        em.merge(sessionUser);
        em.getTransaction().commit();
    }

    public void removeUser(SessionUser sessionUser) throws DatabaseUserNotFoundException {
        String username = sessionUser.getName().toLowerCase();

        TypedQuery<SessionUser> query = em.createQuery("select user from SessionUser user WHERE user.name = :username", SessionUser.class)
                .setParameter("username", username);
        if (!query.getResultList().isEmpty()) {
            em.getTransaction().begin();
            em.remove(query.getResultList().get(0));
            em.getTransaction().commit();
        } else {
            throw new DatabaseUserNotFoundException();
        }
    }

    public void removeExperiment(SessionExperiment sessionExperiment) throws DatabaseExperimentNotFoundException{
        String name = sessionExperiment.getName();
        Date date = sessionExperiment.getDatetime();

        TypedQuery<SessionExperiment> query = 
            em.createQuery("select experiment from SessionExperiment experiment WHERE experiment.name = :experimentName"
                + " AND experiment.datetime = :datetime", SessionExperiment.class)
                .setParameter("experimentName", name)
                .setParameter("datetime", date);
        if (!query.getResultList().isEmpty()) {
            em.getTransaction().begin();
            em.remove(query.getResultList().get(0));
            em.getTransaction().commit();
        } else {
            throw new DatabaseExperimentNotFoundException();
        }
    }
}
