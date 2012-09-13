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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fishjord
 */
public class SequenceSelector {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("USAGE: SequenceSelector <ids_file> <keep | remove> <seq_file>");
            System.exit(1);
        }

        File idFile = new File(args[0]);
        boolean keep = args[1].equals("keep");
        File seqFile = new File(args[2]);

        Set<String> ids = new HashSet();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(idFile));
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.equals("")) {
                ids.add(line);
            }
        }
        reader.close();

        System.err.println("Read in " + ids.size() + " ids to " + (keep ? "keep" : "remove"));

        Sequence seq;
        boolean contains;
        FastaWriter out = new FastaWriter(System.out);

        int foundSeqs = 0;
        int totalSeqs = 0;
        long startTime = System.currentTimeMillis();
        for (int index = 2; index < args.length; index++) {
            SequenceReader seqReader = new SequenceReader(new File(args[index]));
            while ((seq = seqReader.readNextSequence()) != null) {
                contains = ids.contains(seq.getSeqName());
                if ((contains && keep) || (!contains && !keep)) {
                    foundSeqs++;
                    out.writeSeq(seq);
                }

                totalSeqs++;
            }
            seqReader.close();
        }

        System.err.println("Wrote out " + foundSeqs + " / " + totalSeqs + " sequences in " + (System.currentTimeMillis() - startTime));
    }
}
