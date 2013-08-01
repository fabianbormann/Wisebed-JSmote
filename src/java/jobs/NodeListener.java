/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import de.uniluebeck.itm.tr.util.StringUtils;
import de.uniluebeck.itm.wisebed.cmdlineclient.BeanShellHelper;
import de.uniluebeck.itm.wisebed.cmdlineclient.DelegatingController;
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerAdapter;
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerClient;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.WSNAsyncWrapper;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.controller.Controller;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.testbed.api.wsn.WSNServiceHelper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import service.UserService;
import session.SessionExperiment;
import session.SessionUser;

/**
 *
 * @author Fabian
 */
@ManagedBean(name = "Node")
public class NodeListener {

    String localControllerEndpointURL;
    String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";
    String protobufHost = "wisebed.itm.uni-luebeck.de";
    String protobufPortString = "8885";
    private String reservationKey = "";
    private String log = "";
    private UserService userService = new UserService();
    private static final Logger debug = Logger.getLogger(NodeListener.class.getName());
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(true);

    public void run() {
        debug.log(Level.INFO, "runing!");

        if (this.reservationKey.isEmpty()) {
            debug.log(Level.INFO, "Key is empty");
        } else {
            debug.log(Level.INFO, "Key is {0}", this.reservationKey);

            try {
                debug.log(Level.INFO, "reservation key is not empty and listening was starting");
                listenMessageFromNodes();
            } catch (Exception e) {
                debug.log(Level.INFO, "An error ocurred while listen to the nodes");
                debug.log(Level.SEVERE, e.toString());
            }
        }
    }

    public String listen() {
        SessionExperiment experiment = this.getExperiment();

        if (experiment == null) {
            return "null";
        } else {
            this.reservationKey = experiment.getReservationKey();
            this.run();
            return this.log;
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
            debug.log(Level.INFO, "An error ocurred while loading the experiment");
            debug.log(Level.SEVERE, e.toString());
            return null;
        }
    }

    private int getExperimentId() {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        return Integer.parseInt(params.get("show"));
    }

    /**
     * Listen Node Messages from all Nodes in this experiment
     *
     * @throws MalformedURLException
     * @throws ExperimentNotRunningException_Exception
     */
    public void listenMessageFromNodes() throws MalformedURLException, ExperimentNotRunningException_Exception, IOException {

        debug.log(Level.INFO, "start with listen to the nodes");

        final boolean csv = System.getProperty("testbed.listtype") != null && "csv".equals(System.getProperty("testbed.listtype"));

        Integer protobufPort = protobufPortString == null ? null : Integer.parseInt(protobufPortString);
        boolean useProtobuf = protobufHost != null && protobufPort != null;

        SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

        Controller controller = new Controller() {
            @Override
            public void receive(List msgs) {
                for (int i = 0; i < msgs.size(); i++) {
                    Message msg = (Message) msgs.get(i);
                    synchronized (System.out) {

                        String text = StringUtils.replaceNonPrintableAsciiCharacters(new String(msg.getBinaryData()));

                        if (csv) {
                            text = text.replaceAll(";", "\\;");
                        }
                        debug.log(Level.INFO, text);
                        System.out.print(new org.joda.time.DateTime(msg.getTimestamp().toGregorianCalendar()));
                        System.out.print(csv ? ";" : " | ");
                        System.out.print(msg.getSourceNodeId());
                        System.out.print(csv ? ";" : " | ");
                        System.out.print(text);
                        System.out.print(csv ? ";" : " | ");
                        System.out.print(StringUtils.toHexString(msg.getBinaryData()));
                        System.out.println();
                    }
                }
            }

            @Override
            public void receiveStatus(List requestStatuses) {
            }

            @Override
            public void receiveNotification(List msgs) {
                for (int i = 0; i < msgs.size(); i++) {
                    System.err.print(new org.joda.time.DateTime());
                    System.err.print(csv ? ";" : " | ");
                    System.err.print("Notification");
                    System.err.print(csv ? ";" : " | ");
                    System.err.print(msgs.get(i));
                    System.err.println();
                }
            }

            @Override
            public void experimentEnded() {
                debug.log(Level.INFO, "Experiment ended");
            }
        };

        if (useProtobuf) {

            ProtobufControllerClient pcc = ProtobufControllerClient.create(
                    protobufHost,
                    protobufPort,
                    BeanShellHelper.parseSecretReservationKeys(reservationKey));
            pcc.addListener(new ProtobufControllerAdapter(controller));
            try {
                pcc.connect();
            } catch (Exception e) {
                debug.log(Level.SEVERE, e.toString());
                useProtobuf = false;
            }
        }

        if (!useProtobuf) {

            DelegatingController delegator = new DelegatingController(controller);
            delegator.publish(localControllerEndpointURL);
            debug.log(Level.INFO, "Local controller published on url: {}", localControllerEndpointURL);

        }

        String wsnEndpointURL = null;
        try {
            wsnEndpointURL = sessionManagement.getInstance(
                    BeanShellHelper.parseSecretReservationKeys(reservationKey),
                    (useProtobuf ? "NONE" : localControllerEndpointURL));
        } catch (UnknownReservationIdException_Exception e) {
            debug.log(Level.SEVERE, "There was no reservation found with the given secret reservation key. Exiting.");
            debug.log(Level.SEVERE, e.toString());
        }

        debug.log(Level.INFO, "Got a WSN instance URL, endpoint is: {0}", wsnEndpointURL);
        WSN wsnService = WSNServiceHelper.getWSNService(wsnEndpointURL);
        final WSNAsyncWrapper wsn = WSNAsyncWrapper.of(wsnService);

//        InputStream receivedBytes = new ByteArrayInputStream()
//        
//        InputStream inputStream = System.in;
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//        StringBuilder stringBuildReader = new StringBuilder();
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//        String read = bufferedReader.readLine();
//
//        while (read != null) {
//            stringBuildReader.append(read);
//            read = bufferedReader.readLine();
//        }
//
//        this.log += stringBuildReader.toString();
    }
}
