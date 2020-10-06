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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import report.estimasi;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.DecisionEngineRouter;
//import routing.community.VarianceDecisionEngine;

/**
 *
 * @author Afra Rian Yudianto, Sanata Dharma University
 */
public class FuzzyBasedRouter implements RoutingDecisionEngine, estimasi {

    public static final String FCL_SIMILARITY = "Similarity";
    public static final String FCL_RESOURCE = "Resource";
    public static final String FCL_FINAL = "Final";
    public static final String CLOSENESS = "closeness";
    public static final String VARIANCE = "variance";
    public static final String FUZZYRESOURCE = "fuzzyResource";
    public static final String FUZZYSIMILARITY = "fuzzySimilarity";
    public static final String KECEPATANENERGY = "kecepatanEnergy";
    public static final String PERCEPATANENERGY = "percepatanEnergy";
    public static final String TRANSFER_OF_UTILITY = "hasil";
    private FIS fclSimilarity;
    private FIS fclResource;
    private FIS fclFinal;
    LinkedList<Double> kecepatan;
    LinkedList<Double> percepatan;
    LinkedList<Double> resource;
    protected LinkedList<Double> sampelBuffer;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, ArrayList<Double>> ambildata;
    LinkedList<Double> energi;
//    protected List<Double> ambildata2;
    private double interval = 300;
    private double lastRecord;
    private double varianceBuffer;
    private double meanBuffer;
    private int count;
//    private Double energy;

    public FuzzyBasedRouter(Settings s) {
        String fclStringSim = s.getSetting(FCL_SIMILARITY);
        String fclStringRes = s.getSetting(FCL_RESOURCE);
        String fclStringFin = s.getSetting(FCL_FINAL);
        fclSimilarity = FIS.load(fclStringSim);
        fclResource = FIS.load(fclStringRes);
        fclFinal = FIS.load(fclStringFin);

    }

    public FuzzyBasedRouter(FuzzyBasedRouter t) {
        this.fclSimilarity = t.fclSimilarity;
        this.fclResource = t.fclResource;
        this.fclFinal = t.fclFinal;
        kecepatan = new LinkedList<Double>();
        resource = new LinkedList<Double>();
        percepatan = new LinkedList<Double>();
        energi = new LinkedList<Double>();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();

        ambildata = new HashMap<>();
        lastRecord = 0.0;
        count = 0;
//        energy = 0.0;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list
////
        double getLastDisconnect = 0;
        if (startTimestamps.containsKey(peer)) {
            getLastDisconnect = startTimestamps.get(peer);
        }
        double currentTime = SimClock.getTime();

        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
        } else {
            history = connHistory.get(peer);
        }

