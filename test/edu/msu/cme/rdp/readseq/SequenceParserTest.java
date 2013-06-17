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

package edu.msu.cme.rdp.readseq;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import java.io.FileInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class SequenceParserTest {
    /**
     * Test of getNextSequence method, of class SequenceReader.
     */
    @Test
    public void testGetNextSequence() throws Exception {
        SequenceReader parser = new SequenceReader(new FileInputStream("test/test.fa"));

        Sequence seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("test0", seq.getSeqName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq.getSeqString());

        seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("test1", seq.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN-.~", seq.getSeqString());

        seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("test2<I_Hate_You>", seq.getSeqName());
        assertEquals("ccccccccccccccccccccccccccccccccccccc", seq.getSeqString());

        seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("struct", seq.getSeqName());
        assertEquals(".-<({[])}>-.", seq.getSeqString());

        seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("mask", seq.getSeqName());
        assertEquals("00000................1000000000A00000", seq.getSeqString());

        assertNull(parser.readNextSequence());

        parser.close();
    }
    /**
     * Test of getNextSequence method, of class SequenceReader.
     */
    @Test
    public void testWhitespace() throws Exception {
        SequenceReader parser = new SequenceReader(new FileInputStream("test/test_1.fa"));

        Sequence seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("test0", seq.getSeqName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq.getSeqString());

        seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("test1", seq.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN-.~", seq.getSeqString());

        seq = parser.readNextSequence();
        assertNotNull(seq);
        assertEquals("test2<I_Hate_You>", seq.getSeqName());
        assertEquals("ccccccccccccccccccccccccccccccccccccc", seq.getSeqString());

        parser.close();
    }

}