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

import java.io.IOException;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.SequenceType;
import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class SequenceFormatTest {

    @Test
    public void testSequenceFormatCheck() throws IOException {
        assertEquals(SequenceFormat.FASTA, SeqUtils.guessFileFormat(new File("test/test.fa")));
        assertEquals(SequenceFormat.FASTQ, SeqUtils.guessFileFormat(new File("test/test_init_v4.fastq")));
        assertEquals(SequenceFormat.STK, SeqUtils.guessFileFormat(new File("test/test.sto")));
        assertEquals(SequenceFormat.SFF, SeqUtils.guessFileFormat(new File("test/454Reads.sff")));
    }
    
    @Test
    public void testSequenceType() throws IOException {
        assertEquals(SequenceType.Nucleotide, SeqUtils.guessSequenceType(new File("test/test.fa")));
        assertEquals(SequenceType.Nucleotide, SeqUtils.guessSequenceType(new File("test/test_init_v4.fastq")));
        assertEquals(SequenceType.Protein, SeqUtils.guessSequenceType(new File("test/test.sto")));        
    }
}
