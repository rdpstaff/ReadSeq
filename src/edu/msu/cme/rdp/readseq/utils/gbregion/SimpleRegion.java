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

/**
 *
 * @author fishjord
 */
public class SimpleRegion extends SingleSeqRegion {

    private String id;
    private int seqStart;
    private int seqStop;
    private Extends e;

    public SimpleRegion(String id, int start, int stop, Extends e) {
        this.id = id;
        if(start < stop) {
            this.seqStart = start;
            this.seqStop = stop;
        } else {
            this.seqStart = stop;
            this.seqStop = start;
        }
        this.e = e;
    }

    @Override
    public String toString() {
        String ret = "";
        if(id != null && !id.equals("")) {
            ret += id + ":";
        }

	if(seqStart == seqStop) {
	    if(e == Extends.BEYOND_BEGIN) {
		ret += "<";
	    } else if(e == Extends.BEYOND_END) {
		ret += ">";
	    }

	    ret += seqStart;
	} else {
	    if(e == Extends.BEYOND_BEGIN || e == Extends.BEYOND_BOTH) {
		ret += "<";
	    }
	    
	    ret += seqStart + "..";
	    
	    if(e == Extends.BEYOND_END || e == Extends.BEYOND_BOTH) {
		ret += ">";
	    }
	    
	    if(seqStart != seqStop) {
		ret += seqStop;
	    }
	}

        return ret;
    }

    public String getId() {
        return id;
    }

    @Override
    public int getSeqStart() {
        return seqStart;
    }

    @Override
    public int getSeqStop() {
        return seqStop;
    }

    @Override
    public String getSeqRegion(String seq, int offset) {
        if(seqStart < offset)
            throw new IllegalArgumentException("Seq's offset (" + offset + ") is more than my start (" + seqStart + ") [" + this.toString() + "]");

        return seq.substring(seqStart - offset, seqStop - offset + 1);
    }

    @Override
    public Extends getExtends() {
        return e;
    }
}
