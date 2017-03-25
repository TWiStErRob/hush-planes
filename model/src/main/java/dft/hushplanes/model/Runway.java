package dft.hushplanes.model;

/**
 * Created by Jenny on 25/03/2017.
 */

public enum Runway {
    L27 (51.464909,-0.470121, "27L"),
    R27 (51.477612,-0.470121, "27R"),
    L09 (51.477627,-0.452, "09L"),
    R09 (51.464931,-0.446962, "09R");

    public double latitude;
    public double longitude;
    public String name;

    Runway(double lat, double longi, String name) {
        latitude = lat;
        longitude = longi;
        this.name = name;
    }
}