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

package edu.msu.cme.rdp.readseq.utils.kmermatch;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wangqion
 */
public class ProteinSeqMatchTest {
    
    public ProteinSeqMatchTest() {
    }
    
    /**
     * Test of findTopKMatch method, of class ProteinSeqMatch.
     */
    @Test
    public void testFindTopKMatch() {
        System.out.println("findTopKMatch");
        ArrayList<Sequence> refList = new ArrayList<Sequence>();
        refList.add(new Sequence("ref1", "", "aemgqkilivgcdpkadstrlilhakaqdtilslaasagsvedleledvmkvgyqdircvesggpepgvgcagrgvitsinfleengayenidyvs"));
        refList.add(new Sequence("ref3", "", "aemgqkilivgcdpkadstrlilhakxxaqdtilslxxaasagsvedlxxeledvmkvgyqdixxrcvesggpepgvgxxcagrgvitsinfleengayenidyvs"));
        refList.add(new Sequence("ref2", "", "aemgqkilivgcdpkadstrlilhakxxaqdtilslxxaasagsvedlxxeledvmkvgyqdircvesggpepgvgcagrgvitsinfleengayenidyvs"));
        
        Sequence queryseq = new Sequence("test", "", "cactcgtctgatcctgcacgccaaggcggcaggacaccatcctgagcctggcggccagcgccggcagcgtcgaggacctcgagctcgaggacgtgatgaaggtcggctaccagaacatccgttgcgtggaatccggcggtccggagcccggcgtcggctgtgccggccgcggcgtcatcacctcgatcaacttcctcgaagagaacggcgcctatgagga");
        int k = 3;
        ProteinSeqMatch instance = new ProteinSeqMatch(refList, 4);
        ArrayList<KmerMatchCore.BestMatch> result = instance.findTopKMatch(queryseq, k);
        assertEquals("ref1", result.get(0).getBestMatch().getSeqName());
        assertEquals("ref2", result.get(1).getBestMatch().getSeqName());
        assertEquals("ref3", result.get(2).getBestMatch().getSeqName());
        
        Sequence protSeq = new Sequence("test", "", "edlxxeledvmkvgyqdixxrcvesggp");
        result = instance.findTopKMatch(protSeq, k);
        assertEquals("ref3", result.get(0).getBestMatch().getSeqName());
        assertEquals("ref2", result.get(1).getBestMatch().getSeqName());
        assertEquals("ref1", result.get(2).getBestMatch().getSeqName());
    }

    
}
