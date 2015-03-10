/*
 * Copyright (C) 2012 Michigan State University <rdpstaff at msu.edu>
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

import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author wangqion
 */
public class RmDupSeqs {
    
    private static final Options options = new Options();
    static {
        options.addOption("i", "infile", true, "input fasta file"); 
        options.addOption("o", "outfile", true, "output fasta file");
        options.addOption("l", "min_seq_length", true, "filter sequence by minimum sequence length, default is 0");
        options.addOption("d", "duplicates", false, "remove identical sequence, or sequence contained by another sequence");
        options.addOption("g", "debug", false, "output the ids that are contained by other sequences to standard out");
     }
    
    public static void filterDuplicates(String inFile, String outFile, int length, boolean debug) throws IOException{       
        HashMap<String, String> idSet = new HashMap<String, String>();
        IndexedSeqReader reader = new IndexedSeqReader(new File(inFile));
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(new File(outFile)));
        Set<String> allseqIDset = reader.getSeqIdSet();
        Sequence seq;
        if ( debug){
            System.out.println("ID\tdescription" + "\tcontained_by_ID\tdescription");
        }
        for ( String id : allseqIDset) {
            seq = reader.readSeq(id);
            boolean dup = false;
            HashSet<String> tempdupSet = new HashSet<String>();
            for ( String exID: idSet.keySet()){
                String exSeq = idSet.get(exID);
                if ( exSeq.length() >= seq.getSeqString().length()){
                    if ( exSeq.contains(seq.getSeqString())){
                        dup = true;
                        if ( debug){
                            Sequence temp = reader.readSeq(exID);
                            System.out.println(id + "\t" + seq.getDesc() + "\t" + exID + "\t" + temp.getDesc());
                        }
                        break;
                    }
                }else if ( seq.getSeqString().contains(exSeq)){
                    tempdupSet.add(exID);
                }                
            }

            if ( !dup){
                idSet.put(id, seq.getSeqString());
            }
            for ( String dupid: tempdupSet){
                idSet.remove(dupid);  
                if ( debug){
                    Sequence temp = reader.readSeq(dupid);
                    System.out.println(dupid + "\t" + temp.getDesc() + "\t"+ id + "\t" + seq.getDesc());
                }
            }
        }
        // get the unique seq
        for ( String id: idSet.keySet()){
            seq = reader.readSeq(id);
            if ( seq.getSeqString().length() >= length){
                outWriter.write(">" + id + "\t" + seq.getDesc() + "\n" + seq.getSeqString() + "\n");
            }
        }
         reader.close();
         outWriter.close();
    }
    
    
    public static void filterByLength(String inFile, String outFile, int length) throws IOException{
        SequenceReader seqReader = new SequenceReader(new File(inFile));
        FastaWriter outWriter = new FastaWriter(outFile);
        
        Sequence seq = null;
        while ( (seq = seqReader.readNextSequence()) != null){
            if ( seq.getSeqString().length() < length) {
                continue;
            }
            outWriter.writeSeq(seq.getSeqName(), seq.getDesc(), seq.getSeqString());
            
        }
        seqReader.close();
        outWriter.close();
    }
    
    public static void main(String[] args) throws Exception{
        String inFile;
        String outFile;
        int length = 0;
        boolean debug = false;
        boolean removeDuplicates = false;
        
       try{
           CommandLine line = new PosixParser().parse(options, args);

           if (line.hasOption("duplicates")) {
               removeDuplicates = true;
            }
            if (line.hasOption("min_seq_length")) {
                length = Integer.parseInt(line.getOptionValue("min_seq_length"));
            }
            if (line.hasOption("infile")) {
                inFile = line.getOptionValue("infile");
            }else {
                throw new Exception("infile is required");
            }
            if (line.hasOption("outfile")) {
                outFile = line.getOptionValue("outfile");
            }else {
                throw new Exception("outfile is required");
            }
            if (line.hasOption("debug")) {
                debug = true;
            }
            
       }catch (Exception e) {
            new HelpFormatter().printHelp(120, "RmRedundantSeqs [options]", "", options, "");
            System.err.println("ERROR: " + e.getMessage());
            return;
        }
       if ( !removeDuplicates){
           filterByLength(inFile, outFile, length);
       }else {
           filterDuplicates(inFile, outFile, length, debug);
       }
        
    }
    
}
