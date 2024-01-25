package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;


/**
 * This is a class that will help construct the flightpath file. It has all the attributes needed in the Json file,
 * a constructor function and .get functions for all of these attributes
 */
public class FlightPathForFiles {
    String orderNo;
    double fromLongitude;
    double fromLatitude;
    double angle;
    double toLongitude;
    double toLatitude;

    /**
     * This is the constructor function for the class
     * @param orderNum is the order number
     * @param fromLon is the longitude of the starting point
     * @param fromLat is the latitude of the starting point
     * @param ang is the angle of the drone
     * @param toLon is the longitude of the end point
     * @param toLat is the latitude of the end point
     */
    public FlightPathForFiles(String orderNum, double fromLon, double fromLat , double ang, double toLon, double toLat) {
        orderNo = orderNum;
        fromLongitude = fromLon;
        fromLatitude = fromLat;
        angle = ang;
        toLongitude = toLon;
        toLatitude = toLat;
    }

    /**
     * This is a helper function that returns the order number
     * @return String returns the order number
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * This is a helper function that returns the longitude of the starting point
     * @return double returns the longitude of the starting point
     */
    public double getFromLongitude() {
        return fromLongitude;
    }

    /**
     * This is a helper function that returns the latitude of the starting point
     * @return double returns the latitude of the starting point
     */
    public double getFromLatitude() {
        return fromLatitude;
    }

    /**
     * This is a helper function that returns the angle of the drone
     * @return double returns the angle of the drone
     */
    public double getAngle() {
        return angle;
    }

    /**
     * This is a helper function that returns the longitude of the end point
     * @return double returns the longitude of the end point
     */
    public double getToLongitude() {
        return toLongitude;
    }

    /**
     * This is a helper function that returns the latitude of the end point
     * @return double returns the latitude of the end point
     */
    public double getToLatitude() {
        return toLatitude;
    }
}
