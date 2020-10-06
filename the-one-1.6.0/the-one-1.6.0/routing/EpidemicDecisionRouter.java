/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import report.estimasi;

/**
 *
 * @author Afra Rian
 */
public class EpidemicDecisionRouter implements RoutingDecisionEngine, estimasi {

    private double interval = 300;
    private LinkedList energy;
    private double lastRecord;

    public EpidemicDecisionRouter(Settings s) {
    }

    public EpidemicDecisionRouter(EpidemicDecisionRouter proto) {
        energy = new LinkedList();
        lastRecord = 0.0;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (getHasEnergy(otherHost) == false) {
            System.out.println("NODE MATI");

        }
        return true;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicDecisionRouter(this);
    }

    private EpidemicDecisionRouter getOtherEpidemicRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicDecisionRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    public boolean getHasEnergy(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return ((DecisionEngineRouter) otherRouter).hasEnergy();//ambil method di decision
    }

    @Override
    public void update(DTNHost thishost) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            this.energy.add((Double) thishost.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID));
            this.lastRecord = simTime - simTime % interval;
        }
    }

    @Override
    public LinkedList<Double> getTurunan1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkedList<Double> getEnergy() {
        return this.energy;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getT1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
