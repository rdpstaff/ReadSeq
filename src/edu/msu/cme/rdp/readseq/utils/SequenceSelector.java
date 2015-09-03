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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author fishjord
 */
public class SequenceSelector {

    private static final Options options = new Options();
    
    static {
        options.addOption("s", "seq_length", true, "minimum length of sequence");
    }
    public static void main(String[] args) throws IOException {
        int min_length = 1;
        try{
            CommandLine line = new PosixParser().parse(options, args);           
            if ( line.hasOption("seq_length")){
                min_length = Integer.parseInt(line.getOptionValue("seq_length"));
                if ( min_length < 1){
                    throw new Exception("seq_length should be at least 1");
                }
            }             
            args = line.getArgs();
            if (args.length < 5) {
                throw new Exception("Incorrect number of command line arguments");
            }
           
        }catch(Exception e){
            new HelpFormatter().printHelp(120, "[options] ids_file outfile outputformat keep[Y|N] seqfile(s)\n" +
                    "The outputformat can be either fasta or fastq if inputs are fastq\n" +
                    "Default is to keep the sequences in the ids_file. If keep is false or N, the sequences will be removed from output ", "", options, "");
            System.out.println("ERROR: " + e.getMessage());
            return;
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
        
        boolean keep =  true;
        if ( args[3].equalsIgnoreCase("N") || args[3].equalsIgnoreCase("false")){
            keep = false;
        }
                
        Set<String> ids = new HashSet();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(idFile));
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.equals("")) {
                String[] val = line.split("\\s+");
                ids.add(val[0]);
            }
        }
        reader.close();
        Sequence seq;
        boolean contains;
        
        HashSet<String> foundIds = new HashSet<String>();
        for (int index = 4; index < args.length; index++) {
            SequenceReader seqReader = new SequenceReader(new File(args[index]));
            seq = seqReader.readNextSequence();
            if ( (out instanceof FastqWriter) && !(seq instanceof QSequence) ) {
                throw new IllegalArgumentException("input file " + args[index] + " format is not fastq, can not write fastq output");
            } 
            
            contains = ids.contains(seq.getSeqName());
            if ((contains && keep) || (!contains && !keep)) {
                if ( seq.getSeqString().length() >= min_length){
                    out.writeSeq(seq);
                }
                if ( keep){
                    foundIds.add(seq.getSeqName());
                }
                
            }

            while ((seq = seqReader.readNextSequence()) != null) {
                contains = ids.contains(seq.getSeqName());
                if ((contains && keep) || (!contains && !keep)) {
                    if ( seq.getSeqString().length() >= min_length){
                        out.writeSeq(seq);
                    }
                    if ( keep ){
                        foundIds.add(seq.getSeqName());
                        if (foundIds.size() == ids.size()) {
                            break;
                        }                    
                    }
                }
               
            }
            seqReader.close();
        }
        out.close();

    }
}
