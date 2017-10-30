/**
 * Copyright 2012 Andy Turner, The University of Leeds, UK
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification is permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.grids.io.Grids_Files;

/**
 * A class for processing data representing a fluvial glacial outburst flood.
 *
 * The input data has been pre-processed and is in a form of cross sectional
 * data for different locations along the profile of the fluvial glacial outlet.
 * The flood observed did not radically alter the stream. Small amounts of solid
 * sediment will have been carried in the flow of the flood which will have been
 * almost completely liquid in form. The stream flow was measured for change in
 * criticality using a Froude Number measure. A Froude Number of 1 is known as
 * the point of criticality, where the flow moves away downstream as fast as it
 * arrives from upstream. A lower (&lt; 1) Froude Number results in a bow wave
 * whereby obstacles in the water significantly slow down the flow. A higher
 * (&gt; 1) Froude Number is for a super critical flow where essentially the
 * flow is seen to accelerate downstream.
 *
 * Jonathan Carrivick provided a detailed specification for the data processing
 * task on 2012-04-17. Andy Turner did the coding and processing starting on
 * 2012-05-15. After several development iterations, the results were considered
 * satisfactory and the research was drafted for publication. Jonathan
 * visualised and analysed the output from this program as part of that process.
 *
 * The development of this code and its release for the publication took a total
 * of about 7 hours work (on top of what has been done before to platform this
 * effort). More could be done to automate the processing and the work it feeds
 * from and too. For instance the model that produced the data input into this
 * program and the visualisation of results produced by this program could
 * become part of it or part of an automated workflow that can reproduce the
 * results.
 */
public class CrossSectionProcessing {

    /*
     * A reference to the place where the data is read from and written too 
     * file.
     */
    File _Directory_File;

    public CrossSectionProcessing() {
        this(Grids_Files.createNewFile());
    }

    public CrossSectionProcessing(File directory) {
        _Directory_File = directory;
    }

    /**
     * If no argument is input, the users current working directory is the
     * location where input data is expected to be and where output data is
     * written.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            File directory;
            if (args.length > 1) {
                throw new IllegalArgumentException("Expecting only one or no "
                        + "arguments. The argument is meant to be a directory "
                        + "location for input and output.");
            }
            if (args.length == 0) {
                String userdir = System.getProperty("user.dir");
                directory = new File(userdir);
            } else {
                directory = new File(args[0]);
            }
            if (!directory.exists()) {
                throw new IOException("Directory " + directory + " does not "
                        + "exist");
            }
            //File directory = new File("/scratch02/FluvialGlacial/workspace/");
            new CrossSectionProcessing(directory).run();
        } catch (Error e) {
            System.err.println(e.getLocalizedMessage());
            //e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            //e.printStackTrace();
        }
    }

    /**
     * The parameters specified here are specific to the input data. For a more
     * general program these parameters can be input from another file or from
     * command line arguments or they can be worked out based on the input data
     * as is.
     *
     * @throws IOException
     */
    public void run() throws IOException {
        int start = 5;
        int end = 1190;
        int increment = 5;
        process(start, end, increment);
    }

