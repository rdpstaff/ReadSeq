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
package edu.msu.cme.rdp.readseq.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author fishjord
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

    private byte[] buf;
    private long realPos;
    private int bufPos;
    private int bufEnd;
    private boolean sync = false;

    public BufferedRandomAccessFile(File f, String mode, int bufSize) throws IOException {
        super(f, mode);
        buf = new byte[bufSize];
        fillBuffer();
    }

    @Override
    public int skipBytes(int n) throws IOException{
        return super.skipBytes(n);
    }

    @Override
    public final int read() throws IOException {
        if (bufPos >= bufEnd) {
            if (fillBuffer() < 0) {
                return -1;
            }
        }

        if (bufEnd == 0) {
            return -1;
        } else {
            return buf[bufPos++] & 0xff;
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int readCount = 0;
        for(int index = off;index < len;index++) {
            int r = this.read();
            if(r == -1) {
                break;
            }

            readCount++;
            b[index] = (byte)r;
        }
        return readCount;
    }

    @Override
    public final void write(int b) throws IOException {
        if (bufPos >= buf.length) {
            fillBuffer();
        }

        buf[bufPos++] = (byte) b;
        if (bufPos >= bufEnd) {
            bufEnd = bufPos;
        }

        sync = true;
    }

    @Override
    public long getFilePointer() throws IOException {
        long l = realPos;
        return (l - bufEnd + bufPos);
    }

    @Override
    public void seek(long pos) throws IOException {
        int n = (int) (realPos - pos);

        if (n >= 0 && n <= bufEnd) {
            bufPos = bufEnd - n;
        } else {
            sync();
            super.seek(pos);
            invalidate();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        sync();
        super.close();
    }

    private void sync() throws IOException {
        if (sync) {
            super.seek(realPos - bufEnd);
            super.write(buf, 0, bufEnd);
            sync = false;
            //invalidate();
        }
    }

    private int fillBuffer() throws IOException {
        sync();

        int n = super.read(buf, 0, buf.length);
        if (n >= 0) {
            //System.out.println("Filling buffer...read in " + n + " bytes");
            realPos += n;
            bufEnd = n;
            bufPos = 0;
        }
        return n;
    }

    private void invalidate() throws IOException {
        bufEnd = 0;
        bufPos = 0;
        realPos = super.getFilePointer();
    }

    public static void main(String[] args) throws Exception {
        try {
            File file = new File("tmp.bin");
            if (file.exists()) {
                file.delete();
            }

            BufferedRandomAccessFile f = new BufferedRandomAccessFile(file, "rw", 1);

            f.writeInt(1);
            f.writeInt(2);
            f.writeInt(3);
            f.writeInt(4);
            f.writeInt(5);

            f.seek(0);

            System.out.println(f.readInt());
            System.out.println(f.readInt());
            f.writeInt(6);
            System.out.println(f.readInt());
            System.out.println(f.readInt());

            f.seek(0);

            System.out.println(f.readInt());
            System.out.println(f.readInt());
            System.out.println(f.readInt());
            System.out.println(f.readInt());
            System.out.println(f.readInt());
            f.close();
        } catch (Exception e) {
        }
    }
}
