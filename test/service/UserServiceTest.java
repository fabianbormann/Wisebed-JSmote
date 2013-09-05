/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabaseUserNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.UserControllerTest;
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
public class UserServiceTest {

    ArrayList<SessionUser> testUsers;
    ServiceManager session;

    public UserServiceTest() {
    }

    @Before
    public void setUp() {
        this.session = new ServiceManager();
        this.testUsers = new ArrayList<SessionUser>();
    }

    @After
    public void tearDown() {
        for (SessionUser sessionUser : this.testUsers) {
            try {
                session.removeUser(sessionUser);
            } catch (DatabaseUserNotFoundException e) {
                Logger.getLogger(UserControllerTest.class.getName()).log(Level.SEVERE, e.toString());
            }
        }
    }

    @Test
    public void testUserExists() { 
        this.createTestUser();
        
        String username1 = "MyTestUser1";
        String password1 = "1234";

        String username2 = "MyTestUser2";
        String password2 = "12345";

        UserService userService = new UserService();

        boolean user1Exists = userService.userExists(username1, password1);
        boolean user2Exists = userService.userExists(username2, password2);
        
        assertEquals(true, user1Exists);
        assertEquals(false, user2Exists);
    }

    public void createTestUser() {
        SessionUser sessionUser1 = new SessionUser("MyTestUser1", "1234");
        session.createUser(sessionUser1, new SessionExperiment());

        this.testUsers.add(sessionUser1);
    }
}