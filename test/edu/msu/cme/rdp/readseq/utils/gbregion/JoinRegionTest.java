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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class JoinRegionTest {
    private static final String seq = "acgtctagtagccggt";
    private static final String expected = "acgggt";
    private static final JoinRegion regionTest = new JoinRegion("cat", (List)Arrays.asList(new SimpleRegion("cat", 1, 3, Extends.EXACT), new SimpleRegion("cat", 14, 16, Extends.EXACT)), false);

    /**
     * Test of toString method, of class JoinRegion.
     */
    @Test
    public void testToString() {
        assertEquals("join(cat:1..3,cat:14..16)", regionTest.toString());
    }

    /**
     * Test of getId method, of class JoinRegion.
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
        assertEquals(1, regionTest.getSeqStart());
    }

    /**
     * Test of getSeqStop method, of class ComplementRegion.
     */
    @Test
    public void testGetSeqStop() {
        assertEquals(16, regionTest.getSeqStop());
    }

    /**
     * Test of getSeqRegion method, of class ComplementRegion.
     */
    @Test
    public void testGetSeqRegion() {
        assertEquals(expected, regionTest.getSeqRegion(seq));
    }
}