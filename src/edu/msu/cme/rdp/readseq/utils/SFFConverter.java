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

import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import edu.msu.cme.rdp.readseq.writers.FastqWriter;
import edu.msu.cme.rdp.readseq.QSequence;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author fishjord
 */
public class SFFConverter {

    public static void convert(File sffFile, File workDir, String outfilePrefix, SequenceFormat outFormat) throws IOException {
        if(outFormat == SequenceFormat.FASTA) {
            convertToFasta(sffFile, workDir, outfilePrefix);
        } else if(outFormat == SequenceFormat.FASTQ) {
            convertToFastq(sffFile, workDir, outfilePrefix);
        } else {
            throw new IOException("Unsupported target format " + outFormat);
        }
    }

    private static void convertToFasta(File sffFile, File workDir, String outfilePrefix) throws IOException {
        SFFCore core = new SFFCore(sffFile);
        FastaWriter fastaWriter = new FastaWriter(new File(workDir, outfilePrefix + ".fasta"));
        FastaWriter qualWriter = new FastaWriter(new File(workDir, outfilePrefix + ".qual"));
        QSequence seq;

        while ((seq = core.readNextSeq()) != null) {
            fastaWriter.writeSeq(seq);
            qualWriter.writeSeq(seq.getSeqName(), "", SeqUtils.translateQualString(seq.getQuality()));

        }

        qualWriter.close();
        fastaWriter.close();
        core.close();

    }

    private static void convertToFastq(File sffFile, File workDir, String outfilePrefix) throws IOException {
        SFFCore core = new SFFCore(sffFile);
        FastqWriter fastqWriter = new FastqWriter(new File(workDir, outfilePrefix + ".fastq"), FastqCore.Phred33QualFunction);
        QSequence seq;

        while ((seq = core.readNextSeq()) != null) {
            fastqWriter.writeSeq(seq);
        }

        fastqWriter.close();
        core.close();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3 && args.length != 4) {
            System.err.println("USAGE: SFFConverter [fasta|fastq] <sff_file> <outfile_prefix> [workdir]");
            return;
        }

        String format = args[0].toUpperCase();
        File sffFile = new File(args[1]);
        String outfilePrefix = args[2];
        File workdir = new File(".");
        if (args.length == 4) {
            workdir = new File(args[3]);
        }

        if (!format.equals("FASTA") && !format.equals("FASTQ")) {
            System.err.println("SFFConverter will only convert to fasta or fastq files");
            return;
        }

        convert(sffFile, workdir, outfilePrefix, SequenceFormat.valueOf(format));
    }
}
