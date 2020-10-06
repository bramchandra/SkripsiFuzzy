package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import report.estimasi;

/**
 * @author yova
 */
public class EpidemicRouterEASkripsiSelfish implements RoutingDecisionEngine, estimasi {

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
    ArrayList<Integer> mati;
    Map<Integer, ArrayList<Double>> energiNode;
    Map<Integer, ArrayList<Double>> turunan1Node;
    Map<Integer, ArrayList<Double>> turunan2Node;
    Map<Double, ArrayList<Integer>> nodeMati;

    public EpidemicRouterEASkripsiSelfish(Settings s) {
    }

    protected EpidemicRouterEASkripsiSelfish(EpidemicRouterEASkripsiSelfish prototype) {
        turunan1 = new ArrayList<Double>();
        turunan2 = new ArrayList<Double>();
        normalisasi1 = new ArrayList<Double>();
        normalisasi2 = new ArrayList<Double>();
        asli = new ArrayList<Double>();
        energi = new ArrayList<Double>();
        mati = new ArrayList<Integer>();
        energiNode = new HashMap<>();
        turunan1Node = new HashMap<>();
        turunan2Node = new HashMap<>();
        nodeMati = new HashMap<>();
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
        Collection<Message> messageCollection = thisHost.getMessageCollection();
        DTNHost otherHost = m.getFrom();
        EpidemicRouterEASkripsiSelfish de = getOtherDecisionEngine(thisHost);

        for (Message message : messageCollection) {
            if (m.getId().equals(message.getId())) {
                return false;
            }
        }
        
        //selfish dari 70%
        if(getEnergy(thisHost) <= getInitialEnergy(thisHost) * 70 / 100) {
            if (m.getTo() == thisHost) {//cek kita tujuannya apa bukan
                return true;
            }
        }
        if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 50 / 100) {//cek baterai kita berapa
            if (m.getTo() == thisHost) {//cek kita tujuannya apa bukan
                return true;
            }
            if (getEnergy(thisHost) > getEnergy(otherHost)) {
                if (this.getTurunan1().size() - 1 < de.getTurunan1().size() - 1
                        && this.getTurunan2().size() - 1 < de.getTurunan2().size() - 1) {
                    return true;
                }
            }
        } else {
            return true;
        }

//        //selfish dari 50%
//        if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 50 / 100) {//cek baterai kita berapa
//            if (m.getTo() == thisHost) {//cek kita tujuannya apa bukan
//                return true;
//            } else if (getEnergy(thisHost) > getEnergy(otherHost)) {
//                if (this.getTurunan1().size() - 1 < de.getTurunan1().size() - 1
//                        && this.getTurunan2().size() - 1 < de.getTurunan2().size() - 1) {
//                    return true;
//                }
//            }
//        } else {
//            return true;
//        }
        
        return false;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        DTNHost thisHost = m.getFrom();
        EpidemicRouterEASkripsiSelfish de = getOtherDecisionEngine(otherHost);

        if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 70 / 100) {//cek baterai kurang dari 70% nggak
            if (m.getTo() == otherHost) {//kalau dia tujuannya
                return true;//kirim aja
            }
        } else if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 50 / 100) {//kalau lebih 
            if (m.getTo() == otherHost) {//kalau dia tujuannya
                return true;//kirim aja
            }
            if (getEnergy(thisHost) < getEnergy(otherHost)) {//cek dulu energi dia sama kita
                if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
                        && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {//liat dari asli, turunan1 sama 2
                    return true;
                }
            }
        } else {
            return true;
        }

        if (getEnergy(thisHost) <= getInitialEnergy(thisHost) * 50 / 100) {//cek baterai kurang dari 50% nggak
            if (m.getTo() == otherHost) {//kalau dia tujuannya
                return true;//kirim aja
            } else {//kalau lebih dari 50%
                if (getEnergy(thisHost) < getEnergy(otherHost)) {//cek dulu energi dia sama kita
                    if (this.getTurunan1().size() - 1 > de.getTurunan1().size() - 1
                            && this.getTurunan2().size() - 1 > de.getTurunan2().size() - 1) {//liat dari asli, turunan1 sama 2
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
        
        return false;
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
        return new routing.EpidemicRouterEASkripsiSelfish(this);
    }

    private Double getEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }

    public boolean getHasEnergy(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return ((DecisionEngineRouter) otherRouter).hasEnergy();//ambil method di decision
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
                normalisasi1.add(hitNormalisasi1(h));
//                normalisasi1.add(hitNormalisasi2(h));
//            }
//            if (simTime - lastRecord2 >= interval2) {
            energiNode.put(h.getAddress(), energi);
            turunan1Node.put(h.getAddress(), turunan1);
            turunan2Node.put(h.getAddress(), turunan2);
            if (getHasEnergy(h) == false) {
//                System.out.println(mati.add(lastRecord));
                mati.add(h.getAddress());
                nodeMati.put(lastRecord, mati);
            }
//            this.lastRecord = simTime - simTime % interval;
//            }
//            this.lastRecord2 = simTime - simTime % interval2;
        }
        this.lastRecord = simTime - simTime % interval;
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
    public ArrayList<Integer> getMati() {
        return this.mati;
    }

    @Override
    public Map<Double, ArrayList<Integer>> getNodeMati() {
        return this.nodeMati;
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

    @Override
    public ArrayList<Double> getNormalisasi1() {
        return this.normalisasi1;
    }

    @Override
    public ArrayList<Double> getNormalisasi2() {
        return this.normalisasi2;
    }

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

    @Override
    public ArrayList<Double> getEnergy() {
        return this.energi;
    }

    private EpidemicRouterEASkripsiSelfish getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicRouterEASkripsiSelfish) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}
