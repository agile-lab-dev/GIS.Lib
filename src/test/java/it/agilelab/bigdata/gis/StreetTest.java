package it.agilelab.bigdata.gis;


import com.vividsolutions.jts.geom.*;
import it.agilelab.bigdata.gis.domain.models.OSMStreet;
import it.agilelab.bigdata.gis.domain.models.OSMStreetType;
import org.junit.Test;
import scala.Some;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by paolo on 25/01/2017.
 */
public class StreetTest {

    @Test
    public void testStreetType() throws Exception {

        GeometryFactory fact=new GeometryFactory();
        Point queryPoint = fact.createPoint(new Coordinate(45, 45));
        OSMStreet street = new OSMStreet(queryPoint, new Some("strada scaravaglio"), new Some("prova"), new Some(true), new Some(true), new Some(80), new Some(true), new Some(OSMStreetType.Footway()));
        Boolean res1 = street.isForCar();
        assertTrue(res1 == false);

        street = new OSMStreet(queryPoint, new Some("strada scaravaglio"), new Some("prova"), new Some(true), new Some(true), new Some(80), new Some(true), new Some(OSMStreetType.Cycleway()));
        res1 = street.isForCar();
        assertTrue(res1 == false);

        street = new OSMStreet(queryPoint, new Some("strada scaravaglio"), new Some("prova"), new Some(true), new Some(true), new Some(80), new Some(true), new Some(OSMStreetType.Primary()));
        res1 = street.isForCar();
        assertTrue(res1 == true);


    }
}
