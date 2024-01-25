package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;


/**
 * This is a class that will help construct the deliveries file. It has all the attributes needed in the Json file,
 * a constructor function and .get functions for all of these attributes
 */
public class OrdersForFiles {
    String orderNo;
    OrderStatus orderStatus;
    OrderValidationCode orderValidationCode;
    int costInPence;

    /**
     * This is the constructor function for the class
     * @param orderNum is the order number
     * @param orderStat is the order status
     * @param orderValidation is the order validation state
     * @param costInPennies is the cost of the order
     */
    public OrdersForFiles(String orderNum, OrderStatus orderStat, OrderValidationCode orderValidation, int costInPennies) {
        orderNo = orderNum;
        orderStatus = orderStat;
        orderValidationCode = orderValidation;
        costInPence = costInPennies;
    }

    /**
     * This is a helper function that returns the order validation state
     * @return OrderValidationCode returns the order validation state
     */


    public OrderValidationCode getOrderValidationCode() {
        return orderValidationCode;
    }

    /**
     * This is a helper function that returns the order status
     * @return  OrderStatus returns the order status
     */
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    /**
     * This is a helper function that returns the order number
     * @return String returns the order number
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * This is a helper function that returns the cost of the order
     * @return int returns the cost of the order
     */
    public int getCostInPence() {
        return costInPence;
    }
}
