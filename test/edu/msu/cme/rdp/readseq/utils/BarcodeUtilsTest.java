/*
 * Copyright (C) 2012 Jordan Fish <fishjord at msu.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.msu.cme.rdp.readseq.utils;

import edu.msu.cme.rdp.readseq.utils.BarcodeUtils.BarcodeInvalidException;
import java.io.StringReader;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class BarcodeUtilsTest {

    private static final String goodBarcode = "AAAA\ttest1\nTTTT\ttest2\nCCCC\ttest3";
    private static final String badBarcode1 = "AAAA\ttest1\nAAAA\ttest2\nCCCC\ttest3";
    private static final String badBarcode2 = "AAAA\ttest1\nAAA\ttest2\nCCCC\ttest3";

    /**
     * Test of readBarcodeFile method, of class BarcodeUtils.
     */
    @Test
    public void testGoodBarcode() throws Exception {
        try {
            Map<String, String> barcodeMap = BarcodeUtils.readBarcodeFile(new StringReader(goodBarcode));

            assertEquals(barcodeMap.get("aaaa"), "test1");
            assertEquals(barcodeMap.get("tttt"), "test2");
            assertEquals(barcodeMap.get("cccc"), "test3");
        } catch(BarcodeInvalidException e) {
            fail("Good barcode failed: " + e.getMessage());
        }
    }

    @Test
    public void testDupBarcode() throws Exception {
        try {
            Map<String, String> barcodeMap = BarcodeUtils.readBarcodeFile(new StringReader(badBarcode1));
            fail("Bad barcode (duplicate) passed when it shouldn't have");
        } catch(BarcodeInvalidException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testDupPrefixBarcode() throws Exception {
        try {
            Map<String, String> barcodeMap = BarcodeUtils.readBarcodeFile(new StringReader(badBarcode2));
            fail("Bad barcode (overlapping) passed when it shouldn't have");
        } catch(BarcodeInvalidException e) {
            System.out.println(e.getMessage());
        }
    }

}