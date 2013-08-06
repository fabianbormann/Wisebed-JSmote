/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import exceptions.DatabaseUserDuplicationException;
import exceptions.DatabaseUserNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import jobs.NodeWriter;
import model.Experiment;
import model.Node;
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
        //this.doFlash();
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

    public void send() {
        SessionExperiment experiment = this.getExperiment();
        ArrayList<String> nodesAsStrings = new ArrayList<String>();

        for (Node node : experiment.getNodes()) {
            nodesAsStrings.add(node.toString());
        }

        try {
            NodeWriter.sendMessage(nodesAsStrings, this.code, experiment.getReservationKey());
        } catch (Exception e) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    public boolean experimentListIsEmpty() {
        try {
            SessionUser databaseUser = this.userService.getSessionUser((String) httpSession.getAttribute("username"));
            if (databaseUser.getExperiments().isEmpty()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, e.toString());
            return true;
        }
    }
}
