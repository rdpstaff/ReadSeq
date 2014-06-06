/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.msu.cme.rdp.readseq.utils.kmermatch;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author wangqion
 */
public abstract class KmerMatchCore {
    public static class ResultComparator implements Comparator {
        public int compare(Object a, Object b){
            if ( ((BestMatch)a).sab < ((BestMatch)b).sab){
                return 1;
            }else {
                if ( ((BestMatch)a).sab > ((BestMatch)b).sab){
                    return -1;
                }else {
                    return ((BestMatch)a).match.getSeqName().compareTo(((BestMatch)b).match.getSeqName());
                }
            }
        }
    }
    
    public static class BestMatch{
        Sequence match;
        float sab;
        boolean revComp ;
        
        BestMatch(Sequence s, float score, boolean r){
            match = s;
            sab = score;
            revComp = r;
        }
        
        public Sequence getBestMatch(){
            return match;
        }
        
        public boolean isRevComp(){
            return revComp;
        }
        
        public float getSab(){
            return sab;
        }
        
    }
    
    public abstract ArrayList<ProteinSeqMatch.BestMatch> findTopKMatch(Sequence seq, int k) throws IOException;
        
   
}
