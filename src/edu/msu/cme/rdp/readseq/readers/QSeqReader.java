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

import edu.msu.cme.rdp.readseq.QSequence;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fishjord
 */
public class QSeqReader implements SeqReader {

    private List<String> seqids;
    private int seqIndex = 0;
    private IndexedSeqReader seqReader;
    private IndexedSeqReader qualReader = null;
    private SequenceFormat format;
    private boolean validate;

    public QSeqReader(File seqFile, File qualFile) throws IOException {
        this(seqFile, qualFile, true);
    }

    public QSeqReader(File seqFile, File qualFile, boolean validate) throws IOException {
        format = SeqUtils.guessFileFormat(seqFile);
        SequenceFormat qualFormat = SeqUtils.guessFileFormat(qualFile);

        if (format == SequenceFormat.FASTA && qualFormat == SequenceFormat.FASTA) {
            seqReader = new IndexedSeqReader(seqFile);
            qualReader = new IndexedSeqReader(qualFile, false, false);

            seqids = seqReader.getSeqIds();

            if (validate) {
                Set<String> qualIds = new HashSet(qualReader.getSeqIdSet());
                Set<String> tmpSeqIds = new HashSet(seqids);

                tmpSeqIds.removeAll(qualIds);
                qualIds.removeAll(seqids);

                if (!qualIds.isEmpty() || !tmpSeqIds.isEmpty()) {
                    throw new IOException("Quality and sequence files don't contain the same sequences, non-matched qual-ids=" + qualIds + " non-matched seqids=" + tmpSeqIds);
                }
            }

        } else {
            throw new IOException("Both quality and sequence file must be in fasta format for reading from seperate files [sequence file format detected as " + format + " qual file format detected as " + qualFormat);
        }

        this.validate = validate;
    }

    public SequenceFormat getFormat() {
        return format;
    }

    public Sequence readNextSequence() throws IOException {
        if (seqIndex == seqids.size()) {
            return null;
        }

        return readSeq(seqids.get(seqIndex++));
    }

    public List<String> getSeqIds() {
        return Collections.unmodifiableList(seqids);
    }

    public byte[] readQual(String seqid) throws IOException {
        if(qualReader == null) {
            throw new IOException("No quality reader associated");
        }

        Sequence qualSeq = qualReader.readSeq(seqid);

        String[] lexemes = qualSeq.getSeqString().trim().split("\\s+");

        byte[] qual = new byte[lexemes.length];
        for (int index = 0; index < lexemes.length; index++) {
            qual[index] = Byte.valueOf(lexemes[index]);
        }

        return qual;
    }

    public Sequence readPlainSequence(String seqid) throws IOException {
        return seqReader.readSeq(seqid);
    }

    public QSequence readSeq(String seqid) throws IOException {

        Sequence seq = seqReader.readSeq(seqid);
        byte[] qual = null;

        try {
            qual = readQual(seqid);
        } catch(IOException e) {
            if(validate) {
                throw e;
            }
        }

        if(validate) {
            int unalignedLength = SeqUtils.getUnalignedSeqString(seq.getSeqString()).length();

            if (unalignedLength != qual.length) {
                throw new IOException("Sequence " + seq.getSeqName() + " length [" + seq.getSeqString().length() + "] doesn't equal quality sequence length [" + qual.length + "]");
            }
        } else if(qual == null) {
            qual = new byte[SeqUtils.getUnalignedSeqString(seq.getSeqString()).length()];
        }

        return new QSequence(seq, qual);
    }

    public void close() throws IOException {
        seqReader.close();
        if (qualReader != null) {
            qualReader.close();
        }
    }
}
