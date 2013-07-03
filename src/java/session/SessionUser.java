/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Fabian
 */
@Entity
public class SessionUser implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String password;
    
    @OneToMany(mappedBy = "sessionUser", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<SessionExperiment> experiments = new ArrayList<SessionExperiment>();

    public SessionUser(){}
    
    public SessionUser(String username, String password, SessionExperiment experiment){
        this.name = username;
        this.password = password;
        this.experiments.add(experiment);
    }
    
    public SessionUser(String username, String password){
        this.name = username;
        this.password = password;
    }
    
    public void addExperiment(SessionExperiment sessionExperiment){
        this.experiments.add(sessionExperiment);
    }
    
    public SessionUser(String username, String password, ArrayList<SessionExperiment> experiments){
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

    public List<SessionExperiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<SessionExperiment> experiments) {
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
