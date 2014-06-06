/*
 * Copyright (C) 2014 rdpstaff
 *
 */

package edu.msu.cme.rdp.readseq.utils.kmermatch;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wangqion
 */
public class NuclSeqMatchTest {
    
    public NuclSeqMatchTest() {
    }
    
   
    /**
     * Test of findTopKMatch method, of class NuclSeqMatch.
     */
    @Test
    public void testFindTopKMatch() throws IOException {
        System.out.println("findTopKMatch");
        ArrayList<Sequence> refList = new ArrayList<Sequence>();
        refList.add(new Sequence("ref1", "", "cacgcgcctcatcctgcacgcaaaggccca"));
        refList.add(new Sequence("ref2", "", "cacgcgcctcGGcctgcacgGGcaaaggccca"));
        refList.add(new Sequence("ref3", "", "cacgcgcctcTTcctgcacgcaaaggccca"));
        
        Sequence queryseq = new Sequence("test", "", "cacgcgcctcatcctgcacgcaaaggccca");
        int k = 3;
        NuclSeqMatch instance = new NuclSeqMatch(refList);
        ArrayList<KmerMatchCore.BestMatch> result = instance.findTopKMatch(queryseq, k);
        assertEquals("ref1", result.get(0).getBestMatch().getSeqName());
        assertEquals("ref3", result.get(1).getBestMatch().getSeqName());
        assertEquals(0.65, result.get(1).getSab(), 0.01);
        assertEquals("ref2", result.get(2).getBestMatch().getSeqName());
        
    }
    
}
