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
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author fishjord
 */
public class SFFCore extends SeqReaderCore {

    public static final int mftMagicNumber = 778921588;
    public static final int srtMagicNumber = 779317876;
    public static final int v1MagicNumber = 825110576;

    public static class CommonHeader {

        private int magicNumber;
        private int version;
        private long indexOffset;
        private int indexLength;
        private int numReads;
        private short headerLength;
        private short keyLength;
        private short flowLength;
        private byte flowgramFormat;
        private String flow;
        private String key;

        public String getFlow() {
            return flow;
        }

        public short getFlowLength() {
            return flowLength;
        }

        public byte getFlowgramFormat() {
            return flowgramFormat;
        }

        public short getHeaderLength() {
            return headerLength;
        }

        public int getIndexLength() {
            return indexLength;
        }

        public long getIndexOffset() {
            return indexOffset;
        }

        public String getKey() {
            return key;
        }

        public short getKeyLength() {
            return keyLength;
        }

        public int getMagicNumber() {
            return magicNumber;
        }

        public int getNumReads() {
            return numReads;
        }

        public int getVersion() {
            return version;
        }
    }

    public static class ReadBlock {

        private int headerLength;
        private int nameLength;
        private String name;
        private int numBases;
        private int clipQualLeft;
        private int clipQualRight;
        private int clipAdapterLeft;
        private int clipAdapterRight;
        private String seq;
        private short[] flowgrams;
        private byte[] flowIndex;
        private byte[] qual;

        public int getClipAdapterLeft() {
            return clipAdapterLeft;
        }

        public int getClipAdapterRight() {
            return clipAdapterRight;
        }

        public int getClipQualLeft() {
            return clipQualLeft;
        }

        public int getClipQualRight() {
            return clipQualRight;
        }

        public byte[] getFlowIndex() {
            return flowIndex;
        }

        public int getHeaderLength() {
            return headerLength;
        }

        public short[] getFlowgrams() {
            return flowgrams;
        }

        public String getName() {
            return name;
        }

        public int getNameLength() {
            return nameLength;
        }

        public int getNumBases() {
            return numBases;
        }

        public byte[] getQual() {
            return qual;
        }

        public String getSeq() {
            return seq;
        }
    }
    protected CommonHeader commonHeader;
    private String manifest;
    private static final int READ_BLOCK_STATIC_SIZE = 2 + 2 + 4 + 2 + 2 + 2 + 2;
    private static final int COMMON_HEADER_STATIC_SIZE = 4 + 4 + 8 + 4 + 4 + 2 + 2 + 2 + 1;

    public SFFCore(File f) throws IOException {
        super(f);
        parseCommonHeader();
    }

    public SFFCore(InputStream is) throws IOException {
        super(is);
        parseCommonHeader();
    }

    private void parseCommonHeader() throws IOException {
        if(commonHeader != null) {
            throw new IOException("Common header already initialized");
        }
        commonHeader = new CommonHeader();

        DataInput seqFile = super.getDataInput();

        commonHeader.magicNumber = seqFile.readInt();

        if (commonHeader.magicNumber != SeqUtils.SFF_MAGIC_NUMBER) {
            throw new IOException("Not an SFF File");
        }

        commonHeader.version = seqFile.readInt();

        if (commonHeader.version != 1) {
            throw new IOException("Cannot parse v" + commonHeader.version + " sff files");
        }

        commonHeader.indexOffset = seqFile.readLong();
        commonHeader.indexLength = seqFile.readInt();
        commonHeader.numReads = seqFile.readInt();
        commonHeader.headerLength = seqFile.readShort();
        commonHeader.keyLength = seqFile.readShort();
        commonHeader.flowLength = seqFile.readShort();
        commonHeader.flowgramFormat = seqFile.readByte();

        byte[] flow = new byte[commonHeader.flowLength];
        super.read(flow);
        commonHeader.flow = new String(flow);

        byte[] key = new byte[commonHeader.keyLength];
        super.read(key);
        commonHeader.key = new String(key);

        int readBytes = COMMON_HEADER_STATIC_SIZE + flow.length + key.length;

        alignToBoundary(readBytes);

        if (super.isSeekable() && commonHeader.indexOffset > commonHeader.headerLength) {
            readIndex();
        }
    }

    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    public String getManifest() {
        return manifest;
    }

