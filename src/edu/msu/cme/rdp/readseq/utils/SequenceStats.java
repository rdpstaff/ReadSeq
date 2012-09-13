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

/**
 *
 * @author fishjord
 */
public class SequenceStats {

    private int start;
    private int end;
    private int modelPositions;
    private int length;

    public SequenceStats(String seqString) {
        start = -1;
        end = -1;
        int modelPosition = 0;
        length = 0;

        char[] bases = seqString.toCharArray();

        for (int index = 0; index < bases.length; index++) {
            char base = bases[index];

            if (Character.isUpperCase(base) || base == '-') {
                if(base != '-')
                    modelPositions++;
                modelPosition++;
            }

            if (Character.isUpperCase(base)) {
                if (start == -1) {
                    start = modelPosition;
                    end = modelPosition;
                } else {
                    end = modelPosition;
                }
            }
        }

        if(start != -1)
            length = end - start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return length;
    }

    public int getModelPositions() {
        return modelPositions;
    }

    public int getStart() {
        return start;
    }
}
