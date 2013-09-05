/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabaseUserDuplicationException;
import exceptions.DatabaseUserNotFoundException;
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
        .setParameter("name", username.toLowerCase())
        .setParameter("password", password);
         
        return !(query.getResultList().isEmpty());       
    }
    
    public SessionUser getSessionUser(String username) throws DatabaseUserDuplicationException, DatabaseUserNotFoundException{
        TypedQuery<SessionUser> query = em.createQuery("SELECT user FROM SessionUser user WHERE user.name = :name",
         SessionUser.class)
        .setParameter("name", username.toLowerCase());
         
        if(query.getResultList().isEmpty()){
            throw new DatabaseUserNotFoundException();
        }
        else if(query.getResultList().size() > 1){
            throw new DatabaseUserDuplicationException();
        }
        else{
            return query.getResultList().get(0);
        }
    }
}
