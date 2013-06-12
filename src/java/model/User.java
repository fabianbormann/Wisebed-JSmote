/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import eu.wisebed.api.rs.ReservervationConflictExceptionException;
import eu.wisebed.api.snaa.AuthenticationExceptionException;
import eu.wisebed.api.snaa.SNAAExceptionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import logic.Remote;
import logic.Reservation;

/**
 *
 * @author Fabian
 */
@ManagedBean
@SessionScoped
public class User {
    
    Reservation reservation = new Reservation();
    Remote remote;
    
    private String username;
    private String password;
    private String urnPrefix;
    private String nodeURNs;
    private String secretReservationKey;    
    private Integer offset=0;
    private Integer duration=5;
    
    public String getUrnPrefix() {
        return urnPrefix;
    }

    public void setUrnPrefix(String urnPrefix) {
        this.urnPrefix = urnPrefix;
    }

    public String getNodeURNs() {
        return nodeURNs;
    }

    public void setNodeURNs(String nodeURNs) {
        this.nodeURNs = nodeURNs;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretReservationKey() {
        return secretReservationKey;
    }

    public void setSecretReservationKey(String secretReservationKey) {
        this.secretReservationKey = secretReservationKey;
    }
    
    public String reserve(){
        try {
            Logger.getLogger(User.class.getName())
                    .log(Level.INFO, "try to reserve..");
             
            //TODO variable urnPrefix
            this.reservation.reserveNodes("urn:wisebed:uzl1:", 
                    username+"@wisebed1.itm.uni-luebeck.de", 
                    password, nodeURNs, offset, duration);
            
            this.secretReservationKey = this.reservation.getSecretReservationKey();
            
            this.remote = new Remote(reservation.getReservedNodes());
            
            return "manage";
               
        } catch (AuthenticationExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        } catch (SNAAExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        } catch (ReservervationConflictExceptionException e) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, e.toString());
        }
        
        return "index";
    }

    public Remote getRemote() {
        return remote;
    }

    public void setRemote(Remote remote) {
        this.remote = remote;
    }
    
}
