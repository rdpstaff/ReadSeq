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

import edu.msu.cme.rdp.readseq.MaskSequenceNotFoundException;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.readers.core.EMBLCore;
import edu.msu.cme.rdp.readseq.readers.core.FastaCore;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.readers.core.GenbankCore;
import edu.msu.cme.rdp.readseq.readers.core.SeqReaderCore;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore;
import edu.msu.cme.rdp.readseq.readers.core.STKCore;
import edu.msu.cme.rdp.readseq.readers.core.SeqReaderCore.EmptyCore;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author fishjord
 */
public class IndexedSeqReader implements SeqReader {

    public Map<String, Long> seqIndex = new LinkedHashMap();
    private boolean ignoreGaps = false;
    private boolean filterSeqs = false;
    private static ReentrantLock lock = new ReentrantLock();
    private char[] maskSeq;
    private SeqReaderCore core;
    private SequenceFormat format;

    public IndexedSeqReader(File f) throws IOException {
        setup(f, false, true, null, SeqUtils.guessFileFormat(f));
    }

    public IndexedSeqReader(File f, boolean ignoreGaps) throws IOException {
        setup(f, ignoreGaps, true, null, SeqUtils.guessFileFormat(f));
    }

    public IndexedSeqReader(File f, File indexFile) throws IOException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, false, true, readExternalIndex(f, indexFile, guess), guess);
    }

    public IndexedSeqReader(File f, File indexFile, boolean ignoreGaps) throws IOException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, ignoreGaps, true, readExternalIndex(f, indexFile, guess), guess);
    }

    public IndexedSeqReader(File f, File indexFile, String maskSeqId) throws IOException, MaskSequenceNotFoundException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, maskSeqId, false, true, readExternalIndex(f, indexFile, guess), guess);
    }

    public IndexedSeqReader(File f, File indexFile, String maskSeqId, boolean ignoreGaps) throws IOException, MaskSequenceNotFoundException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, maskSeqId, ignoreGaps, true, readExternalIndex(f, indexFile, guess), guess);
    }

    public IndexedSeqReader(File f, String maskSeqId) throws IOException, MaskSequenceNotFoundException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, maskSeqId, false, true, null, guess);
    }

    public IndexedSeqReader(File f, String maskSeqId, boolean ignoreGaps) throws IOException, MaskSequenceNotFoundException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, maskSeqId, ignoreGaps, true, null, guess);
    }

    public IndexedSeqReader(File f, boolean ignoreGaps, boolean filterSeqs) throws IOException {
        setup(f, ignoreGaps, filterSeqs, null, SeqUtils.guessFileFormat(f));
    }

    public IndexedSeqReader(File f, File indexFile, boolean ignoreGaps, boolean filterSeqs) throws IOException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, ignoreGaps, filterSeqs, readExternalIndex(f, indexFile, guess), guess);
    }

    public IndexedSeqReader(File f, File indexFile, String maskSeqId, boolean ignoreGaps, boolean filterSeqs) throws IOException, MaskSequenceNotFoundException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, maskSeqId, ignoreGaps, filterSeqs, readExternalIndex(f, indexFile, guess), guess);
    }

    public IndexedSeqReader(File f, String maskSeqId, boolean ignoreGaps, boolean filterSeqs) throws IOException, MaskSequenceNotFoundException {
        SequenceFormat guess = SeqUtils.guessFileFormat(f);
        setup(f, maskSeqId, ignoreGaps, filterSeqs, null, guess);
    }

    private void setup(File seqFile, String maskSeqId, boolean ignoreGaps, boolean filterSeqs, Map<String, Long> suppliedSeqIndex, SequenceFormat format) throws IOException, MaskSequenceNotFoundException {
        setup(seqFile, ignoreGaps, filterSeqs, suppliedSeqIndex, format);
        if (!seqIndex.containsKey(maskSeqId)) {
            throw new MaskSequenceNotFoundException(maskSeqId);
        }

        Sequence seq = core.parse(seqIndex.get(maskSeqId));
        maskSeq = SeqUtils.filterSeqString(seq.getSeqString(), false).toCharArray();

        core.seek(seqIndex.get(getSeqIds().get(0)));
    }

    private void setup(File seqFile, boolean ignoreGaps, boolean filterSeqs, Map<String, Long> suppliedSeqIndex, SequenceFormat format) throws IOException {
        if (format == SequenceFormat.FASTA) {
            core = new FastaCore(seqFile);
        } else if (format == SequenceFormat.FASTQ) {
            core = new FastqCore(seqFile);
        } else if (format == SequenceFormat.SFF) {
            core = new SFFCore(seqFile);
        } else if (format == SequenceFormat.STK) {
            core = new STKCore(seqFile);
        } else if (format == SequenceFormat.EMBL) {
            core = new EMBLCore(seqFile);
        } else if (format == SequenceFormat.GENBANK) {
            core = new GenbankCore(seqFile);
        } else if (format == SequenceFormat.EMPTY) {
            core = new EmptyCore(seqFile);
        } else {
            throw new IOException("Unsupported file format " + format);
        }

        this.ignoreGaps = ignoreGaps;
        this.filterSeqs = filterSeqs;
        this.format = format;

        if (suppliedSeqIndex == null) {
            suppliedSeqIndex = core.scan();
        }

        this.seqIndex = suppliedSeqIndex;
    }

    public SequenceFormat getFormat() {
        return format;
    }

    public static List<Sequence> readFully(File seqFile, String maskSeq, boolean ignoreGaps, boolean filterSeqs) throws IOException, MaskSequenceNotFoundException {
        List<Sequence> ret = new ArrayList();

        IndexedSeqReader reader = new IndexedSeqReader(seqFile, maskSeq, ignoreGaps, filterSeqs);
        try {
            for (String seqid : reader.getSeqIdSet()) {
                if (seqid.startsWith("#")) {
                    continue;
                }

                ret.add(reader.readSeq(seqid));
            }

            return ret;
        } finally {
            reader.close();
        }
    }

    private static Map<String, Long> readExternalIndex(File seqFile, File indexFile, SequenceFormat expectedFormat) throws IOException {
        DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));

        if (reader.readLong() != seqFile.length()) {
            throw new IOException("Sequence file's size has changed since it was index");
        }

        SequenceFormat format = SequenceFormat.valueOf(reader.readUTF());

        if (format != expectedFormat) {
            throw new IllegalArgumentException("Sequence file format " + format + " is not of the expected format " + expectedFormat);
        }

        Map<String, Long> ret = new HashMap();
        while (true) {
            try {

                String seqid = reader.readUTF();
                Long index = reader.readLong();

                ret.put(seqid, index);
            } catch (EOFException e) {
                break;
            }
        }

        reader.close();
        return ret;
    }

    public static void indexSeqFile(File seqFile, File indexFile) throws IOException {
        indexSeqFile(seqFile, indexFile, SeqUtils.guessFileFormat(seqFile));
    }

    public static void indexSeqFile(File seqFile, File indexFile, SequenceFormat format) throws IOException {
        FastaCore core;
        if (format == SequenceFormat.FASTA) {
            core = new FastaCore(seqFile);
        } else {
            throw new IllegalArgumentException("Format " + format + "is not supported");
        }

        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
        long startTime = System.currentTimeMillis();
        int written = core.scanToStream(out);
        System.out.println("Indexed " + written + " sequences in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void close() throws IOException {
        core.close();
    }

    public List<String> getSeqIds() {
        List<String> ret = new ArrayList(seqIndex.keySet());

        //XXX Hack to prevent meta-sequences from being returned with the list
        Set<String> remove = new HashSet(Arrays.asList("struct", "structure", "mask"));
        for (String seqid : ret) {
            if (seqid.startsWith("#")) {
                remove.add(seqid);
            }
        }

        ret.removeAll(remove);

        return ret;
    }

    public Set<String> getSeqIdSet() {
        return Collections.unmodifiableSet(seqIndex.keySet());
    }

    private Sequence readFrom(long location) throws IOException {
        Sequence seq = core.parse(location);

        try {
            if (filterSeqs && seq.getSeqName() != null) {
                seq.setSeqString(filterSequence(seq.getSeqString()));
            }

            return seq;
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter sequence " + seq.getSeqName(), e);
        }
    }

    public Sequence readSeq(String id) throws IOException {
        try {
            lock.lock();
            if (seqIndex.containsKey(id)) {
                return readFrom(seqIndex.get(id));
            } else {
                throw new IOException("Sequence " + id + " not found in file");
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Sequence> readSeqs(Collection<String> ids) throws IOException {
        List<Sequence> ret = new ArrayList();

        for (String id : ids) {
            ret.add(readSeq(id));
        }

        return ret;
    }

    public Sequence readNextSequence() throws IOException {
        try {
            lock.lock();
            Sequence seq = core.readNextSequence();

            try {
                if (filterSeqs && seq != null && seq.getSeqName() != null) {
                    seq.setSeqString(filterSequence(seq.getSeqString()));
                }

                return seq;
            } catch (Exception e) {
                throw new RuntimeException("Failed to filter sequence " + seq.getSeqName(), e);
            }
        } finally {
            lock.unlock();
        }
    }

    protected String filterSequence(String seq) {

        seq = SeqUtils.filterSeqString(seq, ignoreGaps);
        if (maskSeq != null) {
            seq = SeqUtils.getMaskedSeq(seq, maskSeq);
        }

        return seq;
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        IndexedSeqReader reader = new IndexedSeqReader(new File("/scratch/fishjord/woojun_unifrac_run3/run3.fasta"));
        long stopTime = System.currentTimeMillis();

        System.out.println("Time to index file: " + (stopTime - startTime) + "ms");

        System.out.println("Mask seq index: " + reader.seqIndex.get("#=GC_SS_cons"));
        System.out.println("Mask seq: " + reader.readSeq("#=GC_SS_cons").getSeqString());
        System.out.println("Total seq count: " + reader.getSeqIds().size());
    }
}
