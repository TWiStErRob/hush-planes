package dft.hushplanes.parser;

import java.util.List;

import javax.annotation.Nullable;

public class AircraftListJsonResponse {
	/**
	 * Identifies the version of the aircraft list. Pass this in the ldv query string parameter on the next fetch.
	 */
	String lastDv;
	/**
	 * The total number of aircraft tracked by the list. This can be larger than the rows in acList if the list has been filtered.
	 */
	int totalAc;
	/**
	 * The source that the feed is working from. 0 = unknown, 1 = receiver, 2 = a fake receiver (only used in testing) and 3 = Flight Simulator X.
	 */
	int src;
	/**
	 * True if the server has a silhouettes folder configured. No longer used.
	 */
	boolean showSil;
	/**
	 * True if the server has an operator flags folder configured. No longer used.
	 */
	boolean showFlg;
	/**
	 * True if the server has a pictures folder configured. No longer used.
	 */
	boolean showPic;
	/**
	 * Pixel width of all flags. No longer used, now configured in JavaScript.
	 */
	int flgW;
	/**
	 * Pixel height of all flags. No longer used, now configured in JavaScript.
	 */
	int flgH;
	/**
	 * The number of seconds of positions that short trails contain.
	 */
	int shtTrlSec;
	/**
	 * The server time at UTC in JavaScript ticks.
	 */
	long stm;
	/**
	 * The aircraft list. See below.
	 */
	List<Ac> acList;
	/**
	 * A list of every feed configured on the server. The feed object is { id: integer, name: string }.
	 */
	List<Feed> feeds;
	/**
	 * The ID of the feed that was used to generate the list.
	 * @see Feed#id
	 */
	int srcFeed;
	/**
	 * True if the server configuration has been changed since the last fetch. 
	 */
	boolean configChanged;

	static class Feed {
		int id;
		String name;
		boolean polarPlot;
	}

	static class Ac {

