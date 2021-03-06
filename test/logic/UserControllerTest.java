/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import exceptions.DatabaseUserDuplicationException;
import exceptions.DatabaseUserNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import model.Node;
import model.User;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import service.ServiceManager;
import service.UserService;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
public class UserControllerTest {
    
    ServiceManager session;
    Reservation reservation;
    UserService userService;
    ArrayList<SessionUser> testUsers;
    ArrayList<Node> nodeURNs;
    String experimentName;
    Date date;
    User user;
    
    @Before
    public void setUp() {
        this.session = new ServiceManager(); 
        this.userService = new UserService();
        this.reservation = new Reservation();
        this.user = new User();
        user.setUsername("Fabian");
        user.setPassword("Geheim");
        String nodeURNText = "urn:wisebed:uzl1:0x2001;urn:wisebed:uzl1:0x2004"
                + ",urn:wisebed:uzl1:0x2005,urn:wisebed:uzl1:0x2008";
        
        this.date = new java.util.Date();
        
        this.experimentName = "001_experiment" + this.date.toString();
        Pattern pattern = Pattern.compile(",");
        String[] URNs = pattern.split(nodeURNText);
        List nodeUrnList = Arrays.asList(URNs);
        
        this.nodeURNs = new ArrayList<Node>();
        this.nodeURNs.addAll(nodeUrnList);
        
        this.testUsers = new ArrayList<SessionUser>();
    }
    
    @After
    public void tearDown() {
        for(SessionUser sessionUser : this.testUsers){
            try { 
                session.removeUser(sessionUser);
            } catch (DatabaseUserNotFoundException e) {
                Logger.getLogger(UserControllerTest.class.getName()).log(Level.SEVERE, e.toString());
            }
        }  
    }
        
    @Test
    public void testStoreNewSession() {
        SessionUser sessionUser = new SessionUser(user.getUsername(), user.getPassword());
        SessionExperiment sessionExperiment = new SessionExperiment(this.experimentName, this.nodeURNs, this.date, sessionUser);
        
        session.createExperiment(sessionExperiment);
        session.createUser(sessionUser, sessionExperiment);
        
        List<SessionUser> users = session.getAllUser();
        SessionUser currentUser = users.get(users.size()-1);
        
        assertEquals(sessionUser.getName(), currentUser.getName());
        assertEquals(sessionUser.getPassword(), currentUser.getPassword());
        
        SessionExperiment currentExperiment = currentUser.getExperiments().get(0);
               
        assertEquals(experimentName, currentExperiment.getName());
        assertEquals(this.nodeURNs, currentExperiment.getNodes());
        assertEquals(date, currentExperiment.getDatetime());
        
        this.testUsers.add(currentUser);
    }
    
    @Test
    public void testUpdateExistingUser() {
        createTestUsers();
        
        String userName1 = "TestUser1"; //should be valid
        String userName2 = "TestUser2"; //DatabseUserDuplicationException
        String userName3 = "TestUser3"; //DatabseUserNotFoundException
        
        try {
            SessionUser sessionUser = userService.getSessionUser(userName1);
               
            SessionExperiment sessionExperiment = new SessionExperiment(this.experimentName, this.nodeURNs, this.date, sessionUser);
            session.createExperiment(sessionExperiment);
            
            sessionUser.addExperiment(sessionExperiment);
            session.updateUser(sessionUser);
            
        } catch (Exception e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
        }
        
        try {
            SessionUser sessionUser = userService.getSessionUser(userName2);
               
            SessionExperiment sessionExperiment = new SessionExperiment(this.experimentName, this.nodeURNs, this.date, sessionUser);
            session.createExperiment(sessionExperiment);
            
            sessionUser.addExperiment(sessionExperiment);
            session.updateUser(sessionUser);
            
        } catch (Exception e) {
            assertEquals(DatabaseUserDuplicationException.class, e.getClass());
        }
        
        try {
            SessionUser sessionUser = userService.getSessionUser(userName3);
               
            SessionExperiment sessionExperiment = new SessionExperiment(this.experimentName, this.nodeURNs, this.date, sessionUser);
            session.createExperiment(sessionExperiment);
            
            sessionUser.addExperiment(sessionExperiment);
            session.updateUser(sessionUser);
            
        } catch (Exception e) {
            assertEquals(DatabaseUserNotFoundException.class, e.getClass());
        }
    }
    
    public void createTestUsers(){
        SessionUser sessionUser1 = new SessionUser("TestUser1", "Test1");
        session.createUser(sessionUser1, new SessionExperiment());
        SessionUser sessionUser2 = new SessionUser("TestUser2", "Test2");
        session.createUser(sessionUser2, new SessionExperiment());
        SessionUser sessionUser3 = new SessionUser("TestUser2", "Test2");
        session.createUser(sessionUser3, new SessionExperiment());
        
        this.testUsers.add(sessionUser1);
        this.testUsers.add(sessionUser2);
        this.testUsers.add(sessionUser3);
    }
    
    @Test
    public void testUserExists(){
        createTestUsers();
        
    }
    
}