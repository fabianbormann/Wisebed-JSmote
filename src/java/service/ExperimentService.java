/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import session.SessionExperiment;
import session.SessionUser;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 *
 * @author Fabian
 */
public class ExperimentService {

    private static final String PERSISTENCE_UNIT_NAME = "JSmotePU";
    private static EntityManagerFactory factory;
    private EntityManager em;

    public ExperimentService() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }

    public void updateExperiment(SessionExperiment experiment) {
        em.getTransaction().begin();
        em.merge(experiment);
        em.getTransaction().commit();
    }

    public SessionExperiment getExperiment(SessionExperiment experiment) {

        String name = experiment.getName();
        Date date = experiment.getDatetime();

        TypedQuery<SessionExperiment> query =
                em.createQuery("select experiment from SessionExperiment experiment WHERE experiment.name = :experimentName"
                + " AND experiment.datetime = :datetime", SessionExperiment.class)
                .setParameter("experimentName", name)
                .setParameter("datetime", date);

        return query.getResultList().get(0);
    }
}
