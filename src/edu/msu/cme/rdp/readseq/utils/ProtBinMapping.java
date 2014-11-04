/*
 * Copyright (C) 2014 wangqion
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
 * @author wangqion
 */
public class ProtBinMapping {
    public static final byte[] asciiMap = new byte[127];
    public static final char[] intToChar = new char[127];
    
    static {
        byte alpha = 0;
        Arrays.fill(asciiMap, (byte) -1);

        for (char c : SeqUtils.proteinAlphabet) {
            if (Character.isUpperCase(c)) {
                asciiMap[c] = asciiMap[Character.toLowerCase(c)] = alpha;
                intToChar[alpha] = Character.toLowerCase(c);
                alpha++;
            }            
        }
        intToChar[alpha] = '*';
        asciiMap['*'] = alpha++;
        
        if (alpha != 26) {
            throw new IllegalStateException("More than 25 amino acids...");
        }
    }
    
   
}
