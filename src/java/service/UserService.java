/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabseUserDuplicationException;
import exceptions.DatabseUserNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.User;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
public class UserService {
    
    private static final String PERSISTENCE_UNIT_NAME = "JSmotePU";
    
    private static EntityManagerFactory factory;
    private EntityManager em;

    public UserService() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
    
     /**
     * Returns true if a combination of username and password already exists
     * @return 
     */
    public boolean userExists(String username, String password) {    
        TypedQuery<SessionUser> query = em.createQuery("SELECT user FROM SessionUser user WHERE user.name = :name AND "
                + "user.password = :password",
         SessionUser.class)
        .setParameter("name", username)
        .setParameter("password", password);
         
        return query.getResultList().isEmpty();       
    }
    
    public SessionUser getSessionUser(String username) throws DatabseUserDuplicationException, DatabseUserNotFoundException{
         TypedQuery<SessionUser> query = em.createQuery("SELECT user FROM SessionUser user WHERE user.name = :name",
         SessionUser.class)
        .setParameter("name", username);
         
        if(query.getResultList().isEmpty()){
            throw new DatabseUserNotFoundException();
        }
        else if(query.getResultList().size() > 1){
            throw new DatabseUserDuplicationException();
        }
        else{
            return query.getResultList().get(0);
        }
    }
}
