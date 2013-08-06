/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import logic.ExperimentConsole;
import service.UserService;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
@ManagedBean(name = "job")
@SessionScoped
public class JobManager {

    private static final Logger debug = Logger.getLogger(JobManager.class.getName());
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(true);
    private ArrayList<NodeListener> listeners = new ArrayList<NodeListener>();
    private UserService userService = new UserService();
    private ExperimentConsole console = new ExperimentConsole();
    private String reservationKey = "";
    private String outputMessage = "";

    public String listen() {
        SessionExperiment experiment = this.getExperiment();

        if (experiment == null) {
            return "Experiment not Found!";
        } 
        else {
            String currentKey = experiment.getReservationKey();
            this.reservationKey = currentKey;
            
            if (isAlreadyListen(currentKey)) {
                return "";
            } else {
                try {
                    NodeListener listener = new NodeListener(currentKey, console);
                    listener.startListening();
                    this.listeners.add(listener);
                    return "Start Listening...";
                } catch (Exception e) {
                    if (e.getClass().equals(ExperimentNotRunningException_Exception.class)) {
                        return "Experiment is not running.";
                    }
                    debug.log(Level.SEVERE, e.toString());
                    return "An error occurred!";
                }
            }
        }
    }

    public void refreshConsole(){
        if(!this.reservationKey.isEmpty())
            this.outputMessage = console.getConsole(this.reservationKey);
    }
    
    private boolean isAlreadyListen(String currentKey) {
        for (NodeListener listener : listeners) {
            if (listener.getReservationKey().equals(currentKey)) {
                return true;
            }
        }
        return false;
    }

    private SessionExperiment getExperiment() {
        try {
            SessionUser databaseUser = this.userService.getSessionUser((String) httpSession.getAttribute("username"));
            SessionExperiment experiment = databaseUser.getExperiments().get(this.getExperimentId() - 1);
            if (experiment == null) {
                return null;
            } else {
                return experiment;
            }
        } catch (Exception e) {
            debug.log(Level.INFO, "An error ocurred while loading the experiment");
            debug.log(Level.SEVERE, e.toString());
            return null;
        }
    }

    private int getExperimentId() {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        return Integer.parseInt(params.get("show"));
    }

    public String getOutputMessage() {
        return outputMessage;
    }
    
}
