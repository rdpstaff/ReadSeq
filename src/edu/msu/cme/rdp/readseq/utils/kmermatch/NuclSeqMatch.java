/*
 * Copyright (C) 2014 rdpstaff
 *
 */

package edu.msu.cme.rdp.readseq.utils.kmermatch;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.utils.orientation.GoodWordIterator;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author wangqion
 */
public class NuclSeqMatch extends KmerMatchCore {
    static class WordSequence{
        Sequence seq;
        BitSet bitset = new BitSet(getSetSize());
        
        WordSequence(Sequence s){
            seq = s;
        }
    }
    
    private static int setsize ;
    //key is the seqID, value is the unique integer kmer set
    private HashMap<String, WordSequence> seqWordMap = new HashMap<String, WordSequence> ();
    
    public NuclSeqMatch(String seqfile, int wordSize) throws IOException {
        if ( !GoodWordIterator.isWordsizeHasSet()){
            GoodWordIterator.setWordSize(wordSize);
        }
        setsize = (int) Math.pow(4, GoodWordIterator.getWordsize() );
        SequenceReader parser = new SequenceReader(new File(seqfile));
        Sequence seq;
        while ( (seq = parser.readNextSequence()) != null) {
            GoodWordIterator generator = new GoodWordIterator(seq.getSeqString());
            WordSequence wordSeq = new WordSequence(seq);
            while ( generator.hasNext()){
                wordSeq.bitset.set(generator.next());
            }
            seqWordMap.put(seq.getSeqName(), wordSeq);
        }
        parser.close();
    }  
    
    public NuclSeqMatch(String seqfile) throws IOException {
        this(seqfile, GoodWordIterator.DEFAULT_WORDSIZE);
    }
    
    public NuclSeqMatch(List<Sequence> refSeqs) throws IOException{    
        this(refSeqs,  GoodWordIterator.DEFAULT_WORDSIZE);
    }
    
    public NuclSeqMatch(List<Sequence> refSeqs, int wordSize) throws IOException{ 
        if ( !GoodWordIterator.isWordsizeHasSet()){
            GoodWordIterator.setWordSize(wordSize);
        }
        for (Sequence seq: refSeqs){
            GoodWordIterator generator = new GoodWordIterator(seq.getSeqString());
            WordSequence wordSeq = new WordSequence(seq);
            while ( generator.hasNext()){
                wordSeq.bitset.set(generator.next());
            }
            seqWordMap.put(seq.getSeqName(), wordSeq);
        }
    }
    
    protected static int getSetSize(){
        return setsize;
    }
    
    public TreeSet<BestMatch> findAllMatches(Sequence query) throws IOException{
        WordSequence queryWordSeq = seqWordMap.get(query.getSeqName());        
        if ( queryWordSeq == null){
            queryWordSeq = new WordSequence(query);
            GoodWordIterator generator = new GoodWordIterator(query.getSeqString());
            while ( generator.hasNext()){
                queryWordSeq.bitset.set(generator.next());
            }
        }
        TreeSet<BestMatch> orderedResultSet = new TreeSet<BestMatch>( new ResultComparator());
        BitSet queryWordSet = queryWordSeq.bitset;
        BitSet tempQuerySet;
        float queryWordSize = queryWordSet.cardinality();
        for ( String train: seqWordMap.keySet()){
            tempQuerySet = (BitSet) queryWordSet.clone();
            BitSet trainWordSet = seqWordMap.get(train).bitset;
            int trainWordSize = trainWordSet.cardinality();
            float minWordCount =  queryWordSize <= trainWordSize? queryWordSize: trainWordSize;
            tempQuerySet.and(trainWordSet);           
            orderedResultSet.add( new BestMatch(seqWordMap.get(train).seq, tempQuerySet.cardinality()/minWordCount, false));
            //System.out.println(query.getSeqName() + "\t" + train + "\t" + tempQuerySet.cardinality() + "\t" + minWordCount + "\t" + (Math.round(100*tempQuerySet.cardinality()/minWordCount)));
        }
        
        return orderedResultSet;        
    }
    
    public ArrayList<BestMatch> findTopKMatch(Sequence query, int k) throws IOException{
        TreeSet<BestMatch> orderedResultSet = findAllMatches(query);                
        ArrayList<BestMatch> topkMatchList = new ArrayList<BestMatch>();
        for ( BestMatch m: orderedResultSet){
            if ( topkMatchList.size() < k)
                topkMatchList.add(m);
        }

        return topkMatchList;       
    }
   
    
    public static void main(String[] args) throws IOException{
        String usage = "nucl_ref.fa nucl_query.fa outFile word_size knn\n" + 
                "  This program takes a nucleotide query sequence file, returns the top k best matching nucleotide reference sequences based on nucleotide kmer matching \n" +
                "  word_size 8 is recommended for the best performance. Range from 6 to 10 is recommended.\n" ;
        
        if ( args.length != 5) {
            System.err.println(usage);
            System.exit(1);
        }
        // need to set the word size
        int wordSize = Integer.parseInt(args[3]);        
        KmerMatchCore theObj = new NuclSeqMatch(args[0], wordSize);          
        PrintStream out = new PrintStream(args[2]); 
        
        int k = Integer.parseInt(args[4]);
        SequenceReader parser = new SequenceReader(new File(args[1]));      
        Sequence seq;
        while ( (seq = parser.readNextSequence()) != null) {        
            ArrayList<BestMatch> matches = theObj.findTopKMatch(seq, k);
            for ( BestMatch match: matches){
                out.println(seq.getSeqName() + "\t" + match.getBestMatch().getSeqName() + "\t" + match.getSab() + "\t" + match.getBestMatch().getDesc());
            }
        }
        parser.close();
        out.close();
    }
}
