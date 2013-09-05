package model;

import java.io.Serializable;

/**
 *
 * @author Fabian
 */
public class Node implements Serializable{

    private String nodeURN;

    public Node(String nodeURN) {
        this.nodeURN = nodeURN;
    }
    
    @Override
    public String toString(){
        return this.nodeURN;
    }
}
