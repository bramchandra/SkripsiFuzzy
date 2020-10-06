package report;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * @author yova
 */
public class turunan1 extends Report {

//    private Map<DTNHost, ArrayList<Double>> intercontactData;
//    private Map<DTNHost, Double> avgIntercontactData;
//    

    public turunan1() {
//        intercontactData = new HashMap<>();
//        avgIntercontactData = new HashMap<>();
        
    }

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
            
           
            LinkedList<Double> temp = fe.getTurunan1();
//            write(h+"");
            for (Double value : temp) {
                write(Double.toString(value));
            }
//            Map<DTNHost, ArrayList<Double>> getT1 = fe.getT1();
//            for (Map.Entry<DTNHost, ArrayList<Double>> entry : getT1.entrySet()) {
//                for (Double value : entry.getValue()) {
//                    write(Double.toString(value));
//                }
//
//            }
//            System.out.println(getT1);
        }
//        for (DTNHost node : nodes) {
//            if (intercontactData.containsKey(node)) {
//                double avgIntercontactDuration = calculateAverage(intercontactData.get(node));
//                avgIntercontactData.put(node, avgIntercontactDuration);
//            }
//        }
//        for (Map.Entry<DTNHost, Double> entry : avgIntercontactData.entrySet()) {
//            Double value = entry.getValue();
//            write(Double.toString(value));
//        }
        super.done();
    }

    private double calculateAverage(ArrayList<Double> list) {
        Iterator<Double> i = list.iterator();
        double time = 0;
        while (i.hasNext()) {
            Double d = i.next();
            time += d;
        }
        double avgDuration = time / list.size();
        return avgDuration;
    }
}
