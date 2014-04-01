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
package edu.msu.cme.rdp.readseq.utils;

import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author fishjord
 */
public class SeqFileSplitter {

    public static List<File> splitSeqFile(File seqFile, File outDir, int seqsPerSplit) throws IOException {
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Output directory " + outDir + " doesn't exist and attempt to create failed");
        } else if (!outDir.isDirectory()) {
            throw new IOException("Output directory " + outDir + " isn't a directory");
        }


        SequenceReader seqReader = new SequenceReader(seqFile);
        FastaWriter out = null;
        List<File> ret = new ArrayList();
        try {
            int splitno = 0, seqsWritten = 0;
            String seqFileName = seqFile.getName();

            File split = new File(outDir, (splitno++) + "_" + seqFileName);
            ret.add(split);
            out = new FastaWriter(split);
            Sequence seq;

            while ((seq = seqReader.readNextSequence()) != null) {                
                if (seqsWritten >= seqsPerSplit) {
                    out.close();
                    split = new File(outDir, (splitno++) + "_" + seqFileName);
                    ret.add(split);
                    out = new FastaWriter(split);
                    seqsWritten = 0;
                }
                out.writeSeq(seq);
                seqsWritten ++;
            }

            return ret;
        } finally {
            seqReader.close();
            if (out != null) {
                out.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String usage = "Usage: SeqFileSplitter infile outdir seq_per_split";
        if ( args.length != 3){
            System.err.println(usage);
            return;
        }

        int seqpersplit = Integer.parseInt(args[2]);
        if ( seqpersplit < 1){
            System.err.println(usage);
            return;
        }
        SeqFileSplitter.splitSeqFile(new File(args[0]), new File(args[1]), seqpersplit);
    }
}
