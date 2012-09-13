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

package edu.msu.cme.rdp.readseq.writers;

import edu.msu.cme.rdp.readseq.readers.core.SFFCore;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore.CommonHeader;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore.ReadBlock;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class SFFWriter {

    private RandomAccessFile out;
    private Map<String, Long> seqIndex = new HashMap();
    private String manifest;

    public SFFWriter(File outFile, CommonHeader ch) throws IOException {
        if(outFile.exists()) {
            if(!outFile.delete()) {
                throw new IOException(outFile + " exists and could not be deleted");
            }
        }
        out = new RandomAccessFile(outFile, "rw");

        writeCommonHeader(ch);
    }

    public SFFWriter(File outFile, CommonHeader ch, String manifest) throws IOException {
        this(outFile, ch);
        this.manifest = manifest;
    }

    public void close() throws IOException {
        //We should already be at the end of the file, if not something bad happened...
        long indexStart = out.getFilePointer();

        if(manifest != null) {
            out.writeInt(SFFCore.mftMagicNumber);
            out.writeInt(SFFCore.v1MagicNumber);
            out.writeInt(manifest.getBytes().length);
            out.writeInt(Integer.MIN_VALUE);
            out.write(manifest.getBytes());
        } else {
            out.writeInt(SFFCore.srtMagicNumber);
            out.writeInt(SFFCore.v1MagicNumber);
            out.writeInt(0);
        }

        List<String> seqids = new ArrayList(seqIndex.keySet());
        Collections.sort(seqids);  //Put the SORT in sorted index

        for(String seqid : seqids) {
            out.write(seqid.getBytes());
            long offset = seqIndex.get(seqid);

            //Don't look TOO close, the basic thing is we need to pack 5 bytes of a long and 0xff in to the file
            //Also 0xff has a special meaning so it can't appear in the long (max val = 255, not 256 basically)
            long pt4 = 0;   //Roche never seems to use the 5th byte of this long so neither will we
            long pt3 = offset;
            long pt0 = pt3 % 255;
            pt3 -= pt0;
            long pt1 = pt3 % 65025;
            pt3 -= pt1;
            long pt2 = pt3 % 16581375;
            pt3 -= pt2;

            pt3 /= 16581375;
            pt2 /= 65025;
            pt1 /= 255;

            byte[] indexBytes = new byte[] { 
                (byte)(pt4 & 0xff),
                (byte)(pt3 & 0xff),
                (byte)(pt2 & 0xff),
                (byte)(pt1 & 0xff),
                (byte)(pt0 & 0xff),
                (byte)0xff }; // Pack it!
            out.write(indexBytes);
        }

        int indexSize = (int)(out.getFilePointer() - indexStart);

        alignToBoundary();

        if(manifest != null) {
            out.seek(indexStart + 12); //Seek back to where the data size is suppose to be
            int dataSize = indexSize - 16 - manifest.getBytes().length;  //Size of just the index (no manifest, no magic numbers)
            out.writeInt(dataSize);
        }

        out.seek(8); //Jump back to write out the index offset, length, and read count
        out.writeLong(indexStart);      //Index offset
        out.writeInt(indexSize);        //index size
        out.writeInt(seqids.size());    //Number of reads
        out.close();
    }

    private void writeCommonHeader(CommonHeader ch) throws IOException {
        out.writeInt(ch.getMagicNumber());
        out.writeInt(ch.getVersion());
        out.writeLong(Long.MIN_VALUE);   //index offset(written on close)
        out.writeInt(Integer.MIN_VALUE); //index length (written on close)
        out.writeInt(Integer.MIN_VALUE); //num reads (written on close)
        out.writeShort(ch.getHeaderLength());
        out.writeShort(ch.getKeyLength());
        out.writeShort(ch.getFlowLength());
        out.writeByte(ch.getFlowgramFormat());

        out.write(ch.getFlow().getBytes());
        out.write(ch.getKey().getBytes());

        alignToBoundary();
    }

    public void writeReadBlock(ReadBlock rb) throws IOException {
        seqIndex.put(rb.getName(), out.getFilePointer());

        out.writeShort(rb.getHeaderLength());
        out.writeShort(rb.getNameLength());
        out.writeInt(rb.getNumBases());
        out.writeShort(rb.getClipQualLeft());
        out.writeShort(rb.getClipQualRight());
        out.writeShort(rb.getClipAdapterLeft());
        out.writeShort(rb.getClipAdapterRight());
        out.write(rb.getName().getBytes());
        for(int index = 0;index < rb.getFlowgrams().length;index++) {
            out.writeShort(rb.getFlowgrams()[index]);
        }
        out.write(rb.getFlowIndex());
        out.write(rb.getSeq().getBytes());
        out.write(rb.getQual());

        alignToBoundary();
    }

    private void alignToBoundary() throws IOException {
        long pos = out.getFilePointer();

        if (pos % 8 != 0) {
            byte[] padding = new byte[(int)(8 - pos % 8)]; //There is something seriously wrong if THIS can't be stored in an int...
            out.write(padding); //Thank you java for zeroing all new variables
        }
    }
}
