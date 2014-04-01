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

import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.output.NullWriter;

/**
 *
 * @author fishjord
 */
public class SequenceTrimmer {

    public enum CoordType {

        seq, model, alignment
    };

    public static class TrimStats {
	/**
	   Bases in the trimmed region
	 */
        char[] trimmedBases;
	/**
	   First occupied model position (base 0)
	 */

        int seqStart = -1;
	/**
	   Last occupied model position (base 0)
	*/
        int seqStop;

	/**
	   Sequence position of the trim point (base 1)
	 */
	int seqTrimStart;
	/**
	   Sequence position of the end point (base 1)
	 */
	int seqTrimStop;

	/**
	   Length of the original sequence
	 */
        int seqLength;
	/**
	   Length of the sequence after trimming
	 */
        int trimmedLength;
	/**
	   Length of the model (used for sanity checking)
	 */
	int modelLength;

	/**
	   Number of Ns in the sequence
	*/
        int numNs;
	/**
	   Number of Ns in the trim region
	*/
        int nsInTrim;

	/**
	   Filled model positions in the region
	 */
	float filledRatio;
    }

    private static Sequence readRefSeq(File refSeqFile) throws IOException {
        SeqReader reader = null;
        try {
            reader = new SequenceReader(refSeqFile);

            Sequence seq = reader.readNextSequence();
	    Sequence tmp = reader.readNextSequence();
            if (tmp != null) {
                throw new IOException("Multiple sequences in refseq file");
            }

            return seq;
        } catch (IOException e) {
            throw new IOException("Failed to read reference sequence from " + refSeqFile + ": " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static TrimStats getStats(Sequence seq, int trimStart, int trimStop) {
	if(trimStart >= trimStop) {
	    throw new IllegalArgumentException("Trim start has to come before trim end");
	}

	TrimStats ret = new TrimStats();
        int modelPos = 0;
        int seqPos = 0;
        int index;
	int filled = 0;
	char b;
	boolean ischar, isgap, ismodel, isupper, intrim;

	int alnStart = -1;

        char[] bases = seq.getSeqString().toCharArray();

        for (index = 0; index < bases.length; index++) {
	    b = bases[index];

	    ischar = Character.isLetter(b);
	    isupper = ischar && Character.isUpperCase(b);
	    isgap = !ischar && (b == '.' || b == '~' || b == '-');
	    ismodel = (b == '-') || isupper;
	    intrim = (modelPos >= trimStart && modelPos < trimStop);

            if (ischar) {
                seqPos++;

		if(b == 'n' || b == 'N') {
		    ret.numNs++;

                    if(intrim) {
                        ret.nsInTrim++;
                    }
		}

		if(intrim) {
		    ret.trimmedLength++;
		}
            }

            if (ismodel) {
		if(modelPos == trimStart) {
		    ret.seqTrimStart = seqPos;
		    alnStart = index;
		}
		if(modelPos >= trimStart && modelPos < trimStop) {
		    filled++;
		    if(ischar) {
			ret.filledRatio++;
		    }
		}

		if(modelPos == trimStop) {
		    ret.seqTrimStop = seqPos;
		    ret.trimmedBases = Arrays.copyOfRange(bases, alnStart, index);
		}

		if(!isgap) {
		    if(ret.seqStart == -1) {
			ret.seqStart = modelPos;
		    }
		    ret.seqStop = modelPos;
		}

                modelPos++;
            }
        }

	ret.seqLength = seqPos;
	ret.modelLength = modelPos;
	ret.filledRatio /= filled;

	return ret;
    }

    public static String trimMetaSeq(String seq, int trimStart, int trimStop) {
	int modelPos = 0;
	char[] bases = seq.toCharArray();
	int start = 0, stop = 0;
	char b;

	for(int index = 0;index < bases.length;index++) {
	    b = bases[index];

	    if(b == '.' || b == '~') {
		continue;
	    }

	    if(modelPos == trimStart) {
		start = index;
	    }

	    if(modelPos == trimStop) {
		stop = index;
	    }

	    modelPos++;
	}

        if(stop == 0) {
            stop = bases.length;
        }

	return new String(Arrays.copyOfRange(bases, start, stop));
    }

    public static int translateCoord(int coord, Sequence seq, CoordType in, CoordType out) {
        int modelPos = 0;
        int seqPos = 0;
        int index;

        char[] bases = seq.getSeqString().toCharArray();

        for (index = 0; index < bases.length; index++) {
            if (Character.isLetter(bases[index])) {
                seqPos++;
            }

            if ((in == CoordType.seq && seqPos == coord)
                    || (in == CoordType.model && modelPos == coord)
                    || (in == CoordType.alignment && index == coord)) {
                break;
            }

            if (bases[index] == '-' || Character.isUpperCase(bases[index])) {
                modelPos++;
            }
        }

        switch (out) {
            case seq:
                return seqPos;
            case model:
                return modelPos;
            case alignment:
                return index;
        }

        throw new IllegalArgumentException("Dunno what to do with out value " + out);
    }

    public static boolean didSeqPass(TrimStats stats, int minLength, int minTrimLength, int maxNs, int maxTrimNs, float minFilledRatio) {
	if(stats.trimmedLength <= minTrimLength) {
	    return false;
	}

	if(stats.trimmedLength <= minLength) {
	    return false;
	}

	if(stats.numNs > maxNs) {
	    return false;
	}

	if(stats.nsInTrim > maxTrimNs) {
	    return false;
	}

	if(stats.filledRatio < minFilledRatio) {
	    return false;
	}

	return true;
    }

    public static void writeStatsHeader(PrintWriter out) {
	out.println("#seq name\tfirst model pos\tlast model pos\tfilled ratio\ttrim seq start coord\ttrim seq end coord\tNs\tNs in trimmed seq\ttrimmed seq length\tseq length\tpassed trimming");
    }

    public static void writeStats(PrintWriter out, String seqName, TrimStats stats, boolean passed) {
	out.println(String.format("%s\t%d\t%d\t%.2f\t%d\t%d\t%d\t%d\t%d\t%d\t%b", seqName, stats.seqStart, stats.seqStop, stats.filledRatio, stats.seqTrimStart, stats.seqTrimStop, stats.numNs, stats.nsInTrim, stats.trimmedLength, stats.seqLength, passed));
    }

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption("r", "ref-seq", true, "Trim points are given as positions in a reference sequence from this file");
        options.addOption("i", "inclusive", false, "Trim points are inclusive");
        options.addOption("l", "length", true, "Minimum length of sequence after trimming");
        options.addOption("f", "filled-ratio", true, "Minimum ratio of filled model positions of sequence after trimming");
        options.addOption("o", "out", true, "Write sequences to directory (default=cwd)");
        options.addOption("s", "stats", true, "Write stats to file");

        PrintWriter statsOut = new PrintWriter(new NullWriter());
        boolean inclusive = false;
        int minLength = 0;
	int minTrimmedLength = 0;
	int maxNs = 0;
	int maxTrimNs = 0;

        int trimStart = 0;
        int trimStop = 0;
        Sequence refSeq = null;
	float minFilledRatio = 0;

        int expectedModelPos = -1;
        String[] inputFiles = null;
        File outdir = new File(".");
        try {
            CommandLine line = new PosixParser().parse(options, args);

            if (line.hasOption("ref-seq")) {
                refSeq = readRefSeq(new File(line.getOptionValue("ref-seq")));
            }

            if (line.hasOption("inclusive")) {
                inclusive = true;
            }

            if (line.hasOption("length")) {
                minLength = Integer.valueOf(line.getOptionValue("length"));
            }

            if (line.hasOption("filled-ratio")) {
                minFilledRatio = Float.valueOf(line.getOptionValue("filled-ratio"));
            }

            if (line.hasOption("out")) {
		outdir = new File(line.getOptionValue("out"));
		if(!outdir.isDirectory()) {
		    outdir = outdir.getParentFile();
		    System.err.println("Output option is not a directory, using " + outdir + " instead");
		}
            }

            if (line.hasOption("stats")) {
                statsOut = new PrintWriter(line.getOptionValue("stats"));
            }

            args = line.getArgs();

            if (args.length < 3) {
                throw new Exception("Unexpected number of arguments");
            }

            trimStart = Integer.parseInt(args[0]);
            trimStop = Integer.parseInt(args[1]);
            inputFiles = Arrays.copyOfRange(args, 2, args.length);

            if (refSeq != null) {
                expectedModelPos = SeqUtils.getMaskedBySeqString(refSeq.getSeqString()).length();
                trimStart = translateCoord(trimStart, refSeq, CoordType.seq, CoordType.model);
                trimStop = translateCoord(trimStop, refSeq, CoordType.seq, CoordType.model);
            }

        } catch (Exception e) {
            new HelpFormatter().printHelp("SequenceTrimmer <trim start> <trim stop> <aligned file> ...", options);
            System.err.println("Error: " + e.getMessage());
        }

        System.err.println("Starting sequence trimmer");
        System.err.println("*  Input files:           " + Arrays.asList(inputFiles));
        System.err.println("*  Minimum Length:        " + minLength);
        System.err.println("*  Trim point inclusive?: " + inclusive);
        System.err.println("*  Trim points:           " + trimStart + "-" + trimStop);
        System.err.println("*  Min filled ratio:      " + minFilledRatio);
        System.err.println("*  refSeq:                " + ((refSeq == null) ? "model" : refSeq.getSeqName() + " " + refSeq.getDesc()));

        Sequence seq;
        SeqReader reader;
	TrimStats stats;

	writeStatsHeader(statsOut);

        FastaWriter seqWriter;
	File in;
        for (String infile : inputFiles) {
	    in = new File(infile);
            reader = new SequenceReader(in);
	    seqWriter = new FastaWriter(new File(outdir, "trimmed_" + in.getName()));

            while ((seq = reader.readNextSequence()) != null) {
		if(seq.getSeqName().startsWith("#")) {
		    seqWriter.writeSeq(seq.getSeqName(), "", trimMetaSeq(seq.getSeqString(), trimStart, trimStop));
		    continue;
		}

		stats = getStats(seq, trimStart, trimStop);
		boolean passed = didSeqPass(stats, minLength, minTrimmedLength, maxNs, maxTrimNs, minFilledRatio);
		writeStats(statsOut, seq.getSeqName(), stats, passed);
		if(passed) {
		    seqWriter.writeSeq(seq.getSeqName(), seq.getDesc(), new String(stats.trimmedBases));
		}
            }

            reader.close();
	    seqWriter.close();
        }

	statsOut.close();
    }
}
