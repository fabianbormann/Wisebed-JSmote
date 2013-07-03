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
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author Fabian
 */
@Entity
public class SessionExperiment implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private ArrayList nodes;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date datetime;
    
    @ManyToOne(cascade = CascadeType.PERSIST)
    private SessionUser sessionUser;

    public SessionExperiment() {
    }
    
    public SessionExperiment(String name, ArrayList nodes, Date datetime, SessionUser sessionUser) {
        this.name = name;
        this.nodes = nodes;
        this.datetime = datetime;
        this.sessionUser = sessionUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList nodes) {
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
    
}