		/**
		 * The unique identifier of the aircraft.
		 */
		int Id;
		/**
		 * The number of seconds that the aircraft has been tracked for.
		 */
		int TSecs;
		/**
		 * The ID of the feed that last supplied information about the aircraft. Will be different to srcFeed if the source is a merged feed.
		 */
		int Rcvr;
		/**
		 * The ICAO of the aircraft.
		 */
		@Nullable String Icao;
		/**
		 * True if the ICAO is known to be invalid. This information comes from the local BaseStation.sqb database.
		 */
		@Nullable Boolean Bad;
		/**
		 * The registration.
		 */
		@Nullable String Reg;
		/**
		 * The altitude in feet at standard pressure.
		 */
		@Nullable Integer Alt;
		/**
		 * The altitude adjusted for local air pressure, should be roughly the height above mean sea level.
		 */
		@Nullable Integer GAlt;
		/**
		 * The air pressure in inches of mercury that was used to calculate the AMSL altitude from the standard pressure altitude.
		 */
		@Nullable Double InHg;
		/**
		 * The type of altitude transmitted by the aircraft: 0 = standard pressure altitude, 1 = indicated altitude (above mean sea level). Default to standard pressure altitude until told otherwise.
		 */
		@Nullable Integer AltT;
		/**
		 * The target altitude, in feet, set on the autopilot / FMS etc.
		 */
		@Nullable Integer TAlt;
		/**
		 * The callsign.
		 */
		@Nullable String Call;
		/**
		 * True if the callsign may not be correct.
		 */
		@Nullable Boolean CallSus;
		/**
		 * The aircraft's latitude over the ground.
		 */
		@Nullable Double Lat;
		/**
		 * The aircraft's longitude over the ground.
		 */
		@Nullable Double Long;
		/**
		 * The time (at UTC in JavaScript ticks) that the position was last reported by the aircraft.
		 */
		@Nullable Long PosTime;
		/**
		 * True if the latitude and longitude appear to have been calculated by an MLAT server and were not transmitted by the aircraft.
		 */
		@Nullable Boolean Mlat;
		/**
		 * True if the last position update is older than the display timeout value - usually only seen on MLAT aircraft in merged feeds.
		 */
		@Nullable Boolean PosStale;
		/**
		 * True if the last message received for the aircraft was from a TIS-B source.
		 * <b>Warning: from doc, use {@link #Tisb} that's found in data</b>
		 */
		@Nullable Boolean IsTisb;
		/**
		 * True if the last message received for the aircraft was from a TIS-B source.
		 */
		@Nullable Boolean Tisb;
		/**
		 * The ground speed in knots.
		 */
		@Nullable Float Spd;
		/**
		 * The type of speed that Spd represents. Only used with raw feeds. 0/missing = ground speed, 1 = ground speed reversing, 2 = indicated air speed, 3 = true air speed.
		 */
		int SpdTyp;
		/**
		 * Vertical speed in feet per minute.
		 */
		@Nullable Integer Vsi;
		/**
		 * 0 = vertical speed is barometric, 1 = vertical speed is geometric. Default to barometric until told otherwise.
		 */
		@Nullable Integer VsiT;
		/**
		 * Aircraft's track angle across the ground clockwise from 0° north.
		 */
		@Nullable Float Trak;
		/**
		 * True if Trak is the aircraft's heading, false if it's the ground track. Default to ground track until told otherwise.
		 */
		@Nullable Boolean TrkH;
		/**
		 * The track or heading currently set on the aircraft's autopilot or FMS.
		 */
		@Nullable Double TTrk;
		/**
		 * The aircraft model's ICAO type code.
		 */
		@Nullable String Type;
		/**
		 * A description of the aircraft's model. Usually also includes the manufacturer's name.
		 */
		@Nullable String Mdl;
		/**
		 * The manufacturer's name.
		 */
		@Nullable String Man;
		/**
		 * The aircraft's construction or serial number.
		 */
		@Nullable String CNum;
		/**
		 * The code and name of the departure airport.
		 */
		@Nullable String From;
		/**
		 * The code and name of the arrival airport.
		 */
		@Nullable String To;
		/**
		 * An array of strings, each being a stopover on the route.
		 */
		@Nullable String[] Stops;
		/**
		 * The name of the aircraft's operator.
		 */
		@Nullable String Op;
		/**
		 * From Doc, probably {@link #OpIcao}
		 */
		@Nullable String OpCode;
		/**
		 * The operator's ICAO code.
		 */
		@Nullable String OpIcao;
		/**
		 * The squawk as a decimal number (e.g. a squawk of 7654 is passed as 7654, not 4012).
		 */
		@Nullable String Sqk;
		/**
		 * True if the aircraft is transmitting an emergency squawk.
		 */
		@Nullable Boolean Help;
		/**
		 * The distance to the aircraft in kilometres.
		 */
		@Nullable Float Dst;
		/**
		 * The bearing from the browser to the aircraft clockwise from 0° north.
		 */
		@Nullable Float Brng;
		/**
		 * The wake turbulence category of the aircraft - see enums.js for values.
		 */
		@Nullable Integer WTC;
		/**
		 * The number of engines the aircraft has. Usually '1', '2' etc. but can also be a string - see ICAO documentation.
		 */
		@Nullable String Engines;
		/**
		 * The type of engine the aircraft uses - see enums.js for values.
		 */
		@Nullable Integer EngType;
		/**
		 * The placement of engines on the aircraft - see enums.js for values.
		 */
		@Nullable Integer EngMount;
		/**
		 * The species of the aircraft (helicopter, jet etc.) - see enums.js for values.
		 */
		@Nullable Integer Species;
		/**
		 * True if the aircraft appears to be operated by the military.
		 */
		@Nullable Boolean Mil;
		/**
		 * The country that the aircraft is registered to.
		 */
		@Nullable String Cou;
		/**
		 * True if the aircraft has a picture associated with it.
		 */
		@Nullable Boolean HasPic;
		/**
		 * The width of the picture in pixels.
		 */
		@Nullable Integer PicX;
		/**
		 * The height of the picture in pixels.
		 */
		@Nullable Integer PicY;
		/**
		 * The number of Flights records the aircraft has in the database.
		 */
		@Nullable Integer FlightsCount;
		/**
		 * The count of messages received for the aircraft.
		 */
		@Nullable Integer CMsgs;
		/**
		 * True if the aircraft is on the ground.
		 */
		@Nullable Boolean Gnd;
		/**
		 * The user tag found for the aircraft in the BaseStation.sqb local database.
		 */
		@Nullable String Tag;
		/**
		 * True if the aircraft is flagged as interesting in the BaseStation.sqb local database.
		 */
		@Nullable Boolean Interested;
		/**
		 * Trail type - empty for plain trails, 'a' for trails that include altitude, 's' for trails that include speed.
		 */
		@Nullable String TT;
		/**
		 * Transponder type - 0=Unknown, 1=Mode-S, 2=ADS-B (unknown version), 3=ADS-B 1, 4=ADS-B 2.
		 */
		int Trt;
		/**
		 * The year that the aircraft was manufactured.
		 */
		@Nullable String Year;
		/**
		 * True if the aircraft has been seen on a SatCom ACARS feed (e.g. a JAERO feed).
		 */
		@Nullable Boolean Sat;
		// TODO Cos, Cot omitted
		/**
		 * True if the entire trail has been sent and the JavaScript should discard any existing trail history it's built up for the aircraft.
		 */
		@Nullable Boolean ResetTrail;
		/**
		 * True if the aircraft has a signal level associated with it.
		 */
		@Nullable Boolean HasSig;
		/**
		 * The signal level for the last message received from the aircraft, as reported by the receiver. Not all receivers pass signal levels. The value's units are receiver-dependent.
		 */
		@Nullable Integer Sig;

		/**
		 * {@code /Date(unixEpochMillis)/}, not in docs
		 */
		String FSeen;
	}
}
