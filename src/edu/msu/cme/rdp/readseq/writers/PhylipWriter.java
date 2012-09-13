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

package edu.msu.cme.rdp.readseq.writers;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author fishjord
 */
public class PhylipWriter implements SequenceWriter {
    private PrintStream out;
    private int seqLength;
    private static final int TAX_ID_LENGTH = 10;

    public PhylipWriter(OutputStream o, int numTaxa, int seqLength) {
        this.out = new PrintStream(o);
        out.println(numTaxa + "  " + seqLength);
        this.seqLength = seqLength;
    }

    private static String getSeqId(String seqid) {
        for(int index = seqid.length();index < TAX_ID_LENGTH;index++)
            seqid += " ";
        return seqid;
    }

    public void writeSeq(Sequence seq) throws IOException {
        writeSeq(seq.getSeqName(), seq.getSeqString());
    }

    public void writeSeq(String seqid, String seqString) throws IOException {
        if(seqString.length() != seqLength)
            throw new IOException("Sequencec seq " + seqid + " is not the expected length (" + seqLength + ")");

        out.println(getSeqId(seqid) + seqString);
    }

    public void writeSeq(String seqid, String desc, String seqString) throws IOException {
        throw new UnsupportedOperationException("Phylip format doesn't support sequence descriptions");
    }

    public void close() throws IOException {
        out.close();
    }

    public static void writeSequences(List<Sequence> seqs, OutputStream out) throws IOException {
        PhylipWriter writer = new PhylipWriter(out, seqs.size(), seqs.get(0).getSeqString().length());

        for(Sequence seq : seqs)
            writer.writeSeq(seq);

        out.close();
    }
}
