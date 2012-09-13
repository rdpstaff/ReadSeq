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

import edu.msu.cme.rdp.readseq.SequenceFormat;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author fishjord
 */
public class MemSeqReader implements SeqReader {

    private Iterator<Sequence> seqs;

    public MemSeqReader(Collection<Sequence> seqs) {
        this.seqs = seqs.iterator();
    }

    public MemSeqReader(Sequence ... seqs) {
        this.seqs = Arrays.asList(seqs).iterator();
    }

    public Sequence readNextSequence() throws IOException {
        if(seqs != null && seqs.hasNext()) {
            return seqs.next();
        }

        return null;
    }

    public SequenceFormat getFormat() {
        return SequenceFormat.UNKNOWN;
    }

    public void close() throws IOException {
        seqs = null;    //We can't really 'close' an in memory sequence reader, but we can
        //indicate to the jvm that the backing sequences can be gc'ed
    }
}
