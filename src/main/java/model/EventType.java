package model;

import framework.IEventType;

/**
 * Event types are defined by the requirements of the simulation model
 */
public enum EventType implements IEventType {
	/* a new customer has arrived to the airport */
	ARRIVAL,
	/* customer is departing check-in */
	DEP_CHECKIN,
	/* customer is departing security check */
	DEP_SECURITY,
	/* customer is departing border control */
	DEP_BORDERCTRL,
	/* customer is departing boarding */
	DEP_BOARDING
}
