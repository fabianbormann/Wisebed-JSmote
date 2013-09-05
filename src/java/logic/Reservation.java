package logic;

import com.google.common.base.Joiner;
import de.uniluebeck.itm.wisebed.cmdlineclient.BeanShellHelper;
import eu.wisebed.api.rs.ConfidentialReservationData;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.rs.ReservervationConflictExceptionException;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.AuthenticationExceptionException;
import eu.wisebed.api.snaa.AuthenticationTriple;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.api.snaa.SNAAExceptionException;
import eu.wisebed.testbed.api.rs.RSServiceHelper;
import eu.wisebed.testbed.api.snaa.helpers.SNAAServiceHelper;
import eu.wisebed.testbed.api.wsn.WSNServiceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.faces.bean.SessionScoped;


/**
 *
 * @author Fabian
 */
@SessionScoped
public class Reservation {

    private String urnPrefix;
    private String username;
    private String password;
    private int offset;
    private int duration;
    private List nodeURNs;
    private String secretReservationKey;
    //TODO use variable endpoints
    private String snaaEndpointURL = "http://wisebed.itm.uni-luebeck.de:8890/snaa";
    private String rsEndpointURL = "http://wisebed.itm.uni-luebeck.de:8889/rs";
    private String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";
    private SNAA authenticationSystem = SNAAServiceHelper.getSNAAService(snaaEndpointURL);
    private RS reservationSystem = RSServiceHelper.getRSService(rsEndpointURL);
    private SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

    public String getSecretReservationKey() {
        return secretReservationKey;
    }

    public void reserveNodes(String urnPrefix, String username, String password,
            String nodeURNs, int offset, int duration) throws AuthenticationExceptionException, SNAAExceptionException,
            ReservervationConflictExceptionException {
        this.urnPrefix = urnPrefix;
        this.username = username;
        this.password = password;
        this.nodeURNs = splitURNs(nodeURNs);
        this.duration = duration;
        this.offset = offset;

        List credentialsList = new ArrayList();
        AuthenticationTriple credentials = new AuthenticationTriple();

        credentials.setUrnPrefix(this.urnPrefix);
        credentials.setUsername(this.username);
        credentials.setPassword(this.password);

        credentialsList.add(credentials);

        List secretAuthenticationKeys = authenticationSystem.authenticate(credentialsList);
        Logger.getLogger(Reservation.class.getName()).log(Level.INFO, "Successfully authenticated!");

        List nodeURNsToReserve = this.nodeURNs;

        Logger.getLogger(Reservation.class.getName()).log(Level.INFO,
                "Retrieved this node URNs: {}", Joiner.on(", ").join(nodeURNsToReserve));

        ConfidentialReservationData reservationData = BeanShellHelper.generateConfidentialReservationData(
                nodeURNsToReserve,
                new Date(System.currentTimeMillis() + (this.offset * 60 * 1000)), this.duration, TimeUnit.MINUTES,
                this.urnPrefix, this.username);

        try {
            List secretReservationKeys = reservationSystem.makeReservation(
                    BeanShellHelper.copySnaaToRs(secretAuthenticationKeys),
                    reservationData);

            Logger.getLogger(Reservation.class.getName()).log(Level.INFO,
                    "Successfully reserved nodes: {}", nodeURNsToReserve);
            Logger.getLogger(Reservation.class.getName()).log(Level.INFO,
                    "Reservation Key(s): {}", BeanShellHelper.toString(secretReservationKeys));

            Logger.getLogger(Reservation.class.getName()).log(Level.INFO,
                    BeanShellHelper.toString(secretReservationKeys));
            secretReservationKey = BeanShellHelper.toString(secretReservationKeys);
        } catch (Exception e) {
            Logger.getLogger(Reservation.class.getName()).log(Level.SEVERE, null, e.toString());
        }
    }

    /**
     * Splitt the URNs by "," as delimiter
     *
     * @param nodeURNs
     * @return
     */
    private List splitURNs(String nodeURNasString) {

        Pattern pattern = Pattern.compile(",");
        String[] URNs = pattern.split(nodeURNasString);

        List nodeUrnLIST = Arrays.asList(URNs);

        return nodeUrnLIST;

    }
    
    /**
     * @return reserved Nodes
     */
    public ArrayList getReservedNodes() {
        ArrayList reservedNodes = new ArrayList();
        reservedNodes.addAll(nodeURNs);
        
        return reservedNodes;
    }
}