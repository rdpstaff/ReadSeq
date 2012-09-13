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
import edu.msu.cme.rdp.readseq.sequences.GenbankSequence;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fishjord
 */
public class EMBLCore extends SeqReaderCore {

    public EMBLCore(File seqFile) throws IOException {
        super(seqFile);
    }

    public EMBLCore(InputStream is) throws IOException {
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

        Map<String, Long> seqIndex = new LinkedHashMap();

        out.writeLong(seqFile.length());
        out.writeUTF(SequenceFormat.EMBL.toString());
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
        String seqid = null, desc = "", seqString = null, organism = "";
        Map<String, Map<String, String>> featureTable = new HashMap();
        Map<String, String> features = null;

        while ((line = readUntilNext('\n')) != null) {
            line = line.trim();
            if (line.equals("//")) {
                break;
            }

            if (line.isEmpty()) {
                continue;
            }

            String[] lexemes = line.split("\\s+");
            if (lexemes[0].equals("ID")) {
                seqid = lexemes[1].split(";")[0];
            } else if (lexemes[0].equals("DE")) {
                if (!desc.isEmpty()) {
                    desc += " ";
                }
                desc += line.substring(2).trim();
            } else if (lexemes[0].equals("OS")) {
                if (!organism.isEmpty()) {
                    organism += " ";
                }
                organism += line.substring(2).trim();
            } else if (lexemes[0].equals("SQ")) {
                seqString = parseSeq();
                break; //We assume SQ is the last entry in the record
            } else if (lexemes[0].equals("FT")) {

                if (lexemes[1].charAt(0) == '/') {
                    String qual = line.substring(2).trim();

                    if (!qual.endsWith("\"") && qual.contains("\"")) {
                        String rest = readUntilNext("\"\n");
                        for (String s : rest.split("\n")) {
                            qual += s.substring(2).trim();
                        }
                    }

                    lexemes = qual.replace("\"", "").substring(1).split("=");
                    if (lexemes.length != 2) {
                        throw new IOException("Malformed qualifier " + qual + " in record " + seqid);
                    }

                    features.put(lexemes[0], lexemes[1]);
                } else if (lexemes.length == 3) {
                    features = new HashMap();
                    features.put("location", lexemes[2]);
                    featureTable.put(lexemes[1], features);
                } else {
                    throw new IOException("Malformed qualifier " + line + " in record " + seqid);
                }
            }
        }

        if (seqid == null) {
            return null;
        }

        return new GenbankSequence(seqid, desc, organism, featureTable, seqString.replaceAll("\\s+", ""));
    }

    private String parseSid() throws IOException {
        return readUntilNext(' ', '\t', '\n', '\r');
    }

    private String parseSeq() throws IOException {
        StringBuilder retSeq = new StringBuilder();
        String seq = readUntilNext("//");

        if (seq == null) {
            return null;
        }
        seq = seq.trim();

        for (String line : seq.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            int idx = line.lastIndexOf(" ");
            if (idx == -1) {
                Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Weird sequence parsing seqbuffer={0}, currseq={1}", new Object[]{seq, retSeq});
                continue;   //Uhh, this is weird...
            }
            retSeq.append(line.substring(0, idx));    //We have to strip off the numbers...
        }

        return retSeq.toString();
    }

    public static void main(String[] args) throws IOException {
        EMBLCore tmp = new EMBLCore(new File(args[0]));
        GenbankSequence seq = (GenbankSequence) tmp.readNextSeq();

        System.out.println(seq.getSeqName());
        System.out.println(seq.getDesc());
        System.out.println(seq.getOrganism());
        System.out.println(seq.getFeatureTable());
        System.out.println(seq.getSeqString());
    }
}
