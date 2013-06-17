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
import edu.msu.cme.rdp.readseq.utils.BufferedRandomAccessFile;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public abstract class SeqReaderCore {

    public static class EmptyCore extends SeqReaderCore {

        public EmptyCore(File f) throws IOException {
            super(f);
        }

        public EmptyCore(InputStream is) throws IOException {
            super(is);
        }

        @Override
        public Map<String, Long> scanInternal() throws IOException {
            return new HashMap();
        }

        @Override
        protected Sequence readNextSeq() throws IOException {
            return null;
        }
    }
    private boolean seekable;
    private RandomAccessFile rawRAFile = null;
    private DataInputStream is = null;

    public SeqReaderCore(File seqFile) throws IOException {
        this.rawRAFile = new BufferedRandomAccessFile(seqFile, "r", 4096);
        seekable = true;
    }

    public SeqReaderCore(InputStream is) throws IOException {
        this.is = new DataInputStream(new BufferedInputStream(is));
        seekable = false;
    }

    protected final RandomAccessFile getRawFile() throws IOException {
        if (!seekable) {
            throw new IOException("Stream seq cores are not seekable");
        }

        return rawRAFile;
    }

    protected final DataInput getDataInput() {
        if(seekable) {
            return rawRAFile;
        } else {
            return is;
        }
    }

    protected final int read(byte[] buf) throws IOException {
        if(seekable) {
            return rawRAFile.read(buf);
        } else {
            return is.read(buf);
        }
    }

    public boolean isSeekable() {
        return seekable;
    }

    /**
     * This function indexes the sequence file and returns to the start of the
     * first sequence when complete
     * @return
     * @throws IOException
     */
    public Map<String, Long> scan() throws IOException {
        if (!seekable) {
            throw new IOException("This seq core is not seekable");
        }

        return scanInternal();
    }

    protected abstract Map<String, Long> scanInternal() throws IOException;

    /**
     * This function leaves off at the start of the next sequence
     * @param pos
     * @return
     * @throws IOException
     */
    public Sequence parse(long pos) throws IOException {
        if (!seekable) {
            throw new IOException("This seq core is not seekable");
        }

        rawRAFile.seek(pos);
        return readNextSeq();
    }

    public void seek(long pos) throws IOException {
        if (!seekable) {
            throw new IOException("This seq core is not seekable");
        }
        rawRAFile.seek(pos);
    }

    /**
     * This function should leave off at the start of the next sequence
     * @return
     * @throws IOException
     */
    public Sequence readNextSequence() throws IOException {
        try {
            return this.readNextSeq();
        } catch (EOFException e) {
            return null;
        }
    }

    protected abstract Sequence readNextSeq() throws IOException;

    public void close() throws IOException {
        if(seekable) {
            rawRAFile.close();
        } else {
            is.close();
        }
    }

    private int read() throws IOException {
        if(seekable) {
            return rawRAFile.read();
        } else {
            return is.read();
        }
    }

    public long getPosition() {
        if(!seekable) {
            return -1;
        }

        try {
            return rawRAFile.getFilePointer();
        } catch (IOException e) {
            return -1;
        }
    }

    protected String readUntilNext(char... delims) throws IOException {
        StringBuilder ret = new StringBuilder();
        int r, index;
        char c, prev = 0;

        while ((r = read()) != -1) {
            c = (char) r;

            if ((c == '\r' && prev == '\n') || (c == '\n' && prev == '\r')) {
                prev = c;
                continue;
            }

            prev = c;

            if (c == '\r') {
                c = '\n';
            }

            prev = c;

            for (index = 0; index < delims.length; index++) {
                if (c == delims[index]) {
                    return ret.toString();
                }
            }

            ret.append(c);
        }

        if(ret.length() == 0) {
            return null;
        }

        return ret.toString();
    }

    protected String readUntilNext(String delimStr) throws IOException {
        StringBuilder ret = new StringBuilder();

        int checkIndex = 0, r;
        char c;
        char[] delim = delimStr.toCharArray();

        char prev = 0;

        int i = 0;
        while ((r = read()) != -1) {
            c = (char) r;

            if ((c == '\r' && prev == '\n') || (c == '\n' && prev == '\r')) {
                prev = c;
                continue;
            }

            prev = c;

            if (c == '\r') {
                c = '\n';
            }

            ret.append(c);

            if (c == delim[checkIndex]) {
                checkIndex++;
                if (checkIndex == delim.length) {
                    return ret.substring(0, ret.length() - delim.length);
                }
            } else {
                if (delim[0] == c) { //Weird case where a first delim might be the character that fails the current delim check
                    //Don't need to worry about checking the delim length cause if it was a match...we'd have returned when we first detected it
                    checkIndex = 1;
                } else {
                    checkIndex = 0;
                }
            }
        }

        if(ret.length() == 0) {
            return null;
        }

        return ret.toString();
    }

    protected String readSeqString(String delimStr) throws IOException {
        StringBuilder ret = new StringBuilder();

        int checkIndex = 0, r;
        char c;
        char[] delim = delimStr.toCharArray();
        int delimLength = 0;
        for(int index = 0;index < delim.length;index++) {
            c = delim[index];
            if(!(c == '\n' || c == ' ' || c == '\t')) {
                delimLength += 1;
            }
        }

        char prev = 0;

        int i = 0;
        while ((r = read()) != -1) {
            c = (char) r;

            if ((c == '\r' && prev == '\n') || (c == '\n' && prev == '\r')) {
                prev = c;
                continue;
            }

            prev = c;

            if (c == '\r') {
                c = '\n';
            }

            if(!(c == '\n' || c == ' ' || c == '\t')) {
                ret.append(c);
            }

            if (c == delim[checkIndex]) {
                checkIndex++;
                if (checkIndex == delim.length) {
                    return ret.substring(0, ret.length() - delimLength);
                }
            } else {
                if (delim[0] == c) { //Weird case where a first delim might be the character that fails the current delim check
                    //Don't need to worry about checking the delim length cause if it was a match...we'd have returned when we first detected it
                    checkIndex = 1;
                } else {
                    checkIndex = 0;
                }
            }
        }

        if(ret.length() == 0) {
            return null;
        }

        return ret.toString();
    }
}
