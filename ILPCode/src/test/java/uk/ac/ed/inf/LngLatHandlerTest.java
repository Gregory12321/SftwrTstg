package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import static org.junit.Assert.*;


public class LngLatHandlerTest {

    LngLatHandler lngLatHandler = new LngLatHandler();
    @Test
    public void distanceToTest(){
        double distance = lngLatHandler.distanceTo(new LngLat(0,0), new LngLat(0, 10));
        assertEquals(10.00,distance,0);
    }

    @Test
    public void distanceToTest2(){
        double distance = lngLatHandler.distanceTo(new LngLat(3,12), new LngLat(6, 6));
        assertEquals(6.708203932499369,distance,0);
    }

    @Test
    public void closeToTest(){
        assertTrue(lngLatHandler.isCloseTo(new LngLat(0,0), new LngLat(0,0.0001)));
    }

    @Test
    public void closeToTest2(){
        assertFalse(lngLatHandler.isCloseTo(new LngLat(1,0), new LngLat(0,0.0001)));
    }

    @Test
    public void inRegionTest(){
        NamedRegion region = new NamedRegion("region", new LngLat[]{ new LngLat(0,1), new LngLat(0,0), new LngLat(1,0), new LngLat(1,1) });
        assertTrue(lngLatHandler.isInRegion(new LngLat(0.5,0.5), region));
        assertFalse(lngLatHandler.isInRegion(new LngLat(1,1.1), region));
    }

    @Test
    public void nextPositionTest(){
       LngLat position = lngLatHandler.nextPosition(new LngLat(0,0), 90);
       assertEquals(0, position.lng(), 1E-12);
       assertEquals(0.00015,position.lat(),1E-12);
    }




}
