/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
/**
 *
 * @author Fabian
 */
@ManagedBean
@RequestScoped
public class Experiment {

    private String name;
    private ArrayList nodes;
    private Date datetime;
    
    public Experiment(String name, ArrayList nodes, Date datetime) {
        this.name = name;
        this.nodes = nodes;
        this.datetime = datetime;
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
