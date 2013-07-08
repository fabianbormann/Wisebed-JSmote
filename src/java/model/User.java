/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import logic.EventController;
import service.UserService;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
@ManagedBean
@SessionScoped
public class User {

    private String username;
    private String password;
    private String urnPrefix;
    private String nodeURNs;
    private String secretReservationKey;
    private Integer offset = 0;
    private Integer duration = 5;
    private boolean experimentLogin = false;
    
    private EventController controller = new EventController(this);
    private UserService userService = new UserService();

    public String getUrnPrefix() {
        return urnPrefix;
    }

    public void setUrnPrefix(String urnPrefix) {
        this.urnPrefix = urnPrefix;
    }

    public String getNodeURNs() {
        return nodeURNs;
    }

    public void setNodeURNs(String nodeURNs) {
        this.nodeURNs = nodeURNs;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretReservationKey() {
        return secretReservationKey;
    }

    public void setSecretReservationKey(String secretReservationKey) {
        this.secretReservationKey = secretReservationKey;
    }
    
    public String doLogin(){
        return controller.startLogin();
    }
      
    public String doLogout(){
        return controller.startLogout();
    }
    
    public String doFlash(){
        return controller.startFlashing();
    }

    public Boolean getExperimentLogin() {
        return experimentLogin;
    }
    
    public void setExperimentLogin(Boolean experimentLogin) {
        this.experimentLogin = experimentLogin;
    }

    public Experiment[] getPreviousExperiment(){
            try {
            SessionUser databaseUser = this.userService.getSessionUser(this.username);
            ArrayList<Experiment> experimentList = new ArrayList<Experiment>();

            int experimentIndex = 1;
            
            for(SessionExperiment previousExperiment : databaseUser.getExperiments()){
                experimentList.add(new Experiment(experimentIndex,previousExperiment.getName(), 
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
    
    public String getExperimentName(int experimentId){
        try {
            SessionUser databaseUser = this.userService.getSessionUser(this.username);
            SessionExperiment experiment = databaseUser.getExperiments().get(experimentId-1);
            if(experiment == null){
                return "experiments?faces-redirect=true;";
            }
            else{
                return experiment.getName();
            }     
        } catch (Exception e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
            return "experiments?faces-redirect=true;";
        }
    }
    
    public String getExperimentNodes(int experimentId){
        try {
            SessionUser databaseUser = this.userService.getSessionUser(this.username);
            SessionExperiment experiment = databaseUser.getExperiments().get(experimentId-1);
            if(experiment == null){
                return "experiments?faces-redirect=true;";
            }
            else{
                return experiment.getNodes().toString();
            }     
        } catch (Exception e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
            return "experiments?faces-redirect=true;";
        }  
    }
    
}
