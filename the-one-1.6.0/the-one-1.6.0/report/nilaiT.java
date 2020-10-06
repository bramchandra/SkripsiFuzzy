package report;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author yova
 */
public class nilaiT extends Report {
    
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

            ArrayList<Double> nilaiFreq = fe.getAsli();
            write(h+"\t"+nilaiFreq+"  ");
        }
        super.done();
    }
}
