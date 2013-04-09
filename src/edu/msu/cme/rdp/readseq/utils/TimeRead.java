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

import edu.msu.cme.rdp.memtest.MemUsageUtils;
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import java.io.*;

/**
 *
 * @author Jordan Fish <fishjord at msu.edu>
 */
public class TimeRead {

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.err.println("USAGE: TimeRead <file>...");
            System.exit(1);
        }
        SeqReader reader;
        long seqs;

        for(String fname : args) {
            MemUsageUtils.getInstance().printMemStats("Before reading " + fname);
            reader = new SequenceReader(new File(fname));

            seqs = 0;
            while(reader.readNextSequence() != null) {
                seqs += 1;
            }
            MemUsageUtils.getInstance().printMemStats("After reading " + fname);

            System.gc();
            MemUsageUtils.getInstance().printMemStats("After gc " + fname);
        }

        BufferedReader bufferedReader;

        for(String fname : args) {
            MemUsageUtils.getInstance().printMemStats("Before reading lines " + fname);
            bufferedReader = new BufferedReader(new FileReader(new File(fname)));
            while(bufferedReader.readLine() != null) {

            }

            MemUsageUtils.getInstance().printMemStats("After reading lines " + fname);

            System.gc();
            MemUsageUtils.getInstance().printMemStats("After gc " + fname);
        }

        BufferedInputStream is;
        byte[] buf = new byte[1024];

        for(String fname : args) {
            MemUsageUtils.getInstance().printMemStats("Before reading raw " + fname);
            is = new BufferedInputStream(new FileInputStream(new File(fname)));
            while(is.read(buf) > 0) {

            }

            MemUsageUtils.getInstance().printMemStats("After reading raw " + fname);

            System.gc();
            MemUsageUtils.getInstance().printMemStats("After gc " + fname);
        }
    }
}
