package report;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * @author yova
 */
public class energi extends Report implements UpdateListener {

    public static int interval = 300;
    public Double lastRecord = Double.MIN_VALUE;

    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        double simTime = SimClock.getTime();
//        if (simTime - lastRecord >= interval) {
        for (DTNHost host : nodes) {
            MessageRouter r = host.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof estimasi)) {
                continue;
            }
            estimasi fe = (estimasi) de;

            Double getEner = fe.getEnergy().getLast();
            write(host+" = "+getEner);
//                write(host+"\t"+getEner);
                //haggle node 39
//            if (host.getAddress() == 39) {
//                double menit = 5;
//                LinkedList<Double> getEner = fe.getEnergy();
//                String output = "Menit\tBaterai\n";
//                for (Double value : getEner) {
//                    output += menit + "\t" + value + "\n";
//                    menit += 5;
//                }
//                write(output);
//            }

        }
//            lastRecord = simTime - simTime % interval;
//        }
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
//        double simTime = SimClock.getTime();
//        if (simTime - lastRecord >= interval) {
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
//                Map<Integer, ArrayList<Double>> getEner = fe.getEnergi();
//                for (Map.Entry<Integer, ArrayList<Double>> entry : getEner.entrySet()) {
////                String printLn = entry.getKey() + "";
////                for (Double nilaiE : entry.getValue()) {
////                    printLn = printLn + "\t" + nilaiE;
////                }
////                write(printLn);
////                    if (host.getAddress()  == 0) {
//                        write("Node " + entry.getKey() + "\t" + entry.getValue());
////                    }
//                }
//            }
//            lastRecord = simTime - simTime % interval;
//        }
    }
}
