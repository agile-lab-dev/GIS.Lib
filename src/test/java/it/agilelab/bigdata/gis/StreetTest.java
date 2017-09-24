package it.agilelab.bigdata.gis;


import com.vividsolutions.jts.geom.*;
import it.agilelab.bigdata.gis.models.OSMStreet;
import it.agilelab.bigdata.gis.models.OSMStreetType;
import org.junit.Test;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by paolo on 25/01/2017.
 */
public class StreetTest {

    @Test
    public void testStreetType() throws Exception {

        GeometryFactory fact=new GeometryFactory();
        Point queryPoint = fact.createPoint(new Coordinate(45, 45));
        OSMStreet street = new OSMStreet(queryPoint, "strada scaravaglio", "prova", true, true, 80, true, OSMStreetType.Footway());
        Boolean res1 = street.isForCar();
        assertTrue(res1 == false);

        street = new OSMStreet(queryPoint, "strada scaravaglio", "prova", true, true, 80, true, OSMStreetType.Cycleway());
        res1 = street.isForCar();
        assertTrue(res1 == false);

        street = new OSMStreet(queryPoint, "strada scaravaglio", "prova", true, true, 80, true, OSMStreetType.Primary());
        res1 = street.isForCar();
        assertTrue(res1 == true);


    }
}
