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

import edu.msu.cme.rdp.readseq.QSequence;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore.QualityFunction;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author fishjord
 */
public class FastqWriter implements SequenceWriter {

    private PrintStream out;
    private QualityFunction qualFunction;
    private Byte defaultQuality;

    public FastqWriter(OutputStream is, QualityFunction qualFunction) {
        this(new PrintStream(is), qualFunction);
    }

    public FastqWriter(String s, QualityFunction qualFunction) throws IOException {
        this(new File(s), qualFunction);
    }

    public FastqWriter(File f, QualityFunction qualFunction) throws IOException {
        this(new PrintStream(f), qualFunction);
    }

    public FastqWriter(File f, QualityFunction qualFunction, byte defaultQual) throws IOException {
        this(new PrintStream(f), qualFunction, defaultQual);
    }

    public FastqWriter(PrintStream pw, QualityFunction qualFunction) {
        this.out = pw;
        this.qualFunction = qualFunction;
    }

    public FastqWriter(PrintStream pw, QualityFunction qualFunction, byte defaultQual) {
        this(pw, qualFunction);
        defaultQuality = defaultQual;
    }

    public void close() {
        out.close();
    }

    public void writeSeq(Sequence seq) throws IOException {
        if(seq.getClass() ==  QSequence.class) {
            QSequence s = (QSequence)seq;
            writeSeq(s.getSeqName(), s.getDesc(), s.getSeqString(), s.getQuality());
        } else if(defaultQuality != null) {
            byte[] qual = new byte[seq.getSeqString().length()];
            for(int index = 0;index < qual.length;index++) {
                qual[index] = defaultQuality;
            }
            writeSeq(seq.getSeqName(), seq.getDesc(), seq.getSeqString(), qual);
        } else {
            throw new IOException("Fastq writer can only write QSequences");
        }
    }
    
    public void writeSeq(String seqid, String desc, String seqString, byte[] qualSeq) {
        out.println("@" + seqid + " " + desc);
        out.println(seqString);
        out.println("+");
        StringBuffer qual = new StringBuffer();

        for(byte b : qualSeq) {
            qual.append(qualFunction.translate(b));
        }
        out.println(qual);
    }
}
