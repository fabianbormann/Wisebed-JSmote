/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;
import model.Experiment;
import model.User;
import service.ExperimentService;
import service.UserService;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
@ManagedBean(name = "experiment")
public class ExperimentController {

    private UserService userService = new UserService();
    private ExperimentService experimentService = new ExperimentService();
    private String Name;
    private String code;
    private String Nodes;
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(true);

    public Experiment[] getPreviousExperiments() {
        try {
            SessionUser databaseUser = this.userService.getSessionUser((String) httpSession.getAttribute("username"));
            ArrayList<Experiment> experimentList = new ArrayList<Experiment>();

            int experimentIndex = 1;

            for (SessionExperiment previousExperiment : databaseUser.getExperiments()) {
                experimentList.add(new Experiment(experimentIndex, previousExperiment.getName(),
                        previousExperiment.getNodes().size(), previousExperiment.getDatetime()));
                experimentIndex++;
            }

            return experimentList.toArray(new Experiment[experimentList.size()]);
        } catch (Exception e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
            Experiment[] experiments = new Experiment[1];
            Date date = new Date();
            experiments[0] = new Experiment(1, "There is no Experiment", 0, date);
            return experiments;
        }
    }

    public String getName() {
        SessionExperiment experiment = this.getExperiment();
        if (experiment == null) {
            this.Name = "";
            return "experiments?faces-redirect=true;";
        } else {
            this.Name = experiment.getName();
            return this.Name;
        }
    }
    
    public void setName(String name){
        
    }

    public String getNodes() {
        SessionExperiment experiment = this.getExperiment();
        if (experiment == null) {
            this.Nodes = "";
            return "experiments?faces-redirect=true;";
        } else {
            this.Nodes = experiment.getNodes().toString();
            return this.Nodes;
        }
    }

    private int getExperimentId() {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        return Integer.parseInt(params.get("show"));   
    }

    public void setNodes(String nodes) {
    }

    public String getCode() {
        SessionExperiment experiment = this.getExperiment();
        if (experiment == null) {
            this.code = "";
            return "experiments?faces-redirect=true;";
        } else {
            this.code = experiment.getCode();
            return this.code;
        }
    }

    public void setCode(String code) {
        SessionExperiment experiment = this.getExperiment();
        if (experiment != null) {
            this.code = code;
            experiment.setCode(this.code);
            experimentService.updateExperiment(experiment);       
        }
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
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
            return null;
        }
    }

    public String run() {
        //this.doFlash();
        return "experiment?show=" + this.getExperimentId();
    }
}
