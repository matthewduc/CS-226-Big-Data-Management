package edu.ucr.cs.cs226.dnguy521;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

public class RDDJobs
{
    public static void main( String[] args) throws IOException {
        // set warning level
        Logger.getLogger("org.apache").setLevel(Level.WARN);

        // configure spark
        SparkConf conf = new SparkConf().setAppName("assignment2").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        // external file (command line argument 0) to RDD
        String filename = args[0];
        JavaRDD<String> textFileRDD = sc.textFile(filename);

        // TASK 1
        long start = System.nanoTime();
        // filter RDD with response 200
        JavaRDD<String> twoHundred = textFileRDD.filter((Function<String, Boolean>) s -> {
            String code = s.split("\t")[5];
            return code.equals("200");
        });
        Integer twoHundredLength = twoHundred.map(String::length).reduce(Integer::sum);
        long twoHundredCount = twoHundred.count();
        // filter RDD with response 304
        JavaRDD<String> threeZeroFour = textFileRDD.filter((Function<String, Boolean>) s -> {
            String code = s.split("\t")[5];
            return code.equals("304");
        });
        Integer threeZeroFourLength = threeZeroFour.map(String::length).reduce(Integer::sum);
        long threeZeroFourCount = threeZeroFour.count();
        // filter RDD with response 404
        JavaRDD<String> fourZeroFour = textFileRDD.filter((Function<String, Boolean>) s -> {
            String code = s.split("\t")[5];
            return code.equals("404");
        });
        Integer fourZeroFourLength = fourZeroFour.map(String::length).reduce(Integer::sum);
        long fourZeroFourCount = fourZeroFour.count();
        // filter RDD with response 302
        JavaRDD<String> threeZeroTwo = textFileRDD.filter((Function<String, Boolean>) s -> {
            String code = s.split("\t")[5];
            return code.equals("302");
        });
        Integer threeZeroTwoLength = threeZeroTwo.map(String::length).reduce(Integer::sum);
        long threeZeroTwoCount = threeZeroTwo.count();
        // write average to file
        FileWriter fWrite = new FileWriter("task1.txt");
        fWrite.write("Code 200, average number of bytes = "+(twoHundredLength.floatValue()/twoHundredCount));
        fWrite.write("\nCode 304, average number of bytes = "+(threeZeroFourLength.floatValue()/threeZeroFourCount));
        fWrite.write("\nCode 404, average number of bytes = "+(fourZeroFourLength.floatValue()/fourZeroFourCount));
        fWrite.write("\nCode 302, average number of bytes = "+(threeZeroTwoLength.floatValue()/threeZeroTwoCount));
        fWrite.close();
        long end = System.nanoTime();
        System.out.println("Task 1 runtime: "+((end-start)/1_000_000)+" ms");

        // TASK 2
        long start2 = System.nanoTime();
        PairFunction<String, String, String> keys = new PairFunction<String, String, String>() {
            @Override
            public Tuple2<String, String> call(String s) {
                return new Tuple2(s.split("\t")[0], s);
            }
        };
        // create RDD pair <host, values>
        JavaPairRDD<String, String> hostPairs = textFileRDD.mapToPair(keys);
        // selfJoin by host
        JavaPairRDD<String, Tuple2<String, String>> selfJoin = hostPairs.join(hostPairs);
        JavaPairRDD<String, Tuple2<String, String>> filtered = selfJoin
                .filter(x->x._2()._1().split("\t")[4].equals(x._2()._2().split("\t")[4])) // t1.URL = t2.URL
                .filter(x -> Long.parseLong((x._2()._1().split("\t")[2])) - Long.parseLong((x._2()._2().split("\t")[2])) <= 3600) // |t1.timestamp - t2.timestamp| <= 3600
                .filter(x-> !x._2()._1().equals(x._2()._2())); // t1 != t2

        // collect RDD to List, create iterator
        List<Tuple2<String, Tuple2<String, String>>> filteredList = filtered.collect();
        Iterator<Tuple2<String, Tuple2<String, String>>> i = filteredList.iterator();

        // write to file, append with each iteration
        FileWriter f2Write = new FileWriter("task2.txt");
        while(i.hasNext()){
            Tuple2<String, String> temp = i.next()._2;
            f2Write.append(temp._1 + "\t" + temp._2 + "\n");
        }
        f2Write.close();
        long end2 = System.nanoTime();
        System.out.println("Task 2 runtime: "+((end2-start2)/1_000_000)+" ms");
    }
}
