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
package edu.msu.cme.rdp.readseq.readers.core;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class FastaCore extends SeqReaderCore {

    public FastaCore(File seqFile) throws IOException {
        super(seqFile);
        readUntilNext('>');
    }

    public FastaCore(InputStream is) throws IOException {
        super(is);
        readUntilNext('>');
    }

    public Map<String, Long> scanInternal() throws IOException {
        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(0);
        readUntilNext('>');
        long firstHeader = seqFile.getFilePointer();
        long lastHeader = seqFile.getFilePointer();

        Map<String, Long> seqIndex = new LinkedHashMap();

        while (seqFile.getFilePointer() != seqFile.length()) {
            String sid = parseSid();
            seqIndex.put(sid, lastHeader);

            readUntilNext("\n>");

            lastHeader = seqFile.getFilePointer();
        }

        seqFile.seek(firstHeader);

        return seqIndex;
    }

    public int scanToStream(DataOutputStream out) throws IOException {
        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(0);
        readUntilNext('>');
        long firstHeader = seqFile.getFilePointer();
        long lastHeader = seqFile.getFilePointer();

        Map<String, Long> seqIndex = new LinkedHashMap();

        out.writeLong(seqFile.length());
        out.writeUTF(SequenceFormat.FASTA.toString());
        int written = 0;
        while (seqFile.getFilePointer() != seqFile.length()) {
            String sid = parseSid();
            out.writeUTF(sid);
            out.writeLong(lastHeader);
            written++;

            readUntilNext("\n>");

            lastHeader = seqFile.getFilePointer();
        }
        out.close();

        seqFile.seek(firstHeader);
        return written;
    }

    @Override
    public Sequence readNextSeq() throws IOException {
        String header = parseHeader();
        if (header == null) {
            return null;
        }

        String seq = parseSeq();
        if (seq == null) {
            return null;
        }

        String seqid = header.split("\\s+")[0];
        if (seqid.length() == header.length()) {
            header = "";
        } else {
            header = header.substring(seqid.length()).trim();
        }

        return new Sequence(seqid, header, seq.trim());
    }

    private String parseSid() throws IOException {
        return readUntilNext(' ', '\t', '\n', '\r');
    }

    private String parseHeader() throws IOException {
        return readUntilNext('\n');
    }

    private String parseSeq() throws IOException {
        return readUntilNext("\n>");
    }
}
