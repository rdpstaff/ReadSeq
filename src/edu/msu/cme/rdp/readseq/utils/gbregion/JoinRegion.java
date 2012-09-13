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

import java.util.List;

/**
 *
 * @author fishjord
 */
public class JoinRegion extends SingleSeqRegion {
    private List<SingleSeqRegion> joinRegions;
    private String id = null;
    private boolean isOrder;
    private int smallestStart = Integer.MAX_VALUE;
    private int largestStop = Integer.MIN_VALUE;

    public JoinRegion(String id, List<SingleSeqRegion> joinRegions, boolean isOrder) {
        if(joinRegions == null || joinRegions.size() < 2) {
            throw new IllegalArgumentException("JoinRegions must join at least 2 regions");
	}

        for(SingleSeqRegion region : joinRegions) {
            if(region.getSeqStart() < smallestStart)
                smallestStart = region.getSeqStart();
            if(region.getSeqStop() > largestStop)
                largestStop = region.getSeqStop();
        }

        this.id = id;
        this.joinRegions = joinRegions;
    }

    public List<SingleSeqRegion> getJoinRegions() {
        return joinRegions;
    }

    @Override
    public String toString() {
        String ret = "";
	if(isOrder) {
	    ret = "order(";
	} else {
	    ret = "join(";
	}

        for(SingleSeqRegion r : joinRegions) {
            ret += r.toString() + ",";
	}

        return ret.substring(0, ret.length() - 1) + ")";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getSeqStart() {
        return smallestStart;
    }

    @Override
    public int getSeqStop() {
        return largestStop;
    }

    @Override
    public String getSeqRegion(String seq, int offset) {
        StringBuffer ret = new StringBuffer();

        for(SingleSeqRegion region : joinRegions) {
            ret.append(region.getSeqRegion(seq, offset));
        }

        return ret.toString();
    }

    @Override
    public Extends getExtends() {
        Extends ret = Extends.EXACT;

        if(joinRegions.size() > 0) {
            Extends e = joinRegions.get(0).getExtends();
            if(e == Extends.BEYOND_BEGIN || e == Extends.BEYOND_BOTH) {
                ret = Extends.BEYOND_BEGIN;
	    }

            e = joinRegions.get(joinRegions.size() - 1).getExtends();
            if(e == Extends.BEYOND_END || e == Extends.BEYOND_BOTH) {
                if(ret == Extends.EXACT) {
                    ret = Extends.BEYOND_END;
                } else {
                    ret = Extends.BEYOND_BOTH;
		}
            }

        }

        return ret;
    }
}
