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
package edu.msu.cme.rdp.readseq.readers.core;

import edu.msu.cme.rdp.readseq.QSequence;
import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class FastqCoreTest {

    private static final String expectedSeqName = "001043_1783_0863";
    private static final String expectedSeq = "CTGATCATTGGGCGTAAAGAGTGCGCAGGCGGTTTGTTAAGCGAGATGTGAAAGCCCCGGGCTCAACCTGGGAATTGCATTTCGAACTGGCGAACTAGAGTCTTGTAGAGGGGGTAGAATTCCAGGTGTAGCGGTGAAATGCGTAGAGATCTGGAGGAATACCGGTGGCGAAGGCGGCCCCCTGGACAAAGACTGACGCTCAGGCACGAAAGCGTGGGGAGCAAACAGGATTAAATACCCTCGTA";
    private static final byte[] expectedQual = new byte[]{27, 27, 27, 27, 27, 27, 27, 29, 26, 29, 28, 15, 27, 27, 27, 28, 27, 12, 27, 27, 27, 27, 27, 27, 27, 27, 27, 29, 25, 27, 29, 25, 29, 28, 14, 27, 30, 26, 30, 26, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 29, 28, 15, 27, 27, 27, 20, 7, 28, 27, 12, 27, 27, 27, 29, 26, 30, 26, 27, 29, 28, 15, 29, 26, 30, 26, 27, 27, 27, 29, 28, 14, 27, 27, 29, 25, 26, 27, 27, 23, 27, 27, 30, 26, 27, 27, 27, 25, 27, 27, 27, 27, 30, 26, 27, 27, 27, 27, 27, 26, 25, 22, 16, 7, 26, 26, 26, 30, 26, 29, 24, 29, 25, 27, 29, 25, 26, 27, 27, 27, 26, 27, 29, 24, 27, 27, 29, 28, 16, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 29, 26, 26, 30, 26, 29, 25, 27, 25, 30, 26, 30, 26, 27, 29, 25, 27, 27, 30, 26, 30, 26, 27, 30, 26, 26, 26, 22, 15, 6, 26, 29, 26, 24, 27, 29, 28, 16, 26, 25, 27, 26, 26, 26, 25, 27, 27, 27, 27, 27, 30, 26, 26, 27, 27, 27, 29, 28, 17, 27, 27, 26, 27, 27, 27, 20, 7, 27, 26, 24, 29, 28, 15, 26, 27, 30, 26, 27, 29, 26, 28, 27, 17, 25, 23, 29, 28, 16, 25, 24, 27, 27, 24};

    public FastqCoreTest() {
    }

    @Test
    public void test() throws IOException {
        File seqFile = new File(FastqCoreTest.class.getResource("/test_init_v4.fastq").getFile());
        FastqCore core = new FastqCore(seqFile);

        QSequence seq = (QSequence) core.readNextSeq();

        assertEquals("Sequence name doesn't match =(", expectedSeqName, seq.getSeqName());
        assertEquals("Expected sequence doesn't match actual seq", expectedSeq, seq.getSeqString());
        assertEquals("Quality sequence length doesn't match", expectedQual.length, seq.getQuality().length);

        for (int index = 0; index < expectedQual.length; index++) {
            assertEquals("Qual[" + index + "]", expectedQual[index], seq.getQuality()[index]);
        }
        // check the second sequence
        seq = (QSequence) core.readNextSeq();
        assertEquals(seq.getSeqName(), "001051_2436_2741");
        assertEquals(seq.getQuality()[0], 31);  // the first quality score is @
        assertEquals(seq.getQuality()[1], 27);  // the first quality score is <
        
        // check the third sequence
        seq = (QSequence) core.readNextSeq();
        assertEquals(seq.getSeqName(), "001053_1979_3818");
        assertEquals(seq.getSeqString(), "CTGATCACTGGGCGTAAAGGGTGCGCAGGCGGTTTGTTAAGCGAGATGTGAAAGCCCCGGGCTCAACCTGGGAATTGCATTTCGAACTGGCAAACTAGAGTCTTGTAGAGGGGGTAGAATTCCAGGTGTAGCGGTGAAATGCGTAGAGATCTGGAGGAATACCGGTGGCGAAGGCGGCCCCCTGGACAAAGACTGACGCTCAGGCACGAAAGCGTGGGGAGCAAACAGGATTAGATACCCGAGTA");
        assertEquals(seq.getQuality()[0], 26);  // the first quality score is ;
        assertEquals(seq.getQuality()[1], 27);  // the first quality score is <
         
        seq = (QSequence) core.readNextSeq();
        assertEquals(seq.getSeqName(), "001058_1806_1734");
        assertEquals(seq.getSeqString(), "CATCTCATTGGGCATAAAGAGTGCGCAGGCGGTTGTGTGTGTCAGGTGTGAAGTCTCGGGGCTTAACTCCGAAACTGCGCCTGAAACTACACAACTAGAGTATTGGAGAGGGTAGCAGAATTCATGGTGTAGCAGTGAAATGCGTAGATATCATGAGGAATACCAGAGGCGAAGGCGGCTACCTGGACAATTACTGACGCTCAGGCACGAAAGCGTGGGGAGCAAAAGGGATTAGATACCCCGGTA");
        assertEquals(seq.getQuality()[0], 27);  // the first quality score is <
        assertEquals(seq.getQuality()[1], 27);  // the first quality score is <
        
         IndexedSeqReader reader = new IndexedSeqReader(seqFile);
         seq = (QSequence)reader.readSeq("001053_1979_3818");
         
         assertEquals(seq.getSeqString(), "CTGATCACTGGGCGTAAAGGGTGCGCAGGCGGTTTGTTAAGCGAGATGTGAAAGCCCCGGGCTCAACCTGGGAATTGCATTTCGAACTGGCAAACTAGAGTCTTGTAGAGGGGGTAGAATTCCAGGTGTAGCGGTGAAATGCGTAGAGATCTGGAGGAATACCGGTGGCGAAGGCGGCCCCCTGGACAAAGACTGACGCTCAGGCACGAAAGCGTGGGGAGCAAACAGGATTAGATACCCGAGTA");
         assertEquals(seq.getQuality()[0], 26);  // the first quality score is ;
         assertEquals(seq.getQuality()[1], 27);  // the first quality score is <
         
         seq = (QSequence)reader.readSeq("001117_1940_3761");
         assertEquals(seq.getSeqString(), "AGAGAGATTGGGTGTAAAGAGCGCGTAGGCGGTCCTGTAAGCCCGGCGTGAAAACCTGGAGCTCAACTCCGGGCCTGCGCTGGGAACTGCGGGACTAGAGTCATGGAAGGGAAGTTGGAATTCCAGGTGTAGGGGTGAAATCTGTAGATATCTGGAAGAACACCGGTGGCGAAGGCGAACTTCTGGCCAATGACTGACGCTGAGGCGCGAAAGTGCGGGAGCAAACAGGATTAGATACCCGTGTA");
         assertEquals(seq.getQuality()[0], 20);  // the first quality score is 5
         assertEquals(seq.getQuality()[1], 24);  // the first quality score is 9
         
         
         
    }
}
