package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is the main class of the program. It will be used to run the whole program
 */
public class App 
{

    // creates new instances of fileGenerator, orderValidator and flightPath
    static FileGenerator fileGenerator = new FileGenerator();
    static OrderValidator orderValidator = new OrderValidator();
    static FlightPath flightPath = new FlightPath();

    //Creates a linked hashmap that will store the paths of each valid order, with the order number being the key
    static LinkedHashMap<String,List<Coordinate>> orderNumbers = new LinkedHashMap<>();
    //Creates a linked hashmap that will store the path to each restaurant with the restaurant being the key
    // This is done to avoid recalculating paths unnecessarily
    static LinkedHashMap<String,List<Coordinate>> restaurantPaths = new LinkedHashMap<>();

    /**
     This is a helper function that returns the restaurant that the order wants to get the pizza from
     * @param restaurants is the array of all restaurants
     * @param order is the order that is being checked
     * @return Restaurant returns the found restaurant
     */
    private static Restaurant getRestaurant(Restaurant[] restaurants, Order order){
        Pizza pizza = order.getPizzasInOrder()[0];
        for (Restaurant restaurant:restaurants) {
            for (Pizza matchPizza:restaurant.menu()) {
                if (pizza.equals(matchPizza)){
                    return restaurant;
                }
            }
        }
        return null;
    }

    /**
     * This is a helper function that checks if the inputted arguments are valid
     * @param args is the arguments that are inputted
     * @return boolean returns true if the arguments are valid and false if they are not
     * @throws IOException
     */
    private static boolean validArgs(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        //checks if there are only two arguments
        if (args.length != 2){
            return false;
        }

        try {
            //checks if the URL is valid
            String urlCheck = args[1];
            if (urlCheck.charAt(urlCheck.length() - 1) != '/') {
                urlCheck = urlCheck + '/';
            }
            boolean alive = objectMapper.readValue(new URL(urlCheck + "isAlive"), new TypeReference<>() {
            });
            if (!alive){
                return false;
            }

            //checks if the date is correctly formatted
            LocalDate.parse(args[0]);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }catch (IOException e) {
            return false;
        }
    }

    /**
     * This is the main function that is used to run the whole program
     * @param args is the arguments that are inputted
     */

    public static void main( String[] args )
    {
        try {
            //creates the objectMapper which will be used to read from the Rest Server
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            //checks if the arguments are valid
            if (!validArgs(args)) {
                System.err.println("Arguments are invalid");
                System.exit(1);
            }
            //breaks the arguments into the given date and the url
            String date = args[0];
            String url = args[1];

            //checks if the url ends with a / and if it does not then it adds one
            if (url.charAt(url.length() - 1) != '/') {
                url = url + '/';
            }



                //collects the necessary information from the rest servers
                Restaurant[] restaurants = objectMapper.readValue(new URL(url + "restaurants"), new TypeReference<>() {
                });
                Order[] orders = objectMapper.readValue(new URL(url + "orders/" + date), new TypeReference<>() {
                });
                NamedRegion centralArea = objectMapper.readValue(new URL(url + "centralArea"), new TypeReference<>() {
                });
                NamedRegion[] noFlyZones = objectMapper.readValue(new URL(url + "noFlyZones"), new TypeReference<>() {
                });

                //iterates through ever order (on the given date)
                for (Order o : orders) {
                    //validates the order
                    o = orderValidator.validateOrder(o, restaurants);
                    //checks if the order is valid and has not been delivered yet
                    if (o.getOrderValidationCode().equals(OrderValidationCode.NO_ERROR) &&
                            o.getOrderStatus().equals(OrderStatus.VALID_BUT_NOT_DELIVERED)) {
                        //gets the restaurant for this order
                        Restaurant restaurant = getRestaurant(restaurants, o);
                        //checks if the path to this restaurant has already been calculated
                        if (restaurantPaths.containsKey(restaurant.name())) {
                            //changes the order state to delivered and adds the order number and precalculated path to
                            //the HashMap
                            o.setOrderStatus(OrderStatus.DELIVERED);
                            orderNumbers.put(o.getOrderNo(), restaurantPaths.get(restaurant.name()));
                        } else {
                            //calculates the flightpath of the drone from Appleton tower to the restaurant
                            List<Coordinate> path = flightPath.calculateFlightPath(noFlyZones,
                                    centralArea,
                                    new LngLat(-3.186874, 55.944494),
                                    restaurant.location());
                            //changes the order state to delivered if a path was found and adds the order number and
                            // calculated path to the HashMap
                            if (path != null) {
                                o.setOrderStatus(OrderStatus.DELIVERED);
                            }
                            orderNumbers.put(o.getOrderNo(), path);
                            //adds the path to the restaurant HashMap so the path will not be recalculated
                            restaurantPaths.put(restaurant.name(), path);
                        }
                    }
                }
                //generates the three files
                fileGenerator.generateFiles(orderNumbers, date, orders);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
