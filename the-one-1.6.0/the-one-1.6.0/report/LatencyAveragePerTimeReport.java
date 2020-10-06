package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yovac
 */
public class LatencyAveragePerTimeReport extends Report implements MessageListener, UpdateListener {

    private Map<Double, String> latencies;
    private Map<String, Double> creationTime;
    private List<Double> lat;
    public int interval = 300;
    public double lastRecord = Double.MIN_VALUE;

    public LatencyAveragePerTimeReport() {
        super();
        latencies = new HashMap<>();
        creationTime = new HashMap<>();
        lat = new ArrayList<Double>();
    }

    @Override
    public void newMessage(Message m) {
        this.creationTime.put(m.getId(), getSimTime());
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {

    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (finalTarget) {
            double latenciesValue = getSimTime() - this.creationTime.get(m.getId());
            this.lat.add(latenciesValue);
        }
    }
    
    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
//            getAverage(this.lat);
            this.lastRecord = simTime - simTime % interval;
            latencies.put(lastRecord, getAverage(this.lat));
        }
    }

    @Override
    public void done() {
        String printLn = "Time\tLatencyAverage\n";
        for (Map.Entry<Double, String> entry : latencies.entrySet()) {
            Double key = entry.getKey();
            String value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);
        super.done();
    }
}
