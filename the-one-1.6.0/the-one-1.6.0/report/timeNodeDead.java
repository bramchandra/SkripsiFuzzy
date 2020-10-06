package report;

import core.*;
import java.util.*;

/**
 *
 * @author yova
 */
public class timeNodeDead extends Report implements UpdateListener {

    public static int interval = 300;
    public Double lastRecord = Double.MIN_VALUE;
    int va;
    public Map<Integer, Double> mati;

    public timeNodeDead() {
        super();
        int va = this.va;
        mati = new HashMap<Integer, Double>();
    }

    @Override
    public void done() {
        for (Map.Entry<Integer, Double> entry : mati.entrySet()) {
            write(" " + entry.getKey() + " \t " + entry.getValue());
        }
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            for (DTNHost h : hosts) {
                if (getEnergy(h) <= 0) {
                    va = h.getAddress();
                    mati.put(va, lastRecord);
                }
            }
            lastRecord = simTime - simTime % interval;
        }

    }

    private Double getEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

}
