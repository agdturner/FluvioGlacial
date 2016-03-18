/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import uk.ac.leeds.ccg.andyt.generic.data.Generic_XYNumericalData;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.generic.math.Generic_BigDecimal;
import uk.ac.leeds.ccg.andyt.generic.visualisation.charts.Generic_ScatterPlot;

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

        int n = 1000;//5;
        int counter = 0;
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
        File fileIn;
        fileIn = new File(
                dirIn,
                "slope_area2.csv");
        File fileOut;
        TreeMap<Integer, Object[]> allData = readData(fileIn);
        PrintDataSummary(allData);

        int dataWidth = 500;//400;//250;
        int dataHeight = 500;//657;
        String xAxisLabel = "Slope";
        String yAxisLabel = "Upstream accumulation area";
        boolean drawOriginLinesOnPlot = true;
        int decimalPlacePrecisionForCalculations = 10;
        int decimalPlacePrecisionForDisplay = 3;
        RoundingMode aRoundingMode = RoundingMode.HALF_UP;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SlopeAreaScatterPlot plot;

        String format = "PNG";
        String title;
        title = "Title";

        Iterator<Integer> ite;
        ite = allData.keySet().iterator();
        int ID;
        Object[] data;
        while (ite.hasNext()) {
            ID = ite.next();
            title = "GlacierID = " + ID;
            fileOut = new File(
                    dirOut,
                    "SlopeUAAScatterPlot" + ID + ".PNG");
            data = allData.get(ID);
            ArrayList<Generic_XYNumericalData> theGeneric_XYNumericalData;
            theGeneric_XYNumericalData = (ArrayList<Generic_XYNumericalData>) data[0];
            if (theGeneric_XYNumericalData.size() > minNumberOfDataPoints) {
                if (counter < n) {
                    plot = new SlopeAreaScatterPlot(
                            data,
                            executorService,
                            fileOut,
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
                    counter++;
                }
            }
        }
    }

    protected TreeMap<Integer, Object[]> readData(File fileIn) {
        TreeMap<Integer, Object[]> result;
        result = new TreeMap<Integer, Object[]>();
        BufferedReader br;
        br = Generic_StaticIO.getBufferedReader(fileIn);
        StreamTokenizer st;
        st = new StreamTokenizer(br);
        Generic_StaticIO.setStreamTokenizerSyntax5(st);
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('%', '%');
        Generic_StaticIO.skipline(st);
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
                            BigDecimal flowacc;
                            BigDecimal area;
                            BigDecimal slope;
                            Object[] data;
                            BigDecimal maxx;
                            BigDecimal maxy;
                            BigDecimal minx;
                            BigDecimal miny;
                            data = result.get(ID);
                            ArrayList<Generic_XYNumericalData> theGeneric_XYNumericalData;
                            if (data == null) {
                                data = new Object[5];
                                theGeneric_XYNumericalData = new ArrayList<Generic_XYNumericalData>();
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
                                theGeneric_XYNumericalData = (ArrayList<Generic_XYNumericalData>) data[0];
                                maxx = (BigDecimal) data[1];
                                minx = (BigDecimal) data[2];
                                maxy = (BigDecimal) data[3];
                                miny = (BigDecimal) data[4];
                            }
                            //pointID = Integer.valueOf(fields[4]);
                            flowacc = new BigDecimal(fields[0]);
                            area = new BigDecimal(fields[1]);
                            if (area.compareTo(BigDecimal.ZERO) == 1) {
                                area = Generic_BigDecimal.log(10, area, 10, RoundingMode.HALF_UP);
                            } else {
                                area = BigDecimal.ZERO;
                            }
                            slope = new BigDecimal(fields[2]);
                            if (slope.compareTo(BigDecimal.ZERO) == 1) {
                                slope = Generic_BigDecimal.log(10, slope, 10, RoundingMode.HALF_UP);
                            } else {
                                slope = BigDecimal.ZERO;
                            }
                            Generic_XYNumericalData point;
                            point = new Generic_XYNumericalData(
                                    slope,
                                    area);
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
        ArrayList<Generic_XYNumericalData> theGeneric_XYNumericalData;
        Generic_XYNumericalData point;
        BigDecimal maxx;
        BigDecimal minx;
        BigDecimal maxy;
        BigDecimal miny;
        System.out.println("N, MaxX, MinX, MaxY, MinY");
        while (ite.hasNext()) {
            ID = ite.next();
            data = allData.get(ID);
            theGeneric_XYNumericalData = (ArrayList<Generic_XYNumericalData>) data[0];
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
