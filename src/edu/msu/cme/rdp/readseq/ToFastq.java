/*
 * Copyright (C) 2013 Jordan Fish <fishjord at msu.edu>
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

import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import edu.msu.cme.rdp.readseq.readers.QSeqReader;
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.writers.FastqWriter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Jordan Fish <fishjord at msu.edu>
 */
public class ToFastq {

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            System.err.println("USAGE: ToFastq <seqfile> [qualfile]");
            System.exit(1);
        }

        final SeqReader reader;
        if (args.length == 2) {
            reader = new QSeqReader(new File(args[0]), new File(args[1]));
        } else {
            reader = new SequenceReader(new File(args[0]));
        }

        Sequence seq = reader.readNextSequence();
        if (!(seq instanceof QSequence)) {
            throw new IOException("Input doesn't contain quality information");
        }

        FastqWriter out = new FastqWriter(System.out, FastqCore.Phred33QualFunction);

        long startTime = System.currentTimeMillis();
        int thisFileTotalSeqs = 0;
        do {
            out.writeSeq(seq);
            thisFileTotalSeqs++;
        } while ((seq = reader.readNextSequence()) != null);
        System.err.println("Converted " + thisFileTotalSeqs + " sequences (" + reader.getFormat() + ") to fastq in " + (System.currentTimeMillis() - startTime) / 1000 + " s");

    }
}
