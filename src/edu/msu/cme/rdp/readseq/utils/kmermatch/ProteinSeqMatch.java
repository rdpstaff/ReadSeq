/*
 * Copyright (C) 2014 rdpstaff
 *
 */

package edu.msu.cme.rdp.readseq.utils.kmermatch;

import edu.msu.cme.rdp.readseq.SequenceType;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.utils.IUBUtilities;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import edu.msu.cme.rdp.readseq.utils.orientation.ProteinWordGenerator;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author wangqion
 */
public class ProteinSeqMatch extends KmerMatchCore{
    
    private ConcurrentHashMap<String, HashSet<String>> refWordMap = new ConcurrentHashMap<String, HashSet<String>>();
    private ConcurrentHashMap<String, Sequence> refSeqMap = new ConcurrentHashMap<String, Sequence>();
    private ProteinWordGenerator proteinWordGenerator = null;
    private static final float SabThreshold = 0.6f; //
     
    public ProteinSeqMatch(String seqFile, int wordSize) throws IOException {
        proteinWordGenerator = new ProteinWordGenerator(wordSize);
        SequenceReader parser = new SequenceReader(new File(seqFile));
        Sequence seq;
        while ( (seq = parser.readNextSequence()) != null) {            
            addRefSeq(seq);
        }
        parser.close();         
     }     
    
    public ProteinSeqMatch(String seqFile) throws IOException {
        this(seqFile, ProteinWordGenerator.WORDSIZE);
    }
    
    public ProteinSeqMatch(List<Sequence> refSeqs, int wordSize){
        proteinWordGenerator = new ProteinWordGenerator(wordSize);
        // initialize the protein words 
        for (Sequence seq: refSeqs){
            addRefSeq(seq);
        }
    }
    
    public synchronized void addRefSeq(Sequence seq){
        refSeqMap.put(seq.getSeqName(), seq);
        refWordMap.put(seq.getSeqName(), proteinWordGenerator.parseProtein(SeqUtils.getUnalignedSeqString(seq.getSeqString())) );
    }
    
    public Sequence getRefSeq(String seqName){
        return refSeqMap.get(seqName);
    }
    
    /*
    * This program take a nucleotide or protein sequence, returns the top k best matching reference sequences
    * If it's nucleotide sequence, translates to three protein sequence strings, one for each frame.
    * It then calclates the sab score (shared protein kmers) between query and each reference seq.
    * The above process is repeated for the reverse orientation.    
    */
    public ArrayList<BestMatch> findTopKMatch(Sequence seq, int k){
        TreeSet<BestMatch> orderedResultSet = new TreeSet<BestMatch>( new ResultComparator());
        SequenceType seqtype = SeqUtils.guessSequenceType(seq);
        HashSet<String> queryWordSet;
        float queryWordSize;
        if (seqtype == SequenceType.Nucleotide){
            queryWordSet = proteinWordGenerator.parseNuclAllFrames(seq.getSeqString());
            // we need to divide the query word size by three since we have words from three frames
            queryWordSize = queryWordSet.size()/3f;
        } else {
            queryWordSet = proteinWordGenerator.parseProtein(seq.getSeqString());
            queryWordSize = queryWordSet.size();
        }
        float tempBestSab = 0;
        HashSet<String> targetWordSet ;
        for ( Sequence target: refSeqMap.values()){
            targetWordSet = refWordMap.get(target.getSeqName());
            HashSet<String> tempSet = new HashSet();
            tempSet.addAll(queryWordSet);
            float minWordCount =  queryWordSize<= targetWordSet.size()? queryWordSize: targetWordSet.size();
            tempSet.retainAll(targetWordSet);
            float sab = tempSet.size()/minWordCount;
            if ( sab >= tempBestSab){
                tempBestSab = sab;
            }
            orderedResultSet.add( new BestMatch(target, sab, false));
            
        }
        
        if(seqtype == SequenceType.Nucleotide && tempBestSab < this.SabThreshold) {  // check reverse
            queryWordSet = proteinWordGenerator.parseNuclAllFrames( IUBUtilities.reverseComplement(seq.getSeqString()) );
            for ( Sequence target: refSeqMap.values()){
                targetWordSet = refWordMap.get(target.getSeqName());
                HashSet<String> tempSet = new HashSet();
                tempSet.addAll(queryWordSet);
                float minWordCount =  queryWordSize<= targetWordSet.size()? queryWordSize: targetWordSet.size();
                tempSet.retainAll(targetWordSet);
                float sab = tempSet.size()/minWordCount;                
                orderedResultSet.add( new BestMatch(target, sab, true));
            }
        }
        
        ArrayList<BestMatch> topkMatchList = new ArrayList<BestMatch>();
        for ( BestMatch m: orderedResultSet){
            if ( topkMatchList.size() < k)
                topkMatchList.add(m);
        }
        
        return topkMatchList;        
    }
   
    public static void main (String[] args ) throws IOException{
        String usage = "Usage: protein_ref.fa query.fa outfile word_size knn\n" + 
                "  This program takes a nucleotide or protein query sequence file, returns the top k best matching protein reference sequences based on amino acid kmer matching\n" + 
                "  word_size 4 is recommended for the best performance. Range from 3 to 6 is recommended.\n" +
                "  protein_ref.fa must be protein sequences\n" + 
                "  query.fa can be either protein sequences or nucleotide sequences";
        
        if ( args.length != 5){
            System.err.println(usage);
            System.exit(1);
        }
        
        PrintStream out = new PrintStream(new File(args[2]));
        int wordSize = Integer.parseInt(args[3]);
        int k = Integer.parseInt(args[4]);
        ProteinSeqMatch theObj = new ProteinSeqMatch(args[0], wordSize);
        
        SequenceReader queryReader = new SequenceReader(new File(args[1]) );
        Sequence seq;
        while ( ( seq = queryReader.readNextSequence()) != null){
            ArrayList<BestMatch> results = theObj.findTopKMatch(seq, k);
            for ( BestMatch m: results){
                out.println(seq.getSeqName() + "\t" + m.getBestMatch().getSeqName() + "\t" + m.getSab() + "\t" + m.getBestMatch().getDesc());
            }
        }
        queryReader.close();
        out.close();
    }
}
