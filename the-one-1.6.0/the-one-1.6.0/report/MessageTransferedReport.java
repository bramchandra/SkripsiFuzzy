package report;

import core.*;
import java.util.*;

public class MessageTransferedReport extends Report implements MessageListener {

    private final Map<DTNHost, Integer> y;

    public MessageTransferedReport() {
        init();
        this.y = new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {
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
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (firstDelivery) {
            if (y.containsKey(to)) {
                y.put(to, y.get(to) + 1);
            } else {
                y.put(to, 1);
            }
        }
    }

    @Override
    public void done() {
        for (Map.Entry<DTNHost, Integer> entry : y.entrySet()) {
            write(entry.getKey() + " \t" + entry.getValue());
        }
        super.done();
    }
}
