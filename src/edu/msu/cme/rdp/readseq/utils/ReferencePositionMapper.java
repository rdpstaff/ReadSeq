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

import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import java.io.File;

/**
 *
 * @author fishjord
 */
public class ReferencePositionMapper {

    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            System.err.println("USAGE: ReferencePositionMapping <ref_seq_file>");
            System.exit(1);
        }

        SeqReader reader = new SequenceReader(new File(args[0]));
        Sequence refSeq = reader.readNextSequence();
        char[] bases = refSeq.getSeqString().toCharArray();
        int seqPos = 0;
        int modelPos = 0;

        for(int index = 0;index < bases.length;index++) {
            char b = bases[index];
            if(Character.isLetter(b)) {
                seqPos++;
                System.err.println(seqPos + "\t" + modelPos);
            }

            if(Character.isUpperCase(b) || b == '-') {
                modelPos++;
            }
        }

        reader.close();
    }
}
