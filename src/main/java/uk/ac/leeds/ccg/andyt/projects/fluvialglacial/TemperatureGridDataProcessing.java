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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import uk.ac.leeds.ccg.andyt.generic.math.Generic_BigDecimal;
import uk.ac.leeds.ccg.andyt.grids.core.AbstractGridStatistics;
import uk.ac.leeds.ccg.andyt.grids.core.Grid2DSquareCellDouble;
import uk.ac.leeds.ccg.andyt.grids.core.Grid2DSquareCellDoubleFactory;
import uk.ac.leeds.ccg.andyt.grids.core.GridStatistics0;
import uk.ac.leeds.ccg.andyt.grids.core.GridStatistics1;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.andyt.grids.exchange.ESRIAsciiGridExporter;
import uk.ac.leeds.ccg.andyt.grids.exchange.ESRIAsciiGridImporter;
import uk.ac.leeds.ccg.andyt.grids.utilities.FileCreator;

/**
 * A class developed for processing stream temperature data.
 */
public class TemperatureGridDataProcessing {

    File _Directory_File;
    Grids_Environment _Grids_Environment;
    Grid2DSquareCellDoubleFactory _Grid2DSquareCellDoubleFactory;
    //Grid2DSquareCellProcessor _Grid2DSquareCellProcessor;
    ESRIAsciiGridImporter _ESRIAsciiGridImporter;
    boolean _HandleOutOfMemoryError;
    String _FileSeparator;
    //ImageExporter _ImageExporter;
    String[] _ImageTypes;
    ESRIAsciiGridExporter _ESRIAsciiGridExporter;

    public TemperatureGridDataProcessing() {
        this(FileCreator.createNewFile());
    }

    public TemperatureGridDataProcessing(File directory) {
        _Directory_File = directory;
        _Grids_Environment = new Grids_Environment();
        _Grid2DSquareCellDoubleFactory = new Grid2DSquareCellDoubleFactory(
                _Grids_Environment, _HandleOutOfMemoryError);
//        // No need to set these here!.
//        _Grid2DSquareCellDoubleFactory.set_NoDataValue(-9999.0d);
//        BigDecimal[] dimensions = new BigDecimal[5];
//        dimensions[0] = new BigDecimal("1");
//        dimensions[1] = new BigDecimal("1");
//        dimensions[2] = new BigDecimal("1");
//        dimensions[3] = new BigDecimal("1");
//        dimensions[4] = new BigDecimal("1");
//        _Grid2DSquareCellDoubleFactory.set_Dimensions(dimensions);
//        _Grid2DSquareCellProcessor = new Grid2DSquareCellProcessor(
//                _Grids_Environment);
//        _Grid2DSquareCellProcessor.set_Directory(
//                directory, false, _HandleOutOfMemoryError);
    }

