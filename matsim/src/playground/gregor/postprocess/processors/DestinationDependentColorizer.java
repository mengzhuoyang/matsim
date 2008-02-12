/* *********************************************************************** *
 * project: org.matsim.*
 * DestinationDependentColorizer.java
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

package playground.gregor.postprocess.processors;

import java.util.HashMap;

import org.matsim.basic.v01.Id;
import org.matsim.plans.Leg;
import org.matsim.plans.Plans;
import org.matsim.utils.identifiers.IdI;

public class DestinationDependentColorizer implements PostProcessorI {

	private final static int NUM_OF_COLOR_SLOTS = 256;
	
	private static HashMap<IdI,String> destNodeMapping = new HashMap<IdI,String>();

	private Plans plans;
	
	public DestinationDependentColorizer(Plans plans){
		this.plans = plans;
	}
	
	public String[] processEvent(String[] event) {
		IdI id = new Id(event[0]);
		String color = getColor(id);
		event[15] = color;
		return event;
	}

	private String getColor(IdI id) {
		if(!destNodeMapping.containsKey(id)){
			addMapping(id);
		}
		return destNodeMapping.get(id);
	}

	private synchronized void addMapping(IdI id) {
		Leg leg = ((Leg)this.plans.getPerson(id).getSelectedPlan().getActsLegs().get(1)); 
		IdI nodeId = leg.getRoute().getRoute().get(leg.getRoute().getRoute().size()-2).getId();
		int mapping = Integer.parseInt(nodeId.toString()) % NUM_OF_COLOR_SLOTS; 
		destNodeMapping.put(id,  Integer.toString(mapping));
	}
}
