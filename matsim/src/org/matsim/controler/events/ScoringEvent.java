/* *********************************************************************** *
 * project: org.matsim.*
 * ControlerScoringEvent.java.java
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

package org.matsim.controler.events;

import org.matsim.controler.Controler;

/**
 * Event class to notify observers that scoring should happen
 *
 * @author mrieser
 */
public class ScoringEvent extends ControlerEvent {

	/**
	 * The iteration number
	 */
	private final int iteration;

	public ScoringEvent(final Controler controler, final int iteration) {
		super(controler);
		this.iteration = iteration;
	}

	/**
	 * @return the number of the current iteration
	 */
	public int getIteration() {
		return this.iteration;
	}

}
