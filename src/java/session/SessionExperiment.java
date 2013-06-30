/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;

/**
 *
 * @author Fabian
 */
@Entity
public class SessionExperiment implements Serializable{
    @Id @GeneratedValue
    private Long id;
    private String name;
    private ArrayList nodes;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date datetime;

    public SessionExperiment() {}
   
    public SessionExperiment(String name, ArrayList nodes, Date datetime){
        this.name = name;
        this.nodes = nodes;
        this.datetime = datetime;
    }

    public Long getId() {
        return id;
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
}
