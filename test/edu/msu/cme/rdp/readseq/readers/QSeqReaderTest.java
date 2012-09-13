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

package edu.msu.cme.rdp.readseq.readers;

import edu.msu.cme.rdp.readseq.QSequence;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class QSeqReaderTest {

    private static final String expectedSeqName = "GB4XUSJ08J0EJJ";
    private static final String expectedSeq = "GGCGCAGACGGTTACTTAAGCAGGATGTGAAATCCCCGGGCTCAACCCGGGAACTGCGTTCTGAACTGGGTGACTCGAGTGTGTCAGAGGGAGGTAGAATTCCACGTGTAGCAGTGAAATGCGTAGAGATGTGGAGGAATACCGATGGCGAAGGCAGCCTCCTGGGACAACACTGACGTTCATGCCCGAAAGCGTGGGTAGCAAACAGGATTAGATACCCTGGTAGTCCGCGCCCTAAACGATGTCAATTAGCTGTTGGGCAACCTGATTGCTTGGTAGCGTAGCTAACGCGTGAAATTGACCGCCTGGGGAGTACGGTCGCAAGATTAAA";
    private static final byte[] expectedQual = new byte[]{21, 21, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 34, 34, 33, 33, 39, 39, 39, 40, 40, 40, 40, 40, 39, 39, 39, 30, 30, 30, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 21, 21, 21, 35, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 39, 39, 39, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 34, 34, 34, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 28, 28, 28, 28, 35, 29, 40, 40, 40, 40, 40, 40, 40, 40, 40, 39, 39, 36, 36, 38, 38, 22, 22, 24};

    public QSeqReaderTest() {
    }

    @Test
    public void testPairedFile() throws Exception {
        File seqFile = new File(QSeqReaderTest.class.getResource("/BMC200_trimmed.fasta").getFile());
        File qualFile = new File(QSeqReaderTest.class.getResource("/BMC200_trimmed.qual").getFile());

        QSeqReader seqReader = new QSeqReader(seqFile, qualFile);

        QSequence seq = (QSequence)seqReader.readNextSequence();
        assertEquals("Sequence name doesn't match =(", expectedSeqName, seq.getSeqName());
        assertEquals("Expected sequence doesn't match actual seq", expectedSeq, seq.getSeqString());
        assertEquals("Quality sequence length doesn't match", expectedQual.length, seq.getQuality().length);

        for (int index = 0; index < expectedQual.length; index++) {
            assertEquals("Qual[" + index + "]", expectedQual[index], seq.getQuality()[index]);
        }
    }

}