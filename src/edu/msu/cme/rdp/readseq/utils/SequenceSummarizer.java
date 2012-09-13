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

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class SequenceSummarizer {

    private Map<Integer, Integer> lengthCounts = new HashMap();

    public void addSequence(Sequence s) {
        int len = SeqUtils.getUnalignedSeqString(s.getSeqString()).length();
        
        if(!lengthCounts.containsKey(len)) {
            lengthCounts.put(len, 0);
        }
        
        lengthCounts.put(len, lengthCounts.get(len) + 1);
    }
    
    public void addAlignedSequence(Sequence s) {
        
    }

    public static void main(String[] args) {

    }
}
