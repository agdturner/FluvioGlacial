/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.io.File;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_AbstractGrid2DSquareCell;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_Grid2DSquareCellDouble;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_Grid2DSquareCellDoubleFactory;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.andyt.grids.process.Grid2DSquareCellProcessorDEM;

/**
 *
 * @author geoagdt
 */
public class Geomorphometrics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        File dir;
        dir = new File("/nfs/see-fs-02_users/geoagdt/scratch01/Work/people/Scott Watson/");

        File workspace;
        workspace = new File("/nfs/see-fs-02_users/geoagdt/scratch01/Work/people/Scott Watson/workspace");
        
        Grids_Environment ge;
        ge = new Grids_Environment();
        Grid2DSquareCellProcessorDEM Grid2DSquareCellProcessorDEM;
        Grid2DSquareCellProcessorDEM = new Grid2DSquareCellProcessorDEM(ge, workspace);
        
        Grids_Grid2DSquareCellDoubleFactory factory;
        factory = new Grids_Grid2DSquareCellDoubleFactory(ge, true);
        
        File inputfile;
        inputfile = new File(dir,
                "rastert_kdem_oc1.txt");

        Grids_AbstractGrid2DSquareCell input;
        input = factory.create(inputfile);
        
//        Grid2DSquareCellProcessorDEM.getMetrics1(
//                input,
//                0,
//                0,
//                0,
//                factory,
//                _Grid2DSquareCellIntFactory,
//                true, true, true);
        
        
        
    }
    
}
