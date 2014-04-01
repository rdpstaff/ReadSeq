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
import edu.msu.cme.rdp.readseq.readers.core.EMBLCore;
import edu.msu.cme.rdp.readseq.readers.core.FastaCore;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.readers.core.GenbankCore;
import edu.msu.cme.rdp.readseq.readers.core.SeqReaderCore;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore;
import edu.msu.cme.rdp.readseq.readers.core.STKCore;
import edu.msu.cme.rdp.readseq.readers.core.SeqReaderCore.EmptyCore;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fishjord
 */
public class SequenceReader implements SeqReader {

    private SeqReaderCore core;
    private SequenceFormat format;

    public SequenceReader(File f) throws IOException {
        core = SeqUtils.getSeqReaderCore(f);
        format = SeqUtils.guessFileFormat(f);
    }

    public SequenceReader(InputStream in) throws IOException {
        core = SeqUtils.getSeqReaderCore(in);
        format = SequenceFormat.UNKNOWN; //XXX hack
    }

    public SequenceFormat getFormat() {
        return format;
    }

    public long getPosition() {
	return core.getPosition();
    }

    public SequenceFormat getSeqFileFormat() {
        return format;
    }

    public Sequence readNextSequence() throws IOException {
        Sequence seq = core.readNextSequence();
        if(seq != null) {
            seq.setSeqString(SeqUtils.filterSeqString(seq.getSeqString(), false));
        }
        return seq;
    }

    public void close() throws IOException {
        core.close();
    }

    public static List<Sequence> readFully(File f) throws IOException {
        SequenceReader reader = new SequenceReader(f);
        List<Sequence> ret = new ArrayList();
        Sequence seq;

        try {
            while ((seq = reader.readNextSequence()) != null) {
                ret.add(seq);
            }

            return ret;
        } finally {
            reader.close();
        }
    }

    public static List<Sequence> readFully(InputStream f) throws IOException {
        SequenceReader reader = new SequenceReader(f);
        List<Sequence> ret = new ArrayList();
        Sequence seq;

        try {
            while ((seq = reader.readNextSequence()) != null) {
                ret.add(seq);
            }

            return ret;
        } finally {
            reader.close();
        }
    }
}
