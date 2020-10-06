package report;

import core.*;
import java.util.*;

/**
 *
 * @author yova
 */
public class networkLifetime extends Report implements UpdateListener {

    public static int interval = 300;
    public Double lastRecord = Double.MIN_VALUE;
    public Map<Double, Integer> mati;

    public networkLifetime(){
        super();
        mati = new HashMap<Double, Integer>();
    }
    
    @Override
    public void done() {
        for (Map.Entry<Double, Integer> entry : mati.entrySet()) {
            Double key = entry.getKey();
            Integer value = entry.getValue();
            write(key+ " \t" +value);
        }
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();

        if (simTime - lastRecord >= interval) {
//            System.out.println("HALO");
            int va = 0;
            for (DTNHost h : hosts) {
                if (getEnergy(h) <= 0) {
                    va++;
                }
            }
            lastRecord = simTime - simTime % interval;
            mati.put(lastRecord, va);
        }

    }

    private Double getEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

}
