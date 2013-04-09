/*
 * Copyright (C) 2012 wangqion
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
package edu.msu.cme.rdp.readseq.writers;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wangqion
 */
public class StkWriter implements SequenceWriter {
   
    public static final String STK_HEADER = "# STOCKHOLM 1.0";
    private PrintStream out;
    private int max_seqIDLength = 0;
    private static final int GAP_LENGTH = 10;

    
    public StkWriter(int seqIDLength, OutputStream o) throws IOException {
        this(seqIDLength, o, STK_HEADER);
    }
    
    
    public StkWriter(int seqIDLength, OutputStream o, String header) throws IOException {
        this.out = new PrintStream(o);
        max_seqIDLength = seqIDLength;
        out.println(header + "\n");
    }
     
    public StkWriter(SequenceReader reader, OutputStream o, String header) throws IOException {
        this(getSeqIDLength(reader), o, header);       
    }
       

    public static int getSeqIDLength(SequenceReader reader) throws IOException{
        int seqIDLength = 0;
        int seqLength = 0;
        Sequence seq;
        while ( (seq = reader.readNextSequence()) !=  null) {
            if ( seq.getSeqName().trim().length() > seqIDLength){
                seqIDLength = seq.getSeqName().trim().length();
            }
            if (seqLength == 0){
                seqLength = seq.getSeqString().trim().length();
            }else if ( seq.getSeqString().trim().length() != seqLength){
                 throw new IOException("Sequencec seq " + seq.getSeqName() + " is not the expected length (" + seqLength + ")");
            }
        }        
        reader.close();
        return seqIDLength;        
    }
    
    public static int getSeqIDLength(List<Sequence> seqs) throws IOException{
        int seqIDLength = 0;
        int seqLength = 0;
        for ( Sequence seq: seqs) {
            if ( seq.getSeqName().trim().length() > seqIDLength){
                seqIDLength = seq.getSeqName().trim().length();
            }
            if (seqLength == 0){
                seqLength = seq.getSeqString().trim().length();
            }else if ( seq.getSeqString().trim().length() != seqLength){
                 throw new IOException("Sequencec seq " + seq.getSeqName() + " is not the expected length (" + seqLength + ")");
            }
        }        
        return seqIDLength;        
    }
    
    /** 
     * need to pad spaces
     * @param seqid
     * @return 
     */
    private String getSeqId(String seqid) {
        return String.format("%1$-" + (max_seqIDLength + GAP_LENGTH) + "s", seqid);
    }

    public void writeSeq(Sequence seq) throws IOException {
        writeSeq(seq.getSeqName(), seq.getSeqString());
    }

    public void writeSeq(String seqid, String seqString) throws IOException {
        out.println(getSeqId(seqid) + seqString);
    }

    public void writeSeq(String seqid, String desc, String seqString) throws IOException {
        throw new UnsupportedOperationException("Phylip format doesn't support sequence descriptions");
    }

    public void writeEndOfBlock() throws IOException{
        out.println("//");
    }
    
    public void close() throws IOException {
        out.close();
    }

    
    public static void writeSequences(SequenceReader reader, OutputStream out) throws IOException {
        writeSequences(reader, out, STK_HEADER);
    }
    
    public static void writeSequences(SequenceReader reader, OutputStream out, String header) throws IOException {
        ArrayList<Sequence> seqList = new ArrayList<Sequence>();
        Sequence seq;
        while ( (seq = reader.readNextSequence()) !=  null) {
            seqList.add(seq);
        }
        reader.close();
        StkWriter writer = new StkWriter(getSeqIDLength(seqList), out, header);
        
        for(Sequence aseq : seqList)
            writer.writeSeq(aseq);
        
        writer.writeEndOfBlock();
        writer.close();
    }
    
    public static void main(String[] args) throws Exception{        
        if ( args.length != 2 && args.length != 3){
            throw new Exception("Usage: infile outfile <header>");
        }
        String header = STK_HEADER;
        if ( args.length == 3){
            header = args[2];
        }
         
        System.err.println(header);
        //StkWriter.writeSequences(new SequenceReader(new File(args[0])), new PrintStream(new File(args[1])), header);
        SequenceReader reader = new SequenceReader(new File(args[0]));        
        PrintStream out = new PrintStream(new File(args[1]));
        StkWriter writer = new StkWriter(reader, out, header);
        reader = new SequenceReader(new File(args[0]));
        Sequence seq;
        while ( (seq = reader.readNextSequence()) !=  null) {
            writer.writeSeq(seq);
        }
        writer.writeEndOfBlock();
        writer.close();
        reader.close();
        
    }
}
