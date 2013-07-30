/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.google.common.collect.Lists;
import de.itm.uniluebeck.tr.wiseml.WiseMLHelper;
import de.uniluebeck.itm.tr.util.StringUtils;
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
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
    private String nodes;
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(true);
    String localControllerEndpointURL;
    String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";
    String protobufHost = "wisebed.itm.uni-luebeck.de";
    String protobufPortString = "8885";

    public ExperimentController() {
        try {
            localControllerEndpointURL = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8091/controller";
        } catch (UnknownHostException e) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, e.toString());
        }
    }
    
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

    public void setName(String name) {
    }

    public String getNodes() {
        SessionExperiment experiment = this.getExperiment();
        if (experiment == null) {
            this.nodes = "";
            return "experiments?faces-redirect=true;";
        } else {
            this.nodes = experiment.getNodes().toString();
            return this.nodes;
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
        this.doFlash();
        return "experiment?show=" + this.getExperimentId();
    }

    public String getTimeleft() {

        SessionExperiment sessionExperiment = getExperiment();

        long start = sessionExperiment.getDatetime().getTime();
        int duration = sessionExperiment.getDuration() * 1000 * 60;
        int offset = sessionExperiment.getOffset() * 1000 * 60;
        start += offset;
        long end = start + duration;

        Date experimentEnd = new Date();
        experimentEnd.setTime(end);
        Date experimentStart = new Date();
        experimentStart.setTime(start);

        Date currentDatetime = new Date();
        long current = currentDatetime.getTime();

        long timeLeft = (end - current) / 1000;

        if (currentDatetime.before(experimentStart)) {
            return "This experiment begins in " + this.getTimeToExperiment(start, current, end, 0) + ". The secret reservation key is: " + sessionExperiment.getReservationKey();
        } else if (currentDatetime.after(experimentEnd)) {
            return "This experiment is already finished.";
        } else if (currentDatetime.before(experimentEnd)) {
            return "This experiment will be finished in " + this.getTimeToExperiment(start, current, end, 1) + ". The secret reservation key is: " + sessionExperiment.getReservationKey();
        }

        return "";
    }

    private String getTimeToExperiment(long start, long current, long stop, int mode) {

        int seconds;

        if (mode == 0) {
            seconds = (int) ((start - current) / 1000);
        } else {
            seconds = (int) ((stop - current) / 1000);
        }

        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        if (day > 0) {
            return day + " day(s) " + hours + " hour(s) " + minutes + " minute(s) " + second + " secound(s)";
        } else if (hours > 0) {
            return hours + " hour(s) " + minutes + " minute(s) " + second + " secound(s)";
        } else if (minutes > 0) {
            return minutes + " minute(s) " + second + " secound(s)";
        } else {
            return second + " secound(s)";
        }
    }

    private void doFlash() {
        SessionExperiment experiment = this.getExperiment();
        Remote remote = new Remote(experiment.getNodes(), experiment.getReservationKey());

        remote.flashRemoteImage();
    }

    /**
     * Send Message to targetNodes
     * @param targetNodes
     * @param message
     * @throws UnknownHostException
     * @throws ExperimentNotRunningException_Exception
     * @throws MalformedURLException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws DatatypeConfigurationException 
     */
    public void sendMessageToNodes(String targetNodes, String message) throws UnknownHostException, ExperimentNotRunningException_Exception, MalformedURLException, InterruptedException, ExecutionException, DatatypeConfigurationException {

        String secretReservationKeys = this.getExperiment().getReservationKey();
        boolean csv = System.getProperty("testbed.listtype") != null && "csv".equals(System.getProperty("testbed.listtype"));

        Integer protobufPort = protobufPortString == null ? null : Integer.parseInt(protobufPortString);
        boolean useProtobuf = protobufHost != null && protobufPort != null;

        SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

        String wsnEndpointURL = null;
        try {
            wsnEndpointURL = sessionManagement.getInstance(
                    BeanShellHelper.parseSecretReservationKeys(secretReservationKeys),
                    (useProtobuf ? "NONE" : this.localControllerEndpointURL));
        } catch (UnknownReservationIdException_Exception e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, "There was no reservation found with the given secret reservation key. Exiting.");
        }

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
                    Logger.getLogger(User.class.getName()).log(Level.INFO, (String) msgs.get(i));
                }
            }

            @Override
            public void experimentEnded() {
                Logger.getLogger(User.class.getName()).log(Level.INFO, "Experiment ended");
            }
        };

        // try to connect via unofficial protocol buffers API if hostname and port are set in the configuration
        if (useProtobuf) {

            ProtobufControllerClient pcc = ProtobufControllerClient.create(
                    protobufHost,
                    protobufPort,
                    BeanShellHelper.parseSecretReservationKeys(secretReservationKeys));
            pcc.addListener(new ProtobufControllerAdapter(controller));
            try {
                pcc.connect();
            } catch (Exception e) {
                useProtobuf = false;
            }
        }

        if (!useProtobuf) {

            DelegatingController delegator = new DelegatingController(controller);
            delegator.publish(this.localControllerEndpointURL);
            Logger.getLogger(User.class.getName()).log(Level.INFO, "Local controller published on url: {}", this.localControllerEndpointURL);

        }

        Logger.getLogger(User.class.getName()).log(Level.INFO, "Got a WSN instance URL, endpoint is: {}", wsnEndpointURL);

        // retrieve reserved node URNs from testbed
        List nodeURNs;
        if (targetNodes != null && !"".equals(targetNodes)) {
            nodeURNs = Lists.newArrayList(targetNodes.split(","));
            Logger.getLogger(User.class.getName()).log(Level.INFO, "Selected the following node URNs: {}", nodeURNs);
        } else {
            nodeURNs = WiseMLHelper.getNodeUrns(wsn.getNetwork().get(), new String[]{});
            Logger.getLogger(User.class.getName()).log(Level.INFO, "Retrieved the following node URNs: {}", nodeURNs);
        }

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

        Logger.getLogger(User.class.getName()).log(Level.INFO, "Sending Message [ {0}] to nodes...", messageForOutputInLog);

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
            Logger.getLogger(User.class.getName()).log(Level.INFO, "Shutting down...");

        } catch (Exception e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, e.toString());
        }
    }
}
