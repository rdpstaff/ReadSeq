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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class STKCore extends SeqReaderCore {

    private Map<String, List<Long>> seqIndex = new LinkedHashMap();
    private Map<String, Long> seqDescIndex = new LinkedHashMap();
    private List<String> seqids;

    public STKCore(File f) throws IOException {
        super(f);
        RandomAccessFile seqFile = super.getRawFile();

        long firstSeqStart = -1;

        while (seqFile.getFilePointer() != seqFile.length()) {
            long currSeqIndex = seqFile.getFilePointer();
            String lineStr = seqFile.readLine().trim();
            char[] line = lineStr.toCharArray();

            if (line.length == 0) {
                continue; //blank lines
            } else if (line.length == 2 && line[0] == '/' && line[1] == '/') {
                break;
            }

            String seqid;

            if (line[0] == '#') {
                if (line[1] != '=') {   //comment
                    continue;
                }

                if(line.length < 5) {
                    throw new IOException("Malformed line " + lineStr);
                }

                char metaSeqType = line[3];

                if (line[2] != 'G' || (metaSeqType != 'S' && metaSeqType != 'R' && metaSeqType != 'C')) {
                    System.err.println("Unknown metasequence type " + lineStr.split("\\s+")[0] + ", not indexing");
                    continue;
                }

                if(line[4] != ' ') {
                    throw new IOException("Malformed line " + lineStr);
                }

                StringBuilder sid = new StringBuilder();
                for(int index = 5;index < line.length;index++) {
                    if(line[index] == ' ') {
                        break;
                    }

                    sid.append(line[index]);
                }

                if(metaSeqType == 'S') { //We're going to be bad and not process any sort of metaseqs besides GS and GC
                    seqDescIndex.put(sid.toString(), currSeqIndex);
                    continue;
                } else if(metaSeqType != 'C') {
                    continue;
                }

                seqid = "#=GC " + sid.toString();
            } else {
                StringBuilder sid = new StringBuilder();
                for(int index = 0;index < line.length;index++) {
                    if(line[index] == ' ') {
                        break;
                    }

                    sid.append(line[index]);
                }

                seqid = sid.toString();
            }
            
            if (firstSeqStart == -1) {
                firstSeqStart = currSeqIndex;
            }

            if (!seqIndex.containsKey(seqid)) {
                seqIndex.put(seqid, new ArrayList());
            }

            seqIndex.get(seqid).add(currSeqIndex);
        }

        seqFile.seek(firstSeqStart);
        seqids = new ArrayList(seqIndex.keySet());
    }

    @Override
    public LinkedHashMap<String, Long> scanInternal() throws IOException {
        LinkedHashMap<String, Long> ret = new LinkedHashMap();
        for (String key : seqIndex.keySet()) {
            ret.put(key, seqIndex.get(key).get(0));
        }
        return ret;
    }

    @Override
    public Sequence readNextSeq() throws IOException {
        RandomAccessFile seqFile = super.getRawFile();
        
        long seqStart = seqFile.getFilePointer();

        String line = seqFile.readLine().trim();

        while (line.equals("")) {
            if (seqFile.getFilePointer() == seqFile.length()) {
                return null;
            }

            seqStart = seqFile.getFilePointer();
            line = seqFile.readLine().trim();
        }

        if (line.equals("//")) {
            return null;
        }

        String seqid = line.substring(0, line.lastIndexOf(" ")).trim();

        if (seqIndex.get(seqid).get(0) != seqStart) {
            return null;
        }

        StringBuilder bases = new StringBuilder();

        if (!seqIndex.containsKey(seqid)) {
            throw new IOException("Huh, that's odd, this sequence(" + seqid + ") didn't get indexed...");
        }

        int nextSeqIndex = seqids.indexOf(seqid) + 1;
        long seekBackTo;
        if (nextSeqIndex == seqids.size()) {
            seekBackTo = seqFile.getFilePointer();
        } else {
            seekBackTo = seqIndex.get(seqids.get(nextSeqIndex)).get(0);

        }
        for (long loc : seqIndex.get(seqid)) {
            seqFile.seek(loc);

            line = seqFile.readLine().trim();

            bases.append(line.substring(line.lastIndexOf(" ") + 1).trim());
        }

        String desc = "";

        if (seqDescIndex.containsKey(seqid)) {
            seqFile.seek(seqDescIndex.get(seqid));
            String[] lexemes = seqFile.readLine().split("\\s+");

            if (!lexemes[0].equals("#=GS")) {
                throw new IOException("Sequence description index error!");
            }

            if (!lexemes[1].equals(seqid)) {
                throw new IOException("I expected to find the description for " + seqid + " but I found the description for " + lexemes[1]);
            }

            for (int index = 3; index < lexemes.length; index++) {
                desc += lexemes[index] + " ";
            }
            desc = desc.trim();
        }

        seqFile.seek(seekBackTo);

        return new Sequence(seqid, desc, bases.toString());
    }
}
