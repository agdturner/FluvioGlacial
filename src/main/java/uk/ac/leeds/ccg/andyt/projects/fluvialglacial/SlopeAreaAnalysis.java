/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import uk.ac.leeds.ccg.andyt.data.Data_BiNumeric;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_IO;
import uk.ac.leeds.ccg.andyt.math.Math_BigDecimal;
import uk.ac.leeds.ccg.andyt.chart.examples.Chart_Scatter;

/**
 *
 * @author geoagdt
 */
public class SlopeAreaAnalysis {

    public SlopeAreaAnalysis() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new SlopeAreaAnalysis().run();
    }

    private static String sComma = ",";

    public void run() {

        int minNumberOfDataPoints = 10;

        File projectDir;
        //projectDir = new File("/scratch02/JonathanCarrivick/SlopeArea/");
        projectDir = new File("C:\\Users\\geoagdt\\projects\\JonathanCarrivick");

        File dirIn;
        dirIn = new File(
                projectDir,
                "input");
        File dirOut;
        dirOut = new File(
                projectDir,
                "output");
        File austriaDirOut;
        austriaDirOut = new File(
                dirOut,
                "austria");
        File swissDirOut;
        swissDirOut = new File(
                dirOut,
                "swiss");

        File swissFileIn;
        swissFileIn = new File(
                dirIn,
                "slope_area2.csv");
        File austriaFileIn;
        austriaFileIn = new File(
                dirIn,
                "Austria_proglac_export.txt");

        File swissFileOut2;
        swissFileOut2 = new File(
                swissDirOut,
                //"SwissID_LogSlope.txt");
                "SwissID_Slope.txt");
        File austriaFileOut2;
        austriaFileOut2 = new File(
                austriaDirOut,
                //"AustriaID_LogSlope.txt");
                "AustriaID_Slope.txt");

//        System.out.println("Swiss");
//        TreeMap<Integer, Object[]> swissData = readSwissData(swissFileIn);
//        PrintDataSummary(swissData);
//        run(swissData,
//                swissDirOut,
//                swissFileOut2,
//                minNumberOfDataPoints);

        System.out.println("Austria");
        TreeMap<Integer, Object[]> austriaData = readAustriaData(austriaFileIn);
        PrintDataSummary(austriaData);
        run(austriaData,
                austriaDirOut,
                austriaFileOut2,
                minNumberOfDataPoints);
    }

    public void run(
            TreeMap<Integer, Object[]> allData,
            File outDir,
            File outFile2,
            int minNumberOfDataPoints) {
        File outfile;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outFile2);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SlopeAreaAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        //pw.println("ID, log(Slope)");
        pw.println("ID, Slope");
        int dataWidth = 500;//400;//250;
        int dataHeight = 500;//657;
        String xAxisLabel = "x = log(Slope)";
        String yAxisLabel = "y = log(Upstream Accumulation Area)";
        boolean drawOriginLinesOnPlot;
