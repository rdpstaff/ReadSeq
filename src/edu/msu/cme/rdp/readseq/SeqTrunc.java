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

import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author fishjord
 */
public class SeqTrunc {

    public static void main(String[] args) throws IOException {
        if(args.length != 2) {
            System.err.println("USAGE: SeqTrunc <in_file> <trunc_len>");
            System.exit(1);
        }

        File seqFile = new File(args[0]);
        int truncLen = Integer.parseInt(args[1]);
        FastaWriter out = new FastaWriter(new BufferedOutputStream(new FileOutputStream("trunc_seqs.fasta")));
        int[] lengthHisto = new int[255];

        Sequence seq;
        SeqReader reader = new SequenceReader(seqFile);
        while((seq = reader.readNextSequence()) != null) {
            String seqString = seq.getSeqString();
            int seqLength = seqString.length();
            if(seqLength > truncLen) {
                seqString = seqString.substring(0, truncLen);
            }

            lengthHisto[seqLength]++;

            out.writeSeq(seq.getSeqName(), seq.getDesc(), seqString);
        }
        out.close();

        int count = 0;
        int tot = 0;
        for(int index = 0;index < lengthHisto.length;index++) {
            tot += lengthHisto[index] * index;
            count += lengthHisto[index];

            if(lengthHisto[index] > 0) {
                System.err.println(index + "\t" + lengthHisto[index]);
            }
        }

        System.err.println("Total sequences: " + count + ", average length: " + ((float)tot / count));
    }
}
