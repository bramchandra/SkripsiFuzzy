/*
 * @(#)DistributedBubbleRap.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package routing.community;

import java.util.*;

import core.*;
import report.estimasi;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * <p>
 * Implements the Distributed BubbleRap Routing Algorithm from Hui et al. 2008
 * (Bibtex record included for convenience). The paper is a bit fuzzy on
 * thevactual implementation details. Choices exist for methods of community
 * detection (SIMPLE, K-CLIQUE, MODULARITY) and local centrality approximation
 * (DEGREE, S-WINDOW, C-WINDOW).</p>
 *
 * <p>
 * In general, each node maintains an idea of it's local community, a group of
 * nodes it meets with frequently. It also approximates its centrality within
 * the social network defined by this local community and within the global
 * social network defined by all nodes.</p>
 *
 * <p>
 * When a node has a message for a destination, D, and D is not part of its
 * local community, it forwards the message to "more globally central" nodes,
 * those that estimate a higher global centrality value. The intuition here is
 * that nodes in the center of the social network are more likely to contact the
 * destination. In this fashion the message bubbles up social network to more
 * central nodes until a node is found that reports D in its local community. At
 * this point, the message is only routed with in the nodes of the local
 * community and propagated towards more locally central nodes or the
 * destination until delivered.<p>
 *
 * <pre>
 * \@inproceedings{1374652,
 *	Address = {New York, NY, USA},
 *	Author = {Hui, Pan and Crowcroft, Jon and Yoneki, Eiko},
 *	Booktitle = {MobiHoc '08: Proceedings of the 9th ACM international symposium
 *		on Mobile ad hoc networking and computing},
 *	Doi = {http://doi.acm.org/10.1145/1374618.1374652},
 *	Isbn = {978-1-60558-073-9},
 *	Location = {Hong Kong, Hong Kong, China},
 *	Pages = {241--250},
 *	Publisher = {ACM},
 *	Title = {BUBBLE Rap: Social-based Forwarding in Delay Tolerant Networks},
 *	Url = {http://portal.acm.org/ft_gateway.cfm?id=1374652&type=pdf&coll=GUIDE&dl=GUIDE&CFID=55195392&CFTOKEN=93998863},
 *	Year = {2008}
 * }
 * </pre>
 *
 * @author PJ Dillon, University of Pittsburgh
 * @version Yovanda Priscilla
 *
 */
public class BubbleRapEADerivative implements RoutingDecisionEngine, CommunityDetectionEngine, estimasi {

    /** Community Detection Algorithm to employ -setting id {@value} */
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    /** Centrality Computation Algorithm to employ -setting id {@value}*/
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected CommunityDetection community;
    protected Centrality centrality;
    
    public static int interval = 600;//10 menit
    public double lastRecord = Double.MIN_VALUE;
    ArrayList<Double> turunan1 = new ArrayList<>();
    ArrayList<Double> turunan2 = new ArrayList<>();
    ArrayList<Double> asli = new ArrayList<>();
    ArrayList<Double> normalisasi1 = new ArrayList<>();
    ArrayList<Double> normalisasi2 = new ArrayList<>();

    /**
     * Constructs a DistributedBubbleRap Decision Engine based upon the settings
     * defined in the Settings object parameter. The class looks for the class
     * names of the community detection and centrality algorithms that should be
     * employed used to perform the routing.
     *
     * @param s Settings to configure the object
     */
    public BubbleRapEADerivative(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }

        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        } else {
            this.centrality = new SWindowCentrality(s);
        }
    }

    /**
     * Constructs a DistributedBubbleRap Decision Engine from the argument
     * prototype.
     *
     * @param proto Prototype DistributedBubbleRap upon which to base this
     * object
     */
    public BubbleRapEADerivative(BubbleRapEADerivative proto) {
        this.community = proto.community.replicate();
        this.centrality = proto.centrality.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    /**
     * Starts timing the duration of this new connection and informs the
     * community detection object that a new connection was formed.
     *
     * @see
     * routing.RoutingDecisionEngine#doExchangeForNewConnection(core.Connection,
     * core.DTNHost)
     */
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        BubbleRapEADerivative de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, de.community);
    }
    
    //add new method
    public double cek(DTNHost thiHost, DTNHost peer) {
        if (startTimestamps.containsKey(thiHost)) {
            startTimestamps.get(peer);
        }
        return 0;
    }

    public void connectionDown(DTNHost thisHost, DTNHost peer) {
//        double time = startTimestamps.get(peer);
        double time = cek(thisHost, peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;

        // inform the community detection object that a connection was lost.
        // The object might need the whole connection history at this point.
        community.connectionLost(thisHost, peer, peerCD, history);

        startTimestamps.remove(peer);
    }

    public boolean newMessage(Message m) {
        return true; // Always keep and attempt to forward a created message
    }

    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost; // Unicast Routing
    }

    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true; // trivial to deliver to final dest
        }
        /* Here is where we decide when to forward along a message. 
            * DiBuBB works such that it first forwards to the most globally central
            * nodes in the network until it finds a node that has the message's 
            * destination as part of it's local community. At this point, it uses 
            * the local centrality metric to forward a message within the community.*/
        
        DTNHost dest = m.getTo();
        BubbleRapEADerivative de = getOtherDecisionEngine(otherHost);

        // Which of us has the dest in our local communities, this host or the peer
        boolean peerInCommunity = de.commumesWithHost(dest);
        boolean meInCommunity = this.commumesWithHost(dest);
