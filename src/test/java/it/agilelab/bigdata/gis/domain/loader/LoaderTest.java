package it.agilelab.bigdata.gis.domain.loader;

import com.vividsolutions.jts.geom.*;
import it.agilelab.bigdata.gis.domain.models.HereMapsStreet;
import it.agilelab.bigdata.gis.domain.models.OSMStreet;
import it.agilelab.bigdata.gis.domain.spatialList.*;
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import scala.collection.JavaConversions;
/**
 * Created by paolo on 25/01/2017.
 */
public class LoaderTest {

    @Test
    public void testParsing() throws Exception {
        String str1 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74409; 45,1; 7,74276; 45,10033; 7,74204; 45,10047; 7,74136; 45,10076; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
        String str2 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74724; 45,09953; 7,74693; 45,09968; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
        String[] fields1 = new CTLLoader(7).parseGeometry(str1);
        String[] fields2 = new CTLLoader(7).parseGeometry(str2);
        System.out.println(fields1.length);
        Integer i = 0;
        for(String field: fields1){
            System.out.println(i + " - " + field);
            i++;
        }

        System.out.println("-----------------------------------------");

        System.out.println(fields2.length);
        i = 0;
        for(String field: fields2){
            System.out.println(i + " - " + field);
            i++;
        }
    }


    @Test
    public void testLineStringBuilding() throws Exception {
        String str1 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74409; 45,1; 7,74276; 45,10033; 7,74204; 45,10047; 7,74136; 45,10076; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
        String str2 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74724; 45,09953; 7,74693; 45,09968; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
        Geometry line1 = new CTLLoader(7).buildGeometry(str1).get();
        Geometry line2 = new CTLLoader(7).buildGeometry(str2).get();

        System.out.println(line1.toString());
        System.out.println(line2.toString());
    }




//    @Test
//    public void testFileLoading() throws Exception {
//        long start = System.currentTimeMillis();
//
//
//        CTLLoader loader = new CTLLoader(7);
//        ArrayList<String> paths = new ArrayList<String>();
//        paths.add("C:\\Users\\paolo\\Desktop\\data\\out2.ctl");
//
//        GeometryList<HereMapsStreet> lineStringList = loader.loadIndex(scala.collection.JavaConversions.asScalaIterator(paths.iterator()).toSeq());
//
//        long end = System.currentTimeMillis();
//        System.out.println("time: "+(end-start) + " ms");
//
//
//        GeometryFactory fact=new GeometryFactory();
//
//        double minx = 2.285949;
//        double maxx = 2.404142;
//        double miny = 48.828021;
//        double maxy = 48.890957;
//
//        double deltax = maxx-minx;
//        double deltay = maxy-miny;
//        Random r = new Random();
//
//        long start1 = System.currentTimeMillis();
//        for(int i=0; i<1000; i++){
//
//            double nexty = miny + r.nextDouble()*deltay;
//            double nextx = minx + r.nextDouble()*deltax;
//
//            Point queryPoint = fact.createPoint(new Coordinate(nextx, nexty));
//            HereMapsStreet queryResult = KNNQueryMem.SpatialKnnQueryJava(lineStringList, queryPoint, 1, true).get(0);
//
//        }
//
//        long end1 = System.currentTimeMillis();
//        System.out.println("time: "+(end1-start1) + " ms");
//    }



    @Test
    public void testShapeLoading() throws Exception {
        long start = System.currentTimeMillis();

        OSMStreetShapeLoader loader = new OSMStreetShapeLoader();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("src/test/resources/sud-190403-free.shp//gis_osm_roads_free_1.shp");

        GeometryList<OSMStreet> lineStringList =
            loader
                .loadIndex(JavaConversions.asScalaIterator(paths.iterator())
                .toSeq());
        long end = System.currentTimeMillis();
        System.out.println("time: "+(end-start) + " ms");

        GeometryFactory fact=new GeometryFactory();

        double minx = 16.00;
        double maxx = 16.10;
        double miny = 40.66;
        double maxy = 40.76;

        double deltax = maxx-minx;
        double deltay = maxy-miny;
        Random r = new Random();

        long start1 = System.currentTimeMillis();

        for(int i=0; i<1000; i++){

            double nexty = miny + r.nextDouble()*deltay;
            double nextx = minx + r.nextDouble()*deltax;

            System.out.println("point: " + nextx + " - " + nexty);

            Point queryPoint = fact.createPoint(new Coordinate(nextx, nexty));
            OSMStreet queryResult = KNNQueryMem.SpatialKnnQueryJava(lineStringList, queryPoint, 1, true).get(0);

            if(queryResult.street().isDefined() && !queryResult.street().get().isEmpty())
                System.out.println(queryResult.street());

        }

        long end1 = System.currentTimeMillis();
        System.out.println("time: "+(end1-start1) + " ms");

        assert(true);
    }


    @Test
    public void testReverseGeocoding() {

        OSMStreetShapeLoader loader = new OSMStreetShapeLoader();
        ArrayList<String> paths = new ArrayList<String>();

        paths.add("src/test/resources/italy.shp/sud_gis_osm_roads_free_1.shp");

        GeometryList<OSMStreet> lineStringList =
            loader
                .loadIndex(JavaConversions.asScalaIterator(paths.iterator())
                .toSeq());

        GeometryFactory fact=new GeometryFactory();

        Double x = 16.01505D;
        Double y = 40.69412D;

        Point queryPoint = fact.createPoint(new Coordinate(x, y));
        OSMStreet queryResult = KNNQueryMem.SpatialKnnQueryJava(lineStringList, queryPoint, 1, true).get(0);

        assert(queryResult.street().get().equals("Via Sandro Pertini"));

    }
}
