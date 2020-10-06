/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.*;
import java.util.*;
/**
 *
 * @author yova
 */
public class totalKontak extends Report implements ConnectionListener {

    private Map<DTNHost, Integer> creationTimes;
    
    public totalKontak() {
        super();
        this.creationTimes = new HashMap<DTNHost, Integer>();
    }
    
    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        if (creationTimes.containsKey(host2)) {
            creationTimes.put(host2, creationTimes.get(host2) + 1);
        } else {
            creationTimes.put(host2, 1);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }
    
    public void done() {
        for (Map.Entry<DTNHost, Integer> entry : creationTimes.entrySet()) {
            write(entry.getKey()+" "+entry.getValue());
        }
        super.done();
    }
}
