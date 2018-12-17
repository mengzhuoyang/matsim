package org.matsim.contrib.spatialDrt.run;


import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.drt.data.validator.DefaultDrtRequestValidator;
import org.matsim.contrib.drt.data.validator.DrtRequestValidator;
import org.matsim.contrib.drt.optimizer.depot.DepotFinder;
import org.matsim.contrib.drt.optimizer.depot.NearestStartLinkAsDepot;
import org.matsim.contrib.drt.optimizer.insertion.InsertionCostCalculator;
import org.matsim.contrib.drt.optimizer.rebalancing.NoRebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.drt.routing.DefaultAccessEgressStopFinder;
import org.matsim.contrib.drt.routing.DrtMainModeIdentifier;
import org.matsim.contrib.drt.routing.StopBasedDrtRoutingModule;
import org.matsim.contrib.drt.run.Drt;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.contrib.spatialDrt.bayInfrastructure.BayManager;
import org.matsim.contrib.spatialDrt.dwelling.ClearNetworkChangeEvents;
import org.matsim.contrib.spatialDrt.dwelling.DrtAndTransitStopHandlerFactory;
import org.matsim.contrib.spatialDrt.eav.ChargerManager;
import org.matsim.contrib.spatialDrt.eav.DischargingRate;
import org.matsim.contrib.spatialDrt.eav.SimpleChargerManager;
import org.matsim.contrib.spatialDrt.parkingStrategy.DefaultDrtOptimizer;
import org.matsim.contrib.spatialDrt.routingModule.DrtRoutingModule;
import org.matsim.contrib.spatialDrt.scheduler.ModifyLanes;
import org.matsim.contrib.spatialDrt.vehicle.FleetProvider;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.pt.TransitStopHandlerFactory;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;


public final class
DrtModule extends AbstractModule {
	@Inject
	DrtConfigGroup drtCfg;
	@Inject
	AtodConfigGroup atodCfg;

	@Override
	public void install() {
		//bind(VehicleType.class).annotatedWith(Names.named(VrpAgentSource.DVRP_VEHICLE_TYPE)).to(DynVehicleType.class);
		String mode = drtCfg.getMode();
		install(FleetProvider.createModule(drtCfg.getMode(),drtCfg.getVehiclesFileUrl( getConfig().getContext())));
		bind(Fleet.class).annotatedWith(Drt.class).to(Key.get(Fleet.class, Names.named(mode))).asEagerSingleton();

		bind(DrtRequestValidator.class).to(DefaultDrtRequestValidator.class);
		bind(DepotFinder.class).to(NearestStartLinkAsDepot.class);
		bind(BayManager.class).asEagerSingleton();

		bind(RebalancingStrategy.class).to(NoRebalancingStrategy.class);
		bind(TravelDisutilityFactory.class).annotatedWith(Names.named(DefaultDrtOptimizer.DRT_OPTIMIZER))
				.toInstance(TimeAsTravelDisutility::new);
		bind(TransitStopHandlerFactory.class ).to( DrtAndTransitStopHandlerFactory.class );
		bind(InsertionCostCalculator.PenaltyCalculator.class).to(drtCfg.isRequestRejection() ?
				InsertionCostCalculator.RejectSoftConstraintViolations.class :
				InsertionCostCalculator.DiscourageSoftConstraintViolations.class).asEagerSingleton();
		addControlerListenerBinding().to(ClearNetworkChangeEvents.class).asEagerSingleton();
		addControlerListenerBinding().to(BayManager.class).asEagerSingleton();

		if (atodCfg.isEAV()) {
			this.bind(DischargingRate.class).asEagerSingleton();
			this.bind(ChargerManager.class).to(SimpleChargerManager.class).asEagerSingleton();
		}


		switch (drtCfg.getOperationalScheme()) {
			case door2door:
				addRoutingModuleBinding(TransportMode.drt).to(DrtRoutingModule.class);
				break;

			case stopbased:
				final Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
				new TransitScheduleReader(scenario2).readFile(
						drtCfg.getTransitStopsFileUrl(getConfig().getContext()).getFile());
				bind(TransitSchedule.class).annotatedWith(Names.named(TransportMode.drt))
						.toInstance(scenario2.getTransitSchedule());
				bind(MainModeIdentifier.class).to(DrtMainModeIdentifier.class).asEagerSingleton();
				bind(DrtRoutingModule.class);
				addRoutingModuleBinding(TransportMode.drt).to(StopBasedDrtRoutingModule.class);
				bind(StopBasedDrtRoutingModule.AccessEgressStopFinder.class).to(DefaultAccessEgressStopFinder.class).asEagerSingleton();
				break;

			default:
				throw new IllegalStateException();
		}
	}
}