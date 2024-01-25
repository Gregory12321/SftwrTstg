package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import java.util.*;


/**
 * This is a class called Coordinate that will store the f, g and h values for the A star calculations, as well as
 * the lng lat values of the coordinate and the parent Coordinate. This includes functions for the initializer
 * and some Override functions to ensure all calculations work as expected.
 */
class Coordinate {
    double f, g, h;
    Coordinate parent;
    double lng, lat;

    /**
     * This is the initializer for the Coordinate class
     * @param lng is the longitude of the coordinate
     * @param lat is the latitude of the coordinate
     */
    public Coordinate(double lng, double lat) {
        f = 0;
        g = 0;
        h = 0;
        parent = null;
        this.lng = lng;
        this.lat = lat;
    }

    /**
     * This overrides the usual hasCode function
     * @return  int returns the hashcode of the coordinate
     */
    @Override
    public int hashCode() {
        return Objects.hash(lng, lat);
    }

    /**
     * This overrides the usual equals function
     * @param obj is the object that is being compared to
     * @return boolean returns true if the objects are equal and false if they are not
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        return other.lng == lng && other.lat == lat;
    }
}
/**
 * This is a class that will ensure the priority queue sorts by the f value of the Coordinates
 */
class SortbyF implements Comparator<Coordinate> {
    public int compare(Coordinate a, Coordinate b)
    {
        if (a.f < b.f) return -1;
        if (a.f > b.f) return 1;
        return 0;
    }
}


/**
 * This is a class that will calculate the flight path from appleton tower to the restaurant
 */
public class FlightPath {
    
    static LngLatHandler lngLatHandler = new LngLatHandler();
    //Creates HashSets to store the closedSet and openSet of the Coordinates. This will be used to indicate which
    //have been visited and which have to be visited next. The openSetHash is here as it is easier to see if a value
    //is already in a hashset than in a priority queue so it will speed up the program
    static HashSet<Coordinate> closedSet = new HashSet<>();
    static HashSet<Coordinate> openSetHash = new HashSet<>();
    //The path will be stored as a list of Coordinates
    static List<Coordinate> path = new ArrayList<>();
    //This Prioritises what Coordinate will be visited next depending on the f value
    static PriorityQueue<Coordinate> openSet = new PriorityQueue<>(new SortbyF());
    //These are all the possible angles the drone can move
    double[] angles = {22.5,45,67.5,90,112.5,135,157.5,180,202.5,225,247.5,270,292.5,315,337.5,360};
    //This boolean value will help Figure out if the drone has left and then re entered the central area which is not
    //allowed
    boolean isInCentral = true;

    /**
     * This is a helper function that checks if a point is in any of the nofly zones
     * @param point is the point that is being checked
     * @param noFly is the array of nofly zones
     * @return boolean returns true if the point is in a nofly zone and false if it is not
     */
    private boolean isInNoFly(LngLat point, NamedRegion[] noFly){
        //iterates through each nofly zone and calls isInRegion to see if the point is inside
        for (NamedRegion noFlyZone: noFly) {
            if (lngLatHandler.isInRegion(point,noFlyZone)){
                return true;
            }
        }
        return false;
    }

    /**
     * This is a helper function that checks if the drone has left the central area and then re entered it
     * @param point is the point that is being checked
     * @param centralZone is the central zone area
     * @return boolean returns true if the drone has re entered the central area and false if it has not
     */
    private boolean hasReEnteredCentral(LngLat point, NamedRegion centralZone) {
        if (isInCentral == false && lngLatHandler.isInRegion(point,centralZone)){
            return true;
        }
        return false;
    }

    /**
     * This is a helper function that checks if a coordinate is already on the open set and returns it if it is
     * @param neighbour is the coordinate that is being checked
     * @return Coordinate returns the coordinate if it is on the open set and null if it is not
     */
    private static Coordinate isOnOpenSet(Coordinate neighbour){
        if(openSet.isEmpty()){
            return null;
        }
        //iterates through the coordinates
        Iterator<Coordinate> iterator = openSet.iterator();
        Coordinate find = null;
        while (iterator.hasNext()) {
            //if the given coordinate matches the coordinate in the open set it will return this
            Coordinate next = iterator.next();
            if(next.equals(neighbour)){
                find = next;
                break;
            }
        }
        return find;
    }

