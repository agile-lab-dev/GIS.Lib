//package it.agilelab.bigdata.gis.loader;
//
//import com.vividsolutions.jts.geom.*;
//import it.agilelab.bigdata.gis.models.HereMapsStreet;
//import it.agilelab.bigdata.gis.models.OSMStreet;
//import it.agilelab.bigdata.gis.spatialList.*;
//import it.agilelab.bigdata.gis.spatialOperator.KNNQueryMem;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.Random;
//
///**
// * Created by paolo on 25/01/2017.
// */
//public class LoaderTest {
//
//    @Test
//    public void testParsing() throws Exception {
//        String str1 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74409; 45,1; 7,74276; 45,10033; 7,74204; 45,10047; 7,74136; 45,10076; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
//        String str2 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74724; 45,09953; 7,74693; 45,09968; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
//        String[] fields1 = new CTLLoader(7).parseGeometry(str1);
//        String[] fields2 = new CTLLoader(7).parseGeometry(str2);
//        System.out.println(fields1.length);
//        Integer i = 0;
//        for(String field: fields1){
//            System.out.println(i + " - " + field);
//            i++;
//        }
//
//        System.out.println("-----------------------------------------");
//
//        System.out.println(fields2.length);
//        i = 0;
//        for(String field: fields2){
//            System.out.println(i + " - " + field);
//            i++;
//        }
//    }
//
//
//    @Test
//    public void testLineStringBuilding() throws Exception {
//        String str1 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74409; 45,1; 7,74276; 45,10033; 7,74204; 45,10047; 7,74136; 45,10076; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
//        String str2 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74724; 45,09953; 7,74693; 45,09968; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))";
//        Geometry line1 = new CTLLoader(7).buildGeometry(str1).get();
//        Geometry line2 = new CTLLoader(7).buildGeometry(str2).get();
//
//        System.out.println(line1.toString());
//        System.out.println(line2.toString());
//    }
//
//
//
//
//    @Test
//    public void testFileLoading() throws Exception {
//        long start = System.currentTimeMillis();
//
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
//      /*  ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject(lineStringList);
//        oos.close();
//        System.out.println(baos.size());
//*/
//
//
//        //48.865078, 2.329587
//        //3 Rue d'Alger
//        //75001 Paris, Francia
//
//
//        //48.890957, 2.302972
//        //48.877937, 2.404142
//        //48.831760, 2.398175
//        //48.828021, 2.285949
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
//            //long start2 = System.currentTimeMillis();
//            HereMapsStreet queryResult = KNNQueryMem.SpatialKnnQueryJava(lineStringList, queryPoint, 1, true).get(0);
//            //long end2 = System.currentTimeMillis();
//            //System.out.println("time: "+(end2-start2) + " ms");
//            //System.out.println(queryResult.toString());
//
//        }
//
//        long end1 = System.currentTimeMillis();
//        System.out.println("time: "+(end1-start1) + " ms");
//    }
//
//
//
//    @Test
//    public void testShapeLoading() throws Exception {
//        long start = System.currentTimeMillis();
//
//        OSMStreetShapeLoader loader = new OSMStreetShapeLoader();
//        ArrayList<String> paths = new ArrayList<String>();
//        paths.add("C:\\Users\\paolo\\Documents\\data\\GIS\\nord-est-latest-free.shp\\gis.osm_roads_free_1.shp");
//        /*paths.add("C:\\Users\\paolo\\Documents\\data\\GIS\\nord-ovest-latest-free.shp\\gis.osm_roads_free_1.shp");
//        paths.add("C:\\Users\\paolo\\Documents\\data\\GIS\\sud-latest-free.shp\\gis.osm_roads_free_1.shp");
//        paths.add("C:\\Users\\paolo\\Documents\\data\\GIS\\centro-latest-free.shp\\gis.osm_roads_free_1.shp");
//        paths.add("C:\\Users\\paolo\\Documents\\data\\GIS\\isole-latest-free.shp\\gis.osm_roads_free_1.shp");*/
//
//
//        GeometryList<OSMStreet> lineStringList = loader.loadIndex(scala.collection.JavaConversions.asScalaIterator(paths.iterator()).toSeq());
//        long end = System.currentTimeMillis();
//        System.out.println("time: "+(end-start) + " ms");
//
//
//      /*  ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject(lineStringList);
//        oos.close();
//        System.out.println(baos.size());
//*/
//
//
//        //48.865078, 2.329587
//        //3 Rue d'Alger
//        //75001 Paris, Francia
//
//
//        //48.890957, 2.302972
//        //48.877937, 2.404142
//        //48.831760, 2.398175
//        //48.828021, 2.285949
//
//        GeometryFactory fact=new GeometryFactory();
//
//        double minx = 7.60;
//        double maxx = 7.70;
//        double miny = 45.00;
//        double maxy = 45.15;
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
//            System.out.println("point: " + nextx + " - " + nexty);
//
//            Point queryPoint = fact.createPoint(new Coordinate(nextx, nexty));
//            //long start2 = System.currentTimeMillis();
//            OSMStreet queryResult = KNNQueryMem.SpatialKnnQueryJava(lineStringList, queryPoint, 1, true).get(0);
//            //long end2 = System.currentTimeMillis();
//            //System.out.println("time: "+(end2-start2) + " ms");
//            System.out.println(queryResult.toString());
//
//        }
//
//        long end1 = System.currentTimeMillis();
//        System.out.println("time: "+(end1-start1) + " ms");
//    }
//}
