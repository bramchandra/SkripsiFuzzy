package report;

import core.DTNHost;
import java.util.*;

/**
 * @author Jarkom
 */
public interface estimasi {


    
    public LinkedList<Double> getTurunan1();
    public LinkedList<Double> getEnergy();
    public Map<DTNHost, ArrayList<Double>> getT1();

}
