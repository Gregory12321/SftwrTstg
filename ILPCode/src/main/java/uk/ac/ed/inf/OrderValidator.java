package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * This is a class that will help validate the orders. It has all the attributes needed in the Json file,
 * a constructor function and .get functions for all of these attributes
 */
public class OrderValidator implements OrderValidation {

    /**
     * This is a helper function that checks if the card number is the correct length, and if all characters are digits
     * @param order is the order that is being checked
     * @return boolean returns true if the card number is valid and false if it is not
     */
    private boolean cardNumberValid(Order order) {
        boolean isValid = false;
        String cardNumber = order.getCreditCardInformation().getCreditCardNumber();
        if(cardNumber != null){
            if(cardNumber.length() == 16){
                boolean allDigits = true;
                for (int i = 0; i < cardNumber.length(); i++){
                    if (!(cardNumber.charAt(i) >= '0' && cardNumber.charAt(i) <= '9')){
                        allDigits = false;
                    }
                }
                if (allDigits){
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    /**
     * This is a helper function that checks if the expiry date is not null, if the year and month are correct year and
     * months and if the card has expired yet
     * @param order is the order that is being checked
     * @return boolean returns true if the expiry date is valid and false if it is not
     */
    private boolean expiryDateValid(Order order) {
        boolean isValid = false;
        String expiryDate = order.getCreditCardInformation().getCreditCardExpiry();
        if (order.getCreditCardInformation().getCreditCardExpiry() != null){
            String[] splitExpiryDate = expiryDate.split("/");
            int expiryMonth = Integer.parseInt(splitExpiryDate[0]);
            int expiryYear = Integer.parseInt(splitExpiryDate[1]);
            int currentMonth = order.getOrderDate().getMonthValue();
            int currentYear = order.getOrderDate().getYear() % 100;
            if (expiryMonth <= 12 && expiryMonth >= 0 && expiryYear >= currentYear){
                if(expiryYear == currentYear){
                    if(expiryMonth >= currentMonth){
                        isValid = true;
                    }
                    else{
                        isValid = false;
                    }
                }
                else{
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    //Checks if the CVV is the correct length, and if all characters are digits

    /**
     * This is a helper function that checks if the CVV is the correct length, and if all characters are digits
     * @param order is the order that is being checked
     * @return boolean returns true if the CVV is valid and false if it is not
     */
    private boolean cvvValid(Order order) {
        boolean isValid = false;
        String cvv = order.getCreditCardInformation().getCvv();
        if(cvv != null){
            if(cvv.length() == 3){
                boolean allDigits = true;
                for (int i = 0; i < cvv.length(); i++){
                    if (!(cvv.charAt(i) >= '0' && cvv.charAt(i) <= '9')){
                        allDigits = false;
                    }
                }
                if (allDigits){
                    isValid = true;
                }
            }
        }
        return isValid;
    }


    /**
     * This is a helper function that checks if the total cost of the pizzas is correct and if the prices of the pizzas
     * are correct
     * @param order is the order that is being checked
     * @param restaurants is the array of all restaurants
     * @return boolean returns true if the total cost is correct and false if it is not
     */
    private boolean totalCostValid(Order order, Restaurant[] restaurants) {
        int calculatedCost = 0;
        HashMap<String, Restaurant> availablePizzas = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            for (Pizza pizza : restaurant.menu()) {
                availablePizzas.put(pizza.name(), restaurant);
            }
        }
        if (availablePizzas.get(order.getPizzasInOrder()[0].name()) == null) {return true;}
        Restaurant correctRestaurant = availablePizzas.get(order.getPizzasInOrder()[0].name());
        HashMap<String, Integer> pizzaPricing = new HashMap<>();
        for (Pizza pizza : correctRestaurant.menu()) {
            pizzaPricing.put(pizza.name(), pizza.priceInPence());
        }
        for (Pizza pizza : order.getPizzasInOrder())
            if (pizzaPricing.get(pizza.name()) == null) {return true;}
            else if (pizza.priceInPence() != pizzaPricing.get(pizza.name()))
                return false;

        for (int i = 0; i < order.getPizzasInOrder().length; i++){
            calculatedCost += order.getPizzasInOrder()[i].priceInPence();
        }
        return (calculatedCost + 100 == order.getPriceTotalInPence());
    }

    //checks if the pizzas are on the menu by checking the pizzas against every restaurants menus

    /**
     * This is a helper function that checks if the pizzas are on the menu by checking the pizzas against every
     * restaurants menus
     * @param order is the order that is being checked
     * @param restaurants is the array of all restaurants
     * @return boolean returns true if the pizzas are on the menu and false if they are not
     */
    private boolean pizzaExists(Order order, Restaurant[] restaurants) {
        boolean isValid = true;
        Pizza[] pizzas = order.getPizzasInOrder();
        ArrayList<String> availablePizzas = new ArrayList<String>();
        for (Restaurant restaurant : restaurants) {
            for (Pizza pizza : restaurant.menu()) {
                availablePizzas.add(pizza.name());
            }
        }
        for (Pizza pizza : pizzas) {
            if (!(availablePizzas.contains(pizza.name()))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    /**
     * This is a helper function that checks if the pizzas ordered are all from the same restaurant
     * @param order is the order that is being checked
     * @param restaurants is the array of all restaurants
     * @return boolean returns true if the pizzas are from the same restaurant and false if they are not
     */
    private boolean pizzaFromSameRestaurants(Order order, Restaurant[] restaurants) {
        boolean isValid = false;
        ArrayList<Restaurant> restaurantList = new ArrayList<Restaurant>();
        Pizza[] pizzas = order.getPizzasInOrder();
        HashMap<String, Restaurant> availablePizzas = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            for (Pizza pizza : restaurant.menu()) {
                availablePizzas.put(pizza.name(), restaurant);
            }
        }
        for (Pizza pizza : pizzas) {
            if (availablePizzas.containsKey(pizza.name())) {
                if (!restaurantList.contains(availablePizzas.get(pizza.name()))) {
                    restaurantList.add(availablePizzas.get(pizza.name()));
                }
            }
        }
        if (restaurantList.size()==1) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * This is a helper function that checks if the restaurant is open on the day that the order is made
     * @param order is the order that is being checked
     * @param restaurants is the array of all restaurants
     * @return boolean returns true if the restaurant is open and false if it is not
     */
    private boolean restaurantOpen(Order order, Restaurant[] restaurants) {
        boolean isValid = false;
        DayOfWeek dayOfWeek = order.getOrderDate().getDayOfWeek();
        Pizza firstPizza = order.getPizzasInOrder()[0];
        HashMap<String, Restaurant> availablePizzas = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            for (Pizza pizza : restaurant.menu()) {
                availablePizzas.put(pizza.name(), restaurant);
            }
        }
        Restaurant restaurant = availablePizzas.get(firstPizza.name());
        DayOfWeek[] openDays = restaurant.openingDays();
        for (DayOfWeek day : openDays) {
            if (day == dayOfWeek) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    /**
     * This is a helper function that checks if the order is valid and if it is not, it sets the order validation code
     * and order status
     * @param orderToValidate is the order that is being checked
     * @param definedRestaurants is the array of all restaurants
     * @return Order returns the order that has been checked
     */
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        orderToValidate.setOrderValidationCode(OrderValidationCode.UNDEFINED);
        orderToValidate.setOrderStatus(OrderStatus.INVALID);

        //If the card number is invalid
        if (!cardNumberValid(orderToValidate)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
        }
        //If the expiry date is invalid
        else if (!expiryDateValid(orderToValidate)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
        }
        //If the CVV is invalid
        else if (!cvvValid(orderToValidate)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
        }
        //If the total price is valid
        else if (!totalCostValid(orderToValidate, definedRestaurants)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
        }
        //If the pizzas are not on the restaurants menus
        else if (!pizzaExists(orderToValidate, definedRestaurants)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
        }
        //If there are more than 4 pizzas
        else if (orderToValidate.getPizzasInOrder().length > 4) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
        }
        //If the pizzas are from multiple restaurants
        else if (!pizzaFromSameRestaurants(orderToValidate, definedRestaurants)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
        }
        //If the restaurant is closed
        else if (!restaurantOpen(orderToValidate, definedRestaurants)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.RESTAURANT_CLOSED);
        }
        //If there is no error then the order is valid
        else {
            orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
            orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        }
        return orderToValidate;
    }
}
