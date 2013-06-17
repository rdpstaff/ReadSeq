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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fishjord
 */
public class GenbankCore extends SeqReaderCore {

    public GenbankCore(File seqFile) throws IOException {
        super(seqFile);
    }

    public GenbankCore(InputStream is) throws IOException {
        super(is);
    }

    public Map<String, Long> scanInternal() throws IOException {
        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(0);
        long firstHeader = seqFile.getFilePointer();
        long lastHeader = seqFile.getFilePointer();

        Map<String, Long> seqIndex = new LinkedHashMap();

        while (seqFile.getFilePointer() != seqFile.length()) {
            String sid = parseSid();
            seqIndex.put(sid, lastHeader);

            readUntilNext("\n//");

            lastHeader = seqFile.getFilePointer();
        }

        seqFile.seek(firstHeader);

        return seqIndex;
    }

    public int scanToStream(DataOutputStream out) throws IOException {
        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(0);
        long firstHeader = seqFile.getFilePointer();
        long lastHeader = seqFile.getFilePointer();

        out.writeLong(seqFile.length());
        out.writeUTF(SequenceFormat.GENBANK.toString());
        int written = 0;
        while (seqFile.getFilePointer() != seqFile.length()) {
            String sid = parseSid();
            out.writeUTF(sid);
            out.writeLong(lastHeader);
            written++;

            readUntilNext("\n//");

            lastHeader = seqFile.getFilePointer();
        }
        out.close();

        seqFile.seek(firstHeader);
        return written;
    }

    @Override
    public Sequence readNextSeq() throws IOException {
        String line;
        String seqid = null, desc = "", seqString = null;

        while((line = readUntilNext('\n')) != null) {
            line = line.trim();
            if(line.equals("//")) {
                break;
            }

            if(line.isEmpty()) {
                continue;
            }

            String[] lexemes = line.split("\\s+");
            if(lexemes[0].equals("LOCUS")) {
                seqid = lexemes[1].split(";")[0];
            } else if(lexemes[0].equals("DEFINITION")) {
                if(!desc.isEmpty()) {
                    desc += " ";
                }
                desc += line.substring(2).trim();
            } else if(lexemes[0].equals("ORIGIN")) {
                seqString = parseSeq();
                break;      //We assume ORIGIN is the last entry in the record
            }

        }

        if(seqid == null) {
            return null;
        }

        return new Sequence(seqid, desc, seqString.replaceAll("\\s+", ""));
    }

    private String parseSid() throws IOException {
        return readUntilNext(' ', '\t', '\n', '\r');
    }

    private String parseSeq() throws IOException {
        StringBuilder retSeq = new StringBuilder();
        String seq = readUntilNext("//");

        if(seq == null) {
            return null;
        }

        seq = seq.trim();

        for(String line : seq.split("\n")) {
            line = line.trim();
            int idx = line.indexOf(" ");
            if(idx == -1) {
                Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Weird sequence parsing seqbuffer={0}, currseq={1}", new Object[]{seq, retSeq});
                continue;   //Uhh, this is weird...
            }
            retSeq.append(line.substring(idx));    //We have to strip off the numbers...
        }

        return retSeq.toString();
    }
}
