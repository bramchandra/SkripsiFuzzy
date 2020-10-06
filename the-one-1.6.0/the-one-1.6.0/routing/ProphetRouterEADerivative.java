package routing;

import java.util.*;

import core.*;
import report.estimasi;

public class ProphetRouterEADerivative implements RoutingDecisionEngine, estimasi {

    protected final static String BETA_SETTING = "beta";
    protected final static String P_INIT_SETTING = "initial_p";
    protected final static String SECONDS_IN_UNIT_S = "secondsInTimeUnit";

    public static final String PROPHET_NS = "DecisionProphetRouter";
    
    protected static final double DEFAULT_P_INIT = 0.75;
    protected static final double GAMMA = 0.92;
    protected static final double DEFAULT_BETA = 0.45;
    protected static final int DEFAULT_UNIT = 30;

    protected double beta;
    protected double pinit;
    protected double lastAgeUpdate;
    protected int secondsInTimeUnit;
    
        //di data offline haggle 3 : 37000 pas di running T nya di sini 88987
    //di data offline haggle 6 : 42900 pas di running T nya di sini 99000
    int threshold = 80000; //ini buat haggle 3
//    int threshold = 100000; //ini buat haggle 6
    public static int interval = 300;
//    public static int interval2 = 1500;
    public double lastRecord = Double.MIN_VALUE;
    public double lastRecord2 = Double.MIN_VALUE;
    Double cariT;
    DTNHost thisHost;
    ArrayList<Double> cariTnya;
    ArrayList<Double> turunan1;
    ArrayList<Double> turunan2;
    ArrayList<Double> asli;
    ArrayList<Double> energi;
    Map<DTNHost, ArrayList<Double>> energiNode;
    Map<DTNHost, ArrayList<Double>> turunan1Node;
    Map<DTNHost, ArrayList<Double>> turunan2Node;

    /**
     * delivery predictabilities
     */
    private Map<DTNHost, Double> preds;

    public ProphetRouterEADerivative(Settings s) {
        s = new Settings(PROPHET_NS);
        if (s.contains(BETA_SETTING)) {
            beta = s.getDouble(BETA_SETTING);
        } else {
            beta = DEFAULT_BETA;
        }

        if (s.contains(P_INIT_SETTING)) {
            pinit = s.getDouble(P_INIT_SETTING);
        } else {
            pinit = DEFAULT_P_INIT;
        }

        if (s.contains(SECONDS_IN_UNIT_S)) {
            secondsInTimeUnit = s.getInt(SECONDS_IN_UNIT_S);
        } else {
            secondsInTimeUnit = DEFAULT_UNIT;
        }

        preds = new HashMap<DTNHost, Double>();
        this.lastAgeUpdate = 0.0;
    }

    public ProphetRouterEADerivative(ProphetRouterEADerivative de) {
        beta = de.beta;
        pinit = de.pinit;
        secondsInTimeUnit = de.secondsInTimeUnit;
        preds = new HashMap<DTNHost, Double>();
        this.lastAgeUpdate = de.lastAgeUpdate;
        this.cariT = de.cariT;
        this.thisHost = de.thisHost;
        cariTnya = new ArrayList<>();
        turunan1 = new ArrayList<>();
        turunan2 = new ArrayList<>();
        asli = new ArrayList<>();
        energi = new ArrayList<>();
//        cariT = new ArrayList<>();
        energiNode = new HashMap<>();
        turunan1Node = new HashMap<>();
        turunan2Node = new HashMap<>();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new ProphetRouterEADerivative(this);
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        this.thisHost = thisHost;
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        ProphetRouterEADerivative de = getOtherProphetDecisionEngine(peer);
        Set<DTNHost> hostSet = new HashSet<DTNHost>(this.preds.size()
                + de.preds.size());
        hostSet.addAll(this.preds.keySet());
        hostSet.addAll(de.preds.keySet());

        this.agePreds();
        de.agePreds();

        // Update preds for this connection
        double myOldValue = this.getPredFor(peer),
                peerOldValue = de.getPredFor(myHost),
                myPforHost = myOldValue + (1 - myOldValue) * pinit,
                peerPforMe = peerOldValue + (1 - peerOldValue) * de.pinit;
        preds.put(peer, myPforHost);
        de.preds.put(myHost, peerPforMe);

        // Update transistivities
        for (DTNHost h : hostSet) {
            myOldValue = 0.0;
            peerOldValue = 0.0;

            if (preds.containsKey(h)) {
                myOldValue = preds.get(h);
            }
            if (de.preds.containsKey(h)) {
                peerOldValue = de.preds.get(h);
            }

            if (h != myHost) {
                preds.put(h, myOldValue + (1 - myOldValue) * myPforHost * peerOldValue * beta);
            }
            if (h != peer) {
                de.preds.put(h, peerOldValue + (1 - peerOldValue) * peerPforMe * myOldValue * beta);
            }
        }
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
//        if (getEnergy(this.thisHost) < (getInitialEnergy(this.thisHost) * 25 / 100)) { //cek baterai kita 
//            cariT = this.hitT(this.thisHost); //hitung nilai T untuk prediksi
//            if (cariT < threshold) { //bandingkan nilai T dengan threshold
//                return false; //jangan simpan pesan
//            } else {
//                return m.getTo() != thisHost;
//            }
//        } else {
//            return m.getTo() != thisHost;
//        }
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }

