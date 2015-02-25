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

import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author fishjord
 */
public class ResampleSeqFile {

    private static Random random = new Random();
    private static final Options options = new Options();
    
    static {
        options.addOption("n", "num-selection", true, "number of sequence to select for each sample. "
                + "Default is the smallest sample size. Limit to the default value.");
        options.addOption("s", "subregion_length", true, "If specified, radomly choose a subregion with the required length from the sequence. "
                + " If a selected sequence is shorter than the specified length, that sequence will not be included in the output, "
                + " which may result in not equal number of sequences in some samples.");

    }
    
    public static void select(String infiledir, String outdir, int num_of_seqs, int subregion_length)throws IOException{
        File outdirFile = new File(outdir);
        if ( !outdirFile.exists()){
            outdirFile.mkdir();
        }
        File infile = new File(infiledir);
        
        // need to find the smallest sample size 
        int min_num_of_seqs = Integer.MAX_VALUE;
        if ( infile.isDirectory() ) {
            for ( File f: infile.listFiles()){
                IndexedSeqReader reader = new IndexedSeqReader(f);
                if ( reader.getSeqIdSet().size() < min_num_of_seqs){
                    min_num_of_seqs = reader.getSeqIdSet().size();
                }
            }
        }else {
            IndexedSeqReader reader = new IndexedSeqReader(infile);
            if ( reader.getSeqIdSet().size() < min_num_of_seqs){
                min_num_of_seqs = reader.getSeqIdSet().size();
            }
        }            
       
        if ( num_of_seqs == 0){  // if user dis not specify the number, use the minimum sample size
            num_of_seqs = min_num_of_seqs;
        }
        if ( min_num_of_seqs < num_of_seqs){
            throw new IllegalArgumentException("The smallest sample size is " + min_num_of_seqs +". Please modify the value for option num_selection");
        }
        
        String subregion = (subregion_length == 0) ? "": "_" + subregion_length + "bp";
        
        if ( infile.isDirectory() ) {
            for ( File f: infile.listFiles()){
                File outfile = new File( outdir, "subset_" + subregion + f.getName());
                selectOne(f, num_of_seqs, subregion_length, outfile);
            }
        }else {
            String infile_prefix = infile.getName();
            String infile_suffix = "";
            int index = infile.getName().lastIndexOf(".");
            
            if ( index != -1){
                infile_prefix = infile.getName().substring(0, index);
                infile_suffix = infile.getName().substring(index);
            }
            File outfile = new File( outdir, infile_prefix + ".sub" + subregion + infile_suffix);
            selectOne(infile, num_of_seqs, subregion_length, outfile);
        }
    }
    
    public static void selectOne(File infile, int num_of_seqs, int subregion_length, File outfile) throws IOException{
               
        IndexedSeqReader reader = new IndexedSeqReader(infile);
        PrintStream out = new PrintStream(new FileOutputStream(outfile));
        Object[] seqIdSet = reader.getSeqIdSet().toArray();
        HashSet<Integer> selectedIndexSet = randomSelectIndices(seqIdSet, num_of_seqs);
        // get the  seq
        Sequence seq;
        if ( subregion_length == 0){
            for ( int index: selectedIndexSet){
                seq = reader.readSeq((String)seqIdSet[index]);           
                out.println(">" + seqIdSet[index] + "\t" + seq.getDesc() + "\n" + seq.getSeqString()); 
            }
        }else{
             for ( int index: selectedIndexSet){
                seq = reader.readSeq((String)seqIdSet[index]);           
                if ( seq.getSeqString().length() >= subregion_length){
                    int rdmIndex = (int)(Math.floor(Math.random()* (seq.getSeqString().length() - subregion_length) ));
                    out.println(">" + seqIdSet[index] + "\t" + seq.getDesc() + "\n" + seq.getSeqString().substring(rdmIndex, (rdmIndex+subregion_length)) ); 
                }
            }
        }        
    
       reader.close();
       out.close();
    }
    
    /**
     * random select (without replacement) a fraction of indices from an array
     * @param object[]
     * @param num_of_selection
     * @return 
     * @throws IOException
     */
    public static HashSet<Integer> randomSelectIndices(Object[] seqIdSet, int num_of_selection) throws IOException{
        int n = Math.min(num_of_selection, seqIdSet.length);
        HashSet<Integer> selectedIndexSet = new HashSet<Integer>();
        while(selectedIndexSet.size() < n){
            selectedIndexSet.add(random.nextInt(n));
        }        
        return selectedIndexSet;
    }
    
    /**
     * random select (without replacement) a fraction of sequence IDs from the input file
     * @param infile
     * @param fraction
     * @return 
     * @throws IOException
     */
    public static Set<String> randomSelectSeq(File infile, float fraction) throws IOException{
        IndexedSeqReader reader = new IndexedSeqReader(infile);
        Object[] seqIdSet = reader.getSeqIdSet().toArray();
        int num_of_seqs = (int) (fraction * seqIdSet.length);        
        HashSet<Integer> selectedIndexSet = randomSelectIndices(seqIdSet, num_of_seqs);
        Set<String> selectedSeqIDs = new HashSet<String>();
        for ( int index: selectedIndexSet){
            selectedSeqIDs.add((String) seqIdSet[index]);
            
        } 
        return selectedSeqIDs;
    }
    

    public static void main(String[] args) throws IOException{
        int numOfSelection = 0;
        int subregion_length = 0;
        
        try{
            CommandLine line = new PosixParser().parse(options, args);
            if ( line.hasOption("num-selection")){
                numOfSelection = Integer.parseInt(line.getOptionValue("num-selection"));
                if ( numOfSelection < 1){
                    throw new Exception("num-selection should be at least 1");
                }
            }
            if ( line.hasOption("subregion_length")){
                subregion_length = Integer.parseInt(line.getOptionValue("subregion_length"));
                if ( subregion_length < 1){
                    throw new Exception("subregion_length should be at least 1");
                }
            }
             
            args = line.getArgs();
            if ( args.length != 2){
                throw new Exception("Incorrect number of command line arguments");
            }
            ResampleSeqFile.select(args[0], args[1], numOfSelection, subregion_length);
        }catch(Exception e){
            new HelpFormatter().printHelp(120, "ResampleSeqFile [options] <infile(dir)> <outdir>", "", options, "");
            System.out.println("ERROR: " + e.getMessage());
            return;
        } 
    }
}
