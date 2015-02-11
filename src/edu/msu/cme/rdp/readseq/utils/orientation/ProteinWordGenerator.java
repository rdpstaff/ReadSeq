/*
 * Copyright (C) 2014 wangqion
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

package edu.msu.cme.rdp.readseq.utils.orientation;

import edu.msu.cme.rdp.readseq.utils.ProteinUtils;
import java.util.HashSet;

/**
 *
 * @author wangqion
 */
public class ProteinWordGenerator {
    public static final int WORDSIZE = 4;
    private int wordSize = WORDSIZE;
    private ProteinUtils protUtils = ProteinUtils.getInstance();
    
    public ProteinWordGenerator(int wordSize){
        if ( wordSize < 3 || wordSize > 6){
           // System.out.println("wordsize used: " + wordSize + ". Warning: recommended word size is " + WORDSIZE + ", or range [3-6]");
        }
        this.wordSize = wordSize;
    }
       
    /**
     * returns all the overlapping words from a protein sequence
     * @param seq
     * @return 
     */
    public HashSet<String> parseProtein(String seq){
        HashSet<String> wordSet = new HashSet<String>();
        
        for ( int i = 0; i <= seq.length() - wordSize; i++){
            wordSet.add(seq.substring(i, i + wordSize).toLowerCase());
        }        
        return wordSet;
    }
    
    /**
     * This is recommended if frameshifts exist in the sequence.
     * This method translates a nucleotide sequence to protein in all three frames, 
     * and returns the all the overlapping words from each frame. 
     * @param nucl
     * @param translTable
     * @return 
     */
    public HashSet<String> parseNuclAllFrames(String nucl){
        return parseNuclAllFrames(nucl, 11);
    }
    
    public HashSet<String> parseNuclAllFrames(String nucl, int translTable){
        HashSet<String> wordSet = new HashSet<String>();
        for ( int i = 0; i < 3; i++){            
            String s = protUtils.translateToProtein(nucl.substring(i ), true, translTable);
            HashSet<String> tempSet = parseProtein(s);
            wordSet.addAll(tempSet);
        }
        return wordSet;
    }
    
    /**
     * This method translates a nucleotide sequence to protein assuming nucl sequence is in frame, 
     * and returns the all the overlapping words
     * @param nucl
     * @param translTable
     * @return 
     */
    public HashSet<String> parseNuclOneFrame(String nucl){
        return parseNuclOneFrame(nucl, 11);
    }
    
    public HashSet<String> parseNuclOneFrame(String nucl, int translTable){
        return parseProtein(protUtils.translateToProtein(nucl, true, translTable));
    }
    
    public static void main (String[] args) {
        ProteinWordGenerator theObj = new ProteinWordGenerator(ProteinWordGenerator.WORDSIZE);       
        String nucl = "ttgaaacagattgcattttacggaaaaggagggattggaaagtcaactac";
        HashSet<String> wordSet = theObj.parseNuclAllFrames(nucl);
        for( String w: wordSet){
            System.out.println(w);
        }
    }
}