    @Override
    public LinkedHashMap<String, Long> scanInternal() throws IOException {
        if (commonHeader.indexOffset > commonHeader.headerLength) {
            return readIndex();
        }

        RandomAccessFile seqFile = super.getRawFile();

        seqFile.seek(commonHeader.headerLength);
        LinkedHashMap<String, Long> seqIndex = new LinkedHashMap(commonHeader.numReads);

        for (int index = 0; index < commonHeader.numReads; index++) {
            long pos = seqFile.getFilePointer();
            ReadBlock block = readReadBlock();
            seqIndex.put(block.name, pos);
        }

        return seqIndex;
    }

    @Override
    public QSequence readNextSeq() throws IOException {
        ReadBlock readBlock = readReadBlock();
        if (readBlock == null) {
            return null;
        }

        int clipLeft = Math.max(readBlock.clipAdapterLeft, readBlock.clipQualLeft);
        int clipRight = readBlock.numBases;

        if (readBlock.clipAdapterRight > 0 && readBlock.clipAdapterRight < clipRight) {
            clipRight = readBlock.clipAdapterRight;
        }

        if (readBlock.clipQualRight != 0 && readBlock.clipQualRight < clipRight) {
            clipRight = readBlock.clipQualRight;
        }
        if (clipLeft  > clipRight )
           return readNextSeq(); 
        String seq = readBlock.seq.substring(clipLeft - 1, clipRight);
        

        return new QSequence(readBlock.name, "", seq, Arrays.copyOfRange(readBlock.qual, clipLeft - 1, clipRight));
    }

    public ReadBlock readReadBlock() throws IOException {
        try {
            DataInput seqFile = super.getDataInput();

            ReadBlock ret = new ReadBlock();

            /*
             * READ BLOCK HEADER
             */

            ret.headerLength = seqFile.readShort();
            ret.nameLength = seqFile.readShort();

	    int tmp = (ret.headerLength << 16) | ret.nameLength;
	    if(tmp == mftMagicNumber) { //We ended up in the index...certainly possible
		return null;
	    }

            ret.numBases = seqFile.readInt();
            ret.clipQualLeft = seqFile.readUnsignedShort();
            ret.clipQualRight = seqFile.readUnsignedShort();
            ret.clipAdapterLeft = seqFile.readUnsignedShort();
            ret.clipAdapterRight = seqFile.readUnsignedShort();

            byte[] readName = new byte[ret.nameLength];
            super.read(readName);

            int dataOffset = ret.headerLength - (ret.nameLength + READ_BLOCK_STATIC_SIZE);
            if (dataOffset < 0) {
                throw new IOException("Illegal ReadBlock header length (" + ret.headerLength + "), it would have me seek back in to the readblock");
            }

            seqFile.skipBytes(dataOffset);

            /*
             * READ BLOCK DATA
             */

            byte[] flowgramIndex = new byte[ret.numBases];
            byte[] bases = new byte[ret.numBases];
            byte[] quality = new byte[ret.numBases];

            byte[] homopolymerStretchEstimates = new byte[(commonHeader.flowLength) * 2];

            super.read(homopolymerStretchEstimates);
            super.read(flowgramIndex);
            super.read(bases);
            super.read(quality);

            DataInputStream flowgramStream = new DataInputStream(new ByteArrayInputStream(homopolymerStretchEstimates));

            short[] flowgrams = new short[commonHeader.flowLength];
            for (int index = 0; index < commonHeader.flowLength; index++) {
                flowgrams[index] = flowgramStream.readShort();
            }
            flowgramStream.close();

            ret.name = new String(readName);
            ret.flowgrams = flowgrams;
            ret.flowIndex = flowgramIndex;
            ret.seq = new String(bases);
            ret.qual = quality;
            int bytesRead = homopolymerStretchEstimates.length + flowgramIndex.length + bases.length + quality.length;

            alignToBoundary(bytesRead);

            return ret;
        } catch (EOFException e) {
            return null;
        }
    }