//        drawOriginLinesOnPlot = true;
        drawOriginLinesOnPlot = false;
        int decimalPlacePrecisionForCalculations = 10;
        int decimalPlacePrecisionForDisplay = 3;
        RoundingMode aRoundingMode = RoundingMode.HALF_UP;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SlopeAreaScatterPlot plot;

        String format = "PNG";
        String title;
        
        Iterator<Integer> ite;
        ite = allData.keySet().iterator();
        int ID;
        Object[] data;
        while (ite.hasNext()) {
            ID = ite.next();
            //if (ID == 388) {

            data = allData.get(ID);
            ArrayList<Data_BiNumeric> theGeneric_XYNumericalData;
            theGeneric_XYNumericalData = (ArrayList<Data_BiNumeric>) data[0];
            int np;
            np = theGeneric_XYNumericalData.size();
            for (int degree = 2; degree < 3; degree++) {
                title = "GlacierID " + ID + ", n = " + np;
                //title += ", degree = " + degree;
                File outDir2 = new File(
                        outDir,
                        "degree" + degree);
                outDir2.mkdirs();
                outfile = new File(
                        outDir2,
                        "SlopeUAAScatterPlot" + ID + ".PNG");
                if (np >= minNumberOfDataPoints) {
                    plot = new SlopeAreaScatterPlot(
                            degree,
                            data,
                            executorService,
                            outfile,
                            format,
                            title,
                            dataWidth,
                            dataHeight,
                            xAxisLabel,
                            yAxisLabel,
                            drawOriginLinesOnPlot,
                            decimalPlacePrecisionForCalculations,
                            decimalPlacePrecisionForDisplay,
                            aRoundingMode);
                    //plot.setData(plot.getDefaultData());
                    //plot.setStartAgeOfEndYearInterval(0); // To avoid null pointer
                    plot.run();
                    if (plot.isHump) {
                        System.out.println("" + ID + ", " + plot.xAtMaxy2);
                        double x = Math.pow(10.0d, plot.xAtMaxy2);
                        //pw.println("" + ID + ", " + plot.xAtMaxy2);
                        pw.println("" + ID + ", " + x);
                    }
                }
            }
            pw.flush();
            //}
        }
                pw.close();
    }

    protected TreeMap<Integer, Object[]> readSwissData(File fileIn) {
        TreeMap<Integer, Object[]> result;
        result = new TreeMap<Integer, Object[]>();
        BufferedReader br;
        br = Generic_IO.getBufferedReader(fileIn);
        StreamTokenizer st;
        st = new StreamTokenizer(br);
        Generic_IO.setStreamTokenizerSyntax5(st);
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('%', '%');
        Generic_IO.skipline(st);
        int token;
        String line = "";
        String[] fields;
        try {
            token = st.nextToken();
            int ID;
            //int pointID;
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_EOL:
                        //flowacc,area (km2),slope_25_(%),proglac_ID,COUNT
                        //12.11111069,0.00756944,32.33880000000,0,250631
                        fields = line.split(sComma);
                        ID = Integer.valueOf(fields[3]);
                        if (ID > 0) {
                            //BigDecimal flowacc;
                            BigDecimal area;
                            BigDecimal slope;
                            Object[] data;
                            BigDecimal maxx;
                            BigDecimal maxy;
                            BigDecimal minx;
                            BigDecimal miny;
                            data = result.get(ID);
                            ArrayList<Data_BiNumeric> theGeneric_XYNumericalData;
                            if (data == null) {
                                data = new Object[5];
                                theGeneric_XYNumericalData = new ArrayList<>();
                                maxx = BigDecimal.ZERO;
                                maxy = BigDecimal.ZERO;
                                minx = BigDecimal.valueOf(Double.MAX_VALUE);
                                miny = BigDecimal.valueOf(Double.MAX_VALUE);
                                data[0] = theGeneric_XYNumericalData;
                                data[1] = maxx;
                                data[2] = minx;
                                data[3] = maxy;
                                data[4] = miny;
                                result.put(ID, data);
                            } else {
                                theGeneric_XYNumericalData = (ArrayList<Data_BiNumeric>) data[0];
                                maxx = (BigDecimal) data[1];
                                minx = (BigDecimal) data[2];
                                maxy = (BigDecimal) data[3];
                                miny = (BigDecimal) data[4];
                            }
                            //pointID = Integer.valueOf(fields[4]);
                            //flowacc = new BigDecimal(fields[0]);
                            area = new BigDecimal(fields[1]);
                            if (area.compareTo(BigDecimal.ZERO) == 1) {
                                area = Math_BigDecimal.log(10, area, 10, RoundingMode.HALF_UP);
                            } else {
                                area = BigDecimal.ZERO;
                            }
                            slope = new BigDecimal(fields[2]);
                            if (slope.compareTo(BigDecimal.ZERO) == 1) {
                                slope = Math_BigDecimal.log(10, slope, 10, RoundingMode.HALF_UP);
                            } else {
                                slope = BigDecimal.ZERO;
                            }
                            Data_BiNumeric point;
                            point = new Data_BiNumeric(                                    slope,                                    area);
                            theGeneric_XYNumericalData.add(point);
                            data[0] = theGeneric_XYNumericalData;
                            data[1] = maxx.max(slope);
                            data[2] = minx.min(slope);
                            data[3] = maxy.max(area);
                            data[4] = miny.min(area);
                        }
                        break;
                    case StreamTokenizer.TT_WORD:
                        line = st.sval;
                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException ex) {
            Logger.getLogger(SlopeAreaAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    protected TreeMap<Integer, Object[]> readAustriaData(File fileIn) {
        TreeMap<Integer, Object[]> result;
        result = new TreeMap<Integer, Object[]>();
        BufferedReader br;
        br = Generic_IO.getBufferedReader(fileIn);
        StreamTokenizer st;
        st = new StreamTokenizer(br);
        Generic_IO.setStreamTokenizerSyntax5(st);
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('%', '%');
        Generic_IO.skipline(st);
        int token;
        String line = "";
        String[] fields;
        try {
            token = st.nextToken();
            int ID;
            //int pointID;
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_EOL:
                        //flowacc,area (km2),slope_25_(%),proglac_ID,COUNT
                        //12.11111069,0.00756944,32.33880000000,0,250631
                        fields = line.split(sComma);
                        ID = Double.valueOf(fields[1]).intValue();
                        if (ID > 0) {
                            //BigDecimal flowacc;
                            BigDecimal area;
                            BigDecimal slope;
                            Object[] data;
                            BigDecimal maxx;
                            BigDecimal maxy;
                            BigDecimal minx;
                            BigDecimal miny;
                            data = result.get(ID);
                            ArrayList<Data_BiNumeric> theGeneric_XYNumericalData;
                            if (data == null) {
                                data = new Object[5];
                                theGeneric_XYNumericalData = new ArrayList<>();
                                maxx = BigDecimal.ZERO;
                                maxy = BigDecimal.ZERO;
                                minx = BigDecimal.valueOf(Double.MAX_VALUE);
                                miny = BigDecimal.valueOf(Double.MAX_VALUE);
                                data[0] = theGeneric_XYNumericalData;
                                data[1] = maxx;
                                data[2] = minx;
                                data[3] = maxy;
                                data[4] = miny;
                                result.put(ID, data);
                            } else {
                                theGeneric_XYNumericalData = (ArrayList<Data_BiNumeric>) data[0];
                                maxx = (BigDecimal) data[1];
                                minx = (BigDecimal) data[2];
                                maxy = (BigDecimal) data[3];
                                miny = (BigDecimal) data[4];
                            }
                            //pointID = Integer.valueOf(fields[4]);
                            //flowacc = new BigDecimal(fields[0]);
                            area = new BigDecimal(fields[3]);
                            if (area.compareTo(BigDecimal.ZERO) == 1) {
                                area = Math_BigDecimal.log(10, area, 10, RoundingMode.HALF_UP);
                            } else {
                                area = BigDecimal.ZERO;
                            }
                            slope = new BigDecimal(fields[2]);
                            if (slope.compareTo(BigDecimal.ZERO) == 1) {
                                slope = Math_BigDecimal.log(10, slope, 10, RoundingMode.HALF_UP);
                            } else {
                                slope = BigDecimal.ZERO;
                            }
                            Data_BiNumeric point;
                            point = new Data_BiNumeric(                                    slope,                                    area);
                            theGeneric_XYNumericalData.add(point);
                            data[0] = theGeneric_XYNumericalData;
                            data[1] = maxx.max(slope);
                            data[2] = minx.min(slope);
                            data[3] = maxy.max(area);
                            data[4] = miny.min(area);
                        }
                        break;
                    case StreamTokenizer.TT_WORD:
                        line = st.sval;
                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException ex) {
            Logger.getLogger(SlopeAreaAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void PrintDataSummary(TreeMap<Integer, Object[]> allData) {
        Iterator<Integer> ite;
        ite = allData.keySet().iterator();
        int ID;
        Object[] data;
        ArrayList<Data_BiNumeric> theGeneric_XYNumericalData;
        Data_BiNumeric point;
        BigDecimal maxx;
        BigDecimal minx;
        BigDecimal maxy;
        BigDecimal miny;
        System.out.println("N, MaxX, MinX, MaxY, MinY");
        while (ite.hasNext()) {
            ID = ite.next();
            data = allData.get(ID);
            theGeneric_XYNumericalData = (ArrayList<Data_BiNumeric>) data[0];
            maxx = (BigDecimal) data[1];
            minx = (BigDecimal) data[2];
            maxy = (BigDecimal) data[3];
            miny = (BigDecimal) data[4];
            System.out.println("" + theGeneric_XYNumericalData.size()
                    + ", " + maxx
                    + ", " + minx
                    + ", " + maxy
                    + ", " + miny);
        }
    }
}
