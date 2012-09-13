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

import edu.msu.cme.rdp.readseq.SeqFactory;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fishjord
 */
public class ReferenceSeqHelper {
    private Map<String, Map<Integer, Integer>> modelReferenceSeqMap = new HashMap();
    private Map<String, String> referenceSeqMap = new HashMap();

    private static class ReferenceSeqHelperHolder {
        private static ReferenceSeqHelper helper = new ReferenceSeqHelper();
    }

    public static ReferenceSeqHelper getInstance() {
        return ReferenceSeqHelperHolder.helper;
    }

    private ReferenceSeqHelper() {
        InputStream is = this.getClass().getResourceAsStream("/data/ref_seqs.fa");
        if(is == null)
            throw new RuntimeException("Failed to find ref_seqs.fa in the data directory on the classpath");

        try {
            SequenceReader seqReader = new SequenceReader(is);
            Sequence seq;

            while((seq = seqReader.readNextSequence()) != null) {
                modelReferenceSeqMap.put(seq.getSeqName(), SeqUtils.generateReferenceSeqMapping(seq.getSeqString()));
                referenceSeqMap.put(seq.getSeqName(), seq.getDesc());
            }

            seqReader.close();
        } catch(Exception e) {
            throw new RuntimeException("Fatal error reading ref_seq.fa", e);
        }
    }

    public String getRefSeq(String model) {
        return referenceSeqMap.get(model);
    }

    public Set<String> getKnownModels() {
        return modelReferenceSeqMap.keySet();
    }

    public Map<Integer, Integer> getReferenceMap(String model) {
        return modelReferenceSeqMap.get(model);
    }
    
    public int translate(String model, int loc) {
        if(!modelReferenceSeqMap.containsKey(model))
            throw new IllegalArgumentException("Unknown model '" + model + "'");
        if(!modelReferenceSeqMap.get(model).containsKey(loc))
            throw new IllegalArgumentException(loc + " isn't a model position");

        return modelReferenceSeqMap.get(model).get(loc);
    }

    public int[] getEndPoints(String model, String seqString) {
        int[] modelEndPoints = SeqUtils.getSeqEndPoints(seqString);

        int refStart =  translate(model, modelEndPoints[0]);
        int refEnd =  translate(model, modelEndPoints[0]);

        return new int[] {refStart, refEnd};
    }
}
