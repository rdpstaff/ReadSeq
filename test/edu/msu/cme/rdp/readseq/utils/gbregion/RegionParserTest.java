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

package edu.msu.cme.rdp.readseq.utils.gbregion;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class RegionParserTest {

    public RegionParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of parse method, of class RegionParser.
     */
    @Test
    public void testParse() throws Exception {
        String str = "AB66732";
        assertTrue(str.matches(RegionParser.accnoGiPattern));

        str = "3532123";
        assertTrue(str.matches(RegionParser.accnoGiPattern));

        str = "asdfEAdf";
        assertFalse(str.matches(RegionParser.accnoGiPattern));

        str = "NC_009349.1:97292..97852";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "97292..97852";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "complement(NC_010473.1:4187575..>4188645)";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "join(M33876.1:3924..4086,M33876.1:4146..4844,M33876.1:4905..4924)";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "complement(join(BX294016.1:32656..33913,BX294016.1:33974..34061,BX294016.1:34124..34394))";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "DQ783514.1:<1..>473";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "DQ783514.1:<1..473";
        assertEquals(str, RegionParser.parse(str).toString());

        str = "DQ783514.1:1..>473";
        assertEquals(str, RegionParser.parse(str).toString());
    }

}