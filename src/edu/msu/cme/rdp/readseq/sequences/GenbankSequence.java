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
package edu.msu.cme.rdp.readseq.sequences;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class GenbankSequence extends Sequence {
    private Map<String, Map<String, String>> featureTable;
    private String organism;

    public GenbankSequence(String seqName, String desc, String organism, Map<String, Map<String, String>> featureTable, String seqString) {
        super(seqName, desc, seqString);
        this.organism = organism;
        this.featureTable = featureTable;
    }

    public GenbankSequence(Sequence seq, String organism, Map<String, Map<String, String>> featureTable) {
        super(seq);
        this.organism = organism;
        this.featureTable = featureTable;
    }

    public Map<String, Map<String, String>> getFeatureTable() {
        return featureTable;
    }

    public String getOrganism() {
        return organism;
    }

}
