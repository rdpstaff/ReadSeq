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
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import edu.msu.cme.rdp.readseq.writers.FastqWriter;
import edu.msu.cme.rdp.readseq.writers.SequenceWriter;
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
        if (args.length < 5) {
            System.err.println("USAGE: SequenceSelector ids_file outfile outputformat keep seqfile(s) \n"  +
             "Input format is fasta or fastq. The outputformat can be either fasta or fastq if inputs are fastq\n" + 
             "If keep is false, the sequences will be remove from output ");
            System.exit(1);
        }

        File idFile = new File(args[0]);        
        SequenceWriter out = null;     
        if ( args[2].equalsIgnoreCase("fasta")){
            out = new FastaWriter(args[1]);
        } else if ( args[2].equalsIgnoreCase("fastq")){
            out = new FastqWriter(args[1], FastqCore.Phred33QualFunction);
        } else {
            throw new IllegalArgumentException("only fasta and fastq output are supported");
        }
        boolean keep =  Boolean.parseBoolean(args[3]);
        
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
        Sequence seq;
        boolean contains;
        
        for (int index = 4; index < args.length; index++) {
            SequenceReader seqReader = new SequenceReader(new File(args[index]));
            seq = seqReader.readNextSequence();
          
            if ( (out instanceof FastqWriter) && !(seq instanceof QSequence) ) {
                throw new IllegalArgumentException("input file " + args[index] + " format is not fastq, can not write fastq output");
            } 
            
            contains = ids.contains(seq.getSeqName());
            if ((contains && keep) || (!contains && !keep)) {
                out.writeSeq(seq);
            }

            while ((seq = seqReader.readNextSequence()) != null) {
                contains = ids.contains(seq.getSeqName());
                if ((contains && keep) || (!contains && !keep)) {
                    out.writeSeq(seq);
                }
            }
            seqReader.close();
        }
        out.close();

    }
}
