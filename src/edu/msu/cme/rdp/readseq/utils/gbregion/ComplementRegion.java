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

package edu.msu.cme.rdp.readseq.utils.gbregion;

import edu.msu.cme.rdp.readseq.utils.IUBUtilities;

/**
 *
 * @author fishjord
 */
public class ComplementRegion extends SingleSeqRegion {
    private SingleSeqRegion complementOf;

    public ComplementRegion(SingleSeqRegion complementOf) {
        if(complementOf == null)
            throw new IllegalArgumentException("ComplementOf must not be null");
        
        this.complementOf = complementOf;
    }

    public SingleSeqRegion getComplementOf() {
        return complementOf;
    }

    @Override
    public String toString() {
        return "complement(" + complementOf.toString() + ")";
    }

    @Override
    public String getId() {
        return complementOf.getId();
    }

    @Override
    public int getSeqStart() {
        return complementOf.getSeqStart();
    }

    @Override
    public int getSeqStop() {
        return complementOf.getSeqStop();
    }

    @Override
    public String getSeqRegion(String seq, int offset) {
        return IUBUtilities.reverseComplement(complementOf.getSeqRegion(seq, offset));
    }

    @Override
    public Extends getExtends() {
        Extends e = complementOf.getExtends();
        if(e == Extends.BEYOND_BEGIN)
            return Extends.BEYOND_END;
        else if(e == Extends.BEYOND_END)
            return Extends.BEYOND_BEGIN;
        else
            return e;
    }
}
