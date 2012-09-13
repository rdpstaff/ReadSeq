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

package edu.msu.cme.rdp.readseq.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author fishjord
 */
public class BufferedLineReader {

    private BufferedReader reader;
    private String peekBuffer = null;
    private int lineno = 0;

    public BufferedLineReader(InputStream is) {
        reader = new BufferedReader(new InputStreamReader(is));
    }

    public BufferedLineReader(File f) throws IOException {
        reader = new BufferedReader(new FileReader(f));
    }

    public BufferedLineReader(Reader reader) {
        if(reader instanceof BufferedReader)
            this.reader = (BufferedReader)reader;
        else
            this.reader = new BufferedReader(reader);
    }

    public String readLine() throws IOException {
        String ret;
        if(peekBuffer != null) {
            ret = peekBuffer;
            peekBuffer = null;
        } else
            ret = reader.readLine();

        if(ret != null)
            lineno++;

        return ret;
    }

    public String peek() throws IOException {
        if(peekBuffer == null) {
            peekBuffer = readLine();
        }
        
        return peekBuffer;
    }

    public int getLineno() {
        return lineno;
    }

    public void close() throws IOException {
        reader.close();
    }
}
