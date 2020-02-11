/*
 * Copyright 2019 Centre for Computational Geography.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.projects.fg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
//import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import uk.ac.leeds.ccg.chart.data.Data_BiBigDecimal;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.generic.io.Generic_Defaults;
import uk.ac.leeds.ccg.generic.io.Generic_IO;
import uk.ac.leeds.ccg.math.Math_BigDecimal;
//import uk.ac.leeds.ccg.chart.examples.Chart_Scatter;
import uk.ac.leeds.ccg.projects.fg.core.FG_Environment;
import uk.ac.leeds.ccg.projects.fg.core.FG_Object;

/**
 * SlopeAreaAnalysis
 *
 * @author Andy Turner
 * @version 1.0.0
 */
public class SlopeAreaAnalysis extends FG_Object {

    private static final long serialVersionUID = 1L;

    public SlopeAreaAnalysis(FG_Environment e) {
        super(e);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Generic_Environment ge = new Generic_Environment(
                    new Generic_Defaults());
            new SlopeAreaAnalysis(new FG_Environment(ge)).run();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static String sComma = ",";

    public void run() throws FileNotFoundException, IOException {

        // Main switches
        boolean runSwiss = true;//false;
        boolean runAustria = true;//false;

        int minNumberOfDataPoints = 10;

        Path projectDir;
        //projectDir = Paths.get("/scratch02/JonathanCarrivick/SlopeArea/");
        projectDir = Paths.get("C:\\Users\\geoagdt\\projects\\JonathanCarrivick");

        Path dirIn = Paths.get(projectDir.toString(), "input");
        Path dirOut = Paths.get(projectDir.toString(), "output");
        if (runSwiss) {
            Path swissDirOut = Paths.get(dirOut.toString(), "swiss");
            Path swissFileIn = Paths.get(dirIn.toString(), "slope_area2.csv");
            Path swissFileOut2 = Paths.get(swissDirOut.toString(),
                    //"SwissID_LogSlope.txt");
                    "SwissID_Slope.txt");
            System.out.println("Swiss");
            TreeMap<Integer, Object[]> swissData = readSwissData(swissFileIn);
            PrintDataSummary(swissData);
            run(swissData, swissDirOut, swissFileOut2, minNumberOfDataPoints);
        }
        if (runAustria) {
            Path austriaDirOut = Paths.get(dirOut.toString(), "austria");
            Path austriaFileIn = Paths.get(dirIn.toString(), "Austria_proglac_export.txt");
            Path austriaFileOut2 = Paths.get(austriaDirOut.toString(),
                    //"AustriaID_LogSlope.txt");
                    "AustriaID_Slope.txt");
            System.out.println("Austria");
            TreeMap<Integer, Object[]> austriaData = readAustriaData(austriaFileIn);
            PrintDataSummary(austriaData);
            run(austriaData, austriaDirOut, austriaFileOut2, minNumberOfDataPoints);
        }
    }

    public void run(TreeMap<Integer, Object[]> allData, Path outDir,
            Path outFile2, int minNumberOfDataPoints) throws IOException {
        Path outfile;
        PrintWriter pw = Generic_IO.getPrintWriter(outFile2, false);
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
            ArrayList<Data_BiBigDecimal> xy = (ArrayList<Data_BiBigDecimal>) data[0];
            int np = xy.size();
            for (int degree = 2; degree < 3; degree++) {
                title = "GlacierID " + ID + ", n = " + np;
                //title += ", degree = " + degree;
                Path outDir2 = Paths.get(outDir.toString(), "degree" + degree);
                Files.createDirectories(outDir2);
                outfile = Paths.get(outDir2.toString(), "SlopeUAAScatterPlot" + ID + ".PNG");
                if (np >= minNumberOfDataPoints) {
                    plot = new SlopeAreaScatterPlot(env.env,
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

    protected TreeMap<Integer, Object[]> readSwissData(Path fileIn) 
            throws FileNotFoundException, IOException {
        TreeMap<Integer, Object[]> result = new TreeMap<>();
        BufferedReader br = Generic_IO.getBufferedReader(fileIn);
        StreamTokenizer st = new StreamTokenizer(br);
        Generic_IO.setStreamTokenizerSyntax5(st);
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('%', '%');
        br.readLine();
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
                            ArrayList<Data_BiBigDecimal> theGeneric_XYNumericalData;
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
                                theGeneric_XYNumericalData = (ArrayList<Data_BiBigDecimal>) data[0];
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
                            Data_BiBigDecimal point;
                            point = new Data_BiBigDecimal(slope, area);
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

    protected TreeMap<Integer, Object[]> readAustriaData(Path fileIn) 
            throws FileNotFoundException, IOException {
        TreeMap<Integer, Object[]> r = new TreeMap<>();
        BufferedReader br = Generic_IO.getBufferedReader(fileIn);
        StreamTokenizer st = new StreamTokenizer(br);
        Generic_IO.setStreamTokenizerSyntax5(st);
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('%', '%');
        br.readLine();
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
                            data = r.get(ID);
                            ArrayList<Data_BiBigDecimal> theGeneric_XYNumericalData;
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
                                r.put(ID, data);
                            } else {
                                theGeneric_XYNumericalData = (ArrayList<Data_BiBigDecimal>) data[0];
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
                            Data_BiBigDecimal point;
                            point = new Data_BiBigDecimal(slope, area);
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
        return r;
    }

    public void PrintDataSummary(TreeMap<Integer, Object[]> allData) {
        Iterator<Integer> ite;
        ite = allData.keySet().iterator();
        int ID;
        Object[] data;
        ArrayList<Data_BiBigDecimal> theGeneric_XYNumericalData;
        Data_BiBigDecimal point;
        BigDecimal maxx;
        BigDecimal minx;
        BigDecimal maxy;
        BigDecimal miny;
        System.out.println("N, MaxX, MinX, MaxY, MinY");
        while (ite.hasNext()) {
            ID = ite.next();
            data = allData.get(ID);
            theGeneric_XYNumericalData = (ArrayList<Data_BiBigDecimal>) data[0];
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
