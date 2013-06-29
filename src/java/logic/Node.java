package logic;

/**
 *
 * @author Fabian
 */
public class Node {

    private String nodeURN;

    public Node(String nodeURN) {
        this.nodeURN = nodeURN;
    }
    
    @Override
    public String toString(){
        return this.nodeURN;
    }
}
