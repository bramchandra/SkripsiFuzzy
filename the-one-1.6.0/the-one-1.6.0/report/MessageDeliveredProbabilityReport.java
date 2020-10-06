package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Yovac
 */
public class MessageDeliveredProbabilityReport extends Report implements MessageListener {

    private Map<String, Double> creationTimes;
    private Map<DTNHost, Double> nrofDeliver;
    private Map<DTNHost, Double> nrofDeliverProbability;

    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    public MessageDeliveredProbabilityReport() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.nrofDeliver = new HashMap<>();
        this.nrofDeliverProbability = new HashMap<>();
        this.nrofDropped = 0;
        this.nrofRemoved = 0;
        this.nrofStarted = 0;
        this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }

        if (dropped) {
            this.nrofDropped++;
        } else {
            this.nrofRemoved++;
        }

    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofAborted++;
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to,
            boolean finalTarget) {
        Double deliveryProb = 0.0; // delivery probability
        Double overHead = Double.NaN;

        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofRelayed++;
        if (finalTarget) {
            this.nrofDelivered++;
            if (this.nrofCreated > 0) {
                deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
                nrofDeliverProbability.put(to, deliveryProb);
            }
        }
    }

    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }

        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        if (m.getResponseSize() > 0) {
            this.nrofResponseReqCreated++;
        }
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofStarted++;
    }

    @Override
    public void done() {
        
        for (Map.Entry<DTNHost, Double> entry : nrofDeliverProbability.entrySet()) {
            write("Node " + entry.getKey() + "\t\t " + entry.getValue());
        }
        super.done();
    }

}
