/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 * @author Fabian
 */
@Entity
public class SessionUser implements Serializable{
    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue
    private long id;

    private String name;
    private String password;
    private ArrayList<String> experiments = new ArrayList<String>();

    public SessionUser(){}
    
    public SessionUser(String username, String password, String experiment){
        this.name = username;
        this.password = password;
        this.experiments.add(experiment);
    }
    
    public SessionUser(String username, String password, ArrayList<String> experiments){
        this.name = username;
        this.password = password;
        this.experiments = experiments;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getExperiments() {
        return experiments;
    }

    public void setExperiments(ArrayList<String> experiments) {
        this.experiments = experiments;
    }
    
    public long getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return String.format("(%s, %s)", this.name, this.experiments.toString());
    }
}
