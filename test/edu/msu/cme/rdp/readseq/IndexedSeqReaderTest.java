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
import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class IndexedSeqReaderTest {

    /**
     * Test of getSeqIds method, of class IndexedSeqReader.
     */
    @Test
    public void testGetSeqIds() throws Exception {
        IndexedSeqReader reader = new IndexedSeqReader(new File("test/test.fa"));

        Sequence s = reader.readSeq("test1");

        assertEquals("test1", s.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN-.~", s.getSeqString());

        s = reader.readSeq("struct");
        assertEquals("struct", s.getSeqName());
        assertEquals(".-<({[])}>-.", s.getSeqString());
        
        reader.close();

        reader = new IndexedSeqReader(new File("test/test.fa"), true);
        s = reader.readSeq("test1");

        assertEquals("test1", s.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN", s.getSeqString());
        
        s = reader.readSeq("test2<I_Hate_You>");

        assertEquals("test2<I_Hate_You>", s.getSeqName());
        assertEquals("ccccccccccccccccccccccccccccccccccccc", s.getSeqString());

        reader.close();
    }

    /**
     * Test of readSeq method, of class IndexedSeqReader.
     */
    @Test
    public void testReadSeq() throws Exception {
        IndexedSeqReader reader = new IndexedSeqReader(new File("test/test.fa"));

        assertEquals("test0", reader.getSeqIds().get(0));
        assertEquals("test1", reader.getSeqIds().get(1));
        assertEquals("test2<I_Hate_You>", reader.getSeqIds().get(2));

        reader.close();
    }

    @Test
    public void testExternalIndex() throws Exception {
        IndexedSeqReader.indexSeqFile(new File("test/test.fa"), new File("test/test.index"), SequenceFormat.FASTA);

        IndexedSeqReader reader = new IndexedSeqReader(new File("test/test.fa"), new File("test/test.index"));

        Sequence s = reader.readSeq("test1");

        assertEquals("test1", s.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN-.~", s.getSeqString());
        reader.close();
    }

    @Test
    public void testMaskingReader() throws Exception {
        IndexedSeqReader reader = new IndexedSeqReader(new File("test/test.fa"), "mask");
        Sequence s = reader.readSeq("test1");

        assertEquals("test1", s.getSeqName());
        assertEquals("KX", s.getSeqString());
        reader.close();

    }
}