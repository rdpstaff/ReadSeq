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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fishjord
 */
public class RegionParser {

    final static String accnoGiPattern = "(\\d+)|([A-Z_]+\\d+(\\.\\d+)?)|(gi\\|\\d+)";

    public static SingleSeqRegion parse(String regionString) throws IOException {
        StringReader reader = new StringReader(regionString.replaceAll("\\s+", ""));

        return parseRegion(reader);
    }

    private static SingleSeqRegion parseRegion(StringReader in) throws IOException {
        IdentifierOrRange idOrRange = parseIdentifierOrRange(in);

        if (idOrRange.identifier == null) {
            GBRange range = idOrRange.range;
            return new SimpleRegion("", range.rangeStart, range.rangeEnd, range.rangeExtends);
        } else {
            String identifier = idOrRange.identifier;
            if (identifier.equals("join") || identifier.equals("order")) {
                return parseJoin(in, identifier.equals("order"));
            } else if (identifier.equals("complement")) {
                return parseComplement(in);
            } else if (identifier.matches(accnoGiPattern)) {
                idOrRange = parseIdentifierOrRange(in);
                GBRange range = idOrRange.range;
                if (range == null) {
                    throw new IOException("Fatal error when trying to parse range \"" + idOrRange.identifier + "\"");
                }

                return new SimpleRegion(identifier, range.rangeStart, range.rangeEnd, range.rangeExtends);
            } else {
                throw new IOException("Unexpected token \"" + identifier + "\"");
            }
        }
    }

    private static SingleSeqRegion parseJoin(StringReader in, boolean isOrder) throws IOException {
        List<SingleSeqRegion> joinOf = new ArrayList();
        String id = null;

        while (true) {
            SingleSeqRegion r = parseRegion(in);
            if (id == null || id.isEmpty()) {
                id = r.getId();
            } else if (!id.equals(r.getId()) && !r.getId().isEmpty()) {
                throw new IOException("SingleSeqRegion can only handle sequences of...a single seq...");
            }

            joinOf.add(r);
            int n = in.read();
            if (n == -1) {
                throw new IOException("Malformed join statement buffer=" + joinOf);
            }

            char c = (char) n;
            if (c == ')') {
                return new JoinRegion(id, joinOf, isOrder);
            }
	    else if (c != ',') {
                throw new IOException("Unexpected '" + c + "\' in join, buffer=" + joinOf);
             }
        }
    }

    private static SingleSeqRegion parseComplement(StringReader in) throws IOException {
        SingleSeqRegion ret = new ComplementRegion(parseRegion(in));
        int next = in.read();

        if (next == -1) {
            throw new IOException("Unexpected end of region, last region= " + ret.toString());
        }
        if (next != ')') {
            throw new IOException("Unexpected '" + (char) next + "', last region= " + ret.toString());
        }

        return ret;
    }

    private static class IdentifierOrRange {

        String identifier;
        GBRange range;
    }

    private static class GBRange {

        public int rangeStart;
        public int rangeEnd;
        public Extends rangeExtends;
    }

    private static IdentifierOrRange parseIdentifierOrRange(StringReader in) throws IOException {
        IdentifierOrRange ret = new IdentifierOrRange();

        StringBuilder buf = new StringBuilder();
        int i;
        Extends e = Extends.EXACT;
        int rangeStart, rangeEnd;

        in.mark(1);
        char c = (char) in.read();
        if (c == '<') {
            e = Extends.BEYOND_BEGIN;
        } else {
            buf.append(c);
        }

	in.mark(1);
        while ((i = in.read()) != -1) {
            c = (char) i;

	    if (c == ',' || c == ')') {
		in.reset();
		break;
	    }
            if (c == ':' || c == '(' || c == '.') {
                break;
            }

            buf.append(c);
	    in.mark(1);
        }

        boolean isIdentifier = false;
        if (c == ':' || c == '(') {    //So we know if we see a : or a ( we've got an identifier, think 'join(', 'complement(', 'AHQ000:'
            isIdentifier = true;
        } else if (c == '.' && (c = (char) in.read()) != '.') {  //If we didn't see a : or ( but we saw a . we could be parsing a range, or it could still be an identifier, if the following character is a '.' it has to be a range, think 123..456, otherwise just treat it as an id
            isIdentifier = true;
            buf.append('.').append(c);

            while ((i = in.read()) != -1) {
                c = (char) i;

                if (c == ':' || c == '(') {
                    break;
                }

                buf.append(c);
            }
        }

        if (isIdentifier) {
            if (e == Extends.BEYOND_BEGIN) {
                buf.insert(0, "<");
            }

            ret.identifier = buf.toString();
            return ret;
        }

        rangeStart = new Integer(buf.toString());

	if(c != ',' && c != ')') {
	    buf = new StringBuilder();

	    c = (char) in.read();
	    if (c == '>') {
		if (e == Extends.BEYOND_BEGIN) {
		    e = Extends.BEYOND_BOTH;
		} else {
		    e = Extends.BEYOND_END;
		}
	    } else {
		buf.append(c);
	    }

	    in.mark(1);
	    while ((i = in.read()) != -1) {
		c = (char) i;
		if (c == ',' || c == ')') {
		    in.reset();
		    break;
		}

		if (!Character.isDigit(c)) {
		    throw new IOException("Expected digit in range, not " + c);
		}

		buf.append(c);

		in.mark(1);
	    }
	}
	rangeEnd = new Integer(buf.toString());

        GBRange range = new GBRange();
        range.rangeStart = rangeStart;
        range.rangeEnd = rangeEnd;
        range.rangeExtends = e;

        ret.range = range;
        return ret;
    }

    public static void main(String[] args) throws IOException {
	for(String s : args) {
	    System.out.println(s);
	    System.out.println(parse(s));
	    System.out.println();
	}
    }
}
