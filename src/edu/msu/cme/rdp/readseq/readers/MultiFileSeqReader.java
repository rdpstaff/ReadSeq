/*
 * Copyright (C) 2013 Jordan Fish <fishjord at msu.edu>
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

import edu.msu.cme.rdp.readseq.SequenceFormat;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jordan Fish <fishjord at msu.edu>
 */
public class MultiFileSeqReader implements SeqReader {
    private final List<File> inputFiles;
    private SeqReader currReader;
    private int seqFileIndex;

    public MultiFileSeqReader(List<File> inputFiles) throws IOException {
        this.inputFiles = inputFiles;
        reset();
    }

    public Sequence readNextSequence() throws IOException {
        if(currReader == null && !nextReader()) {
            return null;
        }

        Sequence ret;
        do {
            ret = currReader.readNextSequence();
        } while(ret == null && nextReader());

        return ret;
    }

    private boolean nextReader() throws IOException {
        close();

        if(seqFileIndex >= inputFiles.size()) {
            return false;
        }

        currReader = new SequenceReader(inputFiles.get(seqFileIndex++));
        return true;
    }

    public SequenceFormat getFormat() {
        if(currReader != null) {
            return currReader.getFormat();
        } else {
            return SequenceFormat.UNKNOWN;
        }
    }

    public void close() throws IOException {
        if(currReader != null) {
            currReader.close();
            currReader = null;
        }
    }

    public final void reset() throws IOException {
        close();
        seqFileIndex = 0;
        nextReader();
    }

}
