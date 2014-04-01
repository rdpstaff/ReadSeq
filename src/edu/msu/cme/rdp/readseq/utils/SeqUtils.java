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

import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.SequenceType;
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.core.EMBLCore;
import edu.msu.cme.rdp.readseq.readers.core.FastaCore;
import edu.msu.cme.rdp.readseq.readers.core.FastqCore;
import edu.msu.cme.rdp.readseq.readers.core.GenbankCore;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore;
import edu.msu.cme.rdp.readseq.readers.core.STKCore;
import edu.msu.cme.rdp.readseq.readers.core.SeqReaderCore;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author fishjord
 */
public class SeqUtils {

    private static final List<Character> gapChars = Arrays.asList('.', '-', '~');
    public static final int SFF_MAGIC_NUMBER = 779314790;
    public static final int SRF_MAGIC_NUMBER = 1397969478;
    public static final int STK_MAGIC_NUMBER = 589321044;
    public static final int GENBANK_MAGIC_NUMBER = 1280262997;
    public static final int EMBL_MAGIC_NUMBER = 1229201440;
    public static final Set<Character> RNAAlphabet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new Character[]{
        'A', 'a', 'C', 'c', 'G', 'g', 'U', 'u', 'T', 't', 'M', 'm', 'R', 'r', 'W', 'w', 'S', 's', 'Y', 'y', 'K', 'k', 'B', 'b', 'H', 'h', 'V', 'v', 'D', 'd', 'N', 'n', 'I', 'i'
    })));
    public static final Set<Character> proteinAlphabet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new Character[]{
        'A', 'a', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'G', 'g', 'H', 'h', 'I', 'i', 'K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'P', 'p', 'Q', 'q', 'R', 'r', 'S', 's', 'T', 't', 'V', 'v', 'W', 'w', 'Y', 'y', 'Z', 'z', 'B', 'b', 'X', 'x', 'u', 'U', 'j', 'J', '*'
    })));
    public static final Set<Character> rrnaAmbiguity = Collections.unmodifiableSet(new HashSet(Arrays.asList(new Character[]{
        'y', 'Y',
        'r', 'R',
        'w', 'W',
        's', 'S',
        'k', 'K',
        'm', 'M',
        'd', 'D',
        'v', 'V',
        'h', 'H',
        'b', 'B',
        'x', 'X',
        'n', 'N'
    })));
    public static final Set<Character> proteinAmbiguity = Collections.unmodifiableSet(new HashSet(Arrays.asList(new Character[]{
        'x', 'X', '*'
    })));
    public static final byte[] IUPAC = new byte[127];
    public static final byte NON_COMPAREABLE = Byte.parseByte("0110" + "0000", 2);
    public static final byte A = (byte) (Byte.parseByte("0000" + "0001", 2));
    public static final byte C = (byte) (Byte.parseByte("0000" + "0010", 2));
    public static final byte G = (byte) (Byte.parseByte("0000" + "0100", 2));
    public static final byte U = (byte) (Byte.parseByte("0000" + "1000", 2));
    public static final byte AMB = Byte.parseByte("0010" + "0000", 2);
    public static final byte GAP = Byte.parseByte("0100" + "0000", 2);
    public static final byte Y = (byte) (C | U | AMB);
    public static final byte R = (byte) (A | G | AMB);
    public static final byte W = (byte) (A | U | AMB);
    public static final byte S = (byte) (G | C | AMB);
    public static final byte K = (byte) (G | U | AMB);
    public static final byte M = (byte) (A | C | AMB);
    public static final byte D = (byte) (A | G | U | AMB);
    public static final byte V = (byte) (A | G | C | AMB);
    public static final byte H = (byte) (A | C | U | AMB);
    public static final byte B = (byte) (G | C | U | AMB);
    public static final byte N = (byte) (A | C | G | U | AMB);

    static {
        for (int i = 0; i < IUPAC.length; ++i) {
            IUPAC[i] = -1;
        }

        IUPAC['A'] = IUPAC['a'] = A;
        IUPAC['C'] = IUPAC['c'] = C;
        IUPAC['G'] = IUPAC['g'] = G;
        IUPAC['U'] = IUPAC['u'] = IUPAC['T'] = IUPAC['t'] = U;
        IUPAC['M'] = M;
        IUPAC['R'] = R;
        IUPAC['W'] = W;
        IUPAC['S'] = S;
        IUPAC['Y'] = Y;
        IUPAC['K'] = K;
        IUPAC['V'] = V;
        IUPAC['H'] = H;
        IUPAC['D'] = D;
        IUPAC['B'] = B;
        IUPAC['-'] = GAP;
        IUPAC['.'] = GAP;
        IUPAC['~'] = GAP;
        IUPAC['N'] = N;
        IUPAC['X'] = N;
        IUPAC['m'] = IUPAC['M'];
        IUPAC['r'] = IUPAC['R'];
        IUPAC['w'] = IUPAC['W'];
        IUPAC['s'] = IUPAC['S'];
        IUPAC['y'] = IUPAC['Y'];
        IUPAC['k'] = IUPAC['K'];
        IUPAC['v'] = IUPAC['V'];
        IUPAC['h'] = IUPAC['H'];
        IUPAC['d'] = IUPAC['D'];
        IUPAC['b'] = IUPAC['B'];
        IUPAC['n'] = IUPAC['N'];
        IUPAC['x'] = IUPAC['X'];
    }

    public static byte[] toBytes(String seq) {
        byte[] seqBytes = new byte[seq.length()];
        for (int col = 0; col < seq.length(); col++) {
            seqBytes[col] = SeqUtils.IUPAC[seq.charAt(col)];
        }
        return seqBytes;
    }

    public static String fromBytes(byte[] bases) {
        StringBuilder ret = new StringBuilder();

        for (byte base : bases) {

            if (base == A) {
                ret.append('A');
            } else if (base == C) {
                ret.append('C');
            } else if (base == G) {
                ret.append('G');
            } else if (base == U) {
                ret.append('T');
            } else if (base == GAP) {
                ret.append('-');
            } else if (base == Y) {
                ret.append('Y');
            } else if (base == R) {
                ret.append('R');
            } else if (base == W) {
                ret.append('W');
            } else if (base == S) {
                ret.append('S');
            } else if (base == K) {
                ret.append('K');
            } else if (base == M) {
                ret.append('M');
            } else if (base == D) {
                ret.append('D');
            } else if (base == V) {
                ret.append('V');
            } else if (base == H) {
                ret.append('H');
            } else if (base == B) {
                ret.append('B');
            } else if (base == N) {
                ret.append('N');
            } else {
                ret.append('?');
            }
        }

        return ret.toString();
    }

    public static Sequence getMaskedSeq(Sequence seq, char[] maskSeq) {
        return new Sequence(seq.getSeqName(), seq.getDesc(), getMaskedSeq(seq.getSeqString(), maskSeq));
    }

    public static String getMaskedSeq(String bases, char[] maskSeq) {
        StringBuilder ret = new StringBuilder();

        if (bases.length() != maskSeq.length) {
            System.err.println("\"" + bases + "\"");
            StringBuffer b = new StringBuffer();
            for (char c : maskSeq) {
                b.append(c);
            }
            System.err.println("\"" + b + "\"");
            throw new IllegalArgumentException("Seq string[" + bases.length() + "] and mask string[" + maskSeq.length + "] are different lengths");
        }

        for (int index = 0; index < maskSeq.length; index++) {
            if (maskSeq[index] != '0' && maskSeq[index] != '.' && maskSeq[index] != '~') {
                ret.append(bases.charAt(index));
            }
        }

        return ret.toString();
    }

    public static Sequence getMaskedBySeqString(Sequence seq) {
        return new Sequence(seq.getSeqName(), seq.getDesc(), getMaskedBySeqString(seq.getSeqString()));
    }

    public static String getMaskedBySeqString(String seqString) {
        StringBuilder ret = new StringBuilder();
        char[] bases = seqString.toCharArray();

        for (int index = 0; index < bases.length; index++) {
            if (bases[index] == '-' || Character.isUpperCase(bases[index])) {
                ret.append(bases[index]);
            }
        }

        return ret.toString();
    }

    /**
     * Filters out gap characters if ignoreGaps is true and removes all white
     * space
     *
     * @param seqString
     * @param ignoreGaps
     * @return
     */
    public static String filterSeqString(String seqString, boolean ignoreGaps) {

        StringBuilder ret = new StringBuilder();
        for (char b : seqString.toCharArray()) {
            if (gapChars.contains(b) && ignoreGaps) {
                continue;
            } else if (!Character.isWhitespace(b)) {
                ret.append(b);
            }
        }

        return ret.toString();
    }

    public static SeqReaderCore getSeqReaderCore(File f) throws IOException {
        SequenceFormat format = SeqUtils.guessFileFormat(f);
        SeqReaderCore core;
        if (format == SequenceFormat.GZIP) {
            return getSeqReaderCore(new GZIPInputStream(new FileInputStream(f)));
        } else if (format == SequenceFormat.FASTA) {
            core = new FastaCore(f);
        } else if (format == SequenceFormat.FASTQ) {
            core = new FastqCore(f);
        } else if (format == SequenceFormat.SFF) {
            core = new SFFCore(f);
        } else if (format == SequenceFormat.STK) {
            core = new STKCore(f);
        } else if (format == SequenceFormat.EMBL) {
            core = new EMBLCore(f);
        } else if (format == SequenceFormat.GENBANK) {
            core = new GenbankCore(f);
        } else if (format == SequenceFormat.EMPTY) {
            core = new SeqReaderCore.EmptyCore(f);
        } else {
            throw new IOException("Unable to process file format " + format);
        }

        return core;
    }

    public static SeqReaderCore getSeqReaderCore(InputStream is) throws IOException {
        BufferedInputStream in;
        if (is instanceof BufferedInputStream) {
            in = (BufferedInputStream) is;
        } else {
            in = new BufferedInputStream(is);
        }

        SequenceFormat format = SeqUtils.guessSequenceFormat(in);
        SeqReaderCore core;

        if (format == SequenceFormat.GZIP) {
            return getSeqReaderCore(new GZIPInputStream(is));
        } else if (format == SequenceFormat.FASTA) {
            core = new FastaCore(in);
        } else if (format == SequenceFormat.FASTQ) {
            core = new FastqCore(in);
        } else if (format == SequenceFormat.SFF) {
            core = new SFFCore(in);
        } else if (format == SequenceFormat.EMBL) {
            core = new EMBLCore(in);
        } else if (format == SequenceFormat.GENBANK) {
            core = new GenbankCore(in);
        } else if (format == SequenceFormat.EMPTY) {
            core = new SeqReaderCore.EmptyCore(in);
        } else {
            throw new IOException("Unable to process file format " + format);
        }

        return core;
    }

    /**
     * Guess the file format for the given file
     *
     * @param f
     * @return
     */
    public static SequenceFormat guessFileFormat(File f) throws IOException {

        if (!f.exists()) {
            throw new FileNotFoundException(f.getPath());
        }

        if (f.length() == 0) {
            return SequenceFormat.EMPTY;
        }

        InputStream is = new FileInputStream(f);
        SequenceFormat format = guessSequenceFormat(new BufferedInputStream(is));
        is.close();

        return format;
    }

    public static SequenceFormat guessSequenceFormat(InputStream in) throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Cannot test sequence format of an input stream that doesn't support marking");
        }

        DataInputStream is = new DataInputStream(in);

        is.mark(4);

        try {
            int magicNumber = is.readInt();
            switch (magicNumber) {
                case SFF_MAGIC_NUMBER:
                    return SequenceFormat.SFF;
                case SRF_MAGIC_NUMBER:
                    return SequenceFormat.SFF;
                case STK_MAGIC_NUMBER:
                    return SequenceFormat.STK;
                case GENBANK_MAGIC_NUMBER:
                    return SequenceFormat.GENBANK;
                case EMBL_MAGIC_NUMBER:
                    return SequenceFormat.EMBL;
            }

            magicNumber >>= 16;

            if (((magicNumber >> 8) | ((magicNumber & 0xff) << 8)) == GZIPInputStream.GZIP_MAGIC) {
                return SequenceFormat.GZIP;
            }

            magicNumber >>= 8; //Shift us to the left most word hack to detect fasta/fastq that don't have a 4 byte magic number
            //Remember, an ascii character is 7 bits

            if (magicNumber == '>') {
                return SequenceFormat.FASTA;
            } else if (magicNumber == '@') {
                return SequenceFormat.FASTQ;
            }


            return SequenceFormat.UNKNOWN;
        } catch (EOFException e) {

            return SequenceFormat.EMPTY;
        } finally {
            is.reset();
        }
    }

    public static SequenceType guessSequenceType(File f) throws IOException {
        SequenceReader reader = new SequenceReader(f);
        Sequence seq = reader.readNextSequence();
        reader.close();

        return guessSequenceType(seq);
    }

    public static SequenceType guessSequenceType(Sequence seq) {

        boolean canBeNucl = true;
        boolean canBeProt = true;
        for (char c : getUnalignedSeqString(seq.getSeqString()).toCharArray()) {
            if (!RNAAlphabet.contains(c)) {
                canBeNucl = false;
            }

            if (!proteinAlphabet.contains(c)) {
                canBeProt = false;
            }
        }

        if (canBeNucl) {
            return SequenceType.Nucleotide;
        } else if (canBeProt) {
            return SequenceType.Protein;
        } else {
            return SequenceType.Unknown;
        }
    }

    /**
     * Find the start and end points for an aligned sequence (first location of
     * a base) This method assumes that all non-model position gaps are the '.'
     * character
     *
     * @param seqString Sequence to test
     * @return 2 element array with the first element as the start and the
     * second element as the end (in model positions)
     */
    public static int[] getSeqEndPoints(String seqString) {
        SequenceStats stats = new SequenceStats(seqString);

        return new int[]{stats.getStart(), stats.getEnd()};
    }

    /**
     * Strips all gap characters ('.', '-', '~') and white space then lower
     * cases the supplied string to get the unaligned sequence
     *
     * @param seqString
     * @return
     */
    public static String getUnalignedSeqString(String seqString) {
        return seqString.toLowerCase().replaceAll("[\\-\\.\\~\\s]", "");
    }

    /**
     * Strips all gap characters ('.', '-', '~') and white space then lower
     * cases the supplied string to get the unaligned sequence
     *
     * @param seqString
     * @return
     */
    public static Sequence getUnalignedSeq(Sequence seq) {
        return new Sequence(seq.getSeqName(), seq.getDesc(), getUnalignedSeqString(seq.getSeqString()));
    }

    /**
     *
     * Generates a mapping of model positions to reference sequence positions
     *
     * Model position start = 1 Ref sequence start = 1
     *
     * @param referenceSeq
     * @return
     */
    public static Map<Integer, Integer> generateReferenceSeqMapping(String referenceSeq) {
        Map<Integer, Integer> refMapping = new HashMap();

        int modelPosition = 0;
        int refSeqPosition = 0;

        char[] bases = referenceSeq.toCharArray();
        for (int index = 0; index < bases.length; index++) {
            char base = bases[index];
            if (Character.isLetter(base)) {
                refSeqPosition++;
            }
            if (Character.isUpperCase(base) || base == '-') {
                modelPosition++;
                refMapping.put(modelPosition, refSeqPosition);
            }
        }


        return refMapping;
    }

    public static int countModelPositions(String seqString) {
        int count = 0;

        for (char b : seqString.toCharArray()) {
            if (Character.isUpperCase(b) || b == '-') {
                count++;
            }
        }

        return count;
    }

    /**
     * Turns an array of bytes in to a string of the numeric values they
     * represent
     *
     * @param qual
     * @return
     */
    public static String translateQualString(byte[] qual) {
        StringBuilder ret = new StringBuilder();
        for (byte c : qual) {
            ret.append(c).append("  ");
        }

        return ret.toString();
    }

    public static int countSequences(List<File> seqFiles) throws IOException {
        int seqCount = 0;
        Sequence seq;
        SeqReader reader;

        for (File file : seqFiles) {
            reader = null;
            try {
                reader = new SequenceReader(file);
                while ((seq = reader.readNextSequence()) != null) {
                    if (!seq.getSeqName().startsWith("#")) {
                        seqCount++;
                    }
                }
            } catch (IOException e) {
                throw new IOException("Invalid sequence found in file " + file.getName());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        return seqCount;
    }

    public static void main(String[] args) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(">   ".getBytes()));
        int fasta = dis.readInt();

        System.out.println(SeqUtils.guessFileFormat(new File("test/test.fa.gz")));

        //System.out.println(fasta & 0xf0000000);
        System.out.println(fasta >> 24);
        System.out.println((int) '>');

        dis = new DataInputStream(new ByteArrayInputStream("@   ".getBytes()));
        int fastq = dis.readInt();

        //System.out.println(fastq & 0xf0000000);
        System.out.println(fastq >> 24);
        System.out.println((int) '@');

        dis = new DataInputStream(new ByteArrayInputStream(">".getBytes()));
        System.out.println(dis.read());
    }
}
