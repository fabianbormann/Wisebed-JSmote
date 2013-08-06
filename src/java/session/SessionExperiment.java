/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import model.Node;

/**
 *
 * @author Fabian
 */
@Entity
public class SessionExperiment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    @Lob
    private String code;
    private String console;
    private ArrayList<Node> nodes;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date datetime;
    private int duration;
    private int offset;
    private String reservationKey;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private SessionUser sessionUser;

    public SessionExperiment() {
        this.code = getDefaultCode();
    }

    public SessionExperiment(String name, ArrayList<Node> nodes, Date datetime, SessionUser sessionUser, String reservationKey) {
        this.name = name;
        this.nodes = nodes;
        this.datetime = datetime;
        this.sessionUser = sessionUser;
        this.reservationKey = reservationKey;
        this.code = getDefaultCode();
    }

    public SessionExperiment(String name, ArrayList<Node> nodes, Date datetime, SessionUser sessionUser) {
        this.name = name;
        this.nodes = nodes;
        this.datetime = datetime;
        this.sessionUser = sessionUser;
        this.code = getDefaultCode();
    }

    public String getReservationKey() {
        return reservationKey;
    }

    public void setReservationKey(String reservationKey) {
        this.reservationKey = reservationKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public SessionUser getSessionUser() {
        return sessionUser;
    }

    public void setSessionUser(SessionUser sessionUser) {
        this.sessionUser = sessionUser;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getConsole() {
        return console;
    }

    public void setConsole(String console) {
        this.console = console;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    private String getDefaultCode() {
        return "<!doctype html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <style>p {font-family: monospace;}</style>\n"
                + "    <script>\n"
                + "      function foo(){\n"
                + "      	alert(\"foo\");\n"
                + "      }\n"
                + "      \n"
                + "      function wisebedEcho(){\n"
                + "      	alert(\"wisebed.js send coap message via uart\");\n"
                + "      }\n"
                + "    </script>\n"
                + "  </head>\n"
                + "  <body>\n"
                + "    <h1>Example Project</h1>\n"
                + "    <p onclick=\"foo()\">Click me to call foo funtion!</p>\n"
                + "    <input type=text/>\n"
                + "    <button onclick=\"wisebedEcho()\">wisebed echo</button>\n"
                + "  </body>\n"
                + "</html>";
    }
}