        if (currentTime - getLastDisconnect > 0) {
            history.add(new Duration(getLastDisconnect, currentTime));

        }
        connHistory.put(peer, history);
        this.startTimestamps.remove(peer);

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        this.startTimestamps.put(peer, SimClock.getTime());
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
        return m.getTo() != thisHost;
    }

    private double getKecepatanEnergy(DTNHost h) {//Kecepatan
        double K = 10.0;
        double epsilon = Math.pow(10, -3);
        double energy = (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
        double eMax = (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
        double pangkat = Math.exp(Math.log(epsilon) * energy / eMax);
        double turunanSatu = ((K * Math.log(epsilon)) / eMax) * pangkat;
        return Math.abs(turunanSatu);
    }

    private double getPercepatanEnergy(DTNHost h) {//Percepatan
        double K = 10.0;
        double epsilon = Math.pow(10, -3);
        double energy = (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
        double eMax = (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
        double pangkat = (Math.log(epsilon) * energy) / eMax;
        double a = (Math.log(epsilon)) / eMax;
        double turunanDua = (K * Math.pow(a, 2)) * Math.exp(pangkat);
        return turunanDua;
    }

    public boolean getHasEnergy(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return ((DecisionEngineRouter) otherRouter).hasEnergy();//ambil method di decision
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        FuzzyBasedRouter de = getOtherDecisionEngine(otherHost);
        double me = this.DefuzzificationFinal(dest);
        double peer = de.DefuzzificationFinal(dest);

//        ArrayList<Double> history;
//        if (!ambildata.containsKey(peer)) {
//            history = new ArrayList<Double>();
//        } else {
//            history = ambildata.get(peer);
//        }
//        history.add(me);
//        ambildata.put(otherHost, history);
//        if((getHasEnergy(thisHost)==false)||(getHasEnergy(otherHost)==false)){
//            System.out.println("NODE MATI");  
//            
//        }
        return me < peer;
    }

    private double DefuzzificationResource() {
        double kecepatanEnergyValue = kecepatan.getLast();
        double percepatanEnergyValue = percepatan.getLast();
        //sisa
        FunctionBlock functionBlock = fclResource.getFunctionBlock("haggle3Infocom5");
//        FunctionBlock functionBlock = fclResource.getFunctionBlock("reality");
       
        functionBlock.setVariable(KECEPATANENERGY, kecepatanEnergyValue);
        functionBlock.setVariable(PERCEPATANENERGY, percepatanEnergyValue);
        functionBlock.evaluate();
        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double DefuzzificationSimilarity(DTNHost nodes) {
        double closenessValue = getClosenessOfNodes(nodes);
        double varianceValue = getNormalizedVarianceOfNodes(nodes);
        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock("haggle3Infocom5Rian");
//        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock("realityRian");
//        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock("haggle3Infocom5");
//        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock("reality");
        functionBlock.setVariable(CLOSENESS, closenessValue);
        functionBlock.setVariable(VARIANCE, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double DefuzzificationFinal(DTNHost nodes) {
        double defSimilarity = DefuzzificationSimilarity(nodes);
        double defResource = resource.getLast();
        FunctionBlock functionBlock = fclFinal.getFunctionBlock("haggle3Infocom5revisi");
//        FunctionBlock functionBlock = fclFinal.getFunctionBlock("realityrevisi");
        functionBlock.setVariable(FUZZYSIMILARITY, defSimilarity);
        functionBlock.setVariable(FUZZYRESOURCE, defResource);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    public double getVarianceOfNodes(DTNHost nodes) {
        List<Duration> list = getList(nodes);
        Iterator<Duration> duration = list.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparationOfNodes(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += Math.pow((d.end - d.start) - mean, 2);
        }
        return temp / list.size();
    }

    public double getNormalizedVarianceOfNodes(DTNHost nodes) {
        double k = getList(nodes).size();
        double N = 0;
        double sigmf = 0;
        Iterator<Duration> iterator = getList(nodes).iterator();
        while (iterator.hasNext()) {
            Duration duration = iterator.next();
            double timeDuration = (duration.end - duration.start);
            N += timeDuration;
            sigmf += Math.pow(timeDuration, 2);
        }
        Double d = (k * (Math.pow(N, 2) - sigmf)) / (Math.pow(N, 2) * (k - 1));
        return d;
    }

    public List<Duration> getList(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
        }
    }

    public double getClosenessOfNodes(DTNHost nodes) {
        double rataShortestSeparation = getAverageShortestSeparationOfNodes(nodes);
        double variansi = getVarianceOfNodes(nodes);
        double c = Math.exp(-(Math.pow(rataShortestSeparation, 2) / (2 * variansi)));
        return c;
    }

    public double getAverageShortestSeparationOfNodes(DTNHost nodes) {
        List<Duration> list = getList(nodes);
        Iterator<Duration> duration = list.iterator();
        double hasil = 0;
        while (duration.hasNext()) {
            Duration d = duration.next();
            hasil += (d.end - d.start);
        }
        return hasil / list.size();
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new FuzzyBasedRouter(this);
    }

    private FuzzyBasedRouter getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (FuzzyBasedRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost thisHost) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            this.energi.add((Double) thisHost.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID));
            this.kecepatan.add(this.getKecepatanEnergy(thisHost));
            this.percepatan.add(this.getPercepatanEnergy(thisHost));
            this.resource.add(this.DefuzzificationResource());
            this.lastRecord = simTime - simTime % interval;
        }
    }

    @Override
    public LinkedList<Double> getTurunan1() {
        return this.kecepatan;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getT1() {
        return this.ambildata;
    }

    @Override
    public LinkedList<Double> getEnergy() {
        return this.energi;
    }

}
