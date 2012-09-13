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

import java.io.Serializable;

/**
 *
 * @author fishjord
 */
public abstract class SingleSeqRegion implements Serializable {

    public abstract String getId();
    public abstract int getSeqStart();
    public abstract int getSeqStop();
    public abstract String getSeqRegion(String seq, int offset);
    public abstract Extends getExtends();

    public String getSeqRegion(String seq) {
        return getSeqRegion(seq, 1);
    }
}
