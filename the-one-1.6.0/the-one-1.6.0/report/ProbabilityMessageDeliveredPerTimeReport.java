package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yova
 */
public class ProbabilityMessageDeliveredPerTimeReport extends Report implements MessageListener, UpdateListener{
    private int created;
    private int delivered;
    public double lastRecord = Double.MIN_VALUE;
    public int interval = 300;
    public Map<Double, Double> persen;
    public Map<Double, Double> probability;

    public ProbabilityMessageDeliveredPerTimeReport() {
        super();
        created = 0;
        delivered = 0;
        persen = new HashMap<>();
        probability = new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {
//        if (isWarmup()) {
//            addWarmupID(m.getId());
//            return;
//        }
        created++;
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {}

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (firstDelivery) {
            delivered++;
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
//            double percentage = ((1.0 * delivered) / created)*100;
            double probabilitynya = ((1.0 * this.delivered) / this.created);
           
            this.lastRecord = simTime - simTime % interval;
//            persen.put(lastRecord, percentage);
            probability.put(lastRecord, probabilitynya);
        }
    }
   
    @Override
    public void done(){
        String printLn = "Time\tProbabilityMessageDelivered\n";
        for (Map.Entry<Double, Double> entry : probability.entrySet()) {
            Double key = entry.getKey();
            Double value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);
        super.done();
    }
}
