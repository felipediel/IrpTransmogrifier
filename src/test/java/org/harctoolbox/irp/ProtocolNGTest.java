/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class ProtocolNGTest {

    public ProtocolNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }



    /**
     * Test of getFrequency method, of class Protocol.
     */
    @Test
    public void testGetFrequency() {
        try {
            System.out.println("getFrequency");
            Protocol instance = new Protocol("{56.0k,268,msb}<-1,1|1,-1>(T=1,(7,-6,3,D:4,1:1,T:1,1:2,0:8,F:8,15:4,C:4,-79m,T=0)+){C =(D:4+4*T+9+F:4+F:4:4+15)&15} [D:0..15,F:0..255]");
            double expResult = 56000f;
            double result = instance.getFrequency();
            assertEquals(result, expResult, 0.0);
            // TODO review the generated test code and remove the default call to fail.
        } catch (IrpSyntaxException | IrpSemanticException | ArithmeticException | IncompatibleArgumentException | InvalidRepeatException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
