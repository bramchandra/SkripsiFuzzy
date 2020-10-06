package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import java.util.Collection;

/**
 * @author yova
 */
public class EpidemicRouterEA implements RoutingDecisionEngine {

    public EpidemicRouterEA(Settings s) {
    }

    protected EpidemicRouterEA(EpidemicRouterEA prototype) {
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
        for (Message message : messageCollection) {
            if (m.getId().equals(message.getId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        return true;
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
        return new routing.EpidemicRouterEA(this);
    }

    private EpidemicRouterEA getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicRouterEA) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
    }
}
