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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author Jordan Fish <fishjord at msu.edu>
 */
public class ToFasta {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("m", "mask", true, "Mask sequence name indicating columns to drop");
        String maskSeqid = null;

        try {
            CommandLine line = new PosixParser().parse(options, args);

            if (line.hasOption("mask")) {
                maskSeqid = line.getOptionValue("mask");
            }

            args = line.getArgs();
            if ( args.length == 0){
                throw new Exception("");
            }

        } catch (Exception e) {
            new HelpFormatter().printHelp("USAGE: to-fasta <input-file>", options);
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
            return;
        }

        SeqReader reader = null;

        FastaWriter out = new FastaWriter(System.out);
        Sequence seq;
        int totalSeqs = 0;
        long totalTime = System.currentTimeMillis();     
         
        for (String fname : args) {
            if (fname.equals("-")) {
                reader = new SequenceReader(System.in);
            } else {
                File seqFile = new File(fname);

                if (maskSeqid == null) {
                    reader = new SequenceReader(seqFile);
                } else {
                    reader = new IndexedSeqReader(seqFile, maskSeqid);
                }              
            }

            long startTime = System.currentTimeMillis();
            int thisFileTotalSeqs = 0;
            while ((seq = reader.readNextSequence()) != null) {
                out.writeSeq(seq.getSeqName().replace(" ", "_"), seq.getDesc(), seq.getSeqString());
                thisFileTotalSeqs++;
            }
            totalSeqs += thisFileTotalSeqs;
            System.err.println("Converted " + thisFileTotalSeqs + " (total sequences: " + totalSeqs + ") sequences from " + fname + " (" + reader.getFormat() + ") to fasta in " + (System.currentTimeMillis() - startTime) / 1000 + " s");
        }
            System.err.println("Converted " + totalSeqs + " to fasta in " + (System.currentTimeMillis() - totalTime) / 1000 + " s");

        out.close();
    }
}
