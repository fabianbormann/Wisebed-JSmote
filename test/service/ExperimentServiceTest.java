/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import exceptions.DatabaseExperimentNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Node;
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
    private ExperimentService experimentService = new ExperimentService();
    private ServiceManager service = new ServiceManager();
    
    @Before
    public void setUp() {
        addTestExperiment();
    }
    
    @Test
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
}