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
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.utils.SequenceTrimmer.TrimStats;
import java.io.StringReader;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class SequenceTrimmerTest {

    /**
     * Test of readBarcodeFile method, of class BarcodeUtils.
     */
    @Test
    public void testTrimStats() throws Exception {
	String trimSeq1 = "aa..---GCTG..GAAT--cc..";
	String trimSeq2 = "aaaaGCGTTGAngTTCTTCggac";
	String trimSeq3 = "....---ACT-..------....";
	String trimSeq4 = "....-------..---TTC....";

	int trimStart = 3;
	int trimStop = 6;

	TrimStats trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq1), trimStart, trimStop);

	assertEquals("seq1 seqStart", 3, trimStats.seqStart);
	assertEquals("seq1 seqStop", 10, trimStats.seqStop);
	assertEquals("seq1 seqTrimStart", 3, trimStats.seqTrimStart);
	assertEquals("seq1 seqTrimStop", 6, trimStats.seqTrimStop);
	assertEquals("seq1 seqLength", 12, trimStats.seqLength);
	assertEquals("seq1 trimmedLength", 3, trimStats.trimmedLength);
	assertEquals("seq1 modelLength", 13, trimStats.modelLength);
	assertEquals("seq1 numNs", 0, trimStats.numNs);
	assertEquals("seq1 nsInTrim", 0, trimStats.nsInTrim);
	assertEquals("seq1 trimmed seq", "GCT", new String(trimStats.trimmedBases));

	trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq2), trimStart, trimStop);

	assertEquals("seq2 seqStart", 0, trimStats.seqStart);
	assertEquals("seq2 seqStop", 12, trimStats.seqStop);
	assertEquals("seq2 seqTrimStart", 8, trimStats.seqTrimStart);
	assertEquals("seq2 seqTrimStop", 11, trimStats.seqTrimStop);
	assertEquals("seq2 seqLength", 23, trimStats.seqLength);
	assertEquals("seq2 trimmedLength", 3, trimStats.trimmedLength);
	assertEquals("seq2 modelLength", 13, trimStats.modelLength);
	assertEquals("seq2 numNs", 1, trimStats.numNs);
	assertEquals("seq2 nsInTrim", 0, trimStats.nsInTrim);
	assertEquals("seq2 trimmed seq", "TTG", new String(trimStats.trimmedBases));

	trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq3), trimStart, trimStop);

	assertEquals("seq3 trimmed seq", "ACT", new String(trimStats.trimmedBases));
	assertEquals("seq3 seqStart", 3, trimStats.seqStart);
	assertEquals("seq3 seqStop", 5, trimStats.seqStop);
	assertEquals("seq3 seqTrimStart", 1, trimStats.seqTrimStart);
	assertEquals("seq3 seqTrimStop", 3, trimStats.seqTrimStop);
	assertEquals("seq3 seqLength", 3, trimStats.seqLength);
	assertEquals("seq3 trimmedLength", 3, trimStats.trimmedLength);
	assertEquals("seq3 modelLength", 13, trimStats.modelLength);
	assertEquals("seq3 numNs", 0, trimStats.numNs);
	assertEquals("seq3 nsInTrim", 0, trimStats.nsInTrim);

	trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq4), trimStart, trimStop);

	assertEquals("seq4 trimmed seq", "---", new String(trimStats.trimmedBases));
	assertEquals("seq4 seqStart", 10, trimStats.seqStart);
	assertEquals("seq4 seqStop", 12, trimStats.seqStop);
	assertEquals("seq4 seqTrimStart", 0, trimStats.seqTrimStart);
	assertEquals("seq4 seqTrimStop", 0, trimStats.seqTrimStop);
	assertEquals("seq4 seqLength", 3, trimStats.seqLength);
	assertEquals("seq4 trimmedLength", 0, trimStats.trimmedLength);
	assertEquals("seq4 modelLength", 13, trimStats.modelLength);
	assertEquals("seq4 numNs", 0, trimStats.numNs);
	assertEquals("seq4 nsInTrim", 0, trimStats.nsInTrim);

	assertEquals("gGG", SequenceTrimmer.trimMetaSeq("....aAUgGGT..AccTTC....", trimStart, trimStop));
	assertEquals("-<,", SequenceTrimmer.trimMetaSeq("....[<<-<,-..->->>]....", trimStart, trimStop));
    }

    /**
     * Test of readBarcodeFile method, of class BarcodeUtils.
     */
    @Test
    public void testTrimStatsWithInsert() throws Exception {
	String trimSeq1 = "aa..---GCTG..GAAT--cc..";
	String trimSeq2 = "aaaaGCGTTGAngTTCTTNggac";
	String trimSeq3 = "....---ACT-..------....";
	String trimSeq4 = "....-------..---TTC....";

	int trimStart = 3;
	int trimStop = 9;

	TrimStats trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq1), trimStart, trimStop);

	assertEquals("seq1 seqStart", 3, trimStats.seqStart);
	assertEquals("seq1 seqStop", 10, trimStats.seqStop);
	assertEquals("seq1 seqTrimStart", 3, trimStats.seqTrimStart);
	assertEquals("seq1 seqTrimStop", 9, trimStats.seqTrimStop);
	assertEquals("seq1 seqLength", 12, trimStats.seqLength);
	assertEquals("seq1 trimmedLength", 6, trimStats.trimmedLength);
	assertEquals("seq1 modelLength", 13, trimStats.modelLength);
	assertEquals("seq1 numNs", 0, trimStats.numNs);
	assertEquals("seq1 nsInTrim", 0, trimStats.nsInTrim);
	assertEquals("seq1 trimmed seq", "GCTG..GA", new String(trimStats.trimmedBases));

	trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq2), trimStart, trimStop);

	assertEquals("seq2 seqStart", 0, trimStats.seqStart);
	assertEquals("seq2 seqStop", 12, trimStats.seqStop);
	assertEquals("seq2 seqTrimStart", 8, trimStats.seqTrimStart);
	assertEquals("seq2 seqTrimStop", 16, trimStats.seqTrimStop);
	assertEquals("seq2 seqLength", 23, trimStats.seqLength);
	assertEquals("seq2 trimmedLength", 8, trimStats.trimmedLength);
	assertEquals("seq2 modelLength", 13, trimStats.modelLength);
	assertEquals("seq2 numNs", 2, trimStats.numNs);
	assertEquals("seq2 nsInTrim", 1, trimStats.nsInTrim);
	assertEquals("seq2 trimmed seq", "TTGAngTT", new String(trimStats.trimmedBases));

	trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq3), trimStart, trimStop);

	System.err.println(new String(trimStats.trimmedBases));
	assertEquals("seq3 seqStart", 3, trimStats.seqStart);
	assertEquals("seq3 seqStop", 5, trimStats.seqStop);
	assertEquals("seq3 seqTrimStart", 1, trimStats.seqTrimStart);
	assertEquals("seq3 seqTrimStop", 3, trimStats.seqTrimStop);
	assertEquals("seq3 seqLength", 3, trimStats.seqLength);
	assertEquals("seq3 trimmedLength", 3, trimStats.trimmedLength);
	assertEquals("seq3 modelLength", 13, trimStats.modelLength);
	assertEquals("seq3 nsInTrim", 0, trimStats.nsInTrim);
	assertEquals("seq3 numNs", 0, trimStats.numNs);

	trimStats = SequenceTrimmer.getStats(new Sequence("", "", trimSeq4), trimStart, trimStop);

	assertEquals("seq4 trimmed seq", "----..--", new String(trimStats.trimmedBases));
	assertEquals("seq4 seqStart", 10, trimStats.seqStart);
	assertEquals("seq4 seqStop", 12, trimStats.seqStop);
	assertEquals("seq4 seqTrimStart", 0, trimStats.seqTrimStart);
	assertEquals("seq4 seqTrimStop", 0, trimStats.seqTrimStop);
	assertEquals("seq4 seqLength", 3, trimStats.seqLength);
	assertEquals("seq4 trimmedLength", 0, trimStats.trimmedLength);
	assertEquals("seq4 modelLength", 13, trimStats.modelLength);
	assertEquals("seq4 nsInTrim", 0, trimStats.nsInTrim);
	assertEquals("seq4 numNs", 0, trimStats.numNs);
    }

    @Test
    public void testCoordTranslation() {
	String trimSeq1 = "aa..---GCTGa.GAAT--cc..";
        Sequence seq = new Sequence("", "", trimSeq1);

        assertEquals(1, SequenceTrimmer.translateCoord(1, seq, SequenceTrimmer.CoordType.seq, SequenceTrimmer.CoordType.seq));

        assertEquals(0, SequenceTrimmer.translateCoord(1, seq, SequenceTrimmer.CoordType.seq, SequenceTrimmer.CoordType.model));
        assertEquals(3, SequenceTrimmer.translateCoord(3, seq, SequenceTrimmer.CoordType.seq, SequenceTrimmer.CoordType.model));
        assertEquals(7, SequenceTrimmer.translateCoord(7, seq, SequenceTrimmer.CoordType.seq, SequenceTrimmer.CoordType.model));
        assertEquals(7, SequenceTrimmer.translateCoord(8, seq, SequenceTrimmer.CoordType.seq, SequenceTrimmer.CoordType.model));

    }
}
