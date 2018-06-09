package org.matsim.contrib.carsharing.bikeshare;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;


public class BikeshareDepartureHandler implements DepartureHandler {

	@Inject
	BikeshareFleet bikeFleet;

	@Inject
	Network network;

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {
		if (agent instanceof PlanAgent) {
			if (agent.getMode().startsWith("access_walk_bike")) {
				// Plan plan = WithinDayAgentUtils.getModifiablePlan( agent ) ;
				Link link = network.getLinks().get(linkId);
				Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				final Leg accessLeg = (Leg) plan.getPlanElements().get(planElementsIndex);
				final Leg leg = (Leg) plan.getPlanElements().get(planElementsIndex + 1);
				final Leg egressLeg = (Leg) plan.getPlanElements().get(planElementsIndex + 2);
				Id<Vehicle> bikeId = bikeFleet.getAndRemoveClosest(link.getCoord(), agent.getId());
				if (bikeId == null)
					agent.setStateToAbort(now);
				Coord bikeCoord = this.bikeFleet.getBikeCoordMap().get(bikeId);
				//TODO: implement proper travel time estimates
				double accessTime = CoordUtils.calcEuclideanDistance(link.getCoord(), bikeCoord) * 1.3 / 0.833;
				
				accessLeg.setTravelTime(accessTime);
				accessLeg.getRoute().setTravelTime(accessTime);
				
				double travelTime = CoordUtils.calcEuclideanDistance(bikeCoord, network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord()) * 1.3 / (14.0 / 3.6);
				leg.setTravelTime(travelTime);
				leg.getRoute().setTravelTime(travelTime);
				egressLeg.setTravelTime(0.0);
				egressLeg.getRoute().setTravelTime(0.0);
				
			}

		}

		return false;
	}

}