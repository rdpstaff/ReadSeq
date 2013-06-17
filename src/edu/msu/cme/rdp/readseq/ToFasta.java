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
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import java.io.File;

/**
 *
 * @author Jordan Fish <fishjord at msu.edu>
 */
public class ToFasta {

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            System.err.println("USAGE: to-fasta <input-file> [mask-seqid]");
            return;
        }

        SeqReader reader = null;
        int totalSeqs = 0;

        if (args[0].equals("-")) {
            reader = new SequenceReader(System.in);
        } else {
            File seqFile = new File(args[0]);

            if (args.length == 1) {
                reader = new SequenceReader(seqFile);
            } else {
                reader = new IndexedSeqReader(seqFile, args[1]);
            }
        }

        FastaWriter out = new FastaWriter(System.out);
        Sequence seq;

        long startTime = System.currentTimeMillis();
        while ((seq = reader.readNextSequence()) != null) {
            out.writeSeq(seq.getSeqName().replace(" ", "_"), seq.getDesc(), seq.getSeqString());
            totalSeqs++;
        }

        System.err.println("Converted " + totalSeqs + " sequences from " + args[0] + " (" + reader.getFormat() + ") to fasta in " + (System.currentTimeMillis() - startTime) / 1000 + " s");
        out.close();
    }
}