    /**
     * This is a helper function that finds the angle between two LngLats
     * @param lngLat is the first LngLat
     * @param lngLat2 is the second LngLat
     * @return double returns the angle between the two LngLats
     */
    public static double findAngle(LngLat lngLat, LngLat lngLat2){
        double angle;
        //If the first lngLat is close to appleton or it is equal to the second then the drone must be in a
        //hovering state so the angle will be 999.0
        if (lngLatHandler.isCloseTo(lngLat, new LngLat(-3.186874, 55.94494)) || lngLat == lngLat2){
            angle = 999.0;
        }
        else{
            //calculates the angle from the coordinates and rounds it to the closest tenth
            angle = Math.toDegrees(Math.atan2((lngLat2.lat() - lngLat.lat()),(lngLat2.lng()- lngLat.lng())));
            angle = Math.round(angle * 10.0) / 10.0;
            if (angle <0){
                angle = angle + 360;
            }
        }
        return angle;
    }

    /**
     * This is the function that calculates the flight path from appleton tower to the restaurant
     * @param noFly is the array of nofly zones
     * @param centralZone  is the central zone area
     * @param start is the starting point of the drone
     * @param end is the end point of the drone
     * @return List<Coordinate> returns the path that the drone will take
     */
    public List<Coordinate> calculateFlightPath(NamedRegion[] noFly, NamedRegion centralZone, LngLat start, LngLat end) {
        //clears the closed and open sets from previous orders
        closedSet.clear();
        openSet.clear();
        //creates the Coordinate for the start and adds it to the priority queue open set and the open set Hash Map
        Coordinate startCoordinate = new Coordinate(start.lng(), start.lat());
        openSet.add(startCoordinate);
        openSetHash.add(startCoordinate);
        //iterates while there are still values on the open set or until the path is found
        while (!openSet.isEmpty()) {
            //Gets the top coordinate from the priority queue and removes it from the queue
            Coordinate current = openSet.poll();
            //adds this to the closed set
            closedSet.add(current);
            //creates a LngLat for the current coordinate
            LngLat currentLngLat = new LngLat(current.lng, current.lat);
            //sets isInCentral to whether or not the current coordinate is in the central zone
            isInCentral = lngLatHandler.isInRegion(currentLngLat,centralZone);
            //if the current coordinate is close to the end coordinate then is will build path and return it
            if (lngLatHandler.isCloseTo(currentLngLat,end)) {
                    path = new ArrayList<>();
                    //iterates through the current coordinates parents and adds them to the path to create the
                    //route the drone wil take
                    while (current != null) {
                        path.add(current);
                        current = current.parent;
                    }
                    //reverses this list, as it was built backwards (from the coordinate close to the restaurant
                    // back to appleton)
                    Collections.reverse(path);
                    return path;
                }

            //iterates through all the possible angles the drone could go in
            for (double angle: angles) {
                //calculates the LngLat of the next position the drone would be in if it took this angle by calling
                //lngLatHandler.nextPosition
                LngLat nextLngLat = lngLatHandler.nextPosition( currentLngLat, angle);
                //creates a coordinate using this LngLat value called neighbour
                Coordinate neighbour = new Coordinate(nextLngLat.lng(), nextLngLat.lat());
                // if this coordinate has not re-entered the central area after leaving, is not in a no fly zone and
                // is not in the closed set then the program will calculate its g,h and f values and set its parent
                if (!hasReEnteredCentral(nextLngLat,centralZone) &&
                        !isInNoFly(nextLngLat,noFly) &&
                        !closedSet.contains(neighbour)){
                    //creates the new g value
                    double tentativeG = current.g + SystemConstants.DRONE_MOVE_DISTANCE;
                    //creates a coordinate currNeighbour that will contain the neighbours current values if it is on
                    //the open set already
                    Coordinate currNeighbour = null;
                    if (openSetHash.contains(neighbour)){
                        currNeighbour = isOnOpenSet(neighbour);
                    }
                    //if the neighbour is on the open set and the tentativeG is a lower value then its current one
                    // then it will update its values with new calculated values
                     if (currNeighbour != null){
                        if(tentativeG < currNeighbour.g){
                            currNeighbour.parent = current;
                            currNeighbour.g = tentativeG;
                            currNeighbour.h = lngLatHandler.distanceTo(new LngLat(currNeighbour.lng, currNeighbour.lat),
                                                                            end);
                            currNeighbour.f = currNeighbour.g + currNeighbour.h;
                        }
                    }
                     //if it is not on the open set yet then it will be added to the open set with the appropriate
                     //calculated values and parents
                    else{
                        neighbour.parent = current;
                        neighbour.g = tentativeG;
                        neighbour.h = lngLatHandler.distanceTo(nextLngLat,end);
                        neighbour.f = neighbour.g + neighbour.h;
                        openSet.add(neighbour);
                        openSetHash.add(neighbour);
                    }
                }
            }

        }
        //if the open set becomes empty it will return null
        return null;
    }
}
