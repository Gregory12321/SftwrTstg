package uk.ac.ed.inf;


import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

/**
 * This is a class that will help with the drone movement calculations needed for the program
 */
public class LngLatHandler implements LngLatHandling {


    /**
     * This is a helper function that calculates the distance between two points using the method provided in the
     * specification
     * @param startPosition is the starting position of the drone
     * @param endPosition is the end position of the drone
     * @return double returns the distance between the two points
     */
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        double lng1 = startPosition.lng();
        double lng2 = endPosition.lng();
        double lat1 = startPosition.lat();
        double lat2 = endPosition.lat();
        return Math.sqrt((lng1 - lng2)*(lng1 - lng2) + (lat1 - lat2)*(lat1 - lat2));
    }

    /**
     * This is a helper function that determines if two points are close to one another using the distanceTo function
     * @param startPosition is the starting position of the drone
     * @param otherPosition is the position of the other point
     * @return boolean returns true if the two points are close to one another and false if they are not
     */
    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    /**
     * This is a helper function that determines if a point is in a region using a ray casting method
     * @param position is the position of the point
     * @param region is the NamedRegion that the point is being checked against
     * @return boolean returns true if the point is in the region and false if it is not
     */
    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        // lng and lat hold the longitude and latitude values and vertices holds the number of vertices the region has
        boolean isInRegion = false;
        double lng = position.lng();
        double lat = position.lat();
        int vertices = region.vertices().length;
        // previousVertex currently holds the position of the first vertex
        LngLat previousVertex = region.vertices()[0];
        //goes through every vertex and once complete, determines if the point is in the region or not
        for (int i = 1; i <= vertices; i++){
            LngLat nextVertex = region.vertices()[i%vertices];
            if (lat > Math.min(previousVertex.lat(), nextVertex.lat())){
                if(lat <= Math.max(previousVertex.lat(), nextVertex.lat())){
                    if(lng <= Math.max(previousVertex.lng(), nextVertex.lng())){
                        double xintersection = (lat - previousVertex.lat())*(nextVertex.lng() - previousVertex.lng())/(nextVertex.lat() - previousVertex.lat()) + previousVertex.lng();
                        if (previousVertex.lng() == nextVertex.lng() || lng <= xintersection){
                            isInRegion = !isInRegion;
                        }
                    }
                }
            }
            previousVertex = nextVertex;
        }
        return isInRegion;
    }

    /**
     * This is a helper function that calculates the next position of the drone given the angle it is travelling in
     * @param startPosition is the starting position of the drone
     * @param angle is the angle the drone is travelling in
     * @return LngLat returns the next position of the drone
     */
    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        //if the drone is hovering (angle is 999), the next position is the same
        if(angle == 999){
            return startPosition;
        }
        //if drone is not hovering, the new Longitude and Latitude values are calculated
        else {
            double newLng = SystemConstants.DRONE_MOVE_DISTANCE * Math.cos(angle * Math.PI / 180) + startPosition.lng();
            double newLat = SystemConstants.DRONE_MOVE_DISTANCE * Math.sin(angle * Math.PI / 180) + startPosition.lat();
            return new LngLat(newLng, newLat);
        }
    }
}
