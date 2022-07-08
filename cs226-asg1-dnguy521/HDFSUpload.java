package edu.ucr.cs.cs226.dnguy521;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IOUtils;
import java.io.*;

public class HDFSUpload
{
    public static void main( String[] args ) throws IOException {
        Configuration conf = new Configuration();

        String input = args[0];
        Path inputPath = new Path(input);
        FileSystem fs = inputPath.getFileSystem(conf);

        if (fs.exists(inputPath)) {
            String output = args[1];
            Path outputPath = new Path(output);
            FileSystem fs2 = outputPath.getFileSystem(conf);

            if (fs2.exists(outputPath)){
                System.out.println("Output path already exist.");
            } else {
                long start = System.nanoTime();
                InputStream in = fs.open(inputPath);
                OutputStream out = fs2.create(outputPath);
                IOUtils.copyBytes(in, out, conf);
                long end = System.nanoTime();
                System.out.println("Runtime: "+((end-start)/1_000_000_000)+" seconds"); // in nano seconds / 1 bil
                fs2.close();

            }
        } else {
            System.out.println("Input path does not exist.");
        }
    }
}
