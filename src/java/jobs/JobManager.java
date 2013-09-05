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
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
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
@ApplicationScoped
public class JobManager {

    private static final Logger debug = Logger.getLogger(JobManager.class.getName());
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(true);
    private ArrayList<NodeListener> listeners = new ArrayList<NodeListener>();
    private UserService userService = new UserService();
    private ExperimentConsole console = new ExperimentConsole();
    private String experimentId;
    private String consoleOutput = "";

    public String listen(String experimentId) {

        if (experimentId.isEmpty()) {
            experimentId = this.experimentId;
        } else {
            this.experimentId = experimentId;
        }

        debug.log(Level.INFO, experimentId);
        SessionExperiment experiment = getExperiment(Integer.parseInt(experimentId));

        if (experiment == null) {
            return "Experiment not Found!";
        } else {
            String currentKey = experiment.getReservationKey();

            if (isAlreadyListen(currentKey)) {
                consoleOutput += console.getConsole(experiment.getReservationKey());
                return consoleOutput;
            } else {
                try {
                    NodeListener listener = new NodeListener(currentKey, console);
                    listener.startListening();
                    this.listeners.add(listener);

                    console.LogToConsole(currentKey, "JSmote: Start Listening...");

                    consoleOutput += console.getConsole(experiment.getReservationKey());
                    return consoleOutput;
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

    private boolean isAlreadyListen(String currentKey) {
        for (NodeListener listener : listeners) {
            if (listener.getReservationKey().equals(currentKey)) {
                return true;
            }
        }
        return false;
    }

    private SessionExperiment getExperiment(int experimentId) {
        try {
            SessionUser databaseUser = this.userService.getSessionUser((String) httpSession.getAttribute("username"));
            SessionExperiment experiment = databaseUser.getExperiments().get(experimentId - 1);
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
}
