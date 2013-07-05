/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import eu.wisebed.api.rs.ReservervationConflictExceptionException;
import eu.wisebed.api.snaa.AuthenticationExceptionException;
import eu.wisebed.api.snaa.SNAAExceptionException;
import exceptions.DatabseUserDuplicationException;
import exceptions.DatabseUserNotFoundException;
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
public class EventController {

    ServiceManager session = new ServiceManager();
    UserService userService = new UserService();
    Reservation reservation = new Reservation();
    @EJB
    User user;
    Remote remote;

    public EventController(User user) {
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
            this.remote = new Remote(reservation.getReservedNodes());
            this.remote.setSecretReservationKey(user.getSecretReservationKey());

            //TODO flashing
            //this.remote.flashRemoteImage();

            if (user.getSecretReservationKey().isEmpty()) {
                return "index?error=authentication_error";
            }

            Date date = new java.util.Date();

            String experimentName = "experiment_" + date.toString();
            Pattern pattern = Pattern.compile(",");
            String[] URNs = pattern.split(user.getNodeURNs());
            List nodeUrnList = Arrays.asList(URNs);
            ArrayList<String> UrnArrayList = new ArrayList<String>();
            UrnArrayList.addAll(nodeUrnList);

            if (userService.userExists(user.getUsername(), user.getPassword())) {
                updateExistingUser(experimentName, UrnArrayList, date);
            } else {
                storeNewSession(experimentName, UrnArrayList, date);
            }

            return "manage";

        } catch (AuthenticationExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        } catch (SNAAExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        } catch (ReservervationConflictExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        }

        return "index";
    }

    private void updateExistingUser(String experimentName, ArrayList<String> NodeList, Date date) {
        try {
            SessionUser sessionUser = userService.getSessionUser(user.getUsername());

            SessionExperiment sessionExperiment = new SessionExperiment(experimentName, NodeList, date, sessionUser);
            session.createExperiment(sessionExperiment);

            sessionUser.addExperiment(sessionExperiment);
            session.updateUser(sessionUser);

        } catch (DatabseUserDuplicationException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
        } catch (DatabseUserNotFoundException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    private void storeNewSession(String experimentName, ArrayList<String> NodeList, Date date) {
        SessionUser sessionUser = new SessionUser(user.getUsername(), user.getPassword());

        SessionExperiment sessionExperiment = new SessionExperiment(experimentName, NodeList, date, sessionUser);
        session.createExperiment(sessionExperiment);
        session.createUser(sessionUser, sessionExperiment);
    }

    public String startFlashing() {
        this.remote.flashRemoteImage();
        return "manage";
    }

    public Remote getRemote() {
        return remote;
    }

    public void setRemote(Remote remote) {
        this.remote = remote;
    }
}
