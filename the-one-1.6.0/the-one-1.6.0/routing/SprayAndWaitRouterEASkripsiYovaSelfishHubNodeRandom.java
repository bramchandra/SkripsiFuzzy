package routing;

import core.*;
import java.util.*;
import report.estimasi;

/**
 * @author yova
 */
public class SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom implements RoutingDecisionEngine, estimasi {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "DecisionSprayAndWaitRouterEA";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";

    protected int initialNrofCopies;
    protected boolean isBinary;

    public static int interval = 300;
    public static int interval2 = 21600;
    public double lastRecord = Double.MIN_VALUE;
    public double lastRecord2 = Double.MIN_VALUE;
    ArrayList<Double> turunan1;
    ArrayList<Double> turunan2;
    ArrayList<Double> normalisasi1;
    ArrayList<Double> normalisasi2;
    ArrayList<Double> asli;
    ArrayList<Double> energi;
    Map<Integer, ArrayList<Double>> energiNode;
    Map<Integer, ArrayList<Double>> turunan1Node;
    Map<Integer, ArrayList<Double>> turunan2Node;

    public SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom(Settings s) {
        s = new Settings(SPRAYANDWAIT_NS);
        initialNrofCopies = s.getInt(NROF_COPIES);

        if (isBinary) {
            isBinary = s.getBoolean(BINARY_MODE);
        }
    }

