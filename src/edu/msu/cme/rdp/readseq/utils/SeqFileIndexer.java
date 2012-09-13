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

import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author fishjord
 */
public class SeqFileIndexer {
    public static void main(String[] args) throws IOException {
        if(args.length != 2) {
            System.err.println("USAGE: SeqFileIndexer <seqfile> <index_out>");
            System.exit(1);
        }
        
        File inFile = new File(args[0]);
        File outFile = new File(args[1]);
        IndexedSeqReader.indexSeqFile(inFile, outFile);
    }
}
