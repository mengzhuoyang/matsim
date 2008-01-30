/* *********************************************************************** *
 * project: org.matsim.*
 * SetVisDataListener.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.controler.corelisteners;

import java.util.ArrayList;
import java.util.HashMap;


import org.matsim.controler.Controler;
import org.matsim.controler.events.BeforeMobsimEvent;
import org.matsim.controler.listener.BeforeMobsimListener;
import org.matsim.network.Node;
import org.matsim.plans.Act;
import org.matsim.plans.Leg;
import org.matsim.plans.Person;
import org.matsim.plans.Plan;
import org.matsim.plans.Plans;
import org.matsim.utils.identifiers.IdI;

public class SetVisualizerData implements BeforeMobsimListener {

	
	private static HashMap<IdI,String> destNodeMapping = new HashMap<IdI,String>();
	private static int mappings = 0;
	
	private final String actType = "h";
	
	
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		
		Controler controler = event.getControler();
		controler.stopwatch.beginOperation("set visualizer data for all person");
		Plans plans = controler.getPopulation();

		for (IdI pid : plans.getPersons().keySet()) {
			Person p = plans.getPerson(pid);
		
			for (int i=p.getPlans().size()-1; i>=0; i--) {
				Plan plan = p.getPlans().get(i);
			
				ArrayList<Object> actLegs = plan.getActsLegs();
				
				for (int j = 1; j < actLegs.size(); j+=2){
					Leg leg = (Leg) actLegs.get(j);
					Act act = (Act) actLegs.get(j+1);
					if (this.actType.equals(act.getType())){
						Node node = leg.getRoute().getRoute().get(leg.getRoute().getRoute().size()-2);
						IdI destNode = node.getId();
						p.setVisulizerData(getVisualizerData(destNode));
						break;
					}
				}
			
			}
		}
		controler.stopwatch.endOperation("set visualizer data for all person");
	}

	private String getVisualizerData(IdI id) {
		if (!destNodeMapping.containsKey(id)){
			addMapping(id);
		}
		return destNodeMapping.get(id);
	}

	private synchronized void addMapping(IdI id) {
		destNodeMapping.put(id, ""+mappings++);		
	}

}
