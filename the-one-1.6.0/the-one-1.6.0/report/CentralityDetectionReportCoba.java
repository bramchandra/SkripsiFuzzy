/*
 * @(#)CommunityDetectionReport.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package report;

import routing.DecisionEngineRouter;
import java.util.*;

import core.*;
import routing.*;
import routing.community.CentralityDetectionEngine;

/**
 * <p>Reports the local communities at each node whenever the done() method is 
 * called. Only those nodes whose router is a DecisionEngineRouter and whose
 * RoutingDecisionEngine implements the 
 * routing.community.CommunityDetectionEngine are reported. In this way, the
 * report is able to output the result of any of the community detection
 * algorithms.</p>
 * 
 * @author PJ Dillon, University of Pittsburgh
 */
public class CentralityDetectionReportCoba extends Report
{
	public CentralityDetectionReportCoba()
	{
		init();
	}

	@Override
	public void done()
	{
		List<DTNHost> nodes = SimScenario.getInstance().getHosts();
		List<Set<DTNHost>> centrality = new LinkedList<Set<DTNHost>>();
		
		for(DTNHost h : nodes)
		{
			MessageRouter r = h.getRouter();
			if(!(r instanceof DecisionEngineRouter) )
				continue;
			RoutingDecisionEngine de = ((DecisionEngineRouter)r).getDecisionEngine();
			if(!(de instanceof CentralityDetectionEngine))
				continue;
			CentralityDetectionEngine cd = (CentralityDetectionEngine)de;
			
			boolean alreadyHaveCommunity = false;
			Set<DTNHost> nodeCent = cd.getLocalDegreeCentrality();
			
			// Test to see if another node already reported this community
			for(Set<DTNHost> c : centrality)
			{
				if(c.containsAll(nodeCent) && nodeCent.containsAll(c))
				{
					alreadyHaveCommunity = true;
				}	
			}
			
			if(!alreadyHaveCommunity && nodeCent.size() > 0)
			{
				centrality.add(nodeCent);
			}
		}
		
		// print each community and its size out to the file
		for(Set<DTNHost> c : centrality)
			write("" + c.size() + ' ' + c);
		
		super.done();
	}

	
}
