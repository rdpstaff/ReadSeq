/*
 * Copyright (C) 2012 Michigan State University <rdpstaff at msu.edu>
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

public class IUBUtilities {

    public static boolean isIUB(char base) {
        switch (base) {
            case '~':
            case '.':
            case '-':
            case 'A':
            case 'a':
            case 'C':
            case 'c':
            case 'G':
            case 'g':
            case 'U':
            case 'u':
            case 'T':
            case 't':
            case 'N':
            case 'n':
            case 'X':
            case 'x':
            case 'M':
            case 'm':
            case 'R':
            case 'r':
            case 'W':
            case 'w':
            case 'S':
            case 's':
            case 'Y':
            case 'y':
            case 'K':
            case 'k':
            case 'V':
            case 'v':
            case 'H':
            case 'h':
            case 'D':
            case 'd':
            case 'B':
            case 'b':
            case 'I':
            case 'i':
                return true;
            default:
                return false;
        }
    }

    public static boolean isIUB(byte[] bases) {
        for (int i = 0; i < bases.length; i++) {
            if (isIUB((char) bases[i]) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIUB(char[] bases) {
        for (int i = 0; i < bases.length; i++) {
            if (isIUB(bases[i]) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGap(char base) {
        switch (base) {
            case '~':
            case '.':
            case '-':
                return true;
            default:
                return false;
        }
    }

    public static boolean isAmbiguity(char base) {
        switch (base) {
            case 'N':
            case 'n':
            case 'X':
            case 'x':
            case 'M':
            case 'm':
            case 'R':
            case 'r':
            case 'W':
            case 'w':
            case 'S':
            case 's':
            case 'Y':
            case 'y':
            case 'K':
            case 'k':
            case 'V':
            case 'v':
            case 'H':
            case 'h':
            case 'D':
            case 'd':
            case 'I':
            case 'i':
            case 'B':
            case 'b':
                return true;
            default:
                return false;
        }
    }

    public static boolean isACGU(char base) {
        switch (base) {
            case 'A':
            case 'a':
            case 'C':
            case 'c':
            case 'G':
            case 'g':
            case 'U':
            case 'u':
            case 'T':
            case 't':
                return true;
            default:
                return false;
        }
    }

    /**
     * IUPAC nucleotide bases
     * @param base
     * @return 
     */
    public static boolean isIUPACDNA(char base) {
        switch (base) {
            case 'A':
            case 'a':
            case 'C':
            case 'c':
            case 'G':
            case 'g':
            case 'U':
            case 'u':
            case 'T':
            case 't':
            case 'N':
            case 'n':
            case 'M':
            case 'm':
            case 'R':
            case 'r':
            case 'W':
            case 'w':
            case 'S':
            case 's':
            case 'Y':
            case 'y':
            case 'K':
            case 'k':
            case 'V':
            case 'v':
            case 'H':
            case 'h':
            case 'D':
            case 'd':
            case 'B':
            case 'b':
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isA(char base) {
        switch (base) {
            case 'A':
            case 'a':
            case 'R':
            case 'r':
            case 'M':
            case 'm':
            case 'W':
            case 'w':
            case 'H':
            case 'h':
            case 'V':
            case 'v':
            case 'D':
            case 'd':
            case 'I':
            case 'i':
            case 'N':
            case 'n':
                return true;
            default:
                return false;
        }
    }

    public static boolean isC(char base) {
        switch (base) {
            case 'C':
            case 'c':
            case 'Y':
            case 'y':
            case 'M':
            case 'm':
            case 'S':
            case 's':
            case 'H':
            case 'h':
            case 'B':
            case 'b':
            case 'V':
            case 'v':
            case 'N':
            case 'n':
                return true;
            default:
                return false;
        }
    }

    public static boolean isG(char base) {
        switch (base) {
            case 'G':
            case 'g':
            case 'R':
            case 'r':
            case 'K':
            case 'k':
            case 'S':
            case 's':
            case 'B':
            case 'b':
            case 'V':
            case 'v':
            case 'D':
            case 'd':
            case 'I':
            case 'i':
            case 'N':
            case 'n':
                return true;
            default:
                return false;
        }
    }

    public static boolean isU(char base) {
        switch (base) {
            case 'U':
            case 'u':
            case 'T':
            case 't':
            case 'Y':
            case 'y':
            case 'K':
            case 'k':
            case 'W':
            case 'w':
            case 'H':
            case 'h':
            case 'B':
            case 'b':
            case 'D':
            case 'd':
            case 'I':
            case 'i':
            case 'N':
            case 'n':
                return true;
            default:
                return false;
        }
    }

    public static boolean matches(char b1, char b2) {
        return (isA(b1) && isA(b2)) || (isC(b1) && isC(b2)) || (isG(b1) && isG(b2)) || (isU(b1) && isU(b2));
    }

    public static String makeProper(String seq) {
        StringBuffer properSeq = new StringBuffer("");
        for (int i = 0; i < seq.length(); i++) {
            switch (seq.charAt(i)) {

                case '~':
                case '.':
                case '-':
                    properSeq = properSeq.append("-");
                    break;
                case 'A':
                case 'a':
                    properSeq = properSeq.append("A");
                    break;
                case 'C':
                case 'c':
                    properSeq = properSeq.append("C");
                    break;
                case 'G':
                case 'g':
                    properSeq = properSeq.append("G");
                    break;
                case 'U':
                case 'u':
                case 'T':
                case 't':
                    properSeq = properSeq.append("T");
                    break;
                case 'N':
                case 'n':
                    properSeq = properSeq.append("N");
                    break;
                case 'X':
                case 'x':
                    properSeq = properSeq.append("X");
                    break;
                case 'M':
                case 'm':
                    properSeq = properSeq.append("M");
                    break;
                case 'R':
                case 'r':
                    properSeq = properSeq.append("R");
                    break;
                case 'W':
                case 'w':
                    properSeq = properSeq.append("W");
                    break;
                case 'S':
                case 's':
                    properSeq = properSeq.append("S");
                    break;
                case 'Y':
                case 'y':
                    properSeq = properSeq.append("Y");
                    break;
                case 'K':
                case 'k':
                    properSeq = properSeq.append("K");
                    break;
                case 'V':
                case 'v':
                    properSeq = properSeq.append("V");
                    break;
                case 'H':
                case 'h':
                    properSeq = properSeq.append("H");
                    break;
                case 'D':
                case 'd':
                    properSeq = properSeq.append("D");
                    break;
                case 'I':
                case 'i':
                    properSeq = properSeq.append("D");
                    break;
                case 'B':
                case 'b':
                    properSeq = properSeq.append("B");
                    break;
                default:
                    break;
            }
        }
        return properSeq.toString();
    }

    public static String reverseComplement(String bases) {
        return reverse(complement(bases));
    }

    public static char[] reverseComplement(char[] bases) {
	return reverse(complement(bases));
    }

    public static String reverse(String str) {
	return new String(reverse(str.toCharArray()));
    }

    public static char[] reverse(char[] bases) {
	char[] ret = new char[bases.length];
	for(int i = bases.length;i > 0;i--) {
	    ret[bases.length - i] = bases[i - 1];
	}

	return ret;
    }

    public static String complement(String bases) {
	return new String(complement(bases.toCharArray()));
    }

    public static char[] complement(char[] bases) {
	char[] ret = new char[bases.length];
	for (int index = 0; index < bases.length; index++) {
            switch (bases[index]) {
	    case '~':
	    case '.':
	    case '-':
		ret[index] = '-';
		break;
	    case 'A':
	    case 'a':
		ret[index] = 'T';
		break;
	    case 'C':
	    case 'c':
		ret[index] = 'G';
		break;
	    case 'G':
	    case 'g':
		ret[index] = 'C';
		break;
	    case 'U':
	    case 'u':
	    case 'T':
	    case 't':
		ret[index] = 'A';
		break;
	    case 'N':
	    case 'n':
		ret[index] = 'N';
		break;
	    case 'X':
	    case 'x':
	        ret[index] = 'X';
		break;
	    case 'M':
	    case 'm':
		ret[index] = 'K';
		break;
	    case 'R':
	    case 'r':
		ret[index] = 'Y';
		break;
	    case 'W':
	    case 'w':
		ret[index] = 'W';
		break;
	    case 'S':
	    case 's':
		ret[index] = 'S';
		break;
	    case 'Y':
	    case 'y':
		ret[index] = 'R';
		break;
	    case 'K':
	    case 'k':
		ret[index] = 'M';
		break;
	    case 'V':
	    case 'v':
		ret[index] = 'B';
		break;
	    case 'H':
	    case 'h':
		ret[index] = 'D';
		break;
	    case 'D':
	    case 'd':
		ret[index] = 'H';
		break;
	    case 'I':
	    case 'i':
		ret[index] = 'H';
		break;
	    case 'B':
	    case 'b':
		ret[index] = 'V';
		break;
	    default:
		break;
            }
	}
        return ret;
    }
}
