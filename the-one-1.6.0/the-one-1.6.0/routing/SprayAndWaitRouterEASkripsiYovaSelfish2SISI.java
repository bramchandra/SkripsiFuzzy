package routing;

import core.*;
import java.util.*;
import report.estimasi;

/**
 * @author yova
 */
public class SprayAndWaitRouterEASkripsiYovaSelfish2SISI implements RoutingDecisionEngine, estimasi {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "DecisionSprayAndWaitRouterEA";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";

    protected int initialNrofCopies;
    protected boolean isBinary;

    public static int interval = 300;
    public static int interval2 = 1500;
    public double lastRecord = Double.MIN_VALUE;
    public double lastRecord2 = Double.MIN_VALUE;
    ArrayList<Double> turunan1;
    ArrayList<Double> turunan2;
    ArrayList<Double> mean1;
    ArrayList<Double> mean2;
    ArrayList<Double> asli;
    ArrayList<Double> energi;
    Map<DTNHost, ArrayList<Double>> energiNode;
    Map<DTNHost, ArrayList<Double>> turunan1Node;
    Map<DTNHost, ArrayList<Double>> turunan2Node;
    Map<DTNHost, ArrayList<Double>> meanTurunan1;
    Map<DTNHost, ArrayList<Double>> meanTurunan2;

    public SprayAndWaitRouterEASkripsiYovaSelfish2SISI(Settings s) {
        s = new Settings(SPRAYANDWAIT_NS);
        initialNrofCopies = s.getInt(NROF_COPIES);

        if (isBinary) {
            isBinary = s.getBoolean(BINARY_MODE);
        }
    }

    protected SprayAndWaitRouterEASkripsiYovaSelfish2SISI(SprayAndWaitRouterEASkripsiYovaSelfish2SISI prototype) {
        this.initialNrofCopies = prototype.initialNrofCopies;
        this.isBinary = prototype.isBinary;
        turunan1 = new ArrayList<Double>();
        turunan2 = new ArrayList<Double>();
        asli = new ArrayList<Double>();
        mean1 = new ArrayList<Double>();
        mean2 = new ArrayList<Double>();
        energi = new ArrayList<Double>();
        energiNode = new HashMap<>();
        turunan1Node = new HashMap<>();
        turunan2Node = new HashMap<>();
        meanTurunan1 = new HashMap<>();
        meanTurunan2 = new HashMap<>();
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

        if (getEnergy(thisHost) > getInitialEnergy(thisHost) * 50 / 100
                && getEnergy(thisHost) <= getInitialEnergy(thisHost) * 70 / 100) {
            fungsiDua(m, thisHost);
        } else if (getEnergy(thisHost) > getInitialEnergy(thisHost) * 30 / 100
                && getEnergy(thisHost) <= getInitialEnergy(thisHost) * 50 / 100) {
            fungsiSatu(m, thisHost);
        } else if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 30 / 100) {
            return false;
        }

        return m.getTo() != thisHost;
    }

        private boolean fungsiSatu(Message m, DTNHost thisHost) {
            if (this.getTurunan1(thisHost).get(turunan1.size() - 1) <= this.getMean1().get(mean1.size() - 1)
                    && this.getTurunan2(thisHost).get(turunan2.size() - 1) <= this.getMean2().get(mean2.size() - 1)) {
                return true;
            } else if (this.getTurunan1(thisHost).get(turunan1.size() - 1) <= this.getMean1().get(mean1.size() - 1)
                    && this.getTurunan2(thisHost).get(turunan2.size() - 1) >= this.getMean2().get(mean2.size() - 1)) {
                return false;
            } else if (this.getTurunan1(thisHost).get(turunan1.size() - 1) >= this.getMean1().get(mean1.size() - 1)
                    && this.getTurunan2(thisHost).get(turunan2.size() - 1) <= this.getMean2().get(mean2.size() - 1)) {
                return false;
            } else if (this.getTurunan1(thisHost).get(turunan1.size() - 1) >= this.getMean1().get(mean1.size() - 1)
                    && this.getTurunan2(thisHost).get(turunan2.size() - 1) >= this.getMean2().get(mean2.size() - 1)) {
                return false;
            }
            return false;
        }
        
    //untuk melihat kecepatan dan percepatan penurunan baterai node
    private boolean fungsiDua(Message m, DTNHost thisHost) {
        if (this.getTurunan1(thisHost).get(turunan1.size() - 1) <= this.getMean1().get(mean1.size() - 1)
                && this.getTurunan2(thisHost).get(turunan2.size() - 1) <= this.getMean2().get(mean2.size() - 1)) {
            return true;
        } else if (this.getTurunan1(thisHost).get(turunan1.size() - 1) <= this.getMean1().get(mean1.size() - 1)
                && this.getTurunan2(thisHost).get(turunan2.size() - 1) >= this.getMean2().get(mean2.size() - 1)) {
            if (getRandomNumberInRange(0, 1) == 1) {
//            if (Math.random() < 0.5) {
                return true;
            } 
        } else if (this.getTurunan1(thisHost).get(turunan1.size() - 1) >= this.getMean1().get(mean1.size() - 1)
                && this.getTurunan2(thisHost).get(turunan2.size() - 1) <= this.getMean2().get(mean2.size() - 1)) {
            if (getRandomNumberInRange(0, 1) == 1) {
//            if (Math.random() < 0.5) {
                return true;
            } 
        } else if (this.getTurunan1(thisHost).get(turunan1.size() - 1) >= this.getMean1().get(mean1.size() - 1)
                && this.getTurunan2(thisHost).get(turunan2.size() - 1) >= this.getMean2().get(mean2.size() - 1)) {
            return false;
        }
        return false;
    }

    private static double getRandomNumberInRange(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((int) ((max - min) + 1)) + min;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        DTNHost thisHost = m.getFrom();

        if (this.getEnergy(thisHost) > this.getInitialEnergy(thisHost) * 50 / 100
                && this.getEnergy(thisHost) <= this.getInitialEnergy(thisHost) * 70 / 100) {
            if (m.getTo() == otherHost) {
                return true;
            } else if (nrofCopies > 1) {
                fungsiDua(m, thisHost);
            }
        } else if (this.getEnergy(thisHost) > this.getInitialEnergy(thisHost) * 30 / 100
                && this.getEnergy(thisHost) <= this.getInitialEnergy(thisHost) * 50 / 100) {
            if (m.getTo() == otherHost) {
                return true;
            } else if (nrofCopies > 1) {
                fungsiSatu(m, thisHost);
            }
        } else if (this.getEnergy(thisHost) <= this.getInitialEnergy(thisHost) * 30 / 100) {
            return false;
        }
        
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
        return new SprayAndWaitRouterEASkripsiYovaSelfish2SISI(this);
    }

    private Double getEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }

    @Override
    public void update(DTNHost h) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            energi.add(this.getEnergy(h));
