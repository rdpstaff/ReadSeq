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

import edu.msu.cme.rdp.readseq.QSequence;
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
public class FastqCore extends SeqReaderCore {

    public static interface QualityFunction {
        public byte translate(char c);
        public char translate(byte b);
    }

    public static final QualityFunction Phred33QualFunction = new QualityFunction() {

        public byte translate(char c) {
            return (byte)(c - 33);
        }

        public char translate(byte q) {
            return (char)(((q <= 93) ? q : 93) + 33);
        }

    };

    public static final QualityFunction Phred64QualFunction = new QualityFunction() {

        public byte translate(char c) {
            return (byte)(c - 33);
        }

        public char translate(byte q) {
            return (char)(((q <= 124) ? q : 124) + 2);
        }

    };
    
    private QualityFunction qualFunction;

    public FastqCore(File seqFile) throws IOException {
        super(seqFile);
        readUntilNext('@');
        this.qualFunction = Phred33QualFunction;
    }

    public FastqCore(InputStream seqFile) throws IOException {
        super(seqFile);
        readUntilNext('@');
        this.qualFunction = Phred33QualFunction;
    }

    public Map<String, Long> scanInternal() throws IOException {
        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(0);
        readUntilNext('@');
        long firstHeader = seqFile.getFilePointer();
        long lastHeader = seqFile.getFilePointer();

        Map<String, Long> seqIndex = new LinkedHashMap();

        while (seqFile.getFilePointer() != seqFile.length()) {
            String sid = parseSid();
            seqIndex.put(sid, lastHeader);    
            String temp = readUntilNext("\n@");
            // when @ is the first char of the quality line    
            String[] lines = temp.split("\n");            
            if ( lines.length == 2){
                readUntilNext("\n@");
            }

            lastHeader = seqFile.getFilePointer();
        }

        seqFile.seek(firstHeader);

        return seqIndex;
    }

    public int scanToStream(DataOutputStream out) throws IOException {
        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(0);
        readUntilNext('@');
        long firstHeader = seqFile.getFilePointer();
        long lastHeader = seqFile.getFilePointer();

        out.writeLong(seqFile.length());
        out.writeUTF(SequenceFormat.FASTQ.toString());
        int written = 0;
        while (seqFile.getFilePointer() != seqFile.length()) {
            String sid = parseSid();
            out.writeUTF(sid);
            out.writeLong(lastHeader);
            written++;

            readUntilNext("\n@");

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
        String seqid = header.split("\\s+")[0];

        String seq = parseSeq();
        if (seq == null) {
            return null;
        }

        String qualHeader = parseHeader();
        if(qualHeader == null) {
            throw new IOException("Unexpected end of file when looking for qual seq for sequence " + seqid);
        }

        String qualId = qualHeader.split("\\s+")[0];
        if(!qualId.equals(seqid) && !qualId.equals("")) {
            throw new IOException("Expected quality sequence for " + seqid + " but found quality sequence for " + qualId);
        }

        byte[] qual = parseQualSeq();
        if(qual == null) {
            throw new IOException("Unexpected end of file when looking for qual seq for sequence " + seqid);
        }

        if (seqid.length() == header.length()) {
            header = "";
        } else {
            header = header.substring(seqid.length()).trim();
        }

        return new QSequence(seqid, header, seq.trim(), qual);
    }

    private String parseSid() throws IOException {
        return readUntilNext(' ', '\t', '\n', '\r');
    }

    private String parseHeader() throws IOException {
        return readUntilNext('\n');
    }

    private String parseSeq() throws IOException {
        String str = readSeqString("\n+");
        return str;
    }

    private byte[] parseQualSeq() throws IOException {
        String str = readSeqString("\n@");
        if(str == null) {
            return null;
        }

        byte[] ret = new byte[str.length()];
        char[] seqChars = str.toCharArray();

        for(int index = 0;index < seqChars.length;index++) {
            ret[index] = qualFunction.translate(seqChars[index]);
        }

        return ret;
    }
}
