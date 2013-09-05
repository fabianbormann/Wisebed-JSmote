/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
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
public class ExperimentControllerTest {
    
    public ExperimentControllerTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getTimeleft() {
        Date date = new Date();
        SessionExperiment sessionExperiment = new SessionExperiment("testExperiment", new ArrayList<Node>(), date, new SessionUser(), "#123ABCFJLISASB123345");
          
        sessionExperiment.setDuration(20);
        sessionExperiment.setOffset(112121);
        
        long start = sessionExperiment.getDatetime().getTime();
        int duration = sessionExperiment.getDuration()*1000;
        int offset = sessionExperiment.getOffset()*1000;
        start += offset;
        long end = start+duration;
   
        Date experimentEnd = new Date();
        experimentEnd.setTime(end);
        
        Date experimentStart = new Date();
        experimentStart.setTime(start);
        
        Date currentDatetime = new Date();
        long current = currentDatetime.getTime();
        
        long timeLeft = (end-current)/1000;
        
        if(currentDatetime.before(experimentStart)){
            System.out.println("This experiment begins in "+this.getTimeToExperiment(start,current)+". The secret reservation key is: "+sessionExperiment.getReservationKey());
        }
        else if(currentDatetime.after(experimentEnd)) {
            System.out.println("This experiment is already finished.");
        }
        else if(currentDatetime.before(experimentEnd)) {
            System.out.println("This experiment will be finished in "+String.valueOf(timeLeft)+" secounds. The secret reservation key is: "+sessionExperiment.getReservationKey());
        }        
    }
    
    private String getTimeToExperiment(long start, long current){
        
        int seconds = (int) ((start-current)/1000);
        
        int day = (int)TimeUnit.SECONDS.toDays(seconds);        
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
        
        if(day > 0){
            return day+" day(s) "+hours+" hour(s) "+minutes+" minute(s) "+second+" secound(s)";
        }
        else if(hours > 0){
            return hours+" hour(s) "+minutes+" minute(s) "+second+" secound(s)";
        }
        else if(minutes > 0){
            return minutes+" minute(s) "+second+" secound(s)";
        }
        else{
            return second+" secound(s)";
        }
    }
}