    /**
     * All the processing is essentially rolled into this single method. For
     * further development of this code, this would probably be best broken down
     * into smaller methods.
     *
     * @param start A numerical name for the first cross section
     * @param end A numerical name for the last cross section
     * @param increment A numerical increment for the cross sections
     */
    public void process(
            int start,
            int end,
            int increment) {
        File inputDirectory = new File(
                _Directory_File.getParentFile() + "/input/MODEL OUTPUT/");
        File outputDirectory = new File(
                _Directory_File.getParentFile() + "/output/MODEL OUTPUT/");

        // Depth preparation
        File depthInputDirectory = new File(
                inputDirectory,
                "depth alltimes");
        File depthOutputDirectory = new File(
                outputDirectory,
                "depth alltimes");
        File depthRowOutputDirectory = new File(
                depthOutputDirectory,
                "rowGeneralisation");
        depthRowOutputDirectory.mkdirs();
        File depthColOutputDirectory = new File(
                depthOutputDirectory,
                "colGeneralisation");
        depthColOutputDirectory.mkdirs();
        File depthRowAndColOutputDirectory = new File(
                depthOutputDirectory,
                "rowAndColGeneralisation");
        depthRowAndColOutputDirectory.mkdirs();
        File depthRowAndColOutputFile = new File(
                depthRowAndColOutputDirectory,
                "depth.csv");
        PrintWriter depthRowAndCol_PrintWriter = null;
        try {
            depthRowAndCol_PrintWriter = new PrintWriter(
                    depthRowAndColOutputFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        // Depth thesholds 4, 6, 8, 10
        depthRowAndCol_PrintWriter.println(
                "Cross Section Number, Total N, "
                + "Total Sum, Mean,"
                + "Max, MaxCount, "
                + "Inundation Time Max Column, Max Time, "
                + "Time to Max, "
                + "Number of values > 4, Number of values > 6, "
                + "Number of values > 8, Number of values > 10");

        // Velocity preparation
        File velocityInputDirectory = new File(
                inputDirectory,
                "velocity alltimes");
        File velocityOutputDirectory = new File(
                outputDirectory,
                "velocity alltimes");
        File velocityRowOutputDirectory = new File(
                velocityOutputDirectory,
                "rowGeneralisation");
        velocityRowOutputDirectory.mkdirs();
        File velocityColOutputDirectory = new File(
                velocityOutputDirectory,
                "colGeneralisation");
        velocityColOutputDirectory.mkdirs();
        File velocityRowAndColOutputDirectory = new File(
                velocityOutputDirectory,
                "rowAndColGeneralisation");
        velocityRowAndColOutputDirectory.mkdirs();
        File velocityRowAndColOutputFile = new File(
                velocityRowAndColOutputDirectory,
                "velocity.csv");
        PrintWriter velocityRowAndCol_PrintWriter = null;
        try {
            velocityRowAndCol_PrintWriter = new PrintWriter(
                    velocityRowAndColOutputFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        // velocity thesholds 4, 6, 8, 10
        velocityRowAndCol_PrintWriter.println(
                "Cross Section Number, Total N, "
                + "Total Sum, Mean,"
                + "Max, MaxCount, "
                + "Inundation Time Max Column, Max Time, "
                + "Time to Max, "
                + "Number of values > 4, Number of values > 6, "
                + "Number of values > 8, Number of values > 10");

        // Shear stress preparation 
        File shearStressInputDirectory = new File(
                inputDirectory,
                "shear stress alltimes");
        File shearStressOutputDirectory = new File(
                outputDirectory,
                "shear stress alltimes");
        File shearStressRowOutputDirectory = new File(
                shearStressOutputDirectory,
                "rowGeneralisation");
        shearStressRowOutputDirectory.mkdirs();
        File shearStressColOutputDirectory = new File(
                shearStressOutputDirectory,
                "colGeneralisation");
        shearStressColOutputDirectory.mkdirs();
        File shearStressRowAndColOutputDirectory = new File(
                shearStressOutputDirectory,
                "rowAndColGeneralisation");
        shearStressRowAndColOutputDirectory.mkdirs();
        File shearStressRowAndColOutputFile = new File(
                shearStressRowAndColOutputDirectory,
                "shear stress.csv");
        PrintWriter shearStressRowAndCol_PrintWriter = null;
        try {
            shearStressRowAndCol_PrintWriter = new PrintWriter(
                    shearStressRowAndColOutputFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        // shearStress thesholds 200, 1000, 2500, 5000
        shearStressRowAndCol_PrintWriter.println(
                "Cross Section Number, Total N, "
                + "Total Sum, Mean,"
                + "Max, MaxCount, "
                + "Inundation Time Max Column, Max Time, "
                + "Time to Max, "
                + "Number of values > 200, Number of values > 1000, "
                + "Number of values > 2500, Number of values > 5000");

        // Froude preparation
        File froudeInputDirectory = new File(
                inputDirectory,
                "froude alltimes");
        File froudeOutputDirectory = new File(
                outputDirectory,
                "froude alltimes");
        File froudeRowOutputDirectory = new File(
                froudeOutputDirectory,
                "rowGeneralisation");
        froudeRowOutputDirectory.mkdirs();
        File froudeColOutputDirectory = new File(
                froudeOutputDirectory,
                "colGeneralisation");
        froudeColOutputDirectory.mkdirs();
        File froudeRowAndColOutputDirectory = new File(
                froudeOutputDirectory,
                "rowAndColGeneralisation");
        froudeRowAndColOutputDirectory.mkdirs();
        File froudeRowAndColOutputFile = new File(
                froudeRowAndColOutputDirectory,
                "froude.csv");
        PrintWriter froudeRowAndCol_PrintWriter = null;
        try {
            froudeRowAndCol_PrintWriter = new PrintWriter(
                    froudeRowAndColOutputFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        // froude thesholds 0.8, 0.9, 1.0, 1.1
        froudeRowAndCol_PrintWriter.println(
                "Cross Section Number, Total N, "
                + "Total Sum, Mean,"
                + "Max, MaxCount, "
                + "Inundation Time Max Column, Max Time, "
                + "Time to Max, "
                + "Number of values > 0.8, Number of values > 0.9, "
                + "Number of values > 1.0, Number of values > 1.1");

        int depthFileCount = 0;
        int velocityFileCount = 0;
        int shearStressFileCount = 0;
        int froudeFileCount = 0;

        for (int i = start; i <= end; i += increment) {

            depthFileCount++;
            velocityFileCount++;
            shearStressFileCount++;
            froudeFileCount++;

            // Initialisation
            // depth
            int depthT1Count = 0;
            int depthT2Count = 0;
            int depthT3Count = 0;
            int depthT4Count = 0;
            File depthInputFile = new File(
                    depthInputDirectory,
                    "depth " + i + ".csv");
            // Test file exists
            if (!depthInputFile.exists()) {
                System.out.println(depthInputFile + "does not exist");
            } else {
                System.out.println("File " + depthInputFile.toString());
            }
            double totalDepthSum = 0;
            double totalDepthMean;
            double totalDepthN = 0;
            double totalDepthMax = 0;
            double depthMaxCount = 0;
            String depthInundationTimeMaxColumn = null;
            String depthMaxTime = null;

            // velocity
            int velocityT1Count = 0;
            int velocityT2Count = 0;
            int velocityT3Count = 0;
            int velocityT4Count = 0;
            File velocityInputFile = new File(
                    velocityInputDirectory,
                    "velocity " + i + ".csv");
            // Test file exists
            if (!velocityInputFile.exists()) {
                System.out.println(velocityInputFile + "does not exist");
            } else {
                System.out.println("File " + velocityInputFile.toString());
            }
            double totalVelocitySum = 0;
            double totalVelocityMean;
            double totalVelocityN = 0;
            double totalVelocityMax = 0;
            double velocityMaxCount = 0;
            String velocityInundationTimeMaxColumn = null;
            String velocityMaxTime = null;

            // shear stress
            int shearStressT1Count = 0;
            int shearStressT2Count = 0;
            int shearStressT3Count = 0;
            int shearStressT4Count = 0;
            File shearStressInputFile = new File(
                    shearStressInputDirectory,
                    "shear stress " + i + ".csv");
            // Test file exists
            if (!shearStressInputFile.exists()) {
                System.out.println(shearStressInputFile + "does not exist");
            } else {
                System.out.println("File " + shearStressInputFile.toString());
            }
            double totalShearStressSum = 0;
            double totalShearStressMean;
            double totalShearStressN = 0;
            double totalShearStressMax = 0;
            double shearStressMaxCount = 0;
            String shearStressInundationTimeMaxColumn = null;
            String shearStressMaxTime = null;

            // froude
            int froudeT1Count = 0;
            int froudeT2Count = 0;
            int froudeT3Count = 0;
            int froudeT4Count = 0;
            File froudeInputFile = new File(
                    froudeInputDirectory,
                    "froude " + i + ".csv");
            // Test file exists
            if (!froudeInputFile.exists()) {
                System.out.println(froudeInputFile + "does not exist");
            } else {
                System.out.println("File " + froudeInputFile.toString());
            }
            double totalFroudeSum = 0;
            double totalFroudeMean;
            double totalFroudeN = 0;
            double totalFroudeMax = 0;
            double froudeMaxCount = 0;
            String froudeInundationTimeMaxColumn = null;
            String froudeMaxTime = null;

            // Read data
            ArrayList[] depthData = readIntoArrayList(
                    depthInputFile);
            ArrayList[] velocityData = readIntoArrayList(
                    velocityInputFile);
            ArrayList[] shearStressData = readIntoArrayList(
                    shearStressInputFile);
            ArrayList[] froudeData = readIntoArrayList(
                    froudeInputFile);

            ArrayList depthTimes = depthData[0];
            ArrayList depthDataValues = depthData[1];
            ArrayList velocityTimes = velocityData[0];
            ArrayList velocityDataValues = velocityData[1];
            ArrayList shearStressTimes = shearStressData[0];
            ArrayList shearStressDataValues = shearStressData[1];
            ArrayList froudeTimes = froudeData[0];
            ArrayList froudeDataValues = froudeData[1];

            // RowProcessing

            // Lose 4 rows as per instructions from Jonathan Carrivick
            int depthNumberOfRows = depthDataValues.size() - 4;
            int velocityNumberOfRows = velocityDataValues.size() - 4;
            int shearStressNumberOfRows = shearStressDataValues.size() - 4;
            int froudeNumberOfRows = froudeDataValues.size() - 4;

            // depth
            File depthRowOutputFile = new File(
                    depthRowOutputDirectory,
                    "depth " + i + ".csv");
            PrintWriter depthRow_PrintWriter = null;
            try {
                depthRow_PrintWriter = new PrintWriter(depthRowOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            depthRow_PrintWriter.println("n,sum,mean,max");
            for (int rowIndex = 0; rowIndex < depthNumberOfRows; rowIndex++) {
                double[] row = (double[]) depthDataValues.get(rowIndex);
                double max = Double.MIN_NORMAL;
                double sum = 0.0d;
                double n = 0;
                double value;
                boolean depthT1 = false;
                boolean depthT2 = false;
                boolean depthT3 = false;
                boolean depthT4 = false;
                for (int column = 0; column < row.length; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        n++;
                        sum += value;
                        max = Math.max(max, value);
                        totalDepthN++;
                        totalDepthSum += value;
                        totalDepthMax = Math.max(totalDepthMax, value);
                        // Depth thesholds 4, 6, 8, 10
                        if (value > 4) {
                            //depthT1Count++;
                            depthT1 = true;
                            if (value > 6) {
                                //depthT2Count++;
                                depthT2 = true;
                                if (value > 8) {
                                    //depthT3Count++;
                                    depthT3 = true;
                                    if (value > 10) {
                                        //depthT4Count++;
                                        depthT4 = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (depthT1) {
                    depthT1Count++;
                    if (depthT2) {
                        depthT2Count++;
                        if (depthT3) {
                            depthT3Count++;
                            if (depthT4) {
                                depthT4Count++;
                            }
                        }
                    }
                }
                if (n > 0) {
                    double mean = sum / n;
                    System.out.println("n " + ((int) n));
                    System.out.println("sum " + sum);
                    System.out.println("mean " + mean);
                    System.out.println("max " + max);
                    depthRow_PrintWriter.println(
                            n + "," + sum + "," + mean + "," + max);
                }
            }
            depthRow_PrintWriter.close();

            // velocity
            File velocityRowOutputFile = new File(
                    velocityRowOutputDirectory,
                    "velocity " + i + ".csv");
            PrintWriter velocityRow_PrintWriter = null;
            try {
                velocityRow_PrintWriter = new PrintWriter(velocityRowOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            velocityRow_PrintWriter.println("n,sum,mean,max");
            for (int rowIndex = 0; rowIndex < velocityNumberOfRows; rowIndex++) {
                double[] row = (double[]) velocityDataValues.get(rowIndex);
                double max = Double.MIN_NORMAL;
                double sum = 0.0d;
                double n = 0;
                double value;
                boolean velocityT1 = false;
                boolean velocityT2 = false;
                boolean velocityT3 = false;
                boolean velocityT4 = false;
                for (int column = 0; column < row.length; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        n++;
                        sum += value;
                        max = Math.max(max, value);
                        totalVelocityN++;
                        totalVelocitySum += value;
                        totalVelocityMax = Math.max(totalVelocityMax, value);
                        // Velocity thesholds 4, 6, 8, 10
                        if (value > 4) {
                            //velocityT1Count++;
                            velocityT1 = true;
                            if (value > 6) {
                                //velocityT2Count++;
                                velocityT2 = true;
                                if (value > 8) {
                                    //velocityT3Count++;
                                    velocityT3 = true;
                                    if (value > 10) {
                                        //velocityT4Count++;
                                        velocityT4 = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (velocityT1) {
                    velocityT1Count++;
                    if (velocityT2) {
                        velocityT2Count++;
                        if (velocityT3) {
                            velocityT3Count++;
                            if (velocityT4) {
                                velocityT4Count++;
                            }
                        }
                    }
                }
                if (n > 0) {
                    double mean = sum / n;
                    System.out.println("n " + ((int) n));
                    System.out.println("sum " + sum);
                    System.out.println("mean " + mean);
                    System.out.println("max " + max);
                    velocityRow_PrintWriter.println(
                            n + "," + sum + "," + mean + "," + max);
                }
            }
            velocityRow_PrintWriter.close();

            // shear stress
            File shearStressRowOutputFile = new File(
                    shearStressRowOutputDirectory,
                    "shear stress " + i + ".csv");
            PrintWriter shearStressRow_PrintWriter = null;
            try {
                shearStressRow_PrintWriter = new PrintWriter(
                        shearStressRowOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            shearStressRow_PrintWriter.println("n,sum,mean,max");
            for (int rowIndex = 0; rowIndex < shearStressNumberOfRows; rowIndex++) {
                double[] row = (double[]) shearStressDataValues.get(rowIndex);
                double max = Double.MIN_NORMAL;
                double sum = 0.0d;
                double n = 0;
                double value;
                boolean shearStressT1 = false;
                boolean shearStressT2 = false;
                boolean shearStressT3 = false;
                boolean shearStressT4 = false;
                for (int column = 0; column < row.length; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        n++;
                        sum += value;
                        max = Math.max(max, value);
                        totalShearStressN++;
                        totalShearStressSum += value;
                        totalShearStressMax = Math.max(totalShearStressMax, value);
                        // ShearStress thesholds 200, 1000, 2500, 5000
                        if (value > 200) {
                            //shearStressT1Count++;
                            shearStressT1 = true;
                            if (value > 1000) {
                                //shearStressT2Count++;
                                shearStressT2 = true;
                                if (value > 2500) {
                                    //shearStressT3Count++;
                                    shearStressT3 = true;
                                    if (value > 5000) {
                                        //shearStressT4Count++;
                                        shearStressT4 = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (shearStressT1) {
                    shearStressT1Count++;
                    if (shearStressT2) {
                        shearStressT2Count++;
                        if (shearStressT3) {
                            shearStressT3Count++;
                            if (shearStressT4) {
                                shearStressT4Count++;
                            }
                        }
                    }
                }
                if (n > 0) {
                    double mean = sum / n;
                    System.out.println("n " + ((int) n));
                    System.out.println("sum " + sum);
                    System.out.println("mean " + mean);
                    System.out.println("max " + max);
                    shearStressRow_PrintWriter.println(
                            n + "," + sum + "," + mean + "," + max);
                }
            }
            shearStressRow_PrintWriter.close();

            // froude
            File froudeRowOutputFile = new File(
                    froudeRowOutputDirectory,
                    "froude " + i + ".csv");
            PrintWriter froudeRow_PrintWriter = null;
            try {
                froudeRow_PrintWriter = new PrintWriter(froudeRowOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            froudeRow_PrintWriter.println("n,sum,mean,max");
            for (int rowIndex = 0; rowIndex < froudeNumberOfRows; rowIndex++) {
                double[] row = (double[]) froudeDataValues.get(rowIndex);
                double max = Double.MIN_NORMAL;
                double sum = 0.0d;
                double n = 0;
                double value;
                boolean froudeT1 = false;
                boolean froudeT2 = false;
                boolean froudeT3 = false;
                boolean froudeT4 = false;
                for (int column = 0; column < row.length; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        n++;
                        sum += value;
                        max = Math.max(max, value);
                        totalFroudeN++;
                        totalFroudeSum += value;
                        totalFroudeMax = Math.max(totalFroudeMax, value);
                        // Froude thesholds 0.8, 0.9, 1.0, 1.1
                        if (value > 0.8) {
                            //froudeT1Count++;
                            froudeT1 = true;
                            if (value > 0.9) {
                                //froudeT2Count++;
                                froudeT2 = true;
                                if (value > 1.0) {
                                    //froudeT3Count++;
                                    froudeT3 = true;
                                    if (value > 1.1) {
                                        //froudeT4Count++;
                                        froudeT4 = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (froudeT1) {
                    froudeT1Count++;
                    if (froudeT2) {
                        froudeT2Count++;
                        if (froudeT3) {
                            froudeT3Count++;
                            if (froudeT4) {
                                froudeT4Count++;
                            }
                        }
                    }
                }
                if (n > 0) {
                    double mean = sum / n;
                    System.out.println("n " + ((int) n));
                    System.out.println("sum " + sum);
                    System.out.println("mean " + mean);
                    System.out.println("max " + max);
                    froudeRow_PrintWriter.println(
                            n + "," + sum + "," + mean + "," + max);
                }
            }
            froudeRow_PrintWriter.close();

            // Column Processing
            // depth
            File depthColOutputFile = new File(
                    depthColOutputDirectory,
                    "depth " + i + ".csv");
            PrintWriter depthCol_PrintWriter = null;
            try {
                depthCol_PrintWriter = new PrintWriter(depthColOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            depthCol_PrintWriter.println(
                    "Cross Section Column, Inundation Time, Time at Max, "
                    + "Time To Max, Max");
            int numberOfCols = ((double[]) depthDataValues.get(0)).length;
            double[] columnMax = new double[numberOfCols];
            String[] inundationTime = new String[numberOfCols];
            boolean[] inundated = new boolean[numberOfCols];
            String[] maxTime = new String[numberOfCols];
            for (int rowIndex = 0; rowIndex < depthNumberOfRows; rowIndex++) {
                double[] row = (double[]) depthDataValues.get(rowIndex);
                double value;
                for (int column = 0; column < numberOfCols; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        if (value == totalDepthMax) {
                            if (depthMaxCount == 0) {
                                depthMaxTime = (String) depthTimes.get(rowIndex);
                                if (inundationTime[column] == null) {
                                    depthInundationTimeMaxColumn = depthMaxTime;
                                } else {
                                    depthInundationTimeMaxColumn =
                                            inundationTime[column];
                                }
                            }
                            depthMaxCount++;
                        }
                        if (!inundated[column]) {
                            inundated[column] = true;
                            inundationTime[column] = (String) depthTimes.get(
                                    rowIndex);
                            maxTime[column] = (String) depthTimes.get(rowIndex);
                            columnMax[column] = value;
                        } else {
                            if (value > columnMax[column]) {
                                maxTime[column] = (String) depthTimes.get(
                                        rowIndex);
                                columnMax[column] = value;
                            }
                        }
                    }
                }
            }
            for (int column = 0; column < numberOfCols; column++) {
                int timeToMax = getTimeToMax(
                        inundationTime[column], maxTime[column]);
                String inTime = null;
                if (inundationTime[column] != null) {
                    inTime = (inundationTime[column].split(" "))[1];
                }
                String mTime = null;
                if (maxTime[column] != null) {
                    mTime = (maxTime[column].split(" "))[1];
                }
                depthCol_PrintWriter.println(column + ", " + inTime
                        + ", " + mTime + ", " + timeToMax + ", "
                        + columnMax[column]);
            }
            depthCol_PrintWriter.close();

            // velocity
            File velocityColOutputFile = new File(
                    velocityColOutputDirectory,
                    "velocity " + i + ".csv");
            PrintWriter velocityCol_PrintWriter = null;
            try {
                velocityCol_PrintWriter = new PrintWriter(velocityColOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            velocityCol_PrintWriter.println(
                    "Cross Section Column, Inundation Time, Time at Max, "
                    + "Time To Max, Max");
            numberOfCols = ((double[]) velocityDataValues.get(0)).length;
            columnMax = new double[numberOfCols];
            inundationTime = new String[numberOfCols];
            inundated = new boolean[numberOfCols];
            maxTime = new String[numberOfCols];
            for (int rowIndex = 0; rowIndex < velocityNumberOfRows; rowIndex++) {
                double[] row = (double[]) velocityDataValues.get(rowIndex);
                double value;
                for (int column = 0; column < numberOfCols; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        if (value == totalVelocityMax) {
                            if (velocityMaxCount == 0) {
                                velocityMaxTime = (String) velocityTimes.get(
                                        rowIndex);
                                if (inundationTime[column] == null) {
                                    velocityInundationTimeMaxColumn =
                                            velocityMaxTime;
                                } else {
                                    velocityInundationTimeMaxColumn =
                                            inundationTime[column];
                                }
                            }
                            velocityMaxCount++;
                        }
                        if (!inundated[column]) {
                            inundated[column] = true;
                            inundationTime[column] = (String) velocityTimes.get(
                                    rowIndex);
                            maxTime[column] = (String) velocityTimes.get(
                                    rowIndex);
                            columnMax[column] = value;
                        } else {
                            if (value > columnMax[column]) {
                                maxTime[column] = (String) velocityTimes.get(
                                        rowIndex);
                                columnMax[column] = value;
                            }
                        }
                    }
                }
            }
            for (int column = 0; column < numberOfCols; column++) {
                int timeToMax = getTimeToMax(
                        inundationTime[column], maxTime[column]);
                String inTime = null;
                if (inundationTime[column] != null) {
                    inTime = (inundationTime[column].split(" "))[1];
                }
                String mTime = null;
                if (maxTime[column] != null) {
                    mTime = (maxTime[column].split(" "))[1];
                }
                velocityRow_PrintWriter.println(column + ", " + inTime
                        + ", " + mTime + ", " + timeToMax + ", "
                        + columnMax[column]);
            }
            velocityRow_PrintWriter.close();

            // shearStress
            File shearStressColOutputFile = new File(
                    shearStressColOutputDirectory,
                    "shear stress " + i + ".csv");
            PrintWriter shearStressCol_PrintWriter = null;
            try {
                shearStressCol_PrintWriter = new PrintWriter(
                        shearStressColOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            shearStressCol_PrintWriter.println(
                    "Cross Section Column, Inundation Time, Time at Max, "
                    + "Time To Max, Max");
            numberOfCols = ((double[]) shearStressDataValues.get(0)).length;
            columnMax = new double[numberOfCols];
            inundationTime = new String[numberOfCols];
            inundated = new boolean[numberOfCols];
            maxTime = new String[numberOfCols];
            for (int rowIndex = 0; rowIndex < shearStressNumberOfRows; rowIndex++) {
                double[] row = (double[]) shearStressDataValues.get(rowIndex);
                double value;
                for (int column = 0; column < numberOfCols; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        if (value == totalShearStressMax) {
                            if (shearStressMaxCount == 0) {
                                shearStressMaxTime =
                                        (String) shearStressTimes.get(rowIndex);
                                if (inundationTime[column] == null) {
                                    shearStressInundationTimeMaxColumn =
                                            shearStressMaxTime;
                                } else {
                                    shearStressInundationTimeMaxColumn =
                                            inundationTime[column];
                                }
                            }
                            shearStressMaxCount++;
                        }
                        if (!inundated[column]) {
                            inundated[column] = true;
                            inundationTime[column] =
                                    (String) shearStressTimes.get(rowIndex);
                            maxTime[column] = (String) shearStressTimes.get(
                                    rowIndex);
                            columnMax[column] = value;
                        } else {
                            if (value > columnMax[column]) {
                                maxTime[column] = (String) shearStressTimes.get(
                                        rowIndex);
                                columnMax[column] = value;
                            }
                        }
                    }
                }
            }

            for (int column = 0; column < numberOfCols; column++) {
                int timeToMax = getTimeToMax(
                        inundationTime[column], maxTime[column]);
                String inTime = null;
                if (inundationTime[column] != null) {
                    inTime = (inundationTime[column].split(" "))[1];
                }
                String mTime = null;
                if (maxTime[column] != null) {
                    mTime = (maxTime[column].split(" "))[1];
                }
                shearStressCol_PrintWriter.println(column + ", " + inTime
                        + ", " + mTime + ", " + timeToMax + ", "
                        + columnMax[column]);
            }
            shearStressCol_PrintWriter.close();

            // froude
            File froudeColOutputFile = new File(
                    froudeColOutputDirectory,
                    "froude " + i + ".csv");
            PrintWriter froudeCol_PrintWriter = null;
            try {
                froudeCol_PrintWriter = new PrintWriter(froudeColOutputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossSectionProcessing.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            froudeCol_PrintWriter.println(
                    "Cross Section Column, Inundation Time, Time at Max, "
                    + "Time To Max, Max");
            numberOfCols = ((double[]) froudeDataValues.get(0)).length;
            columnMax = new double[numberOfCols];
            inundationTime = new String[numberOfCols];
            inundated = new boolean[numberOfCols];
            maxTime = new String[numberOfCols];
            for (int rowIndex = 0; rowIndex < froudeNumberOfRows; rowIndex++) {
                double[] row = (double[]) froudeDataValues.get(rowIndex);
                double value;
                for (int column = 0; column < numberOfCols; column++) {
                    value = Double.valueOf(row[column]);
                    if (value != 0 && value != -999) {
                        if (value == totalFroudeMax) {
                            if (froudeMaxCount == 0) {
                                froudeMaxTime = (String) froudeTimes.get(
                                        rowIndex);
                                if (inundationTime[column] == null) {
                                    froudeInundationTimeMaxColumn =
                                            froudeMaxTime;
                                } else {
                                    froudeInundationTimeMaxColumn =
                                            inundationTime[column];
                                }
                            }
                            froudeMaxCount++;
                        }
                        if (!inundated[column]) {
                            inundated[column] = true;
                            inundationTime[column] = (String) froudeTimes.get(
                                    rowIndex);
                            maxTime[column] = (String) froudeTimes.get(rowIndex);
                            columnMax[column] = value;
                        } else {
                            if (value > columnMax[column]) {
                                maxTime[column] = (String) froudeTimes.get(
                                        rowIndex);
                                columnMax[column] = value;
                            }
                        }
                    }
                }
            }
            for (int column = 0; column < numberOfCols; column++) {
                int timeToMax = getTimeToMax(
                        inundationTime[column], maxTime[column]);
                String inTime = null;
                if (inundationTime[column] != null) {
                    inTime = (inundationTime[column].split(" "))[1];
                }
                String mTime = null;
                if (maxTime[column] != null) {
                    mTime = (maxTime[column].split(" "))[1];
                }
                froudeCol_PrintWriter.println(column + ", " + inTime
                        + ", " + mTime + ", " + timeToMax + ", "
                        + columnMax[column]);
            }
            froudeCol_PrintWriter.close();

            // Row and Column output
            // depth
            totalDepthMean = totalDepthSum / totalDepthN;
            int timeToMaxDepth = getTimeToMax(
                    depthInundationTimeMaxColumn, depthMaxTime);
            String inTime = null;
            if (depthInundationTimeMaxColumn != null) {
                inTime = depthInundationTimeMaxColumn.split(" ")[1];
            }
            String mTime = null;
            if (depthMaxTime != null) {
                mTime = depthMaxTime.split(" ")[1];
            }
            depthRowAndCol_PrintWriter.println(
                    i + ", " + (int) totalDepthN + ", "
                    + totalDepthSum + ", " + totalDepthMean + ", "
                    + totalDepthMax + ", " + depthMaxCount + ", "
                    + inTime + ", " + mTime + ", "
                    + timeToMaxDepth + ", "
                    + depthT1Count + ", " + depthT2Count + ", "
                    + depthT3Count + ", " + depthT4Count);
            // velocity
            totalVelocityMean = totalVelocitySum / totalVelocityN;
            int timeToMaxVelocity = getTimeToMax(
                    velocityInundationTimeMaxColumn, velocityMaxTime);
            inTime = null;
            if (velocityInundationTimeMaxColumn != null) {
                inTime = velocityInundationTimeMaxColumn.split(" ")[1];
            }
            mTime = null;
            if (velocityMaxTime != null) {
                mTime = velocityMaxTime.split(" ")[1];
            }
            velocityRowAndCol_PrintWriter.println(
                    i + ", " + (int) totalVelocityN + ", "
                    + totalVelocitySum + ", " + totalVelocityMean + ", "
                    + totalVelocityMax + ", " + velocityMaxCount + ", "
                    + inTime + ", " + mTime + ", "
                    + timeToMaxVelocity + ", "
                    + velocityT1Count + ", " + velocityT2Count + ", "
                    + velocityT3Count + ", " + velocityT4Count);
            // shearStress
            totalShearStressMean = totalShearStressSum / totalShearStressN;
            int timeToMaxShearStress = getTimeToMax(
                    shearStressInundationTimeMaxColumn, shearStressMaxTime);
            inTime = null;
            if (shearStressInundationTimeMaxColumn != null) {
                inTime = shearStressInundationTimeMaxColumn.split(" ")[1];
            }
            mTime = null;
            if (shearStressMaxTime != null) {
                mTime = shearStressMaxTime.split(" ")[1];
            }
            shearStressRowAndCol_PrintWriter.println(
                    i + ", " + (int) totalShearStressN + ", "
                    + totalShearStressSum + ", " + totalShearStressMean + ", "
                    + totalShearStressMax + ", " + shearStressMaxCount + ", "
                    + inTime + ", " + mTime + ", "
                    + timeToMaxShearStress + ", "
                    + shearStressT1Count + ", " + shearStressT2Count + ", "
                    + shearStressT3Count + ", " + shearStressT4Count);
            // froude
            totalFroudeMean = totalFroudeSum / totalFroudeN;
            int timeToMaxFroude = getTimeToMax(
                    froudeInundationTimeMaxColumn, froudeMaxTime);
            inTime = null;
            if (froudeInundationTimeMaxColumn != null) {
                inTime = froudeInundationTimeMaxColumn.split(" ")[1];
            }
            mTime = null;
            if (froudeMaxTime != null) {
                mTime = froudeMaxTime.split(" ")[1];
            }
            froudeRowAndCol_PrintWriter.println(
                    i + ", " + (int) totalFroudeN + ", "
                    + totalFroudeSum + ", " + totalFroudeMean + ", "
                    + totalFroudeMax + ", " + froudeMaxCount + ", "
                    + inTime + ", " + mTime + ", "
                    + timeToMaxFroude + ", "
                    + froudeT1Count + ", " + froudeT2Count + ", "
                    + froudeT3Count + ", " + froudeT4Count);

            // Check File counts
            // depth
            int depthFileCountCheck =
                    depthInputDirectory.listFiles().length - 1;
            if (depthFileCountCheck != depthFileCount) {
                System.out.println(
                        "depthFileCountCheck != depthFileCount");
                System.out.println(
                        depthFileCountCheck + "!=" + depthFileCount);
            }
            // velocity
            int velocityFileCountCheck =
                    velocityInputDirectory.listFiles().length - 1;
            if (velocityFileCountCheck != velocityFileCount) {
                System.out.println(
                        "velocityFileCountCheck != velocityFileCount");
                System.out.println(
                        velocityFileCountCheck + "!=" + velocityFileCount);
            }
            // shearStress
            int shearStressFileCountCheck =
                    shearStressInputDirectory.listFiles().length - 1;
            if (shearStressFileCountCheck != shearStressFileCount) {
                System.out.println(
                        "shearStressFileCountCheck != shearStressFileCount");
                System.out.println(
                        shearStressFileCountCheck + "!=" + shearStressFileCount);
            }
            // froude
            int froudeFileCountCheck =
                    froudeInputDirectory.listFiles().length - 1;
            if (froudeFileCountCheck != froudeFileCount) {
                System.out.println(
                        "froudeFileCountCheck != froudeFileCount");
                System.out.println(
                        froudeFileCountCheck + "!=" + froudeFileCount);
            }
        }
        // Close output channels
        depthRowAndCol_PrintWriter.close();
        velocityRowAndCol_PrintWriter.close();
        shearStressRowAndCol_PrintWriter.close();
        froudeRowAndCol_PrintWriter.close();
    }

    /**
     * The difference in minutes between inundationTime and the maxTime.
     *
     * @param inundationTime
     * @param maxTime
     * @return The difference in minutes between inundationTime and the maxTime
     * as an int.
     */
    public int getTimeToMax(String inundationTime, String maxTime) {
        int result = 0;
        if (inundationTime != null) {
            String[] inundationDateTime = inundationTime.split(" ");
            String[] maxDateTime = maxTime.split(" ");
            if (inundationDateTime[0].equalsIgnoreCase(maxDateTime[0])) {
                String[] inundationTimeHoursMins =
                        inundationDateTime[1].split(":");
                int inundationHour =
                        Integer.valueOf(inundationTimeHoursMins[0]);
                int inundationMinutes =
                        Integer.valueOf(inundationTimeHoursMins[1]);
                String[] maxTimeHoursMins = maxDateTime[1].split(":");
                int maxHour = Integer.valueOf(maxTimeHoursMins[0]);
                int maxMinutes = Integer.valueOf(maxTimeHoursMins[1]);
                result = ((maxHour * 60) + maxMinutes)
                        - ((inundationHour * 60) + inundationMinutes);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return result;
    }

    /**
     * This reads the input data into an ArrayList[] result: result[0] is a list
     * of times; result[1] is a list of data values.
     *
     * @param inputFile
     * @return an ArrayList[] result: result[0] is a list of times; result[1] is
     * a list of data values.
     */
    public ArrayList[] readIntoArrayList(
            File inputFile) {
        ArrayList[] result = new ArrayList[2];
        ArrayList dataValues = new ArrayList();
        ArrayList times = new ArrayList();
        BufferedReader a_BufferedReader;
        StreamTokenizer a_StreamTokenizer;
        try {
            a_BufferedReader = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(inputFile)));
            a_StreamTokenizer = new StreamTokenizer(a_BufferedReader);
            a_StreamTokenizer.resetSyntax();
            Generic_StaticIO.setStreamTokenizerSyntax1(a_StreamTokenizer);
            a_StreamTokenizer.wordChars('(', '(');
            a_StreamTokenizer.wordChars(')', ')');
            a_StreamTokenizer.wordChars(':', ':');
            a_StreamTokenizer.wordChars('^', '^');
            a_StreamTokenizer.wordChars('/', '/');

            String line = null;
            int lineCount = 0;
            int tokenType;
            a_StreamTokenizer.nextToken();
            // Skip header
            a_StreamTokenizer.nextToken();
            a_StreamTokenizer.nextToken();
            tokenType = a_StreamTokenizer.nextToken();
            while (tokenType != StreamTokenizer.TT_EOF) {
                switch (tokenType) {
                    case StreamTokenizer.TT_EOL:
                        String[] values = line.split(",");
                        double[] row = new double[values.length - 1];
                        double max = Double.MIN_NORMAL;
                        double sum = 0.0d;
                        double n = 0;
                        double value;
                        for (int v = 1; v < values.length; v++) {
                            if (values[v].trim().equalsIgnoreCase("")) {
                                System.out.println(
                                        "value is nodata " + values[v]);
                            } else {
                                value = Double.valueOf(values[v]);
                                if (value != 0 && value != -999) {
                                    n++;
                                    sum += value;
                                    max = Math.max(max, value);
                                    row[v] = value;
                                }
                            }
                        }
                        // Store and return time here
                        times.add(values[0]);
                        dataValues.add(row);
                        if (n > 0) {
                            double mean = sum / n;
                            System.out.println("n " + n);
                            System.out.println("sum " + sum);
                            System.out.println("mean " + mean);
                            System.out.println("max " + max);
                        }
                        break;
                    case StreamTokenizer.TT_WORD:
                        lineCount++;
                        line = a_StreamTokenizer.sval;
                        System.out.println("line " + lineCount + " " + line);
                        break;
                }
                tokenType = a_StreamTokenizer.nextToken();
            }
        } catch (IOException ioe0) {
            System.err.println(ioe0.getMessage());
        }
        result[0] = times;
        result[1] = dataValues;
        return result;
    }
}
