/* 
 * Copyright 2010-2012 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

/**
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * [Simulation time] [average buffer occupancy % [0..100] ] [variance]
 * </p>
 *
 * <p>
 * The occupancy is calculated as an instantaneous snapshot every nth second as
 * defined by the <code>occupancyInterval</code> setting, not as an average over
 * time.
 * </p>
 *
 * @author yova
 */
import routing.DecisionEngineRouter;
import java.util.List;

import core.*;
import java.util.*;
import routing.*;

public class EnergyOccupancyPerTimeEdit extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String ENERGY_REPORT_INTERVAL = "occupancyInterval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_ENERGY_REPORT_INTERVAL = 300;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
//    private Map<DTNHost, ArrayList<Double>> energyCounts = new HashMap<DTNHost, ArrayList<Double>>();
    private Map<Integer, ArrayList<Double>> energyCounts = new HashMap<Integer, ArrayList<Double>>();
    private int i = 0;

    public EnergyOccupancyPerTimeEdit() {
        super();

        Settings settings = getSettings();
        if (settings.contains(ENERGY_REPORT_INTERVAL)) {
            interval = settings.getInt(ENERGY_REPORT_INTERVAL);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_ENERGY_REPORT_INTERVAL;
        }
    }

    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }

        if (simTime - lastRecord >= interval) {
            //lastRecord = SimClock.getTime();
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    /**
     * @param hosts The list of hosts in the simulation
     */
    private void printLine(List<DTNHost> hosts) {
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof estimasi)) {
                continue;
            }
            estimasi fe = (estimasi) de;
            ArrayList<Double> energyList = new ArrayList<Double>();
//            energyCounts = fe.getEnergi();
            Double temp = (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
            if (energyCounts.containsKey(h)) {
                energyList = energyCounts.get(h);
//                energyList.add(temp);
//                energyList = fe
                energyCounts.put(i, energyList);
            } else {
                energyCounts.put(i, energyList);
            }
        }
    }

    @Override
    public void done() {
//        for (Map.Entry<DTNHost, ArrayList<Double>> entry : energyCounts.entrySet()) {
        for (Map.Entry<Integer, ArrayList<Double>> entry : energyCounts.entrySet()) {
//            DTNHost a = entry.getKey();
            Integer a = entry.getKey();
            ArrayList<Double> b = entry.getValue();
//            String printHost = "Node " + entry.getKey().getAddress() + "\t";
//            String printHost = "Node " + a.toString() + "\t";
            String printHost = "Node " + a.toString() + "\t ";
            for (Double energyList : b) {
                printHost = printHost + "\t" + energyList;
            }
            write(printHost);
            //write("" + b + ' ' + entry.getValue());
        }
        super.done();
    }
}
