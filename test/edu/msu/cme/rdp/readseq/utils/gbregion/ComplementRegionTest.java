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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class ComplementRegionTest {

    private static final String seq = "acgtctagtagccggt";
    private static final String expected = "AGACG";

    private ComplementRegion regionTest = new ComplementRegion(new SimpleRegion("cat", 2, 6, Extends.EXACT));

    /**
     * Test of toString method, of class ComplementRegion.
     */
    @Test
    public void testToString() {
        assertEquals("complement(cat:2..6)", regionTest.toString());
    }

    /**
     * Test of getId method, of class ComplementRegion.
     */
    @Test
    public void testGetId() {
        assertEquals("cat", regionTest.getId());
    }

    /**
     * Test of getSeqStart method, of class ComplementRegion.
     */
    @Test
    public void testGetSeqStart() {
        assertEquals(2, regionTest.getSeqStart());
    }

    /**
     * Test of getSeqStop method, of class ComplementRegion.
     */
    @Test
    public void testGetSeqStop() {
        assertEquals(6, regionTest.getSeqStop());
    }

    /**
     * Test of getSeqRegion method, of class ComplementRegion.
     */
    @Test
    public void testGetSeqRegion() {
        assertEquals(expected, regionTest.getSeqRegion(seq));
    }

}