/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.msu.cme.rdp.readseq.utils;

import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.utils.orientation.OrientationChecker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author wangqion
 */
public class RevComplement {            
    private static final Options options = new Options();
    static {
        options.addOption("i", "infile", true, "input fasta file"); 
        options.addOption("o", "outfile", true, "output fasta file");
        options.addOption("f", "format", true, "output format, fasta or fastq. Default is fasta");
        options.addOption("c", "check", false, "If set, will check orientation of the rRNA sequenc, only reverse complement if needed");
     }
    
    public static String revQualityString(String s){
        StringBuilder qual = new StringBuilder();
        for(char b: s.toCharArray()) {
            qual.insert(0,b);
        }
        return qual.toString();
    }
   
    
    public static void main(String[] args) throws IOException {
        boolean checkOrientation = false;
        SequenceFormat outFormat = SequenceFormat.FASTA;
        File infile;
        File outfile ;
        try{
           CommandLine line = new PosixParser().parse(options, args);

           if (line.hasOption("check")) {
               checkOrientation = true;
            }
            if (line.hasOption("format")) {
                String f = line.getOptionValue("format");
                if ( f.equalsIgnoreCase("fasta")){
                   outFormat = SequenceFormat.FASTA; 
                } else if ( f.equalsIgnoreCase("fastq")){
                   outFormat = SequenceFormat.FASTQ; 
                } else {
                    throw new IllegalArgumentException("only fasta and fastq output are supported");
                }
            }
            if (line.hasOption("infile")) {
                infile = new File(line.getOptionValue("infile"));
            }else {
                throw new Exception("infile is required");
            }
            if (line.hasOption("outfile")) {
                outfile = new File(line.getOptionValue("outfile"));
            }else {
                throw new Exception("outfile is required");
            }           
            
       }catch (Exception e) {
            new HelpFormatter().printHelp(120, "RevComplement [options]", "", options, "");
            System.err.println("ERROR: " + e.getMessage());
            return;
        }
                
        OrientationChecker checker = OrientationChecker.getChecker();        
        SequenceFormat format = SeqUtils.guessFileFormat(infile);
        PrintStream out = new PrintStream(outfile);
        if ( format == SequenceFormat.FASTA ){
            if ( outFormat == SequenceFormat.FASTQ){
                throw new IllegalArgumentException("Can not ouput fastq when input is fasta");
            }
            SequenceReader reader = new SequenceReader(infile);
            Sequence seq = null;
            while ( ( seq = reader.readNextSequence()) != null){
                if ( checkOrientation) {
                    if ( checker.isSeqReversed(seq.getSeqString())){
                        String checked_seqstring = IUBUtilities.reverseComplement(seq.getSeqString());
                        out.println(">" + seq.getSeqName()  +"\t" + seq.getDesc() + "\trevcomp=TRUE"+ "\n" + checked_seqstring);
                    }else {
                        out.println(">" + seq.getSeqName()  +"\t" + seq.getDesc()+ "\n" + seq.getSeqString());
                    }
                }else {
                    out.println(">" + seq.getSeqName() +"\t" + seq.getDesc() + "\n" + IUBUtilities.reverseComplement(seq.getSeqString()));
                }

            }
            reader.close();
        } else if ( format == SequenceFormat.FASTQ) {
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            String line = null;
            while (  (line=reader.readLine()) != null){
                String seqID = line;
                String seqString = reader.readLine();
                String qualID = reader.readLine();
                String qualString = reader.readLine();
                if ( checkOrientation) {
                    if ( checker.isSeqReversed(seqString)){
                        seqString = IUBUtilities.reverseComplement(seqString);
                        qualString = revQualityString(qualString);
                    }
                }else {
                    seqString = IUBUtilities.reverseComplement(seqString);
                    qualString = revQualityString(qualString);
                }
                if ( outFormat == SequenceFormat.FASTQ){
                    out.println( seqID  + "\n" + seqString + "\n+" + "\n" + qualString);
                }else {
                    out.println( seqID  + "\n" + seqString);
                }
            }
           
        }        
        
    }
    
}
