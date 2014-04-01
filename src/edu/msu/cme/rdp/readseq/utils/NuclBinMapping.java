/*
 * Copyright (C) 2013 Jordan Fish <fishjord at msu.edu>
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

import java.util.Arrays;

/**
 *
 * @author Jordan Fish <fishjord at msu.edu>
 */
public class NuclBinMapping {
    public static final char[] intToChar = new char[]{'a', 'c', 'g', 't'};
    public static final byte[] validateLookup = new byte[127];
    public static final byte[] complementLookup = new byte[127];

    public static final int a = 0;
    public static final int c = 1;
    public static final int g = 2;
    public static final int t = 3;
    static {
        Arrays.fill(validateLookup, (byte) -1);
        validateLookup['A'] = a;
        validateLookup['C'] = c;
        validateLookup['G'] = g;
        validateLookup['T'] = t;
        validateLookup['a'] = a;
        validateLookup['c'] = c;
        validateLookup['g'] = g;
        validateLookup['t'] = t;
        validateLookup['U'] = t;
        validateLookup['u'] = t;

        Arrays.fill(complementLookup, (byte) -1);

        complementLookup['A'] = t;  // A
        complementLookup['T'] = a;  // T
        complementLookup['U'] = a;  // U
        complementLookup['G'] = c;  // G
        complementLookup['C'] = g;  // C

        complementLookup['a'] = t;  // a
        complementLookup['t'] = a;  // t
        complementLookup['u'] = a;  // t
        complementLookup['g'] = c;  // g
        complementLookup['c'] = g;  // c

        complementLookup[a] = t;  // a
        complementLookup[t] = a;  // t
        complementLookup[g] = c;  // g
        complementLookup[c] = g;  // c
    }

    public static String fromByteArray(byte[] seq) {
        StringBuilder ret = new StringBuilder();

        for(byte b : seq) {
            if(b > intToChar.length) {
                throw new IllegalArgumentException("Bases are expected to be in the range [0-3]");
            } else {
                ret.append(intToChar[b]);
            }
        }

        return ret.toString();
    }

    public static byte[] toBytes(String seq) {
        char[] bases = seq.toCharArray();
        byte[] ret = new byte[bases.length];
        byte b;
        for(int index = 0;index < bases.length;index++) {
            b = validateLookup[bases[index]];
            if(b == -1) {
                throw new IllegalArgumentException("Unable to process base " + (index + 1) + " (" + bases[index] + ")");
            }
            ret[index] = b;
        }

        return ret;
    }
}
