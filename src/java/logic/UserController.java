/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import eu.wisebed.api.rs.ReservervationConflictExceptionException;
import eu.wisebed.api.snaa.AuthenticationExceptionException;
import eu.wisebed.api.snaa.SNAAExceptionException;
import exceptions.DatabaseUserDuplicationException;
import exceptions.DatabaseUserNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import model.Node;
import model.User;
import service.ServiceManager;
import service.UserService;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
@ManagedBean(name = "controller")
@SessionScoped
public class UserController {

    ServiceManager session = new ServiceManager();
    UserService userService = new UserService();
    Reservation reservation = new Reservation();
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession)context.getExternalContext().getSession(true); 
    @EJB
    User user;

    public UserController(User user) {
        this.user = user;
    }
   
    public String reserve() {
        try {
            Logger.getLogger(User.class.getName())
                    .log(Level.INFO, "try to reserve..");

            //TODO autofilled urnPrefix
            this.reservation.reserveNodes("urn:wisebed:uzl1:",
                    user.getUsername() + "@wisebed1.itm.uni-luebeck.de",
                    user.getPassword(), user.getNodeURNs(), user.getOffset(), user.getDuration());
            user.setSecretReservationKey(this.reservation.getSecretReservationKey());
           
            if (user.getSecretReservationKey().isEmpty()) {
                return "index?error=authentication_error";
            }

            Date date = new java.util.Date();

            String experimentName = "experiment_" + date.toString();
            Pattern pattern = Pattern.compile(",");
            String[] URNs = pattern.split(user.getNodeURNs());
            List<String> nodeUrnList = Arrays.asList(URNs);
            ArrayList<Node> UrnArrayList = new ArrayList<Node>();
            
            for(String nodeUrn : nodeUrnList){
                UrnArrayList.add(new Node(nodeUrn));
            }

            if (userService.userExists(user.getUsername(), user.getPassword())) {
                updateExistingUser(experimentName, UrnArrayList, date);
            } else {
                storeNewSession(experimentName, UrnArrayList, date);
            }

            httpSession.setAttribute("authenticated", this.user.hashCode());
            httpSession.setAttribute("username", this.user.getUsername());
            
            try {
                SessionUser sessionUser = userService.getSessionUser(user.getUsername());
                int latestExperiment = sessionUser.getExperiments().size();
                
                return "experiments";
            } catch (Exception e) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, e.toString());
                return "experiment?show=1";
            }
        } catch (AuthenticationExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        } catch (SNAAExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        } catch (ReservervationConflictExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        }

        return "index";
    }

    private void updateExistingUser(String experimentName, ArrayList<Node> NodeList, Date date) {
        try {
            SessionUser sessionUser = userService.getSessionUser(user.getUsername());

            SessionExperiment sessionExperiment = new SessionExperiment(experimentName, NodeList, date, sessionUser, user.getSecretReservationKey());
            session.createExperiment(sessionExperiment);

            sessionUser.addExperiment(sessionExperiment);
            session.updateUser(sessionUser);

        } catch (DatabaseUserDuplicationException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
        } catch (DatabaseUserNotFoundException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    private void storeNewSession(String experimentName, ArrayList<Node> NodeList, Date date) {
        SessionUser sessionUser = new SessionUser(user.getUsername(), user.getPassword());

        SessionExperiment sessionExperiment = new SessionExperiment(experimentName, NodeList, date, sessionUser, user.getSecretReservationKey());
        
        sessionExperiment.setDuration(user.getDuration());
        sessionExperiment.setOffset(user.getOffset());
        
        session.createExperiment(sessionExperiment);
        session.createUser(sessionUser, sessionExperiment);
    }

    public String startLogin() {
        if(user.getExperimentLogin() == true){
            return this.reserve();
        }
        else{    
            return userLoginPossible();
        }
    }

    private String userLoginPossible() {
        try {
            SessionUser sessionUser = userService.getSessionUser(user.getUsername());
            if(sessionUser.getPassword().equals(user.getPassword())){
                httpSession.setAttribute("authenticated", this.user.hashCode());
                httpSession.setAttribute("username", this.user.getUsername());
                return "home";
            }
            else
                return "index";
        } catch (Exception e) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, e.toString());
            return "index";
        }
    }

    public String startLogout() {
        httpSession.removeAttribute("authenticated");
        httpSession.removeAttribute("username");
        return "index";
    }

    public String startReservation() {
        return this.reserve();
    }
}
