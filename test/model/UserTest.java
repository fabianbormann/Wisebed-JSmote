/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import exceptions.DatabseUserDuplicationException;
import exceptions.DatabseUserNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import logic.Reservation;
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
public class UserTest {
    
    ServiceManager session;
    Reservation reservation;
    UserService userService;
    String username;
    String password;
    ArrayList<String> nodeURNs;
    ArrayList<SessionUser> testUsers;
    String experimentName;
    Date date;
    
    @Before
    public void setUp() {
        this.session = new ServiceManager(); 
        this.userService = new UserService();
        this.reservation = new Reservation();
        this.username = "Fabian";
        this.password = "Geheim";
        String nodeURNText = "urn:wisebed:uzl1:0x2001;urn:wisebed:uzl1:0x2004"
                + ";urn:wisebed:uzl1:0x2005;urn:wisebed:uzl1:0x2008";
        
        this.date = new java.util.Date();
        
        this.experimentName = "001_experiment" + this.date.toString();
        Pattern pattern = Pattern.compile(";");
        String[] URNs = pattern.split(nodeURNText);
        List nodeUrnList = Arrays.asList(URNs);
        
        this.nodeURNs = new ArrayList<String>();
        this.nodeURNs.addAll(nodeUrnList);
        
        this.testUsers = new ArrayList<SessionUser>();
    }
    
    @After
    public void tearDown() {
        for(SessionUser user : this.testUsers){
            try { 
                session.removeUser(user);
            } catch (DatabseUserNotFoundException e) {
                Logger.getLogger(UserTest.class.getName()).log(Level.SEVERE, e.toString());
            }
        }  
    }
        
    @Test
    public void testStoreNewSession() {
        SessionUser sessionUser = new SessionUser(this.username, this.password);
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
        
        testUsers.add(currentUser);
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
            assertEquals(DatabseUserDuplicationException.class, e.getClass());
        }
        
        try {
            SessionUser sessionUser = userService.getSessionUser(userName3);
               
            SessionExperiment sessionExperiment = new SessionExperiment(this.experimentName, this.nodeURNs, this.date, sessionUser);
            session.createExperiment(sessionExperiment);
            
            sessionUser.addExperiment(sessionExperiment);
            session.updateUser(sessionUser);
            
        } catch (Exception e) {
            assertEquals(DatabseUserNotFoundException.class, e.getClass());
        }
    }
    
    public void createTestUsers(){
        SessionUser sessionUser1 = new SessionUser("TestUser1", "Test1");
        session.createUser(sessionUser1, new SessionExperiment());
        SessionUser sessionUser2 = new SessionUser("TestUser2", "Test2");
        session.createUser(sessionUser2, new SessionExperiment());
        SessionUser sessionUser3 = new SessionUser("TestUser2", "Test2");
        session.createUser(sessionUser3, new SessionExperiment());
        
        testUsers.add(sessionUser1);
        testUsers.add(sessionUser2);
        testUsers.add(sessionUser3);
    }
    
}