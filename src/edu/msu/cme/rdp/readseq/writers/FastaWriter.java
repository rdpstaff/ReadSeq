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
import edu.msu.cme.rdp.readseq.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author fishjord
 */
public class FastaWriter implements SequenceWriter {

    private PrintStream out;

    public FastaWriter(OutputStream is) {
        this(new PrintStream(is));
    }

    public FastaWriter(String s) throws IOException {
        this(new File(s));
    }

    public FastaWriter(File f) throws IOException {
        this(new PrintStream(f));
    }

    public FastaWriter(PrintStream pw) {
        this.out = pw;
    }

    public void close() {
        out.close();
    }

    public void writeSeq(Sequence s) {
        writeSeq(s.getSeqName(), s.getDesc(), s.getSeqString());
    }

    public void writeSeq(String seqid, String desc, String seqString) {
        out.println(">" + seqid + "  " + desc);
        out.println(seqString);
    }

    public void writeSeq(String seqid, String seqString) throws IOException {
        writeSeq(seqid, "", seqString);
    }

    public void writeRawQual(String seqid, byte[] qualSeq) {
        writeRawQual(seqid, "", qualSeq);
    }

    public void writeRawQual(String seqid, String desc, byte[] qualSeq) {
        StringBuilder qualString = new StringBuilder();
        for(byte b : qualSeq) {
            qualString.append(b).append("  ");
        }

        writeSeq(seqid, desc, qualString.toString());
    }
}
