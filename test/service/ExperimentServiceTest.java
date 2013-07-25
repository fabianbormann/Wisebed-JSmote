/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabaseExperimentNotFoundException;
import exceptions.DatabaseUserDuplicationException;
import exceptions.DatabaseUserNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Node;
import model.User;
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
public class ExperimentServiceTest {

    private SessionExperiment experiment;
    private SessionUser experimentUser;
    private ExperimentService experimentService = new ExperimentService();
    private ServiceManager service = new ServiceManager();

    @Before
    public void setUp() {
        //  addTestExperiment();
        SessionUser sessionUser = new SessionUser("Fabian", "geheim");
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(new Node("0xACF1"));
        Date date = new Date();
        int duration = 40;
        int offset = 22;
        SessionExperiment experiment1 = new SessionExperiment("Experiment01", nodes, date, sessionUser, "XXFGDHSLADJE990uCD");
        experiment1.setOffset(offset);
        experiment1.setDuration(duration);

        service.createUser(sessionUser, experiment1);
        this.experimentUser = sessionUser;
    }

    @After
    public void cleanDatabase() throws DatabaseUserNotFoundException, DatabaseExperimentNotFoundException{
        service.removeUser(experimentUser);  
    }
    
    //@Test
    public void testUpdateExperiment() {
        int experimentCount = this.service.getAllExperiments().size();

        SessionExperiment databaseExperiment = this.experimentService.getExperiment(this.experiment);
        String previousCode = databaseExperiment.getCode();

        String updatedCode = "hello world";
        databaseExperiment.setCode(updatedCode);
        experimentService.updateExperiment(databaseExperiment);

        SessionExperiment loadedDatabaseExperiment = this.experimentService.getExperiment(this.experiment);
        String loadedUpdatedCode = loadedDatabaseExperiment.getCode();

        assertEquals(updatedCode, loadedUpdatedCode);
        assertNotSame(previousCode, loadedUpdatedCode);

        // cleanUpTestExperiment();

        //  assertEquals(experimentCount-1, this.service.getAllExperiments().size());
    }

    private void addTestExperiment() {
        Date date = new Date();
        SessionUser sessionUser = new SessionUser("TestUser22", "1234");

        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(new Node(("urn:wisebed:uzl1:0x2001")));
        SessionExperiment sessionExperiment = new SessionExperiment("testExperiment22", nodes, date, sessionUser);

        sessionUser.addExperiment(sessionExperiment);

        this.experiment = sessionExperiment;
        this.service.createExperiment(this.experiment);
    }

    private void cleanUpTestExperiment() {
        try {
            this.service.removeExperiment(this.experiment);
        } catch (DatabaseExperimentNotFoundException e) {
            Logger.getLogger(ExperimentServiceTest.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    //@Test
    public void addMoreExperimentsTest() {
        //STORE NEW USER AND EXPERIMENT
        //SETUP EXPERIMENT 1
        SessionUser sessionUser = new SessionUser("Fabian", "geheim");
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(new Node("0xACF1"));
        Date date = new Date();
        int duration = 40;
        int offset = 22;
        SessionExperiment experiment1 = new SessionExperiment("Experiment01", nodes, date, sessionUser, "XXFGDHSLADJE990uCD");
        experiment1.setOffset(offset);
        experiment1.setDuration(duration);

        service.createUser(sessionUser, experiment1);

        //FIND USER
        UserService userService = new UserService();
        SessionUser sessionUser2 = null;
        try {
            sessionUser2 = userService.getSessionUser("Fabian");
        } catch (Exception e) {
            Logger.getLogger(ExperimentServiceTest.class.getName()).log(Level.SEVERE, e.toString());
        }

        //ADD NEW EXPERIMENT TO EXISTING USER
        //SETUP EXPERIMENT 2
        nodes.clear();
        nodes.add(new Node("0xDD73"));
        date = new Date();
        duration = 5;
        offset = 0;

        service.updateUser(sessionUser2);

        SessionExperiment experiment2 = new SessionExperiment("Experiment02", nodes, date, sessionUser2, "WACC23333ftb44HJuCD");
        experiment2.setOffset(offset);
        experiment2.setDuration(duration);

        service.createExperiment(experiment2);
        sessionUser.addExperiment(experiment2);
        service.updateUser(sessionUser);

    }

    @Test
    public void UserExperimentConnectionTest() throws DatabaseUserDuplicationException, DatabaseUserNotFoundException {
        int experimentCountBefore = service.getAllExperiments().size();
        UserService userService = new UserService();
        SessionUser sessionUser = userService.getSessionUser("Fabian");
        
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(new Node("0xbbff03"));
        Date date = new Date();
        int duration = 20;
        int offset = 2;
        SessionExperiment newExperiment = new SessionExperiment("Experiment02", nodes, date, sessionUser, "XXFGDSNBDZINW22i39j");
        newExperiment.setOffset(offset);
        newExperiment.setDuration(duration);
            
        int experimentUserCountBefore = sessionUser.getExperiments().size();
             
        sessionUser.addExperiment(newExperiment);
        service.updateUser(sessionUser);
        
        int experimentCountAfter = service.getAllExperiments().size();
        
        SessionUser reloadedSessionUser = userService.getSessionUser("Fabian");
        int experimentUserCountAfter = reloadedSessionUser.getExperiments().size();
        
        assertEquals(experimentUserCountBefore+1, experimentUserCountAfter);
        assertEquals(experimentCountBefore+1, experimentCountAfter);
    }
}