    /**
     * If no argument is input, the users current working directory is the 
     * location where input data is expected to be and where output data is 
     * written.
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
            if (!directory.exists()){
                throw new IOException("Directory " + directory + " does not "
                        + "exist");
            }
            //File directory = new File( "/scratch01/Work/Projects/"
            //            + "StreamTemperatureGridGeneralisation/workspace/");
            new TemperatureGridDataProcessing(directory).run();
        } catch (Error e) {
            System.err.println(e.getLocalizedMessage());
            //e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            //e.printStackTrace();
        }
    }

    public void run() throws IOException {
        //runTest(intervalRange, startIntervalBound);
        // Choose interval and start
        double intervalRange = 0.25d;//0.125d; // 0.25
        double startIntervalBound = 0.0d;

        runJuly(intervalRange, startIntervalBound);
        runAugust(intervalRange, startIntervalBound);
    }

    public void runTest(
            double intervalRange,
            double startIntervalBound) {
        String month = "August";
        File inputDirectory = new File(
                _Directory_File.getAbsolutePath() + "/input/" + month + "/");
        File outputDirectory = getOutputFile(
                intervalRange, startIntervalBound, month);
        File inputFile = new File(inputDirectory, "27t1700.txt");
        getStatistics(
                inputFile,
                intervalRange,
                startIntervalBound,
                outputDirectory);
    }

    public File getOutputFile(
            double intervalRange,
            double startIntervalBound,
            String month) {
        return new File(
                _Directory_File.getAbsolutePath() + "/output/" + 
                intervalRange + "_" + startIntervalBound + "/" + month + "/");
    }

    public void runJuly(
            double intervalRange,
            double startIntervalBound)
            throws IOException {
        String month = "July";
        File inputDirectory = new File(
                _Directory_File.getAbsolutePath() + "/input/" + month);
        File outputDirectory = getOutputFile(
                intervalRange, startIntervalBound, month);
        outputDirectory.mkdirs();
        PrintWriter output_PrintWriter = new PrintWriter(
                new File(outputDirectory, month + ".csv"));
        int expectedNumberOfFiles = (5 * 24 + 1);
        System.out.println("expectedNumberOfFiles " + expectedNumberOfFiles);
        System.out.println("inputDirectory.listFiles().length " + 
                inputDirectory.listFiles().length);
        Object[] result = new Object[expectedNumberOfFiles];
        File inputFile;
        String filename;
        int resultIndex = 0;

        // Get statistics for hours in July files
        for (int day = 26; day < 31; day++) {
            for (int hour = 0; hour < 24; hour++) {
                filename = "" + day + "t";
                if (hour < 10) {
                    filename += "0";
                }
                filename += "" + hour + "00.txt";
                inputFile = new File(
                        inputDirectory,
                        filename);
                result[resultIndex] = getStatistics(
                        inputFile,
                        intervalRange,
                        startIntervalBound,
                        outputDirectory);
                resultIndex++;
            }
        }
        inputFile = new File(
                inputDirectory,
                "31t0000.txt");
        result[resultIndex] = getStatistics(
                inputFile,
                intervalRange,
                startIntervalBound,
                outputDirectory);

        // Dominance
        //double[] dominance = getDominance(result);

        // Write results
        String line = getOutputHeader();
        output_PrintWriter.println(line);
        System.out.println(line);
        resultIndex = 0;
        Object[] resultPart;
        for (int day = 26; day < 31; day++) {
            for (int hour = 0; hour < 24; hour++) {
                line = "" + day + "t";
                if (hour < 10) {
                    line += "0";
                }
                line += "" + hour + "00";
                resultPart = (Object[]) result[resultIndex];
                for (int i = 0; i < resultPart.length; i++) {
                    line += "," + resultPart[i];
                }
                //line += "," + dominance[resultIndex];
                output_PrintWriter.println(line);
                System.out.println(line);
                resultIndex++;
            }
        }
        line = "31t0000";
        resultPart = (Object[]) result[resultIndex];
        for (int i = 0; i < resultPart.length; i++) {
            line += "," + resultPart[i];
        }
        //line += "," + dominance[resultIndex];
        output_PrintWriter.println(line);
        output_PrintWriter.close();
    }

    public void runAugust(
            double intervalRange,
            double startIntervalBound)
            throws IOException {
        String month = "August";
        File inputDirectory = new File(
                _Directory_File.getAbsolutePath() + "/input/" + month);
        File outputDirectory = getOutputFile(
                intervalRange, startIntervalBound, month);
        outputDirectory.mkdirs();
        PrintWriter output_PrintWriter = new PrintWriter(
                new File(outputDirectory, month + ".csv"));
        int expectedNumberOfFiles = (6 * 24 + 1);
        System.out.println("expectedNumberOfFiles " + expectedNumberOfFiles);
        System.out.println("inputDirectory.listFiles().length " + 
                inputDirectory.listFiles().length);
        Object[] statisticsPerHour = new Object[expectedNumberOfFiles];
        File inputFile;
        String filename;
        int resultIndex = 0;

        // Get statistics for hours in July August
        for (int day = 24; day < 30; day++) {
            for (int hour = 0; hour < 24; hour++) {
                filename = "" + day + "t";
                if (hour < 10) {
                    filename += "0";
                }
                filename += "" + hour + "00.txt";
                inputFile = new File(
                        inputDirectory,
                        filename);
                statisticsPerHour[resultIndex] = getStatistics(
                        inputFile,
                        intervalRange,
                        startIntervalBound,
                        outputDirectory);
                resultIndex++;
            }
        }
        inputFile = new File(
                inputDirectory,
                "30t0000.txt");
        statisticsPerHour[resultIndex] = getStatistics(
                inputFile,
                intervalRange,
                startIntervalBound,
                outputDirectory);

        // Dominance
        //double[] dominance = getDominance(statisticsPerHour);

        // Write results
        String line = getOutputHeader();
        output_PrintWriter.println(line);
        System.out.println(line);
        resultIndex = 0;
        for (int day = 24; day < 30; day++) {
            for (int hour = 0; hour < 24; hour++) {
                line = "" + day + "t";
                if (hour < 10) {
                    line += "0";
                }
                line += "" + hour + "00";
                Object[] resultPart = (Object[]) statisticsPerHour[resultIndex];
                for (int i = 0; i < resultPart.length; i++) {
                    line += "," + resultPart[i];
                }
                //line += "," + dominance[resultIndex];
                output_PrintWriter.println(line);
                System.out.println(line);
                resultIndex++;
            }
        }
        line = "30t0000";
        Object[] resultPart = (Object[]) statisticsPerHour[resultIndex];
        for (int i = 0; i < resultPart.length; i++) {
            line += "," + resultPart[i];
        }
        //line += "," + dominance[resultIndex];
        output_PrintWriter.println(line);
        System.out.println(line);
        output_PrintWriter.close();
    }

    public String getOutputHeader() {
        return "input,min,max,mean,standard deviation,sum,n,"
                + "oneCounter,zeroCounter,"
                + "minIgnoringZeroAndOne,maxIgnoringZeroAndOne,"
                + "rangeIgnoringZeroAndOne,meanIgnoringZeroAndOne,"
                + "moment1,moment2,moment3,moment4,"
                + "skewnessCyhelsky,skewness,kurtosis,variety,"
                + "upperQuartile,median,lowerQuartile,"
                + "mode,intervalVariety,diversity,"
                + "intervalVarietyEvenness,numberOfIntervalsInRangeEvenness,"
                + "maximumDiversityOverIntervalsWithValues,"
                + "dominanceOverIntervalsWithValues,"
                + "maximumDiversityOverIntervalsInObservedRange,"
                + "dominanceOverIntervalsInObservedRange";
    }

    /**
     * @return result Object[]
     * result[0] = min
     * result[1] = max
     * result[2] = mean
     * result[3] = standard deviation
     * result[4] = sum
     * result[5] = n
     * result[6] = oneCounter
     * result[7] = zeroCounter
     * result[8] = minIgnoringZeroAndOne
     * result[9] = maxIgnoringZeroAndOne
     * result[10] = rangeIgnoringZeroAndOne
     * result[11] = meanIgnoringZeroAndOne
     * result[12] = moment1
     * result[13] = moment2
     * result[14] = moment3
     * result[15] = moment4
     * result[16] = skewnessCyhelsky
     * result[17] = skewness
     * result[18] = kurtosis
     * result[19] = variety
     * result[20] = upperQuartile
     * result[21] = median
     * result[22] = lowerQuartile
     * result[23] = mode
     * result[24] = intervalVariety
     * result[25] = diversity
     * result[26] = intervalVarietyEvenness
     * result[27] = numberOfIntervalsInRangeEvenness
     * result[28] = maximumDiversityOverIntervalsWithValues
     * result[29] = dominanceOverIntervalsWithValues
     * result[30] = maximumDiversityOverIntervalsInObservedRange
     * result[31] = dominanceOverIntervalsInObservedRange
     */
    public Object[] getStatistics(
            File inputFile,
            double intervalRange,
            double startIntervalBound,
            File outputDirectory) {
        RoundingMode roundingMode = RoundingMode.HALF_UP;
        int numberOfOutputs = 32;
        Object[] result = new Object[numberOfOutputs];
        int outputIndex = 0;
//        Grid2DSquareCellDouble a_Grid2DSquareCellDouble = 
//                (Grid2DSquareCellDouble) 
//                _Grid2DSquareCellProcessor._Grid2DSquareCellDoubleFactory.create(
//                inputFile);
        Grid2DSquareCellDouble a_Grid2DSquareCellDouble = 
                (Grid2DSquareCellDouble) _Grid2DSquareCellDoubleFactory.create(
                inputFile);
//        System.out.println("a_Grid2DSquareCellDouble " + 
//                a_Grid2DSquareCellDouble.toString(_HandleOutOfMemoryError));
        AbstractGridStatistics a_Grid2DSquareCellDouble_AbstractGridStatistics = 
                a_Grid2DSquareCellDouble.getGridStatistics(
                _HandleOutOfMemoryError);
//        System.out.println("a_Grid2DSquareCellDouble_AbstractGridStatistics " + 
//                a_Grid2DSquareCellDouble_AbstractGridStatistics.toString(
//                _HandleOutOfMemoryError));
        GridStatistics1 a_Grid2DSquareCellDouble_GridStatistics1 = 
                (GridStatistics1) a_Grid2DSquareCellDouble_AbstractGridStatistics;
//        System.out.println("a_Grid2DSquareCellDouble_GridStatistics1 " + 
//                a_Grid2DSquareCellDouble_GridStatistics1.toString(
//                _HandleOutOfMemoryError));
        GridStatistics0 a_Grid2DSquareCellDouble_GridStatistics0 = 
                new GridStatistics0(a_Grid2DSquareCellDouble);
//        System.out.println("a_Grid2DSquareCellDouble_GridStatistics0 " + 
//                a_Grid2DSquareCellDouble_GridStatistics0.toString(
//                _HandleOutOfMemoryError));

        int numberOfDecimalPlaces_10 = 10;
        int numberOfDecimalPlaces_100 = 100;
        int scale_10 = 10;

        BigDecimal mean_BigDecimal = 
                a_Grid2DSquareCellDouble_GridStatistics1.getArithmeticMeanBigDecimal(
                numberOfDecimalPlaces_10, _HandleOutOfMemoryError);
        double min = a_Grid2DSquareCellDouble_GridStatistics1.getMinDouble(
                _HandleOutOfMemoryError);
        System.out.println("min " + min);
        result[outputIndex] = min;
        outputIndex++;
        double max = a_Grid2DSquareCellDouble_GridStatistics1.getMaxDouble(
                _HandleOutOfMemoryError);
        System.out.println("max " + max);
        result[outputIndex] = max;
        outputIndex++;
        System.out.println("mean " + mean_BigDecimal.toString());
        result[outputIndex] = mean_BigDecimal.doubleValue();
        outputIndex++;
        BigDecimal standardDeviation_BigDecimal = 
                a_Grid2DSquareCellDouble_GridStatistics1.getStandardDeviationBigDecimal(
                numberOfDecimalPlaces_10, _HandleOutOfMemoryError);
        System.out.println("standard deviation " + 
                standardDeviation_BigDecimal.toString());
        result[outputIndex] = standardDeviation_BigDecimal.doubleValue();
        outputIndex++;
        BigDecimal sum_BigDecimal = 
                a_Grid2DSquareCellDouble_GridStatistics1.getSumBigDecimal(
                _HandleOutOfMemoryError).setScale(scale_10, roundingMode);
        System.out.println("sum " + sum_BigDecimal.toString());
        result[outputIndex] = sum_BigDecimal.doubleValue();
        outputIndex++;
        System.out.println("number of non-NoDataValues " + 
                a_Grid2DSquareCellDouble_GridStatistics1.getNonNoDataValueCountBigInteger(
                _HandleOutOfMemoryError).toString());

        BigDecimal intervalRange_BigDecimal = new BigDecimal("" + intervalRange);
        BigDecimal startIntervalBound_BigDecimal = 
                new BigDecimal("" + startIntervalBound);

        double noDataValue = 
                a_Grid2DSquareCellDouble.get_NoDataValue(_HandleOutOfMemoryError);
        //System.out.println("NoDataValue " + noDataValue);

        long row;
        long col;
        long a_Grid2DSquareCellDouble_NRows = 
                a_Grid2DSquareCellDouble.get_NRows(_HandleOutOfMemoryError);
        long a_Grid2DSquareCellDouble_NCols = 
                a_Grid2DSquareCellDouble.get_NCols(_HandleOutOfMemoryError);
//        HashMap<Double,Long> value_Grid2DSquareCellDoubleValue_HashMap = 
//                new HashMap<Double,Long>();
//        HashMap<Long,Long> interval_Grid2DSquareCellDoubleValue_HashMap = 
//                new HashMap<Long,Long>();
        TreeMap<Double, Long> value_Grid2DSquareCellDoubleValue_TreeMap = 
                new TreeMap<Double, Long>();
        TreeMap<Long, Long> interval_Grid2DSquareCellDoubleValue_TreeMap = 
                new TreeMap<Long, Long>();

        double cellValue;
        BigDecimal cellValue_BigDecimal;
        long count;
        long interval_ID;
        long n = 0;
        int oneCounter = 0;
        int zeroCounter = 0;
        double minIgnoringZeroAndOne = Double.MAX_VALUE;
        double maxIgnoringZeroAndOne = Double.MIN_VALUE;

        // Recalculate statistics ignoring zeros and ones and initialise
        // value and interval maps and
        BigDecimal sumIgnoringOne_BigDecimal = BigDecimal.ZERO;
        for (row = 0; row < a_Grid2DSquareCellDouble_NRows; row++) {
            for (col = 0; col < a_Grid2DSquareCellDouble_NCols; col++) {
                cellValue = a_Grid2DSquareCellDouble.getCell(
                        row, col, _HandleOutOfMemoryError);
                if (cellValue != noDataValue) {
                    if (cellValue == 1.0d) {
                        oneCounter++;
                    } else {
                        if (cellValue == 0.0d) {
                            zeroCounter++;
                        } else {
                            n++;
                            cellValue_BigDecimal = new BigDecimal(
                                    Double.toString(cellValue));
                            sumIgnoringOne_BigDecimal = 
                                    sumIgnoringOne_BigDecimal.add(
                                    cellValue_BigDecimal);
                            minIgnoringZeroAndOne = Math.min(
                                    minIgnoringZeroAndOne, cellValue);
                            maxIgnoringZeroAndOne = Math.max(
                                    maxIgnoringZeroAndOne, cellValue);
                            if (value_Grid2DSquareCellDoubleValue_TreeMap.containsKey(cellValue)) {
                                count = (Long) value_Grid2DSquareCellDoubleValue_TreeMap.get(cellValue);
                                count++;
                                value_Grid2DSquareCellDoubleValue_TreeMap.put(cellValue, count);
                            } else {
                                value_Grid2DSquareCellDoubleValue_TreeMap.put(cellValue, 1L);
                            }
                            interval_ID = getInterval(
                                    cellValue,
                                    intervalRange,
                                    startIntervalBound);
                            if (interval_Grid2DSquareCellDoubleValue_TreeMap.containsKey(interval_ID)) {
                                count = (Long) interval_Grid2DSquareCellDoubleValue_TreeMap.get(interval_ID);
                                count++;
                                interval_Grid2DSquareCellDoubleValue_TreeMap.put(interval_ID, count);
                            } else {
                                interval_Grid2DSquareCellDoubleValue_TreeMap.put(interval_ID, 1L);
                            }
                        }
                    }
                }
            }
        }
        if (n != 0) {
            System.out.println("number of values other than 1 or 0 " + n);
            result[outputIndex] = n;
            outputIndex++;
            System.out.println(
                    "number of values of 1 which are ignored " + oneCounter);
            result[outputIndex] = oneCounter;
            outputIndex++;
            System.out.println(
                    "number of values of 0 which are ignored " + zeroCounter);
            result[outputIndex] = zeroCounter;
            outputIndex++;
            System.out.println(
                    "min value ignoring values of 0 and 1 " + 
                    minIgnoringZeroAndOne);
            result[outputIndex] = minIgnoringZeroAndOne;
            System.out.println(
                    "max value ignoring values of 0 and 1 " + 
                    maxIgnoringZeroAndOne);
            outputIndex++;
            result[outputIndex] = maxIgnoringZeroAndOne;
            outputIndex++;
            double rangeIgnoringZeroAndOne = maxIgnoringZeroAndOne - 
                    minIgnoringZeroAndOne;
            System.out.println(
                    "range ignoring values of 0 and 1 " + 
                    rangeIgnoringZeroAndOne);
            result[outputIndex] = rangeIgnoringZeroAndOne;
            outputIndex++;
            BigDecimal n_BigDecimal = new BigDecimal(n);
            BigDecimal meanIgnoringZeroAndOne_BigDecimal = 
                    sumIgnoringOne_BigDecimal.divide(
                    n_BigDecimal, scale_10, roundingMode);
            System.out.println("mean ignoring values of 0 and 1 " + 
                    meanIgnoringZeroAndOne_BigDecimal.doubleValue());
            result[outputIndex] = meanIgnoringZeroAndOne_BigDecimal;
            outputIndex++;

            // Calculate mean and moments, mode and quartiles
            long numberOfValuesAboveMean = 0L;
            long numberOfValuesBelowMean = 0L;
            // sum of mean differences
            BigDecimal moment1 = BigDecimal.ZERO;
            // sum of mean differences squared
            BigDecimal moment2 = BigDecimal.ZERO;
            // sum of mean differences cubed
            BigDecimal moment3 = BigDecimal.ZERO;
            // sum of mean differences to power 4
            BigDecimal moment4 = BigDecimal.ZERO;
            BigDecimal differenceFromMean;
            HashSet<Double> mode_HashSet = new HashSet<Double>();
            long modeCount = 0;
            double medianIndex = n / 2.0d;
            long medianIndex_long = (long) medianIndex;
            double median = Double.NaN;
            boolean medianUnset = true;
            // if modeIndex == modeIndex_long then there is a single mode 
            // otherwise average most frequent numbers to calculate mode.
            double lowerQuartileIndex = n / 4.0d;
            long lowerQuartileIndex_long = (long) lowerQuartileIndex;
            double lowerQuartile = Double.NaN;
            boolean lowerQuartileUnset = true;
            double upperQuartileIndex = (n * 3) / 4.0d;
            long upperQuartileIndex_long = (long) upperQuartileIndex;
            double upperQuartile = Double.NaN;
            boolean upperQuartileUnset = true;
            long sumCount = 0L;
            System.out.println("<CellValues with counts>");
            Iterator<Double> a_Iterator = value_Grid2DSquareCellDoubleValue_TreeMap.keySet().iterator();
            while (a_Iterator.hasNext()) {
                cellValue = a_Iterator.next();
                count = (Long) value_Grid2DSquareCellDoubleValue_TreeMap.get(cellValue);
                System.out.println("cellValue " + cellValue + " number of such values " + count);
                cellValue_BigDecimal = new BigDecimal(cellValue);
                if (cellValue_BigDecimal.compareTo(meanIgnoringZeroAndOne_BigDecimal) == -1) {
                    numberOfValuesBelowMean++;
                } else {
                    if (cellValue_BigDecimal.compareTo(meanIgnoringZeroAndOne_BigDecimal) == 1) {
                        numberOfValuesAboveMean++;
                    }
                }
                differenceFromMean = cellValue_BigDecimal.subtract(
                        meanIgnoringZeroAndOne_BigDecimal);
                for (int i = 0; i < count; i++) {
                    moment1 = moment1.add(differenceFromMean);
                    moment2 = moment2.add(differenceFromMean.pow(2));
                    moment3 = moment3.add(differenceFromMean.pow(3));
                    moment4 = moment4.add(differenceFromMean.pow(4));
                }
                sumCount += count;
                if (lowerQuartileUnset) {
                    if (sumCount > lowerQuartileIndex_long) {
                        lowerQuartile = cellValue;
                        lowerQuartileUnset = false;
                    }
                }
                if (medianUnset) {
                    if (sumCount > medianIndex_long) {
                        median = cellValue;
                        medianUnset = false;
                    }
                }
                if (upperQuartileUnset) {
                    if (sumCount > upperQuartileIndex_long) {
                        upperQuartile = cellValue;
                        upperQuartileUnset = false;
                    }
                }
                if (count > modeCount) {
                    modeCount = count;
                    mode_HashSet = new HashSet<Double>();
                    mode_HashSet.add(cellValue);
                } else {
                    if (count == modeCount) {
                        mode_HashSet.add(cellValue);
                    }
                }
            }
            System.out.println("</CellValues with counts>");
            moment1 = moment1.divide(n_BigDecimal, scale_10, roundingMode);
            System.out.println("moment1 " + moment1.setScale(scale_10, roundingMode));
            result[outputIndex] = moment1.doubleValue();
            outputIndex++;
            moment2 = moment2.divide(n_BigDecimal, scale_10, roundingMode);
            System.out.println("moment2 " + moment2.setScale(scale_10, roundingMode));
            result[outputIndex] = moment2.doubleValue();
            outputIndex++;
            moment3 = moment3.divide(n_BigDecimal, scale_10, roundingMode);
            System.out.println("moment3 " + moment3.setScale(scale_10, roundingMode));
            result[outputIndex] = moment3.doubleValue();
            outputIndex++;
            moment4 = moment4.divide(n_BigDecimal, scale_10, roundingMode);
            System.out.println("moment4 " + moment4.setScale(scale_10, roundingMode));
            result[outputIndex] = moment4.doubleValue();
            outputIndex++;

            // Skewness
            // Cyhelsky's skewness coefficient
            double skewnessCyhelsky = 
                    (numberOfValuesBelowMean - numberOfValuesAboveMean) / (double) n;
            System.out.println("skewnessCyhelsky " + skewnessCyhelsky);
            result[outputIndex] = skewnessCyhelsky;
            outputIndex++;
            BigDecimal sqrt_moment2 = Generic_BigDecimal.power(
                    moment2,
                    Generic_BigDecimal.HALF,
                    numberOfDecimalPlaces_10,
                    roundingMode);
            BigDecimal skewnessDenominator = sqrt_moment2.multiply(moment2);
            BigDecimal skewness = 
                    moment3.divide(skewnessDenominator, 100, roundingMode);
            System.out.println("skewness " + 
                    skewness.setScale(scale_10, roundingMode));
            result[outputIndex] = skewness.doubleValue();
            outputIndex++;

            BigDecimal kurtosisDenominatorPart = moment2.multiply(moment2);
            BigDecimal kurtosis = 
                    (moment4.divide(kurtosisDenominatorPart, 100, roundingMode))
                    .subtract(new BigDecimal("3"));
            System.out.println("kurtosis " + 
                    kurtosis.setScale(scale_10, roundingMode));
            result[outputIndex] = kurtosis.doubleValue();
            outputIndex++;
            long variety = value_Grid2DSquareCellDoubleValue_TreeMap.size();
            System.out.println("number of different values " + variety);
            result[outputIndex] = variety;
            outputIndex++;

            System.out.println("upperQuartile " + upperQuartile);
            result[outputIndex] = upperQuartile;
            outputIndex++;
            System.out.println("median " + median);
            result[outputIndex] = median;
            outputIndex++;
            System.out.println("lowerQuartile " + lowerQuartile);
            result[outputIndex] = lowerQuartile;
            outputIndex++;

            //System.out.println("mode_HashSet.size() " + mode_HashSet.size());
            a_Iterator = mode_HashSet.iterator();
            BigDecimal modeSum = BigDecimal.ZERO;
            double modePart;
            while (a_Iterator.hasNext()) {
                modePart = a_Iterator.next();
                //System.out.println("modePart " + modePart);
                modeSum = modeSum.add(new BigDecimal(modePart));
            }
            //System.out.println("modeSum " + modeSum.toString());
            BigDecimal mode;
            if (mode_HashSet.isEmpty()) {
                mode = BigDecimal.ZERO;
            } else {
                BigDecimal divisor = new BigDecimal("" + mode_HashSet.size());
                mode = modeSum.divide(divisor, 100, roundingMode);
            }
            System.out.println("mode " + 
                    mode.setScale(scale_10, roundingMode).toString());
            result[outputIndex] = mode.doubleValue();
            outputIndex++;

            // Find mode interval
            int intervalVariety = interval_Grid2DSquareCellDoubleValue_TreeMap.size();
            System.out.println("number of different intervals with values " + intervalVariety);
            result[outputIndex] = intervalVariety;
            outputIndex++;
            HashSet<Long> modeInterval_HashSet = new HashSet<Long>();
            long modeIntervalCount = 0;

            double minOfInterval;
            double maxOfInterval;
            BigDecimal minOfInterval_BigDecimal;
            BigDecimal maxOfInterval_BigDecimal;

            System.out.println("<Intervals with counts>");
            Iterator<Long> b_Iterator = 
                    interval_Grid2DSquareCellDoubleValue_TreeMap.keySet().iterator();
            while (b_Iterator.hasNext()) {
                interval_ID = b_Iterator.next();
                //minOfInterval = ((double) interval_ID * intervalRange) + startIntervalBound;
                minOfInterval_BigDecimal = 
                        (new BigDecimal(interval_ID).multiply(intervalRange_BigDecimal))
                        .add(startIntervalBound_BigDecimal);
                //maxOfInterval = minOfInterval + intervalRange;
                maxOfInterval_BigDecimal = minOfInterval_BigDecimal.add(intervalRange_BigDecimal);
                count = (Long) interval_Grid2DSquareCellDoubleValue_TreeMap.get(interval_ID);
//                System.out.println(
//                        "minOfInterval " + minOfInterval + 
//                        " maxOfInterval " + maxOfInterval + 
//                        " number of such values " + count);
                System.out.println(
                        "minOfInterval_BigDecimal " + minOfInterval_BigDecimal + 
                        " maxOfInterval_BigDecimal " + maxOfInterval_BigDecimal + 
                        " number of such values " + count);
                if (count > modeCount) {
                    modeCount = count;
                    modeInterval_HashSet = new HashSet<Long>();
                    modeInterval_HashSet.add(interval_ID);
                } else {
                    if (count == modeCount) {
                        modeInterval_HashSet.add(interval_ID);
                    }
                }
            }
            System.out.println("</Intervals with counts>");
            //System.out.println("modeInterval_HashSet.size() " + modeInterval_HashSet.size());
            b_Iterator = modeInterval_HashSet.iterator();
            BigDecimal modeIntervalSum = BigDecimal.ZERO;
            double adjuster = startIntervalBound + (intervalRange / 2.0d);
            while (b_Iterator.hasNext()) {
                modePart = (b_Iterator.next() * intervalRange) + adjuster;
                //System.out.println("modeIntervalPart " + modePart);
                modeIntervalSum = modeIntervalSum.add(
                        new BigDecimal(modePart));
            }
            //System.out.println("modeIntervalSum " + modeIntervalSum.toString());
            BigDecimal modeInterval;
            if (modeInterval_HashSet.isEmpty()) {
                modeInterval = BigDecimal.ZERO;
            } else {
                BigDecimal divisor = new BigDecimal(
                        "" + modeInterval_HashSet.size());
                modeInterval = modeIntervalSum.divide(
                        divisor, numberOfDecimalPlaces_10, roundingMode);
            }
            System.out.println("modeInterval " + modeInterval.toString());
            // Mean of the values in the mode intervals might be a better than 
            // the mid point of the mode interval...

            // Dominance
            double proportionOfClass;
            double logProportionOfClass;
            //double log10ProportionOfClass;
            double diversity = 0.0d;
            double sumProportionOfClassSquared = 0.0d;
            double proportionOfClassMax;
            double logProportionOfClassMax;
            //double log10ProportionOfClassMax;
            double sumProportionOfClassMaxSquared = 0.0d;
            double diversityMax = 0.0d;
            double evenSpread = (double) n / (double) intervalVariety;
            long evenSpreadLong;
            long intervalsRemaining = intervalVariety;
            long remainingSpread = n;
            b_Iterator = interval_Grid2DSquareCellDoubleValue_TreeMap.keySet().iterator();
            while (b_Iterator.hasNext()) {
                interval_ID = b_Iterator.next();
                count = interval_Grid2DSquareCellDoubleValue_TreeMap.get(
                        interval_ID);
                proportionOfClass = (double) count / (double) n;
                sumProportionOfClassSquared += 
                        proportionOfClass * proportionOfClass;
                logProportionOfClass = Math.log(proportionOfClass);
                //log10ProportionOfClass = Math.log10(proportionOfClass);
                diversity -= proportionOfClass * logProportionOfClass;
                //diversity -= proportionOfClass * log10ProportionOfClass;
                evenSpread = (double) remainingSpread / 
                        (double) intervalsRemaining;
                evenSpreadLong = (long) evenSpread;
                remainingSpread -= evenSpreadLong;
                intervalsRemaining--;
                proportionOfClassMax = (double) evenSpreadLong / (double) n;
                sumProportionOfClassMaxSquared += 
                        proportionOfClassMax * proportionOfClassMax;
                logProportionOfClassMax = Math.log(proportionOfClassMax);
                //log10ProportionOfClassMax = Math.log10(proportionOfClassMax);
                //diversityMax = proportionOfClassMax * log10ProportionOfClassMax;
                diversityMax -= proportionOfClassMax * logProportionOfClassMax;
            }

            int numberOfIntervalsInRange = 
                    (int) Math.ceil(rangeIgnoringZeroAndOne / intervalRange);
            remainingSpread = n;
            intervalsRemaining = numberOfIntervalsInRange;
            double diversityMax2 = 0.0d;
            //double sumProportionOfClassMaxSquared2 = 0.0d;
            double logProportionOfClassMax2;
            for (int i = 0; i < numberOfIntervalsInRange; i++) {
                evenSpread = 
                        (double) remainingSpread / (double) intervalsRemaining;
                evenSpreadLong = (long) evenSpread;
                remainingSpread -= evenSpreadLong;
                intervalsRemaining--;
                proportionOfClassMax = (double) evenSpreadLong / (double) n;
//                sumProportionOfClassMaxSquared2 += 
//                        proportionOfClassMax * proportionOfClassMax;
                logProportionOfClassMax2 = Math.log(proportionOfClassMax);
                //log10ProportionOfClassMax = Math.log10(proportionOfClassMax);
                //diversityMax = proportionOfClassMax * log10ProportionOfClassMax;
                diversityMax2 -= proportionOfClassMax * logProportionOfClassMax2;
            }
            System.out.println("diversity " + diversity);
            result[outputIndex] = diversity;
            outputIndex++;
//            double intervalVarietyEvenness = 
//                    (-100.0d * Math.log(sumProportionOfClassSquared)) / 
//                    Math.log(intervalVariety);
            double intervalVarietyEvenness = 
                    (-100.0d * Math.log10(sumProportionOfClassSquared)) / 
                    Math.log10(intervalVariety);
            System.out.println(
                    "intervalVarietyEvenness " + intervalVarietyEvenness);
            result[outputIndex] = intervalVarietyEvenness;
            outputIndex++;
//            double numberOfIntervalsInRangeEvenness = 
//                    (-100.0d * Math.log(sumProportionOfClassSquared)) / 
//                    Math.log(numberOfIntervalsInRange);
            double numberOfIntervalsInRangeEvenness = 
                    (-100.0d * Math.log10(sumProportionOfClassSquared)) / 
                    Math.log10(numberOfIntervalsInRange);
            System.out.println(
                    "numberOfIntervalsInRangeEvenness " + 
                    numberOfIntervalsInRangeEvenness);
            result[outputIndex] = numberOfIntervalsInRangeEvenness;
            outputIndex++;
            System.out.println(
                    "maximumDiversityOverIntervalsWithValues " + diversityMax);
            result[outputIndex] = diversityMax;
            outputIndex++;
            double dominance = diversityMax + diversity;
            System.out.println("dominanceOverIntervalsWithValues " + dominance);
            result[outputIndex] = dominance;
            outputIndex++;
            System.out.println(
                    "maximumDiversityOverIntervalsInObservedRange " + 
                    diversityMax2);
            result[outputIndex] = diversityMax2;
            outputIndex++;
            double dominance2 = diversityMax2 + diversity;
            System.out.println(
                    "dominanceOverIntervalsInObservedRange " + dominance2);
            result[outputIndex] = dominance2;
        }
        return result;
    }

    public long getInterval(
            double value,
            double intervalRange,
            double startIntervalBound) {
        long result;
        Double resultAsDouble = (value - startIntervalBound) / intervalRange;
        //System.out.println("resultAsDouble " + resultAsDouble);
        result = resultAsDouble.longValue();
        //System.out.println("resultAslong " + result);
        return result;
    }
}
