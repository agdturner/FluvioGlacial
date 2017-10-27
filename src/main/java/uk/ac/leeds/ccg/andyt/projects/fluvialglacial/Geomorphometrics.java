/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.io.File;
import uk.ac.leeds.ccg.andyt.grids.core.grid.Grids_AbstractGridNumber;
import uk.ac.leeds.ccg.andyt.grids.core.grid.Grids_GridDouble;
import uk.ac.leeds.ccg.andyt.grids.core.grid.Grids_GridDoubleFactory;
import uk.ac.leeds.ccg.andyt.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.andyt.grids.process.Grids_ProcessorDEM;

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
        workspace = new File(
                dir,
                "workspace");
        
        Grids_Environment ge;
        ge = new Grids_Environment(workspace);
        Grids_ProcessorDEM p;
        p = new Grids_ProcessorDEM(ge);
        
        Grids_GridDoubleFactory gf;
        gf = new Grids_GridDoubleFactory(ge, true);
        
        File inputfile;
        inputfile = new File(dir,
                "rastert_kdem_oc1.txt");

        Grids_AbstractGridNumber input;
        input = gf.create(inputfile);
        
//        p.getMetrics1(
//                input,
//                0,
//                0,
//                0,
//                gf,
//                _Grid2DSquareCellIntFactory,
//                true, true, true);
        
        
        
    }
    
}
