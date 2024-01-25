package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static uk.ac.ed.inf.FlightPath.findAngle;


/**
 * This is a class that will generate the files needed for the program. It has three functions, one for each file
 * that needs to be generated. It also has a function that calls all three of these functions.
 */
public class FileGenerator {

    /**
     * This function generates the GeoJson file. It takes in a hashmap of the paths of each order, with the order name
     * as the key.
     * @param orderPaths is the hashmap of the paths of each order
     * @return ObjectNode returns the generated GeoJson file data
     */
    public ObjectNode generateGeoJsonPath(HashMap<String, List<Coordinate>> orderPaths){
        //creates an instance of an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode featureCollection = objectMapper.createObjectNode();
        //Adds the needed Object nodes for features properties and geometry to make it a valid GeoJson file
        featureCollection.put("type", "FeatureCollection");
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode feature = objectMapper.createObjectNode();
        //If there are no orders returns empty but valid geojson data
        if (orderPaths.isEmpty()){
            featureCollection.set("features", objectMapper.createArrayNode());
            return featureCollection;
        }
        feature.put("type", "Feature");
        ObjectNode properties = objectMapper.createObjectNode();
        feature.set("properties",properties );
        ObjectNode geometry = objectMapper.createObjectNode();
        geometry.put("type","LineString");
        ArrayNode coordinatesNode = objectMapper.createArrayNode();


        for (List<Coordinate> coordinateList: orderPaths.values()) {
            // Goes through each different order and its related path
            for (Coordinate coordinates: coordinateList) {
                //for each coordinate in the path add it to the coordinatesNode
                ArrayNode coordinate = objectMapper.createArrayNode();
                coordinate.add(coordinates.lng);
                coordinate.add(coordinates.lat);
                coordinatesNode.add(coordinate);
            }
            //This only adds the path in one direction (from appleton to the restaurant) so we reverse it and
            // repeat the process
            Collections.reverse(coordinateList);
            for (Coordinate coordinates: coordinateList) {
                ArrayNode coordinate = objectMapper.createArrayNode();
                coordinate.add(coordinates.lng);
                coordinate.add(coordinates.lat);
                coordinatesNode.add(coordinate);
            }
            //reverse it back so if this path is reused it does not start the wrong way round
            Collections.reverse(coordinateList);
        }
        //finalises the construction of the file using the coordinatesNode created
        geometry.set("coordinates", coordinatesNode);
        feature.set("geometry", geometry);
        featureCollection.set("features", objectMapper.createArrayNode().add(feature));
        return featureCollection;
    }

    /**
     * This function generates the deliveries file. It takes in an array of orders and returns a list of
     * OrdersForFiles.
     * @param orders is the array of all orders for this date
     * @return List<OrdersForFiles> returns the generated deliveries file data
     */
    private List<OrdersForFiles> generateOrdersJson(Order[] orders){
        List<OrdersForFiles> toFile = new ArrayList<>();
        //goes through each order and creates an OrdersForFiles for it, then adds this to toFile
        for (Order order:orders) {
            toFile.add(new OrdersForFiles(order.getOrderNo(),
                    order.getOrderStatus(),
                    order.getOrderValidationCode(),
                    order.getPriceTotalInPence()));
        }
        return toFile;
    }

