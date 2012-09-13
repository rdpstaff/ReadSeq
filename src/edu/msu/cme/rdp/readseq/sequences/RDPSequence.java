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

/**
 *
 * @author fishjord
 */
public class RDPSequence extends Sequence {
    private String internalId;

    public RDPSequence(String internalId, String seqName, String desc, String seqString) {
        super(seqName, desc, seqString);
        this.internalId = internalId;
    }

    public RDPSequence(Sequence seq, String internalId) {
        super(seq);
        this.internalId = internalId;
    }

    public String getInternalId() {
        return internalId;
    }
}
