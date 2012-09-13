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

import edu.msu.cme.rdp.readseq.QSequence;
import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.IndexedSeqReader;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.utils.BarcodeUtils.BarcodeInvalidException;
import edu.msu.cme.rdp.readseq.writers.FastqWriter;
import edu.msu.cme.rdp.readseq.writers.SequenceWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class BarcodeSorter {

    public static final String NoTag = "NoTag";

    private static File getFastaFile(File workDir, String tag, String suffix) throws IOException {
        return new File(workDir, tag + suffix);
    }

    public static void sort(File seqFile, File tagFile) throws IOException, BarcodeInvalidException {
        sortWithQual(seqFile, null, tagFile, new File("."));
    }

    public static void sort(File seqFile, File tagFile, File workDir) throws IOException, BarcodeInvalidException {
        sortWithQual(seqFile, null, tagFile, workDir);
    }

    public static void sortWithQual(File seqFile, File qualFile, File tagFile) throws IOException, BarcodeInvalidException {
        sortWithQual(seqFile, qualFile, tagFile, new File("."));
    }

    /**
     * Sorts the sequence file (and optional qual file) by barcodes
     *
     * This method will NOT complain if a sequence in the fasta file doesn't
     * have a qual file entry
     *
     * @param seqFile
     * @param qualFile
     * @param tagFile
     * @param workDir
     * @param makeDirForBarcode
     * @throws IOException
     * @throws
     * edu.msu.cme.rdp.readseq.utils.BarcodeUtils.BarcodeInvalidException
     */
    public static void sortWithQual(File seqFile, File qualFile, File tagFile, File workDir) throws IOException, BarcodeInvalidException {
        SequenceFormat format = SeqUtils.guessFileFormat(seqFile);

        if (format != SequenceFormat.FASTA && qualFile != null && qualFile.exists()) {
            throw new IOException("Cannot provide an external quality file with non-fasta files");
        }

        boolean hasQuality = (qualFile != null && qualFile.exists()) || format == SequenceFormat.FASTQ || format == SequenceFormat.SFF;

        SequenceReader seqReader = new SequenceReader(seqFile);
        IndexedSeqReader qualReader = null;

        if (qualFile != null && qualFile.exists()) {
            qualReader = new IndexedSeqReader(qualFile, true, false);
        }

        Map<String, SequenceWriter> tagToStream = new HashMap();
        Map<File, SequenceWriter> fileNameToStream = new HashMap();

        Map<String, String> barcodeMap = BarcodeUtils.readBarcodeFile(tagFile);
        barcodeMap.put(NoTag, NoTag);

        for (String barcode : barcodeMap.keySet()) {
            File fileName = null;
            SequenceWriter stream = null;
            if (hasQuality) {
                fileName = getFastaFile(workDir, barcodeMap.get(barcode), ".fastq");
            } else {
                fileName = getFastaFile(workDir, barcodeMap.get(barcode), ".fasta");
            }

            stream = fileNameToStream.get(fileName);
            if (stream == null) {
                if (hasQuality) {
                    stream = new FastqWriter(fileName, FastqCore.Phred33QualFunction);
                } else {
                    stream = new FastaWriter(fileName);
                }

                fileNameToStream.put(fileName, stream);
            }

            tagToStream.put(barcode, stream);
        }

        Sequence seq;

        while ((seq = readNextSequence(seqReader, qualReader)) != null) {

            boolean tagMatched = false;

            for (String barcode : tagToStream.keySet()) {
                if (seq.getSeqString().toLowerCase().indexOf(barcode) == 0) {
                    tagToStream.get(barcode).writeSeq(seq);

                    tagMatched = true;
                    break;
                }
            }

            if (!tagMatched) {
                tagToStream.get(NoTag).writeSeq(seq);
            }
        }

        seqReader.close();
        if (qualReader != null) {
            qualReader.close();
        }

        for (String tag : tagToStream.keySet()) {
            tagToStream.get(tag).close();
        }

        // check if file exists but is empty
        for (String barcode : barcodeMap.keySet()) {
            String tag = barcodeMap.get(barcode);

            File f = getFastaFile(workDir, tag, ".fasta");
            File parent = f.getParentFile();
            File q = getFastaFile(workDir, tag, ".fastq");

            if (f.exists() && f.length() == 0) {
                f.delete();
                //if delete seq file, delete the qual file
                if (q != null && q.exists() && q.length() == 0) {
                    q.delete();
                }
            }
            // delete the quality file if empty
            if (q != null && q.exists() && q.length() == 0) {
                q.delete();
            }

            // delete the parent directory if empty
            if (parent.listFiles().length == 0) {
                parent.delete();
            }
        }
    }

    private static Sequence readNextSequence(SequenceReader seqReader, IndexedSeqReader qualReader) throws IOException {
        Sequence ret = null;

        if (qualReader != null) {
            Sequence next = seqReader.readNextSequence();

            if (next != null) {
                Sequence qualSeq = qualReader.readSeq(next.getSeqName());

                String[] tokens = qualSeq.getSeqString().split("\\s+");
                byte[] qual = new byte[tokens.length];
                try {
                    for (int index = 0; index < tokens.length; index++) {
                        qual[index] = Byte.parseByte(tokens[index]);
                    }
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid quality sequence for seq " + next.getSeqName());
                }

                ret = new QSequence(next.getSeqName(), next.getDesc(), next.getSeqString(), qual);
            }
        } else {
            ret = seqReader.readNextSequence();
        }

        return ret;
    }

    public static void main(String[] args) throws Exception {
        //BarcodeSorter.sort(new File(args[0]), new File(args[1]), new File(args[2]));
        BarcodeSorter.sortWithQual(new File(args[0]), new File(args[1]), new File(args[2]), new File(args[3]));
    }
}