    /**
     * This function generates the flightpath file. It takes in a hashmap of the paths of each order, with the order
     * name as the key. It returns a list of FlightPathForFiles.
     * @param orderPaths is the hashmap of the paths of each order
     * @return List<FlightPathForFiles> returns the generated flightpath file data
     */
    private List<FlightPathForFiles> generateFlightpathJson(HashMap<String, List<Coordinate>> orderPaths) {
        List<FlightPathForFiles> toFile = new ArrayList<>();
        //starts an iterator that will go through every entry in the hashmap
        Iterator<Map.Entry<String, List<Coordinate>>> mapIterator = orderPaths.entrySet().iterator();
        //If there are no orders, returns the empty ArrayList
        if (!mapIterator.hasNext()){
            return toFile;
        }
        //Gets the first entry of the hashmap
        Map.Entry<String, List<Coordinate>> entry = mapIterator.next();
        //Creates a boolean one more. This is here so that the while loop will have one last
        // loop after .hasNext() is false, to ensure the last order is iterated over
        boolean oneMore = true;
        while (mapIterator.hasNext() || oneMore) {
            //gets the next entry. Now we have access to both the current and next entry
            Map.Entry<String, List<Coordinate>> nextEntry = mapIterator.hasNext()? mapIterator.next() : null;
            //iterates through every coordinate in the path for this order
            for (int i = 0; i < entry.getValue().size(); i++) {
                //gets the current and next coordinate values
                Coordinate currentItem = entry.getValue().get(i);
                Coordinate nextItem = (i < entry.getValue().size() - 1) ? entry.getValue().get(i + 1) : null;
                //if there is no next coordinated then a hover move is added with the angle 999.0
                if (nextItem == null){
                    double angle = 999.0;
                    toFile.add(new FlightPathForFiles (entry.getKey(),
                            currentItem.lng,
                            currentItem.lat,
                            angle,
                            currentItem.lng,
                            currentItem.lat));
                }
                else{
                    //Finds the angle, creates a FlightPathForFiles using the current coordinate, next coordinate
                    //and angle
                    double angle = findAngle(new LngLat(currentItem.lng,currentItem.lat),
                            new LngLat(nextItem.lng, nextItem.lat) );
                    toFile.add(new FlightPathForFiles (entry.getKey(),
                            currentItem.lng,
                            currentItem.lat,
                            angle,
                            nextItem.lng,
                            nextItem.lat));
                }
            }
            //This only creates the files for the path from appleton to the restaurant, so the path must be
            //reversed so we can add the path back
            Collections.reverse(entry.getValue());
            //This is mostly the same iteration as before except..
            for (int i = 0; i < entry.getValue().size(); i++) {
                Coordinate currentItem = entry.getValue().get(i);
                Coordinate nextItem = (i < entry.getValue().size() - 1) ? entry.getValue().get(i + 1) : null;
                if (nextItem == null){
                    //If this is not the last order then the toLng and toLat values are set to the beginning
                    //Values of the next orders path
                    if (nextEntry != null) {
                        Coordinate startNext = nextEntry.getValue().get(0);
                        double angle = 999.0;
                        toFile.add(new FlightPathForFiles(entry.getKey(),
                                currentItem.lng,
                                currentItem.lat,
                                angle,
                                startNext.lng,
                                startNext.lat));
                    }
                    else{
                        double angle = 999.0;
                        toFile.add(new FlightPathForFiles (entry.getKey(),
                                currentItem.lng,
                                currentItem.lat,
                                angle,
                                currentItem.lng,
                                currentItem.lat));
                    }
                }
                else{
                    double angle = findAngle(new LngLat(currentItem.lng,currentItem.lat),
                            new LngLat(nextItem.lng, nextItem.lat) );
                    toFile.add(new FlightPathForFiles (entry.getKey(),
                            currentItem.lng,
                            currentItem.lat,
                            angle,
                            nextItem.lng,
                            nextItem.lat));
                }
            }
            //The path is reversed again in case we need to reuse it later in the program
            Collections.reverse(entry.getValue());
            entry = nextEntry;
            //makes sure that the while loop continues until every order is processed
            if (entry == null){
                oneMore = false;
            }
        }
        return toFile;
    }

    /**
     * This function calls all three file generators. It takes in a hashmap of the paths of each order, with the order
     * name as the key, the date and an array of orders.
     * @param orderPaths is the hashmap of the paths of each order
     * @param date is the date of the orders
     * @param orders is the array of all orders for this date
     */
    public void generateFiles(HashMap<String, List<Coordinate>> orderPaths, String date, Order[] orders) {
        //Calls the file generator functions
        ObjectNode geoJson = generateGeoJsonPath(orderPaths);
        List<OrdersForFiles> deliveries = generateOrdersJson(orders);
        List<FlightPathForFiles> flightpath = generateFlightpathJson(orderPaths);
        try {
            //Writes the data to files in the "resultfiles" directory, with the date used to create the correct names
            String file = "resultfiles";
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(file, "drone-" + date + ".geojson"), geoJson);
            mapper.writeValue(new File(file, "deliveries-" + date + ".json"), deliveries);
            mapper.writeValue(new File(file, "flightpath-" + date + ".json"), flightpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
