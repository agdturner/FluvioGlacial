/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import uk.ac.leeds.ccg.andyt.generic.data.Generic_XYNumericalData;
import uk.ac.leeds.ccg.andyt.generic.visualisation.charts.Generic_ScatterPlot;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
//import org.apache.commons.math3.fitting.PolynomialCurveFitter

/**
 *
 * @author geoagdt
 */
public class SlopeAreaScatterPlot extends Generic_ScatterPlot {

    //private final Object[] data;
    public SlopeAreaScatterPlot() {
        //data = new Object[5];
    }

    public SlopeAreaScatterPlot(
            Object[] data,
            ExecutorService executorService,
            File file,
            String format,
            String title,
            int dataWidth,
            int dataHeight,
            String xAxisLabel,
            String yAxisLabel,
            boolean drawOriginLinesOnPlot,
            int decimalPlacePrecisionForCalculations,
            int decimalPlacePrecisionForDisplay,
            RoundingMode aRoundingMode) {
        init(
                executorService,
                file,
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
        setStartAgeOfEndYearInterval(0);
        setData(data);
        
        int degree = 3;
        PolynomialCurveFitter pcf;
        pcf = PolynomialCurveFitter.create(degree);
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        ArrayList<Generic_XYNumericalData> theGeneric_XYNumericalData;
        theGeneric_XYNumericalData = (ArrayList<Generic_XYNumericalData>) data[0];
        Iterator<Generic_XYNumericalData> ite;
        ite = theGeneric_XYNumericalData.iterator();
        Generic_XYNumericalData generic_XYNumericalData;
        while (ite.hasNext()) {
            generic_XYNumericalData = ite.next();
            obs.add(generic_XYNumericalData.x.doubleValue(),
                    generic_XYNumericalData.y.doubleValue());
        }
        double[] coeffs = pcf.fit(obs.toList());
        for (int i = 0; i < coeffs.length; i++) {
            System.out.println(coeffs[i]);
        }
       PolynomialFunction pf;
       pf = new PolynomialFunction(coeffs);
       double minx = getMinX().doubleValue();
       double maxx = getMaxX().doubleValue();
       double range = maxx - minx;
       int intervals = 100;
       double interval = range / (double) intervals;
       double x;
       double y;
       bestfit = new ArrayList<Generic_XYNumericalData>();
       for (int i = 0; i < 100; i ++) {
           x = minx + interval * i;
           y = pf.value(x);
           generic_XYNumericalData = new Generic_XYNumericalData(
                   BigDecimal.valueOf(x),
                     BigDecimal.valueOf(y));
           bestfit.add(generic_XYNumericalData);
       }
    }

    ArrayList<Generic_XYNumericalData> bestfit;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    @Override
    public void drawData() {
        super.drawData();
        Iterator<Generic_XYNumericalData> ite = bestfit.iterator();
            Generic_XYNumericalData generic_XYNumericalData;
            setPaint(Color.GREEN);
            Point2D aPoint2D;
            while (ite.hasNext()) {
                generic_XYNumericalData = ite.next();
                aPoint2D = coordinateToScreen(
                        generic_XYNumericalData.getX(),
                        generic_XYNumericalData.getY());
                draw(aPoint2D);
            }
    }
}