//            asli.add(this.hitAsli(h)); //cek nilai fungsi
//            turunan1.add(this.hitTurunanPertama(h));
//            turunan2.add(this.hitTurunanKedua(h));
            //rata-rata dari kecepatan dan percepatan akan diambil setiap 5 data
            if(turunan1.size() < 5 && turunan2.size() < 5) {
                turunan1.add(this.hitTurunanPertama(h));
                turunan2.add(this.hitTurunanKedua(h));
                turunan1Node.put(h, turunan1);
                turunan2Node.put(h, turunan2);
            } else {
                mean1.add(this.hitMean1(h));
                mean2.add(this.hitMean2(h));
                meanTurunan1.put(h, mean1);
                meanTurunan2.put(h, mean2);
                turunan1.remove(0);
                turunan2.remove(0);
            }
            energiNode.put(h, energi);
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

//    private Double hitVariansi(DTNHost h) {
//        ArrayList<Double> dataT = getTurunan1();
//        Double sum = 0.0;
//        Double var;
//        for (Double hit : dataT ) {
//            sum += hit;
//        }
//        return var = (sum - hitMean(dataT)) / dataT.size();
//    }
    private Double hitMean1(DTNHost h) {
        ArrayList<Double> t1 = this.getTurunan1(h);
        Double sum = 0.0;
        Double mean;
        for (double dValue : t1) {
            sum += dValue;
        }
        return mean = sum / t1.size();
//        Double n = 5.0;
//        Double Xi = 0.0;
//        Double nilaiXi = 0.0;
//        Double mean = (1 / n) * (Xi);
//        for (Double entry : turunan1) {
//            Xi += entry;
//            nilaiXi = Xi / turunan1.size();
//        }
//        mean = (1 / n) * nilaiXi;
////        System.out.println(mean);
//        return mean;
    }

    private Double hitMean2(DTNHost h) {
        ArrayList<Double> t2 = this.getTurunan2(h);
        Double sum = 0.0;
        Double mean;
        for (double dValue : t2) {
            sum += dValue;
        }
        return mean = sum / t2.size();
    }

//    private Double hitNormalisasi1(DTNHost h) {
//        Double min = getTurunan1().get(turunan1.size() - 1);
//        Double max = getTurunan1().get(0);
//        Double normalisasi = 0.0;
//        for (Double norm : turunan1) {
//            normalisasi = (norm - min) / (max - min);
//        }
//        return normalisasi;
//    }
//
//    private Double hitNormalisasi2(DTNHost h) {
//        Double min = getTurunan2().get(turunan2.size() - 1);
//        Double max = getTurunan2().get(0);
//        Double normalisasi = 0.0;
//        for (Double norm : turunan2) {
//            normalisasi = (norm - min) / (max - min);
//        }
//        return normalisasi;
//    }
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

    @Override
    public ArrayList<Double> getMean1() {
        return this.mean1;
    }

    @Override
    public ArrayList<Double> getMean2() {
        return this.mean2;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getMeanT1() {
        return this.meanTurunan1;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getMeanT2() {
        return this.meanTurunan2;
    }

    private SprayAndWaitRouterEASkripsiYovaSelfish2SISI getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SprayAndWaitRouterEASkripsiYovaSelfish2SISI) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

//    @Override
//    public ArrayList<Double> getVariansi1() {
//        return this.variansi1;
//    }
//
//    @Override
//    public Map<Integer, ArrayList<Double>> getVariansiT1() {
//        return this.variansiTurunan1;
//    }
}
