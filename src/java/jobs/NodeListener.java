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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.ExperimentConsole;

/**
 *
 * @author Fabian
 */
public class NodeListener {

    private static final Logger debug = Logger.getLogger(NodeListener.class.getName());
    private final String reservationKey;
    private final ExperimentConsole console;
    
    private String localControllerEndpointURL;
    private String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";
    private String protobufHost = "wisebed.itm.uni-luebeck.de";
    private String protobufPortString = "8885";
    private Controller controller;

    public NodeListener(String reservationKey, ExperimentConsole console) {
        this.reservationKey = reservationKey;
        this.console = console;
    }
    
    /**
     * Listen Node Messages from all Nodes in this experiment
     *
     * @throws MalformedURLException
     * @throws ExperimentNotRunningException_Exception
     */
    public void startListening() throws MalformedURLException, ExperimentNotRunningException_Exception, IOException {

        debug.log(Level.INFO, "start with listen to the nodes");

        final boolean csv = System.getProperty("testbed.listtype") != null && "csv".equals(System.getProperty("testbed.listtype"));

        Integer protobufPort = protobufPortString == null ? null : Integer.parseInt(protobufPortString);
        boolean useProtobuf = protobufHost != null && protobufPort != null;

        SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

        controller = new Controller() {
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
                        
                        String outputMessage = "";
                        outputMessage += new org.joda.time.DateTime(msg.getTimestamp().toGregorianCalendar());

                        outputMessage += csv ? ";" : " | ";
                        outputMessage += msg.getSourceNodeId();
                        outputMessage += csv ? ";" : " | ";
                        outputMessage += text;
                        outputMessage += csv ? ";" : " | ";
                        outputMessage += StringUtils.toHexString(msg.getBinaryData());
                        
                        console.LogToConsole(reservationKey, outputMessage);
                        debug.log(Level.INFO, outputMessage);
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
    }

    public String getReservationKey() {
        return reservationKey;
    }
}
