package jobs;

import de.uniluebeck.itm.wisebed.cmdlineclient.BeanShellHelper;
import de.uniluebeck.itm.wisebed.cmdlineclient.DelegatingController;
import de.uniluebeck.itm.wisebed.cmdlineclient.jobs.JobResult;
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
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 *
 * @author Fabian
 */
public class NodeWriter {

    private static String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";
    private static String protobufHost = "wisebed.itm.uni-luebeck.de";
    private static String protobufPortString = "8885";
    private static final Logger debug = Logger.getLogger(NodeListener.class.getName());

    public static void sendMessage(List nodeURNs, String message, String secretReservationKey) throws UnknownHostException, MalformedURLException, ExperimentNotRunningException_Exception, InterruptedException, ExecutionException, DatatypeConfigurationException {

        String localControllerEndpointURL = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8089/controller";
        boolean csv = System.getProperty("testbed.listtype") != null && "csv".equals(System.getProperty("testbed.listtype"));

        Integer protobufPort = protobufPortString == null ? null : Integer.parseInt(protobufPortString);
        boolean useProtobuf = protobufHost != null && protobufPort != null;

        SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

        Controller controller = new Controller() {
            @Override
            public void receive(List msg) {
                // nothing to do
            }

            @Override
            public void receiveStatus(List requestStatuses) {
                //wsn.receive(requestStatuses);
            }

            @Override
            public void receiveNotification(List msgs) {
                for (int i = 0; i < msgs.size(); i++) {
                    debug.log(Level.INFO, (String) msgs.get(i));
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
                    BeanShellHelper.parseSecretReservationKeys(secretReservationKey));
            pcc.addListener(new ProtobufControllerAdapter(controller));
            try {
                pcc.connect();
            } catch (Exception e) {
                debug.log(Level.INFO, e.toString());
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
                    BeanShellHelper.parseSecretReservationKeys(secretReservationKey),
                    (useProtobuf ? "NONE" : localControllerEndpointURL));
        } catch (UnknownReservationIdException_Exception e) {
            debug.log(Level.WARNING, "There was no reservation found with the given secret reservation key. Exiting.");
            debug.log(Level.SEVERE, e.toString());
        }

        debug.log(Level.INFO, "Got a WSN instance URL, endpoint is: {0}", wsnEndpointURL);
        WSN wsnService = WSNServiceHelper.getWSNService(wsnEndpointURL);
        final WSNAsyncWrapper wsn = WSNAsyncWrapper.of(wsnService);

        // Constructing UART Message from Input String (Delimited by ",")
        // Supported Prefixes are "0x" and "0b", otherwise Base_10 (DEZ) is assumed	
        String[] splitMessage = message.split(",");
        byte[] messageToSendBytes = new byte[splitMessage.length];
        String messageForOutputInLog = "";
        for (int i = 0; i < splitMessage.length; i++) {
            int type = 10;
            if (splitMessage[i].startsWith("0x")) {
                type = 16;
                splitMessage[i] = splitMessage[i].replace("0x", "");
            } else if (splitMessage[i].startsWith("0b")) {
                type = 2;
                splitMessage[i] = splitMessage[i].replace("0b", "");
            }
            BigInteger b = new BigInteger(splitMessage[i], type);
            messageToSendBytes[i] = (byte) b.intValue();
            messageForOutputInLog = messageForOutputInLog + b.intValue() + " ";
        }

        debug.log(Level.INFO, "Sending Message [{0}] to nodes...", messageForOutputInLog);

        // Constructing the Message
        Message binaryMessage = new Message();
        binaryMessage.setBinaryData(messageToSendBytes);

        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());

        binaryMessage.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        binaryMessage.setSourceNodeId("urn:wisebed:uzl1:0xFFFF");

        Future sendFuture = wsn.send(nodeURNs, binaryMessage, 10, TimeUnit.SECONDS);
        try {
            JobResult sendJobResult = (JobResult) sendFuture.get();
            sendJobResult.printResults(System.out, csv);
            debug.log(Level.INFO, "Shutting down...");
            
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TimeoutException) {
                debug.log(Level.INFO, "Call timed out. Exiting...");
            }
            debug.log(Level.SEVERE, e.toString());
        }
    }
}