    private LinkedHashMap<String, Long> readIndex() throws IOException {
        if (commonHeader.indexOffset <= commonHeader.headerLength) {
            throw new IOException("Index offset is not set correctly");
        }

        RandomAccessFile seqFile = super.getRawFile();

        long seekBackTo = seqFile.getFilePointer();
        seqFile.seek(commonHeader.indexOffset);
        long dataEnd = seqFile.getFilePointer();
        LinkedHashMap<String, Long> seqIndex = new LinkedHashMap(commonHeader.numReads);

        int magicNumber = seqFile.readInt();

        if (magicNumber == mftMagicNumber) {
            int version = seqFile.readInt();

            if (version != v1MagicNumber) {
                throw new IOException("Can only parse .mft v1.0 indices");
            }

            int xmlSize = seqFile.readInt();
            int dataSize = seqFile.readInt();
            dataEnd += dataSize;

            byte[] xml = new byte[xmlSize];
            seqFile.read(xml);
            manifest = new String(xml);

        } else if (magicNumber == srtMagicNumber) {
            int version = seqFile.readInt();

            if (version != v1MagicNumber) {
                throw new IOException("Can only parse .srt v1.0 indices");
            }

            if (seqFile.read() != 0) {
                throw new IOException("Failed to find expected null byte in .srt header");
            }
            dataEnd += commonHeader.indexLength;
        } else {
            throw new IOException("No supported index found");
        }

        List<Integer> currIndex = new ArrayList();
        while (seqFile.getFilePointer() < dataEnd) {
            int b = seqFile.readUnsignedByte();
            if (b == 0xff) {
                byte[] nameArray = new byte[currIndex.size() - 5];
                long indexLoc = 0;
                int[] multipliers = new int[]{0, 16581375, 65025, 255, 1};
                for (int i = 0; i < currIndex.size(); i++) {
                    if (i < nameArray.length) {
                        nameArray[i] = (byte) (currIndex.get(i) & 0xff);
                    } else {
                        int index = i - nameArray.length;
                        indexLoc += currIndex.get(i) * multipliers[index];
                    }
                }
                String name = new String(nameArray);

                seqIndex.put(name, indexLoc);

                currIndex.clear();
            } else {
                currIndex.add(b);
            }
        }
        seqFile.seek(seekBackTo);

        return seqIndex;
    }

    private void alignToBoundary(int bytesRead) throws IOException {
        int pos = bytesRead % 8;
        if (pos != 0) {
            pos = 8 - (pos % 8);
        }

        super.getDataInput().skipBytes(pos);
    }

