/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class BitSpecNGTest {

    private static final String NEC1BitSpec = "<1,-1|1,-3>";
    private static final String RC5BitSpec = "<1,-1|-1,1>";
    private static final String RC6BitSpec = "<-1,1|1,-1>";
    private static final String Nokia32BitSpec = "<164,-276|164,-445|164,-614|164,-783>";
    private static final String NEC1GeneralSpec = "{38.4k,564}";

    public BitSpecNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        BitSpec.reset();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


    /**
     * Test of getNoInstances method, of class BitSpec.
     */
    @Test
    public void testGetNoInstances() {
        try {
            System.out.println("getNoInstances");
            //BitSpec.reset();
            assertEquals(BitSpec.getNoInstances(), 0);
            new BitSpec();
            assertEquals(BitSpec.getNoInstances(), 0);
            new BitSpec(NEC1BitSpec);
            assertEquals(BitSpec.getNoInstances(), 1);
            BitSpec.reset();
            assertEquals(BitSpec.getNoInstances(), 0);
            // TODO review the generated test code and remove the default call to fail.
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            fail();
        }

    }

    /**
     * Test of get method, of class BitSpec.
     * @throws java.lang.Exception
     */
    @Test
    public void testGet() throws Exception {
//        System.out.println("getBitIrsteam");
//        int index = 0;
//        BitSpec instance = new BitSpec();
//        BareIrStream expResult = null;
//        BareIrStream result = instance.get(index);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.

    }

    /**
     * Test of toString method, of class BitSpec.
     */
    @Test
    public void testToString() {
//        try {
//            System.out.println("toString");
//            assertEquals(new BitSpec().toString(), "<null>");
//            assertEquals(new BitSpec(NEC1BitSpec).toString(), NEC1BitSpec);
//        } catch (IrpSyntaxException | InvalidRepeatException ex) {
//            fail();
//        }
    }

    /**
     * Test of getChunkSize method, of class BitSpec.
     */
    @Test
    public void testGetChunkSize() {
        try {
            System.out.println("getChunkSize");
            BitSpec nec1 = new BitSpec(NEC1BitSpec);
            BitSpec nokia32 = new BitSpec(Nokia32BitSpec);
            BitSpec rc5 = new BitSpec(RC5BitSpec);
            assertEquals(nec1.getChunkSize(), 1);
            assertEquals(rc5.getChunkSize(), 1);
            assertEquals(nokia32.getChunkSize(), 2);
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            fail();
        }
    }

    /**
     * Test of isEmpty method, of class BitSpec.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        assertEquals(new BitSpec().isEmpty(), true);
        try {
            assertEquals(new BitSpec(NEC1BitSpec).isEmpty(), false);
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            fail();
        }
    }

    /**
     * Test of isStandardPWM method, of class BitSpec.
     */
    @Test
    public void testIsStandardPWM() {
        try {
            System.out.println("isStandardPWM");
            NameEngine nameEngine = new NameEngine();
            GeneralSpec generalSpec = new GeneralSpec();
            BitSpec nec1 = new BitSpec(NEC1BitSpec);
            BitSpec nokia32 = new BitSpec(Nokia32BitSpec);
            BitSpec rc5 = new BitSpec(RC5BitSpec);
            //BitSpec empty = new BitSpec();
            assertTrue(nec1.isStandardPWM(nameEngine, generalSpec));
            assertFalse(nokia32.isStandardPWM(nameEngine, generalSpec));
            assertFalse(rc5.isStandardPWM(nameEngine, generalSpec));
            //assertEquals(result, expResult);
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            fail();
        }


    }

    /**
     * Test of isStandardBiPhase method, of class BitSpec.
     */
    @Test
    public void testIsStandardBiPhase() {
        System.out.println("isStandardBiPhase");
        try {
            NameEngine nameEngine = new NameEngine();
            GeneralSpec generalSpec = new GeneralSpec(NEC1GeneralSpec);
            BitSpec nec1 = new BitSpec(NEC1BitSpec);
            BitSpec nokia32 = new BitSpec(Nokia32BitSpec);
            BitSpec rc5 = new BitSpec(RC5BitSpec);
            BitSpec rc6 = new BitSpec(RC6BitSpec);
            //BitSpec empty = new BitSpec();
            assertFalse(nec1.isStandardBiPhase(nameEngine, generalSpec));
            assertFalse(nokia32.isStandardBiPhase(nameEngine, generalSpec));
            assertTrue(rc5.isStandardBiPhase(nameEngine, generalSpec));
            assertTrue(rc6.isStandardBiPhase(nameEngine, generalSpec));
        } catch (IrpException | ArithmeticException | IncompatibleArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of toElement method, of class BitSpec.
     */
    @Test
    public void testToElement() {
        System.out.println("toElement");
//        Document document = null;
//        BitSpec instance = new BitSpec();
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.

    }

}