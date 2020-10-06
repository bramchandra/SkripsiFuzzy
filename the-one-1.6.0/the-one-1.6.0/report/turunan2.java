package report;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * @author yova
 */
public class turunan2 extends Report implements UpdateListener {
    
    public static int interval = 300;
    Double lastRecord = Double.MIN_VALUE;
    
    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
       
        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof estimasi)) {
                continue;
            }
            estimasi fe = (estimasi) de;

//            ArrayList<Double> nilaiFreq = fe.getTurunan1();
//            write(h+"\t"+nilaiFreq+"  ");
            Map<DTNHost, ArrayList<Double>> getT2 = fe.getT2();
            for (Map.Entry<DTNHost, ArrayList<Double>> entry : getT2.entrySet()) {
//                    if (host.getAddress() == 0) {
                write("Node " + entry.getKey() + "\t" + entry.getValue());
//                    }
            }
        }
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
//        double simTime = SimClock.getTime();
//        if (simTime - lastRecord >= interval) {
//
//            for (DTNHost host : hosts) {
//                MessageRouter r = host.getRouter();
//                if (!(r instanceof DecisionEngineRouter)) {
//                    continue;
//                }
//                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
//                if (!(de instanceof estimasi)) {
//                    continue;
//                }
//                estimasi fe = (estimasi) de;
//
//                Map<DTNHost, ArrayList<Double>> getT2 = fe.getT2();
//                for (Map.Entry<DTNHost, ArrayList<Double>> entry : getT2.entrySet()) {
////                    if (host.getAddress() == 0) {
//                        write("Node " + entry.getKey() + "\t" + entry.getValue());
////                    }
//                }
//            }
//            lastRecord = simTime - simTime % interval;
//        }
    }
}