    public static void main(String[] args) throws Exception {
        SFFCore core = new SFFCore(new File("test/454Reads.sff"));

        System.out.println("Common header:");
        System.out.println(StringUtils.rightPad("  Version:", 20) + core.commonHeader.version);
        System.out.println(StringUtils.rightPad("  Index offset:", 20) + core.commonHeader.indexOffset);
        System.out.println(StringUtils.rightPad("  Index length:", 20) + core.commonHeader.indexLength);
        System.out.println(StringUtils.rightPad("  Num reads:", 20) + core.commonHeader.numReads);
        System.out.println(StringUtils.rightPad("  Header length:", 20) + core.commonHeader.headerLength);
        System.out.println(StringUtils.rightPad("  Key length:", 20) + core.commonHeader.keyLength);
        System.out.println(StringUtils.rightPad("  Num flows:", 20) + core.commonHeader.flowLength);
        System.out.println(StringUtils.rightPad("  Flowgram format:", 20) + core.commonHeader.flowgramFormat);
        System.out.println(StringUtils.rightPad("  Flow:", 20) + core.commonHeader.flow);
        System.out.println(StringUtils.rightPad("  Key:", 20) + core.commonHeader.key);

        System.out.println();
        ReadBlock block = core.readReadBlock();
        System.out.println(">" + block.name);
        System.out.println(StringUtils.rightPad("  Name:", 20) + block.name);
        System.out.println(StringUtils.rightPad("  Name length:", 20) + block.nameLength);
        System.out.println(StringUtils.rightPad("  Number of bases:", 20) + block.numBases);
        System.out.println(StringUtils.rightPad("  Length:", 20) + block.headerLength);
        System.out.println(StringUtils.rightPad("  Clip adapter left:", 20) + block.clipAdapterLeft);
        System.out.println(StringUtils.rightPad("  Clip adapter right:", 20) + block.clipAdapterRight);
        System.out.println(StringUtils.rightPad("  Clip qual left:", 20) + block.clipQualLeft);
        System.out.println(StringUtils.rightPad("  Clip qual right:", 20) + block.clipQualRight);

        System.out.print(StringUtils.rightPad("Flowgrams:", 20));
        for (int index = 0; index < block.flowgrams.length; index++) {
            System.out.print(StringUtils.rightPad(block.flowgrams[index] + "", 10));
        }
        System.out.println();

        System.out.print(StringUtils.rightPad("Flow Indices:", 20));
        for (int index = 0; index < block.flowIndex.length; index++) {
            System.out.print(StringUtils.rightPad(block.flowIndex[index] + "", 10));
        }
        System.out.println();

        System.out.print(StringUtils.rightPad("Sequence:", 20));
        for (int index = 0; index < block.seq.length(); index++) {
            System.out.print(StringUtils.rightPad(block.seq.charAt(index) + "", 10));
        }
        System.out.println();

        System.out.print(StringUtils.rightPad("Quality:", 20));
        for (int index = 0; index < block.qual.length; index++) {
            System.out.print(StringUtils.rightPad(block.qual[index] + "", 10));
        }
        System.out.println();

        System.out.println();
        block = core.readReadBlock();
        System.out.println(">" + block.name);
        System.out.println(StringUtils.rightPad("  Name:", 20) + block.name);
        System.out.println(StringUtils.rightPad("  Name length:", 20) + block.nameLength);
        System.out.println(StringUtils.rightPad("  Number of bases:", 20) + block.numBases);
        System.out.println(StringUtils.rightPad("  Length:", 20) + block.headerLength);
        System.out.println(StringUtils.rightPad("  Clip adapter left:", 20) + block.clipAdapterLeft);
        System.out.println(StringUtils.rightPad("  Clip adapter right:", 20) + block.clipAdapterRight);
        System.out.println(StringUtils.rightPad("  Clip qual left:", 20) + block.clipQualLeft);
        System.out.println(StringUtils.rightPad("  Clip qual right:", 20) + block.clipQualRight);

        System.out.print(StringUtils.rightPad("Flowgrams:", 20));
        for (int index = 0; index < block.flowgrams.length; index++) {
            System.out.print(StringUtils.rightPad(block.flowgrams[index] + "", 10));
        }
        System.out.println();

        System.out.print(StringUtils.rightPad("Flow Indices:", 20));
        for (int index = 0; index < block.flowIndex.length; index++) {
            System.out.print(StringUtils.rightPad(block.flowIndex[index] + "", 10));
        }
        System.out.println();

        System.out.print(StringUtils.rightPad("Sequence:", 20));
        for (int index = 0; index < block.seq.length(); index++) {
            System.out.print(StringUtils.rightPad(block.seq.charAt(index) + "", 10));
        }
        System.out.println();

        System.out.print(StringUtils.rightPad("Quality:", 20));
        for (int index = 0; index < block.qual.length; index++) {
            System.out.print(StringUtils.rightPad(block.qual[index] + "", 10));
        }
        System.out.println();

        core.close();
    }
}