    protected SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom(SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom prototype) {
        this.initialNrofCopies = prototype.initialNrofCopies;
        this.isBinary = prototype.isBinary;
        turunan1 = new ArrayList<Double>();
        turunan2 = new ArrayList<Double>();
        normalisasi1 = new ArrayList<Double>();
        normalisasi2 = new ArrayList<Double>();
        asli = new ArrayList<Double>();
        energi = new ArrayList<Double>();
        energiNode = new HashMap<>();
        turunan1Node = new HashMap<>();
        turunan2Node = new HashMap<>();
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
        DTNHost otherHost = m.getFrom();
        SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom de = getOtherDecisionEngine(thisHost);

//        //selfish dari 70% EDIT
//        if (thisHost.getAddress() == 0 || thisHost.getAddress() == 23 || thisHost.getAddress() == 39) {
//            if (getEnergy(thisHost) > getInitialEnergy(thisHost) * 50 / 100
//                    && getEnergy(thisHost) <= getInitialEnergy(thisHost) * 70 / 100) {
//                if (getEnergy(thisHost) > getEnergy(otherHost)) {
//                    if (this.getTurunan1().size() - 1 < de.getTurunan1().size() - 1
//                            && this.getTurunan2().size() - 1 < de.getTurunan2().size() - 1) {
//                        return m.getTo() != thisHost;
//                    }
//                }
//            } else if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 50 / 100) {//cek baterai kita berapa
//                return false;
//            } else {
//                return m.getTo() != thisHost;
//            }

        //selfish dari 50%
        if (thisHost.getAddress() == 0 || thisHost.getAddress() == 23 || thisHost.getAddress() == 39) {
            if (getEnergy(thisHost) <= (getInitialEnergy(thisHost) * 50 / 100)) {
                if (getEnergy(thisHost) > getEnergy(otherHost)) {
                    if (this.getTurunan1().size() - 1 < de.getTurunan1().size() - 1
                            && this.getTurunan2().size() - 1 < de.getTurunan2().size() - 1) {
                        return m.getTo() != thisHost;
                    }
                }
            }
        }
//        //selfish dari 50%
//        if (otherHost.getAddress() == 0 || otherHost.getAddress() == 23 || otherHost.getAddress() == 39) {
//            if (getEnergy(otherHost) <= getInitialEnergy(otherHost) * 50 / 100) {//cek baterai kita berapa
//                if (getEnergy(thisHost) > getEnergy(otherHost)) {
//                    if (this.getTurunan1().size() - 1 < de.getTurunan1().size() - 1
//                            && this.getTurunan2().size() - 1 < de.getTurunan2().size() - 1) {
//                        return true;
//                    }
//                }
//            } else {
//                return m.getTo() != thisHost;
//            }

        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        DTNHost thisHost = m.getFrom();
        SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom de = getOtherDecisionEngine(otherHost);

//        //selfish dari 70% EDIT 
//        if (thisHost.getAddress() == 0 || thisHost.getAddress() == 23 || thisHost.getAddress() == 39) {
//            if (this.getEnergy(thisHost) > (this.getInitialEnergy(thisHost) * 50 / 100)
//                    && this.getEnergy(thisHost) <= (this.getInitialEnergy(thisHost) * 70 / 100)) {
//                if (m.getTo() == otherHost) {
//                    return true;
//                } else if (this.getEnergy(thisHost) < de.getEnergy(otherHost)) {
//                    if (nrofCopies > 1) {
//                        if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
//                                && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {
//                            return true;
//                        }
//                    }
//                }
//            } else if (this.getEnergy(thisHost) <= (this.getInitialEnergy(thisHost) * 50 / 100)) {
//                if (m.getTo() == otherHost) {
//                    return true;
//                }
//            } else {
//                if (m.getTo() == otherHost) {
//                    return true;
//                }
//                if (nrofCopies > 1) {
//                    return true;
//                }
//            }
//        } else if (otherHost.getAddress() == 0 || otherHost.getAddress() == 23 || otherHost.getAddress() == 39) {
//            if (de.getEnergy(otherHost) > (de.getInitialEnergy(otherHost) * 50 / 100)
//                    && de.getEnergy(otherHost) <= (de.getInitialEnergy(otherHost) * 70 / 100)) {
//                if (m.getTo() == otherHost) {
//                    return true;
//                } else if (this.getEnergy(thisHost) < de.getEnergy(otherHost)) {
//                    if (nrofCopies > 1) {
//                        if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
//                                && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {
//                            return true;
//                        }
//                    }
//                }
//            } else if (de.getEnergy(otherHost) <= (de.getInitialEnergy(otherHost) * 50 / 100)) {
//                if (m.getTo() == otherHost) {
//                    return true;
//                }
//            } else {
//                if (m.getTo() == otherHost) {
//                    return true;
//                }
//                if (nrofCopies > 1) {
//                    return true;
//                }
//            }
//        } else {
//            if (m.getTo() == otherHost) {
//                return true;
//            }
//            if (nrofCopies > 1) {
//                return true;
//            }
//        }
        
        //selfish dari 50%
        if (thisHost.getAddress() == 0 || thisHost.getAddress() == 23 || thisHost.getAddress() == 39) {
            if (this.getEnergy(thisHost) <= (this.getInitialEnergy(thisHost) * 50 / 100)) {
                if (m.getTo() == otherHost) {
                    return true;
                } else if (this.getEnergy(thisHost) < de.getEnergy(otherHost)) {
                    if (nrofCopies > 1) {
                        if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
                                && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {
                            return true;
                        }
                    }
                }
            } else {
                if (m.getTo() == otherHost) {
                    return true;
                }
                if (nrofCopies > 1) {
                    return true;
                }
            }
        } else if (otherHost.getAddress() == 0 || otherHost.getAddress() == 23 || otherHost.getAddress() == 39) {
            if (de.getEnergy(otherHost) <= (de.getInitialEnergy(otherHost) * 50 / 100)) {
                if (m.getTo() == otherHost) {
                    return true;
                } else if (this.getEnergy(thisHost) < de.getEnergy(otherHost)) {
                    if (nrofCopies > 1) {
                        if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
                                && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {
                            return true;
                        }
                    }
                }
            } else {
                if (m.getTo() == otherHost) {
                    return true;
                }
                if (nrofCopies > 1) {
                    return true;
                }
            }
        } else {
            if (m.getTo() == otherHost) {
                return true;
            }
            if (nrofCopies > 1) {
                return true;
            }
        }

//        //selfish dari 50%
//        if (otherHost.getAddress() == 0 || otherHost.getAddress() == 23 || otherHost.getAddress() == 39) {
//            if (getEnergy(otherHost) <= getInitialEnergy(otherHost) * 50 / 100) {//cek baterai kurang dari 50% nggak
//                if (m.getTo() == otherHost) {//kalau dia tujuannya
//                    return true;//kirim aja
//                } else if (getEnergy(thisHost) < getEnergy(otherHost)) {//cek dulu energi dia sama kita
//                    if (nrofCopies > 1) {
//                        if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
//                                && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {//liat dari asli, turunan1 sama 2
//                            return true;
//                        }
//                    }
//                }
//            } else {//kalau lebih dari 50%
//                if (m.getTo() == otherHost) {//kalau dia tujuannya
//                    return true;//kirim aja
//                }
//                if (nrofCopies > 1) {
//                    return true;
//                }
//            }
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
        return new SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom(this);
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
//            List<Double> dummyList;
//            if (!apa.containsKey(h.getAddress())) {
//                dummyList = new LinkedList<>();
//            } else {
//                dummyList = apa.get(h.getAddress());
//                if (dummyList.size() == 5) {
//                    dummyList.remove(0);
//                }
//            }
//            dummyList.add(getEnergy(h));
//            apa.put(h.getAddress(), dummyList);
//            turunan1.add(this.hitTurunanPertama(h));
//            turunan2.add(this.hitTurunanKedua(h));
//            energi.add(getEnergy(h));       
//            if (energi.size() < 10) {
            energi.add(getEnergy(h));
//            asli.add(this.hitAsli(h));
            turunan1.add(this.hitTurunanPertama(h));
            turunan2.add(this.hitTurunanKedua(h));
//            } else {
//                energi.remove(0);
//                turunan1.remove(0);
//                turunan2.remove(0);
//                energi.add(getEnergy(h));
//                turunan1.add(this.hitTurunanPertama(h));
//                turunan2.add(this.hitTurunanKedua(h));
//                normalisasi1.add(hitNormalisasi1(h));
//                normalisasi1.add(hitNormalisasi2(h));
//            }
//            if (simTime - lastRecord2 >= interval2) {
            energiNode.put(h.getAddress(), energi);
            turunan1Node.put(h.getAddress(), turunan1);
            turunan2Node.put(h.getAddress(), turunan2);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    private Double hitAsli(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double energy = getEnergy(h);
        Double eMax = getInitialEnergy(h);
        Double cariTau = energy / eMax; //mau liat nilai e nya itu eksponensial apa linier
        return cariTau;
//        Double pangkat = (Math.log(epsilon) * energy) / eMax;
//        Double aslinya = K * Math.exp(pangkat);
//        return aslinya;
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

    private Double hitNormalisasi1(DTNHost h) {
        Double min = getTurunan1().get(turunan1.size() - 1);
        Double max = getTurunan1().get(0);
        Double normalisasi = 0.0;
        for (Double norm : turunan1) {
            normalisasi = (norm - min) / (max - min);
        }
        return normalisasi;
    }

    private Double hitNormalisasi2(DTNHost h) {
        Double min = getTurunan2().get(turunan2.size() - 1);
        Double max = getTurunan2().get(0);
        Double normalisasi = 0.0;
        for (Double norm : turunan2) {
            normalisasi = (norm - min) / (max - min);
        }
        return normalisasi;
    }

    @Override
    public ArrayList<Double> getEnergy() {
        return this.energi;
    }

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

//    @Override
//    public ArrayList<Double> getNormalisasi1() {
//        return this.normalisasi1;
//    }
//
//    @Override
//    public ArrayList<Double> getNormalisasi2() {
//        return this.normalisasi2;
//    }

    @Override
    public Map<Integer, ArrayList<Double>> getEnergi() {
        return this.energiNode;
    }

    @Override
    public Map<Integer, ArrayList<Double>> getT1() {
        return this.turunan1Node;
    }

    @Override
    public Map<Integer, ArrayList<Double>> getT2() {
        return this.turunan2Node;
    }

    private SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SprayAndWaitRouterEASkripsiYovaSelfishHubNodeRandom) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}