//        //add
//        Double meNewLocalRank = this.gabungan(dest);
//        Double meNewGlobalRank = this.gabungan(dest);
//        Double peerNewLocalRank = de.gabungan(dest);
//        Double peerNewGlobalRank = de.gabungan(dest);

        if (peerInCommunity && !meInCommunity) { // peer is in local commun. of dest
            return true;
        } else if (!peerInCommunity && meInCommunity) { // I'm in local commun. of dest
            return false;
        } else if (peerInCommunity) { // we're both in the local community of destination
            // Forward to the one with the higher local centrality (in our community)
            if (de.getLocalCentrality() > this.getLocalCentrality() ) {//add
                return true;
            } else {
                return false;
            }
        } // Neither in local community, forward to more globally central node
        else if (de.getGlobalCentrality() > this.getGlobalCentrality()) {//add
            return true;
        }
        return false;
    }

    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // DiBuBB allows a node to remove a message once it's forwarded it into the
        // local community of the destination
        BubbleRapEADerivative de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        BubbleRapEADerivative de = this.getOtherDecisionEngine(hostReportingOld);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    public RoutingDecisionEngine replicate() {
        return new BubbleRapEADerivative(this);
    }

    protected boolean commumesWithHost(DTNHost h) {
        return community.isHostInCommunity(h);
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    protected double getGlobalCentrality() {
        return this.centrality.getGlobalCentrality(connHistory);
    }
    
//    protected double gabungan(DTNHost h) {
//        double gab = getLocalCentrality() + getGlobalCentrality() + hitAsli(h);
////        System.out.println(gab);
//        return gab;
//    }

    private BubbleRapEADerivative getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (BubbleRapEADerivative) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }
    
    private Double getEnergy(DTNHost host) {
        return (Double) host.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }
    
    private Double getInitialEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }
    
    @Override
    public void update(DTNHost h) {
        double simTime = SimClock.getTime();
        if (SimClock.getTime() - lastRecord >= interval) {
            turunan1.add(this.hitTurunanPertama(h));
            turunan2.add(this.hitTurunanKedua(h));
            asli.add(this.hitAsli(h));
            this.lastRecord = simTime - simTime % interval;
        }
    }
    
    private Double hitAsli(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double energy = getEnergy(h);
        Double eMax = getInitialEnergy(h);
        Double pangkat = (Math.log(epsilon) * energy) / eMax;
        Double aslinya = K * Math.exp(pangkat);
        return aslinya;
    }
    
    private Double hitTurunanPertama(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double energy = getEnergy(h);
        Double eMax = getInitialEnergy(h);
        Double pangkat = Math.exp(Math.log(epsilon) * energy / eMax);
        Double turunanSatu = ((K * Math.log(epsilon)) / eMax) * pangkat;
        return turunanSatu;
    }

    private Double hitTurunanKedua(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double energy = getEnergy(h);
        Double eMax = getInitialEnergy(h);
        Double pangkat = (Math.log(epsilon) * energy) / eMax;
        Double a = (Math.log(epsilon)) / eMax;
        Double turunanDua = (K * Math.pow(a, 2)) * Math.exp(pangkat);
        return turunanDua;
    }
    
//    private Double hitNormalisasi(DTNHost h) {
////        double minTurunan1 = -6.90E-03;
//        Double min = Collections.min(getTurunan1());
//        Double max = Collections.max(getTurunan1());
////        double maxTurunan1 = -7.05E-06;
////        ArrayList<Double> x = turunan1;
//        Double normalisasi = 0.0;
//        for (double norm : turunan1) {
//            normalisasi = (norm - min) / (max - min);
////            return normalisasi;
////            System.out.println(normalisasi);
//        }
////        System.out.println(normalisasi);
//        return normalisasi;
//    }

    @Override
    public ArrayList<Double> getTurunan1() {
        return this.turunan1;
    }

    @Override
    public ArrayList<Double> getTurunan2() {
        return this.turunan2;
    }

    @Override
    public ArrayList<Double> getAsli() {
        return this.asli;
    }

    @Override
    public ArrayList<Double> getNormalisasi1() {
        return this.normalisasi1;
    }

    @Override
    public ArrayList<Double> getNormalisasi2() {
        return this.normalisasi2;
    }

//    @Override
//    public ArrayList<Double> getWindowingT1(Map<DTNHost, List<routing.Duration>> connHistory) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public Map<Integer, ArrayList<Double>> getEnergi() {
    }

    @Override
    public Map<Integer, ArrayList<Double>> getT1() {
    }

    @Override
    public Map<Integer, ArrayList<Double>> getT2() {
    }

}
