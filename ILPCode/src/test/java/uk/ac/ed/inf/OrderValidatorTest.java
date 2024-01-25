package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class OrderValidatorTest {
    OrderValidator orderValidator = new OrderValidator();

    Restaurant[] restaurants = new Restaurant[] {
            new Restaurant(
                    "Civerinos Slice",
                    new LngLat(-3.1912869215011597, 55.945535152517735),
                    new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY},
                    new Pizza[] {
                            new Pizza("Margarita", 1000),
                            new Pizza("Calzone", 1400)
                    }
            ),
            new Restaurant(
                    "Sora Lella Vegan Restaurant",
                    new LngLat(-3.202541470527649, 55.943284737579376),
                    new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY},
                    new Pizza[] {
                            new Pizza("Meat Lover", 1400),
                            new Pizza("Vegan Delight", 1100)
                    }
            ),
            new Restaurant(
                    "Domino's Pizza - Edinburgh - Southside",
                    new LngLat(-3.1838572025299072, 55.94449876875712),
                    new DayOfWeek[] {DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY},
                    new Pizza[] {
                            new Pizza("Super Cheese", 1400),
                            new Pizza("All Shrooms", 900)
                    }
            ),
            new Restaurant(
                    "Sodeberg Pavillion",
                    new LngLat(-3.1940174102783203, 55.94390696616939),
                    new DayOfWeek[] {DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY},
                    new Pizza[] {
                            new Pizza("Proper Pizza", 1400),
                            new Pizza("Pineapple & Ham & Cheese", 900)
                    }
            )
    };




    @Test
    public void cardNumberTest(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation(null, "01/01", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void cardNumberTest2(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("123456789012345", "01/01", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void cardNumberTest3(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("12345678901234567", "01/01", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void cardNumberTest4(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("1234A78901234567", "01/01", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void cardNumberTest5(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("1B34178901234567", "01/01", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void expiryDateTest(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("1134178901234567", null, "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void expiryDateTest2(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("1134178901234567", "13/22", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void cvvTest(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                1500,
                new Pizza[]{new Pizza("Meat Lover", 1400)},
                new CreditCardInformation("1134178901234567", "12/25", "1A1"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.CVV_INVALID, order.getOrderValidationCode());
    }
    @Test
    public void totalPriceTest(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                2500,
                new Pizza[]{new Pizza("Margarita", 1000),new Pizza("Calzone", 1420)},
                new CreditCardInformation("1134178901234567", "12/25", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, order.getOrderValidationCode());
    }
    @Test
    public void pizzaDefinedTest(){
        Order order = new Order( "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                3600,
                new Pizza[]{new Pizza("Pineapple & Ham & Cheese", 900),new Pizza("Proper Pizza", 1400),new Pizza("Proper Pizza", 1400),new Pizza("Proper Pizza", 1400),new Pizza("Proper Pizza", 1400)},
                new CreditCardInformation("1134178901234567", "12/25", "111"));
        orderValidator.validateOrder(order, restaurants);
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, order.getOrderValidationCode());
    }

}
