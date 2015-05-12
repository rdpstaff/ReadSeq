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

import edu.msu.cme.rdp.readseq.QSequence;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.readers.QSeqReader;
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import edu.msu.cme.rdp.readseq.writers.FastqWriter;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author fishjord
 */
public class QualityTrimmer {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("f", "fastq-out", false, "Write fastq instead of fasta file, offset 33 (#)");
        options.addOption("l", "less-than", false, "Trim at <= instead of strictly =");
        options.addOption("i", "illumina", false, "Illumina trimming mode");
        options.addOption("m", "min_seq_length", true, "filter sequence by minimum sequence length, default is 1");

        FastqWriter fastqOut = null;
        FastaWriter fastaOut = null;

        byte qualTrim = -1;

        boolean writeFasta = true;
        boolean trimle = false;
        boolean illumina = false;
        int length = 1;

        List<SeqReader> readers = new ArrayList();
        List<File> seqFiles = new ArrayList();
        FastqCore.QualityFunction  qualityFunction = FastqCore.Phred33QualFunction;

        try {
            CommandLine line = new PosixParser().parse(options, args);

            if (line.hasOption("fastq-out")) {
                writeFasta = false;
            }

            if (line.hasOption("less-than")) {
                trimle = true;
            }
            if (line.hasOption("illumina")) {
                illumina = true;
            }
            
            if (line.hasOption("min_seq_length")) {
                length = Integer.parseInt(line.getOptionValue("min_seq_length"));
            }
            
            args = line.getArgs();

            if (args.length < 2) {
                throw new Exception("Unexpected number of arguments");
            }

            if (args[0].length() != 1) {
                throw new Exception("Expected single character quality score");
            }

            qualTrim = qualityFunction.translate(args[0].charAt(0));
            if ( qualTrim != 2 && qualTrim != 33){
                throw new Exception("Expected single character quality score, B or #");
            }

            for (int index = 1; index < args.length; index++) {
                File seqFile = new File(args[index]);
                SeqReader reader;
                if (SeqUtils.guessFileFormat(seqFile) == SequenceFormat.FASTA) {
                    if (index + 1 == args.length) {
                        throw new Exception("Fasta files must be immediately followed by their quality file");
                    }

                    File qualFile = new File(args[index + 1]);
                    if (SeqUtils.guessFileFormat(qualFile) != SequenceFormat.FASTA) {
                        throw new Exception(seqFile + " was not followed by a fasta quality file");
                    }

                    reader = new QSeqReader(seqFile, qualFile);
                    index++;
                } else {
		    if(seqFile.getName().endsWith(".gz")) {
			reader = new SequenceReader(new GZIPInputStream(new FileInputStream(seqFile)));
		    } else {
			reader = new SequenceReader(seqFile);
		    }
                }

                readers.add(reader);
                seqFiles.add(seqFile);
            }
        } catch (Exception e) {
            new HelpFormatter().printHelp("USAGE: QualityTrimmer [options] <ascii_score> <seq_file> [qual_file]",
                    "This program trims off the trailing bases with ascii_score. Use '#' for qscore offset of 33, 'B' for offset 64\n", options, "", true);
	    System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        for (int readerIndex = 0; readerIndex < readers.size(); readerIndex++) {
            File seqFile = seqFiles.get(readerIndex);
            String outStem = "trimmed_" + seqFile.getName().substring(0, seqFile.getName().lastIndexOf("."));
            if (writeFasta) {
                fastaOut = new FastaWriter(outStem + ".fasta");
            } else {
                if ( qualTrim == 2) {
                    fastqOut = new FastqWriter(outStem + ".fastq", FastqCore.Phred33QualFunction);
                } else if ( qualTrim == 33){ // fastq writer is different for different offset, because we output qscore of offset 33 only
                    fastqOut = new FastqWriter(outStem + ".fastq", FastqCore.Phred64QualFunction);
                } 
            }

            int[] lengthHisto = new int[200];

            SeqReader reader = readers.get(readerIndex);

            QSequence qseq;

            long totalLength = 0;
            int totalSeqs = 0;
            long trimmedLength = 0;
            int trimmedSeqs = 0;

            int zeroLengthAfterTrimming = 0;

            long startTime = System.currentTimeMillis();

            while ((qseq = (QSequence) reader.readNextSequence()) != null) {
                char[] bases = qseq.getSeqString().toCharArray();
                byte[] qual = qseq.getQuality();

                if (bases.length != qual.length) {
                    System.err.println(qseq.getSeqName() + ": Quality length doesn't match seq length for seq");
                    continue;
                }

                totalSeqs++;
                totalLength += bases.length;

                int trimIndex = -1;
                if (illumina && qual[bases.length - 1] == qualTrim) {
                    trimIndex = bases.length - 1;
                    while (trimIndex >= 0 && qual[trimIndex] == qualTrim) {
                        trimIndex--;
                    }

                    trimIndex++; //Technically we're positioned over the first good base, move back to the last bad base
                } else if (!illumina) {
                    for (int index = 0; index < bases.length; index++) {
                        if (qual[index] == qualTrim || (trimle && qual[index] < qualTrim)) {
                            trimIndex = index;
                            break;
                        }
                    }
                }

                String outSeq;
                byte[] outQual;
                if (trimIndex == -1) {
                    outSeq = qseq.getSeqString();
                    outQual = qseq.getQuality();
                } else {
                    outSeq = new String(bases, 0, trimIndex);
                    outQual = Arrays.copyOfRange(qual, 0, trimIndex);
                    trimmedSeqs++;
                }
                int len = outSeq.length();
                trimmedLength += len;

                if(len >= lengthHisto.length) {
                    lengthHisto = Arrays.copyOf(lengthHisto, len + 1);
                }
                lengthHisto[len]++;

                if (outSeq.length() == 0) {
                    //System.err.println(qseq.getSeqName() + ": length 0 after trimming");
                    zeroLengthAfterTrimming++;
                    continue;
                }
                if (outSeq.length() < length) {
                    continue;
                }

                if (writeFasta) {
                    fastaOut.writeSeq(qseq.getSeqName(), qseq.getDesc(), outSeq);
                } else {
                    fastqOut.writeSeq(qseq.getSeqName(), qseq.getDesc(), outSeq, outQual);
                }
            }

            reader.close();

            if (writeFasta) {
                fastaOut.close();
            } else {
                fastqOut.close();
            }

            System.out.println("Processed " + seqFile + " in " + (System.currentTimeMillis() - startTime) / 1000.0 + "s");
            System.out.println("Before trimming:");
            System.out.println("Total Sequences:           " + totalSeqs);
            System.out.println("Total Sequence Data:       " + totalLength);
            System.out.println("Average sequence length:   " + ((float) totalLength / totalSeqs));
            System.out.println();
            System.out.println("After trimming:");
            System.out.println("Total Sequences:           " + (totalSeqs - zeroLengthAfterTrimming));
            System.out.println("Sequences Trimmed:         " + trimmedSeqs);
            System.out.println("Total Sequence Data:       " + trimmedLength);
            System.out.println("Average sequence length:   " + ((float) trimmedLength / (totalSeqs - zeroLengthAfterTrimming)));
            System.out.println();

            System.out.println("Length\tCount");
            for(int index = 0;index < lengthHisto.length;index++) {
                if(lengthHisto[index] == 0) {
                    continue;
                }

                System.out.println(index + "\t" + lengthHisto[index]);
            }

            System.out.println();
            System.out.println();
            System.out.println();
        }
    }
}
