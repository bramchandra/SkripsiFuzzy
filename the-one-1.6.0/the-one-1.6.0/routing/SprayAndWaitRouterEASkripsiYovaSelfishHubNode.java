package routing;

import core.*;
import java.util.*;
import report.estimasi;

/**
 * @author yova
 */
public class SprayAndWaitRouterEASkripsiYovaSelfishHubNode implements RoutingDecisionEngine, estimasi {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "DecisionSprayAndWaitRouter";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";

    protected int initialNrofCopies;
    protected boolean isBinary;

    //di data offline haggle 3 : 37000 pas di running T nya di sini 88987
    //di data offline haggle 6 : 42900 pas di running T nya di sini 99000
    int threshold = 80000; //ini buat haggle 3
//    int threshold = 100000; //ini buat haggle 6
    public static int interval = 300;
//    public static int interval2 = 1500;
    public double lastRecord = Double.MIN_VALUE;
    public double lastRecord2 = Double.MIN_VALUE;
    Double cariT;
    DTNHost thisNode;
    ArrayList<Double> cariTnya;
    ArrayList<Double> turunan1;
    ArrayList<Double> turunan2;
    ArrayList<Double> asli;
    ArrayList<Double> energi;
    Map<DTNHost, ArrayList<Double>> energiNode;
    Map<DTNHost, ArrayList<Double>> turunan1Node;
    Map<DTNHost, ArrayList<Double>> turunan2Node;

    public SprayAndWaitRouterEASkripsiYovaSelfishHubNode(Settings s) {
        s = new Settings(SPRAYANDWAIT_NS);
        initialNrofCopies = s.getInt(NROF_COPIES);

        if (isBinary) {
            isBinary = s.getBoolean(BINARY_MODE);
        }
    }

    protected SprayAndWaitRouterEASkripsiYovaSelfishHubNode(SprayAndWaitRouterEASkripsiYovaSelfishHubNode prototype) {
        this.initialNrofCopies = prototype.initialNrofCopies;
        this.isBinary = prototype.isBinary;
        this.cariT = prototype.cariT;
        this.thisNode = prototype.thisNode;
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
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        thisNode = thisHost;
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        assert nrofCopies != null : "Not a SnW message: " + m;

        if (isBinary) {
            /* in binary S'n'W the receiving node gets ceil(n/2) copies */
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            /* in standard S'n'W the receiving node gets only single copy */
            nrofCopies = 1;
        }

        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {

        if (getEnergy(this.thisNode) < (getInitialEnergy(this.thisNode) * 25 / 100)) { //cek baterai kita 
            cariT = this.hitT(this.thisNode); //hitung nilai T untuk prediksi
            if (cariT < threshold) { //bandingkan nilai T dengan threshold
                return false; //jangan simpan pesan
            } else {
                return m.getTo() != thisHost;
            }
        } else {
            return m.getTo() != thisHost;
        }
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {

        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (this.getEnergy(thisNode) < (this.getInitialEnergy(thisNode) * 25 / 100)) { //cek energi kita
            cariT = hitT(thisNode); //hitung nilai T untuk prediksi
            if (cariT < threshold) { //bandingkan nilai T dengan threshold
                if (m.getFrom() == thisNode && m.getTo() == otherHost) 
                    return true;
            } else {
                if (m.getTo() == otherHost || nrofCopies > 1) 
                    return true;
            }
        } else {
            if (m.getTo() == otherHost || nrofCopies > 1)
                return true;
        }

//        if(m.getTo() == otherHost) {
//            return true;
//        }
//        if(nrofCopies > 1) {
//            return true;
//        }

        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);

        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SprayAndWaitRouterEASkripsiYovaSelfishHubNode(this);
    }

    private Double getEnergy(DTNHost h) { //method untuk ambil baterai sekarang dari kelas energy model
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) { //method untuk ambil initial baterai dari kelas energy model
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

    private SprayAndWaitRouterEASkripsiYovaSelfishHubNode getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SprayAndWaitRouterEASkripsiYovaSelfishHubNode) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

//    @Override
//    public ArrayList<Double> getNilaiT() {
//        return this.cariTnya;
//    }
}