        ProphetRouterEADerivative de = getOtherProphetDecisionEngine(otherHost);
        
//        if (this.getEnergy(thisHost) < (this.getInitialEnergy(thisHost) * 25 / 100)) { //cek energi kita
//            cariT = hitT(thisHost); //hitung nilai T untuk prediksi
//            if (cariT < threshold) { //bandingkan nilai T dengan threshold
//                if (m.getFrom() == thisHost && m.getTo() == otherHost) 
//                    return true;
//            } else {
//                if (m.getTo() == otherHost || de.getPredFor(m.getTo()) > this.getPredFor(m.getTo())) 
//                    return true;
//            }
//        } else {
//            if (m.getTo() == otherHost || de.getPredFor(m.getTo()) > this.getPredFor(m.getTo()))
//                return true;
//        }

        return de.getPredFor(m.getTo()) > this.getPredFor(m.getTo());
//        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    private ProphetRouterEADerivative getOtherProphetDecisionEngine(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (ProphetRouterEADerivative) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private void agePreds() {
        double timeDiff = (SimClock.getTime() - this.lastAgeUpdate)
                / secondsInTimeUnit;

        if (timeDiff == 0) {
            return;
        }

        double mult = Math.pow(GAMMA, timeDiff);
        for (Map.Entry<DTNHost, Double> e : preds.entrySet()) {
            e.setValue(e.getValue() * mult);
        }

        this.lastAgeUpdate = SimClock.getTime();
    }

    /**
     * Returns the current prediction (P) value for a host or 0 if entry for the
     * host doesn't exist.
     *
     * @param host The host to look the P for
     * @return the current P value
     */
    private double getPredFor(DTNHost host) {
        agePreds(); // make sure preds are updated before getting
        if (preds.containsKey(host)) {
            return preds.get(host);
        } else {
            return 0;
        }
    }

    private Double getEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }
    
    //method update untuk mengambil data energi, kecepatan, percepatan, mean1 dan mean2 pada interval waktu tertentu
    @Override
    public void update(DTNHost h) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            energi.add(this.getEnergy(h));
            turunan1.add(this.hitTurunanPertama(h));
            turunan2.add(this.hitTurunanKedua(h));
//            cariTnya.add(this.hitT(h));
            energiNode.put(h, energi);
            turunan1Node.put(h, turunan1);
            turunan2Node.put(h, turunan2);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    private Double hitAsli(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double energy = getEnergy(h);
        Double eMax = getInitialEnergy(h);
//        Double cariTau = energy / eMax; //mau liat nilai e nya itu eksponensial apa linier
//        return cariTau;
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

    private Double hitT(DTNHost h) {
        Double Vt;
        Double Vo = getTurunan1(h).get(turunan1.size() - 1); //kecepatan terbaru
        Vo = Math.abs(Vo); //nilai di mutlak
//        System.out.println("kecepatan terbaru : " + Vo);
        Double a = getTurunan2(h).get(turunan2.size() - 1); //kecepatan terbaru
        Double s = getEnergy(h); //sisa energi
//        System.out.println("energi terbaru = "+s);
        Vt = (Math.pow(Vo, 2) + (2 * a * s));
        cariT = ((Math.sqrt(Vt)) - Vo) / a;
//        System.out.println("Vt = " + Vt + "\n" + "t = " + t);
        return cariT;
    }

    @Override
    public ArrayList<Double> getTurunan1(DTNHost h) {
        return this.turunan1;
    }

    @Override
    public ArrayList<Double> getTurunan2(DTNHost h) {
        return this.turunan2;
    }

    @Override
    public ArrayList<Double> getAsli() {
        return this.asli;
    }

//    @Override
//    public ArrayList<Double> getNilaiT() {
//        return this.cariT;
//    }
    @Override
    public Map<DTNHost, ArrayList<Double>> getEnergi() {
        return this.energiNode;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getT1() {
        return this.turunan1Node;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getT2() {
        return this.turunan2Node;
    }

}
