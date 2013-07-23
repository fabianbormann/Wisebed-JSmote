/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import model.Node;
import com.google.common.collect.Lists;
import de.itm.uniluebeck.tr.wiseml.WiseMLHelper;
import de.uniluebeck.itm.tr.util.StringUtils;
import de.uniluebeck.itm.wisebed.cmdlineclient.BeanShellHelper;
import de.uniluebeck.itm.wisebed.cmdlineclient.DelegatingController;
import de.uniluebeck.itm.wisebed.cmdlineclient.jobs.JobResult;
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerAdapter;
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerClient;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.WSNAsyncWrapper;
import eu.wisebed.api.controller.Controller;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.testbed.api.wsn.WSNServiceHelper;
import images.EImageType;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
/**
 *
 * @author Fabian
 */
@ManagedBean
@SessionScoped
public class Remote {

    private final String REMOTE_IMAGE_PATH = EImageType.ISENSE.getBinaryPath();
    
    private String secretReservationKey;
    private ArrayList<Node> nodes = new ArrayList<Node>(){
        @Override
        public String toString(){
            String result = "";
            for(int i = 0; i < nodes.size(); i++){
                if(i != nodes.size())
                    result += nodes.get(i).toString();
                else
                    result += nodes.get(i).toString()+",";
            }
            return result;
        }
    };

    public Remote(ArrayList nodes, String secretReservationKey) {
        this.nodes = nodes;
        this.secretReservationKey = secretReservationKey;
    }

    public void flashRemoteImage() {
        try {
            flash();
        } catch (Exception e) {
            Logger.getLogger(Remote.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    public String getFlashArray() {
        return nodes.toString();
    }
    
    public void setFlashArray(String nodeArray) {}

    public void setSecretReservationKey(String secretReservationKey) {
        this.secretReservationKey = secretReservationKey;
    }

    private void flash() throws UnknownHostException {
        Logger.getLogger(Node.class.getName()).log(Level.INFO, "Start flashing..");

        String binaryFile = this.REMOTE_IMAGE_PATH;
        String nodeUrnsToFlash = this.nodes.toString();

        Logger.getLogger(Node.class.getName()).log(Level.INFO, nodeUrnsToFlash);
        
        String localControllerEndpointURL = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8089/controller";
        boolean csv = System.getProperty("testbed.listtype") != null && "csv".equals(System.getProperty("testbed.listtype"));
        
        //TODO auto config
        String protobufHost = "wisebed.itm.uni-luebeck.de";
        String protobufPortString = "8885";
        String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";

        Integer protobufPort = protobufPortString == null ? null : Integer.parseInt(protobufPortString);
        boolean useProtobuf = protobufHost != null && protobufPort != null;

        SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

        Logger.getLogger(Node.class.getName()).log(Level.INFO, "fail in log");
        
        Logger.getLogger(Node.class.getName()).log(Level.INFO,
                "Using the following parameters for calling getInstance(): {}",
                StringUtils.jaxbMarshal(BeanShellHelper.parseSecretReservationKeys(this.secretReservationKey)));

        Logger.getLogger(Node.class.getName()).log(Level.INFO, "Using the following parameters for calling getInstance(): {}",
                localControllerEndpointURL);

        String wsnEndpointURL = null;
        try {
            try {
                wsnEndpointURL = sessionManagement.getInstance(
                        BeanShellHelper.parseSecretReservationKeys(secretReservationKey),
                        (useProtobuf ? "NONE" : localControllerEndpointURL));
            } catch (ExperimentNotRunningException_Exception ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnknownReservationIdException_Exception e) {
            Logger.getLogger(Node.class.getName()).log(Level.INFO, "There was no reservation found with the given secret reservation key. Exiting.");
            System.exit(1);
        }

        Logger.getLogger(Node.class.getName()).log(Level.INFO, "Got a WSN instance URL, endpoint is: {}", wsnEndpointURL);
        WSN wsnService = WSNServiceHelper.getWSNService(wsnEndpointURL);
        final WSNAsyncWrapper wsn = WSNAsyncWrapper.of(wsnService);

        Controller controller = new Controller() {
            @Override
            public void receive(List msg) {
                // nothing to do
            }

            @Override
            public void receiveStatus(List requestStatuses) {
                wsn.receive(requestStatuses);
            }

            @Override
            public void receiveNotification(List msgs) {
                for (int i = 0; i < msgs.size(); i++) {
                    Logger.getLogger(Node.class.getName()).log(Level.INFO, (String) msgs.get(i));
                }
            }

            @Override
            public void experimentEnded() {
                Logger.getLogger(Node.class.getName()).log(Level.INFO, "Experiment ended");
            }
        };

        // try to connect via unofficial protocol buffers API if hostname and port are set in the configuration
        if (useProtobuf) {

            ProtobufControllerClient pcc = ProtobufControllerClient.create(
                    protobufHost,
                    protobufPort,
                    BeanShellHelper.parseSecretReservationKeys(secretReservationKey));
            pcc.addListener(new ProtobufControllerAdapter(controller));
            try {
                pcc.connect();
            } catch (Exception e) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, e.toString());
                useProtobuf = false;
            }
        }

        if (!useProtobuf) {

            DelegatingController delegator = new DelegatingController(controller);
            try {
                delegator.publish(localControllerEndpointURL);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(Node.class.getName()).log(Level.INFO, "Local controller published on url: {}",
                    localControllerEndpointURL);
        }

        Logger.getLogger(Node.class.getName()).log(Level.INFO, "Using the following parameters for calling getInstance(): {}",
                StringUtils.jaxbMarshal(BeanShellHelper.parseSecretReservationKeys(secretReservationKey)));

        Logger.getLogger(Node.class.getName()).log(Level.INFO, "Using the following parameters for calling getInstance(): {}",
                localControllerEndpointURL);

        List nodeURNs = null;
        if (nodeUrnsToFlash != null && !"".equals(nodeUrnsToFlash)) {
            nodeURNs = Lists.newArrayList(nodeUrnsToFlash.split(", "));
            Logger.getLogger(Node.class.getName()).log(Level.INFO, "Selected node URNs: {}", nodeURNs);
        } else {
            try {
                nodeURNs = WiseMLHelper.getNodeUrns(wsn.getNetwork().get(), new String[]{});
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(Node.class.getName()).log(Level.INFO, "Reserved node URNs: {}", nodeURNs);
        }

        Logger.getLogger(Node.class.getName()).log(Level.INFO, "Flashing nodes...");

        List programIndices;
        List programs;

        programIndices = new ArrayList();
        programs = new ArrayList();

        for (int i = 0; i < nodeURNs.size(); i++) {
            programIndices.add(0);
        }
        try {
            programs.add(BeanShellHelper.readProgram(
                    binaryFile,
                    "",
                    "",
                    "iSense",
                    "1.0"));
        } catch (Exception ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }

        Future flashFuture = wsn.flashPrograms(nodeURNs, programIndices, programs, 3, TimeUnit.MINUTES);
        try {

            JobResult flashJobResult = (JobResult) flashFuture.get();
            flashJobResult.printResults(System.out, csv);
            System.exit(flashJobResult.getSuccessPercent() < 100 ? 1 : 0);

        } catch (Exception e) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, e.toString());
        }
    }
}
