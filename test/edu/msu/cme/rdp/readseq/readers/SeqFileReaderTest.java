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
import java.io.FileReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import edu.msu.cme.rdp.readseq.QSequence;
import java.io.IOException;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fishjord
 */
public class SeqFileReaderTest {

    @Test
    public void testReadSTKFile() throws IOException {
        String exp1Id = "GF040U105F9RVG";
        String exp1Seq = "TRLILNSKAQTTVMDLARERGTVEDLELEDVSVEGHLGVRCAESGGPEPGVGCAGRGVITAINFLEENGAYTEdTDYVFYDVLGDVVCGGFAMPIRENKAKEIYIVT";

        String exp2Id = "GF040U105FRBRW";
        String exp2Seq = "TRLILNSKAQTTVMDLARERGTVEDLELKDVLVEGHLGVRCAESGGPEPGVGCAGRGVITAINFLEENGAYTEdTDYVFYDVLGDVVCGGFAMPIRENKAKEIYIVT";

        SequenceReader seqFileReader = new SequenceReader(new File("test/test.sto"));
        Sequence seq1 = seqFileReader.readNextSequence();

        assertEquals(exp1Id, seq1.getSeqName());
        assertEquals(exp1Seq, seq1.getSeqString());

        seq1 = seqFileReader.readNextSequence();

        assertEquals(exp2Id, seq1.getSeqName());
        assertEquals(exp2Seq, seq1.getSeqString());

        assertEquals("GF040U105FT65L", seqFileReader.readNextSequence().getSeqName());
        assertEquals("#=GC PP_cons", seqFileReader.readNextSequence().getSeqName());
        assertEquals("#=GC RF", seqFileReader.readNextSequence().getSeqName());
    }

    @Test
    public void testReadSFFFile() throws IOException {
        String exp1Id = "GF040U105F9RVG";
        String exp1Seq = "ACGAGTGCGTTGCGACCCGAAGGCCGACTCCACCCGCCTGATTCTGAACAGCAAAGCTCAAACCACGGTTATGGACCTGGCCCGTGAACGGGGAACGGTGGAGGATCTGGAACTGGAAGATGTGTCGGTGGAAGGTCATCTGGGGGTGCGCTGTGCGGAGTCCGGCGGCCCGGAACCGGGTGTGGGCTGTGCCGGACGGGGTGTGATCACCGCCATTTAACTTTCTTGAGGAAAACGGTGCTTACACCGAGGATACAGATTATGTCTTCTATGATGTTCTGGGTGATGTAGTCTGCGGGGGATTTGCCATGCCGATCCGTGAGAACAAAGCCAAGGAAATCTATATCGTCACCTCCGGCGAGATGATGGCC";

        String exp2Id = "GF040U105FRBRW";
        String exp2Seq = "ACGAGTGCGTTGCGATCCGAAGGCTGACTCCACCCGCCTGATTCTGAACAGCAAAGCTCAAACCACGGTTATGGACCTGGCTCGTGAACGGGGAACGGTGGAGGATCTGGAACTGAAAGATGTGTTGGTGGAAGGTCATCTGGGGGTGCGCTGTGCGGAGTCCGGCGGCCCGGAACCGGGTGTGGGCTGTGCCGGACGGGGTGTGATCACCGCCATTAACTTTCTTGAGGAAAACGGTGCTTACACCGAGGATACAGATTATGTCTTCTATGATGTTCTGGGTGATGTAGTCTGCGGGGGATTTGCCATGCCGATCCGTGAGAACAAAGCCAAGGAAATCTATATCGTCACCTCCGGTGAAATGATGGCC";

        SequenceReader seqFileReader = new SequenceReader(new File("test/454Reads.sff"));
        Sequence seq1 = seqFileReader.readNextSequence();

        assertEquals(exp1Id, seq1.getSeqName());
        assertEquals(exp1Seq, seq1.getSeqString());

        seq1 = seqFileReader.readNextSequence();

        assertEquals(exp2Id, seq1.getSeqName());
        assertEquals(exp2Seq, seq1.getSeqString());

    }

    @Test
    public void testReadStream() throws IOException {
        String seqs = ">test1\nacgt\n>test2\ngggg";
        String exp1Id = "test1";
        String exp1Seq = "acgt";

        String exp2Id = "test2";
        String exp2Seq = "gggg";

        SequenceReader seqFileReader = new SequenceReader(new BufferedInputStream(new ByteArrayInputStream(seqs.getBytes())));
        Sequence seq1 = seqFileReader.readNextSequence();

        assertEquals(exp1Id, seq1.getSeqName());
        assertEquals(exp1Seq, seq1.getSeqString());

        seq1 = seqFileReader.readNextSequence();

        assertEquals(exp2Id, seq1.getSeqName());
        assertEquals(exp2Seq, seq1.getSeqString());
    }

    @Test
    public void testReadFastaFile() throws IOException {
        SequenceReader reader = new SequenceReader(new File("test/test.fa"));

        Sequence s = reader.readNextSequence();

        assertEquals("test0", s.getSeqName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s.getSeqString());

        s = reader.readNextSequence();
        assertEquals("test1", s.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN-.~", s.getSeqString());

        reader.close();
    }


    @Test
    public void testReadFastqFile() throws IOException {
        String exp1Id = "001043_1783_0863";
        String exp1Seq = "CTGATCATTGGGCGTAAAGAGTGCGCAGGCGGTTTGTTAAGCGAGATGTGAAAGCCCCGGGCTCAACCTGGGAATTGCATTTCGAACTGGCGAACTAGAGTCTTGTAGAGGGGGTAGAATTCCAGGTGTAGCGGTGAAATGCGTAGAGATCTGGAGGAATACCGGTGGCGAAGGCGGCCCCCTGGACAAAGACTGACGCTCAGGCACGAAAGCGTGGGGAGCAAACAGGATTAAATACCCTCGTA";

        String exp2Id = "001051_2436_2741";
        String exp2Seq = "CATCTCACTGGGCATAAAGGGCACGCAGACGGACCGACAGGTCGTTTGTGAAAGGCGAGGGCTCAACCCTTGTTTGCGGACGAAACCGTCGGACTGGAGTACCGGAGAGGGAAGTGGAATTCCCGGTGTAGCGGTGAAATGCGTAGATATCGGGAGGAACACCAGTGGCGAAGGCGGCTTCCTGGCCGGATACTGACGCTCAAGTGCGAAAGCTGGGGGAGCGAACGGGATTAGATACCCTTGTA";

        SequenceReader seqFileReader = new SequenceReader(new File("test/test_init_v4.fastq"));
        Sequence seq1 = seqFileReader.readNextSequence();

        assertTrue(seq1 instanceof QSequence);

        assertEquals(exp1Id, seq1.getSeqName());
        assertEquals(exp1Seq, seq1.getSeqString());

        seq1 = seqFileReader.readNextSequence();

        assertEquals(exp2Id, seq1.getSeqName());
        assertEquals(exp2Seq, seq1.getSeqString());
    }

    @Test
    public void testReadEMBL() throws IOException {
        SequenceReader reader = new SequenceReader(new File("test/test.embl"));
        Sequence seq;

        String expectedSeqid;
        String expectedSeq;

        expectedSeqid = "FR838948";
        expectedSeq = "atgaaaataatcaatattggaattcttgcccatgtagacgctggaaagacgaccttgacggagagcctgctatatgccagcggagccatttcagaaccggggagcgtcgaaaaagggacaacgaggacggacaccatgtttttggagcggcagcgtgggattaccattcaagcggcagtcacttccttccagtggcacagatgtaaagttaacattgtggatacgcccggccacatggattttttggcggaggtgtaccgctctttggctgttttagatggggccatcttggtgatctccgctaaagatggcgtgcaggcccagacccgtattctgttccatgccctgcggaaaatgaacattcccaccgttatctttatcaacaagatcgaccaggctggcgttgatttgcagagcgtggttcagtctgttcgggataagctctccgccgatattatcatcaagcagacggtgtcgctgtccccggaaatagtcctggaggaaaataccgacatagaagcatgggatgcggtcatcgaaaataacgatgaattattggaaaagtatatcgcaggagaaccaatcagccgggaaaaacttgcgcgggaggaacagcagcgggttcaagacgcctccctgttcccggtctatcatggcagcgccaaaaatggccttggcattcaaccgttgatggatgcggtgacagggctgttccaaccgattggggaacaggggggcgccgccctatgcggcagcgttttcaaggttgagtacaccgattgcggccagcggcgtgtctatctacggttatacagcggaacgctgcgcctgcgggatacggtggccctggccgggagagaaaagctgaaaatcacagagatgcgtattccatccaaaggggaaattgttcggacagacaccgcttatcagggtgaaattgttatccttcccagcgacagcgtgaggttaaacgatgtattaggggaccaaacccggctccctcgtaaaaggtggcgcgaggaccccctccccatgctgcggacgacgattgcgccgaaaacggcagcgcaaagagaacggctgctggacgctcttacgcaacttgcggatactgacccgcttttgcgttgcgaagtggattccatcacccatgagatcattctttcttttttgggccgggtgcagttggaggttgtttccgctttgctgtcggaaaaatacaagcttgaaacagtggtaaaggaaccctccgtcatttatatggagcggccgctcaaagcagccagccacaccatccatatcgaggtgccgcccaacccgttttgggcatccataggactgtctgttacaccactctcgcttggctccggtgtacaatacgagagccgggtttcgctgggatacttgaaccagagttttcaaaacgctgtcagggatggtatccgttacgggctggagcagggcttgttcggctggaacgtaacggactgtaagatttgctttgaatacgggctttattacagtccggtcagcacgccggcggacttccgctcattggccccgattgtattggaacaggcattgaaggaatcggggacgcagctgctggaaccttatctctccttcatcctctatgcgccccaggaatacctttccagggcttatcatgatgcaccgaaatactgtgccaccatcgaaacggcccaggtaaaaaaggatgaagttgtctttactggcgagattcccgcccgctgtatacaggcataccgtactgatctggccttttacaccaacgggcggagcgtatgccttacagagctgaaaggatatcaggccgctgtcggtcagccggtcatccagccccgccgtccaaacagccgcctggacaaggtgcgccatatgtttcagaaggtaatgtaa";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        expectedSeqid = "FR838949";
        expectedSeq = "atgaaaataatcaatattggaattcttgcccatgtagacgctggaaagacgaccttgacggagagcctgctatatgccagcggagccatttcagaaccggggagcgtcgaaaaagggacaacgaggacggacaccatgtttttggagcggcagcgtgggattaccattcaagcggcagtcacttccttccagtggcacagatgtaaagttaacattgtggatacgcccggccacatggattttttggcggaggtgtaccgctctttggctgttttagatggggccatcttggtgatctccgctaaagatggcgtgcaggcccagacccgtattctgttccatgccctgcggaaaatgaacattcccaccgttatctttatcaacaagatcgaccaggctggcgttgatttgcagagcgtggttcagtctgttcgggataagctctccgccgatattatcatcaagcagacggtgtcgctgtccccggaaatagtcctggaggaaaataccgacatagaagcatgggatgcggtcatcgaaaataacgatgaattattggaaaagtatatcgcaggagaaccaatcagccgggaaaaacttgcgcgggaggaacagcggcgggttcaagacgcctccctgttcccggtctattatggcagcgccaaaaagggccttggcattcaaccgttgatggatgcggtgacagggctgttccaaccgattggggaacaggggagcgccgccctatgcggcagcgttttcaaggtggagtatacagattgcggccagcggcgtgtctatctacggctatacagcggaacgctgcgcctgcgggatacggtggccctggccgggagagaaaagctgaaaatcacagagatgcgtattccatccaaaggggaaattgttcggacagacaccgcttatccgggtgaaattgttatccttcccagcgacagcgtgaggttaaacgatgtattaggggacccaacccggctccctcgtaaaaggtggcgtgaggaccccctccccatgctgcggacgtcgattgcgccgaaaacggcagcgcaaagagaacggctgctggacgctcttacgcaacttgcggatactgacccgcttttgcgctgcgaggtggattccatcacccatgagatcattctttcttttttgggccgggtgcagttggaggttgtttccgctttgctgtcggaaaaatacaagcttgaaacagtggtaaaggaacccaccgtcatttatatggagcggccgctcaaagcagccagccacaccatccatatcgaggtgccgcccaacccgttttgggcatccatcggactgtctgttacaccactcccgcttggctccggtgtacaatacgagagccgggtttcgctgggatacttgaaccagagttttcaaaacgctgtcagggatggtatccgttacgggctggagcagggcttgttcggctggaacgtaacggactgtaagatttgctttgaatacgggctttattacagtccggtcagcacgccggcggacttccgctcattggccccgattgtattggaacaggcattgaaggaatcagggacgcaactgctggaaccttatctctccttcaccctctatgcgccccgggaatatctttccagggcttatcatgatgcaccgaaatactgtgccaccatcgaaacggtccaggtaaaaaaggatgaagttgtctttactggcgagattcccgcccgctgtatacaggcataccgtactgatctggccttttacaccaacgggcagagcgtatgccttacagaactgaaagggtatcaggccgctgtcggcaagccagtcatccagccccgccgtccaaacagccgcctggacaaggtgcgccatatgtttcagaaggtaatgtaa";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        assertNull(reader.readNextSequence());
    }

    @Test
    public void testReadGenbank() throws IOException {
        SequenceReader reader = new SequenceReader(new File("test/test.genbank"));
        Sequence seq;

        String expectedSeqid;
        String expectedSeq;

        expectedSeqid = "S000349357";
        expectedSeq = "gagtttgatcctggctcaggacgaacgctggcggcgtgcctaacacatgcaagccaaaggaaagtagcaatatgagtacttggcgcaagggtgcgtaatgtataggttatctacccttcggttcgggataacttcgcgaaagcgaagataataccggatattgaggagacttgaaagatttatcgccgaaggatgagcttatatcccatcaggtagttggtagggtaaaagcctaccaagcctacgacgggtagctggtctgagaggatgatcagccacactggaactgagacacggtccagactcctacgggaggcagcagtgaggaatattgctcaatgggtgaaagcctgaagcagcaacgccgcgtgaacgatgaaggtcttcggattgtaaagttcttttgcaggggacgaaaaactcgctttgcgagtctgacggtactctgcgaataagccacggctaactctgtgccagcagccgcggtgatacagaggtggcaagcgttgtccggatttactgggtgtaaagggtgcgtaggcggatttgcaagtcgggggttaaagactcttgcttaacaagagaaacgccttcgatactgcatgtctagagtgccgaagaggaaactggaatttccggtgtagcggtggaatgtgcagagatcggaaggaacaccagtggcgaaggcaggtttgtggtcggtaactgacgctgatgcacgaaagcgtggggagcaaacaggattagataccctggtagtccacgccctaaacgatggatgctagatgttggacttcggttcagtgtcgtagctaacgcagtaagcatcccacctggggagtacgcgcgcaagcgtgaaactcaaaggaattgacgggggcccgcacaagcggtggagtatgtggtttaattcgatgcaacgcgaagaaccttacctaggcttgacatgctgggtaaagcggatgaaagtccgtgtccgaaagggatccagcacaggtgctgcatggctgtcgtcagctcgtgtcgtgagatgttgggttaagtcccgcaacgagcgcaacccctattgttagttgctaccaagtaatgttgagcactctagcaagactgcctacgcaagtagagaggaaggaggggatgacgtcaagtcctcatggcccttacgcctagggccacacacgtactacaatgggtactacaatgggcgaagtcgcgagacggaggtaatcccaaaaaagcactctcagttcagatcggagtctgcaactcgactccgtgaagttggaatcgctagtaatcgcggatcagcatgccgcggtgaatacgttcccgggccttgtacacaccgcccgtcaagccatggaagtcatcagcgcccgaagacgctttgcgtttaaggcgagggtggtaactggggctaagtcgtaacaaggtaac";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        expectedSeqid = "S002189637";
        expectedSeq = "acacatgcagccaaaggaagtagcaatacgagtacttggcgtaagggtgagtaacgcataggtcatctgcccttaggttcgggataacttcgcgaaagcgaagataataccggatattgaggaaacttgaaagatttatcgcctaaggatgagcttatgtcccatcaggtagttggtagggtaaaagcctaccaagcctacgacgggtagctggtctgagaggatgatcagccacactggaactgagacacggtccagactcctacgggaggcagcagtgaggaatattgcgcaatgggtgaaagcctgacgcagcaacgccgcgtgtgcgacgaaggtcttcggattgtaaagcacttttgcaggggacgaacagcctattttatagacctgacggtaccttgcgaataagccacggctaactctgtgccagcagccgcggtgatacagaggtggcaagcgttgtccggatttactgggtataaagggtgcgtaggcggacctataagtcgagcgttaaagatcttcgcttaacgaagaaaatgcgctcgatactgttggtctagagtgttagagaggaaactggaatttccggtgtagcggtggaatgtgtagagatcggaaggaacaccagtggcgaaggcaggtttctggctaacaactgacgctgaggcacgaaagcgcgggtagcaaacaggattagataccctggtagtccgcgccctaaacgatggatgctagatgtcggacttcggttcggtgtcgcagctaacgcattaagcatcccacctgggaagtacgcgcgcaagcgtgaaactcaaaggaattgacgggggcccgcacaagcggtggagtatgtggtttaattcgatgcaacgcgaagaaccttacctaggcttgacatggtagctaaggcggatgaaagtccgcgtccgaaagggagctatcacaggtgctgcatggctgtcgtcagctcgtgtcgtgagatgttgggttaagtcccgcaacgagcgcaacccctattgttagttgctaccgggtaatgccgagcactctagcaagactgcctacgcaagtagagaggaaggaggggatgacgtcaagtcctcatggcccttacgcctagggcaacacacgtactacaatgggcattacaatgggcgaaggcgcgagccggagataatcccaaaaaagtgctctcagttcagatcggagtctgcaactcgactccgtgaagttggaatcgctagtaatcgcaggtcagcatactgcggtgaatacgttcccgggccttgtacacaccgcccgtcaagccatggaagttatcggcgcccgaagacgcattgcgt";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        assertNull(reader.readNextSequence());
    }

    @Test
    public void testReadCompressedFastaFile() throws IOException {
        SequenceReader reader = new SequenceReader(new File("test/test.fa.gz"));

        Sequence s = reader.readNextSequence();

        assertEquals("test0", s.getSeqName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s.getSeqString());

        s = reader.readNextSequence();
        assertEquals("test1", s.getSeqName());
        assertEquals("aAcCgGtTuUmMrRwWsSyYkKvVhHdDbBxXnN-.~", s.getSeqString());

        reader.close();
    }


    @Test
    public void testReadCompressedFastqFile() throws IOException {
        String exp1Id = "001043_1783_0863";
        String exp1Seq = "CTGATCATTGGGCGTAAAGAGTGCGCAGGCGGTTTGTTAAGCGAGATGTGAAAGCCCCGGGCTCAACCTGGGAATTGCATTTCGAACTGGCGAACTAGAGTCTTGTAGAGGGGGTAGAATTCCAGGTGTAGCGGTGAAATGCGTAGAGATCTGGAGGAATACCGGTGGCGAAGGCGGCCCCCTGGACAAAGACTGACGCTCAGGCACGAAAGCGTGGGGAGCAAACAGGATTAAATACCCTCGTA";

        String exp2Id = "001051_2436_2741";
        String exp2Seq = "CATCTCACTGGGCATAAAGGGCACGCAGACGGACCGACAGGTCGTTTGTGAAAGGCGAGGGCTCAACCCTTGTTTGCGGACGAAACCGTCGGACTGGAGTACCGGAGAGGGAAGTGGAATTCCCGGTGTAGCGGTGAAATGCGTAGATATCGGGAGGAACACCAGTGGCGAAGGCGGCTTCCTGGCCGGATACTGACGCTCAAGTGCGAAAGCTGGGGGAGCGAACGGGATTAGATACCCTTGTA";

        SequenceReader seqFileReader = new SequenceReader(new File("test/test_init_v4.fastq.gz"));
        Sequence seq1 = seqFileReader.readNextSequence();

        assertTrue(seq1 instanceof QSequence);

        assertEquals(exp1Id, seq1.getSeqName());
        assertEquals(exp1Seq, seq1.getSeqString());

        seq1 = seqFileReader.readNextSequence();

        assertEquals(exp2Id, seq1.getSeqName());
        assertEquals(exp2Seq, seq1.getSeqString());
    }

    @Test
    public void testReadCompressedEMBL() throws IOException {
        SequenceReader reader = new SequenceReader(new File("test/test.embl.gz"));
        Sequence seq;

        String expectedSeqid;
        String expectedSeq;

        expectedSeqid = "FR838948";
        expectedSeq = "atgaaaataatcaatattggaattcttgcccatgtagacgctggaaagacgaccttgacggagagcctgctatatgccagcggagccatttcagaaccggggagcgtcgaaaaagggacaacgaggacggacaccatgtttttggagcggcagcgtgggattaccattcaagcggcagtcacttccttccagtggcacagatgtaaagttaacattgtggatacgcccggccacatggattttttggcggaggtgtaccgctctttggctgttttagatggggccatcttggtgatctccgctaaagatggcgtgcaggcccagacccgtattctgttccatgccctgcggaaaatgaacattcccaccgttatctttatcaacaagatcgaccaggctggcgttgatttgcagagcgtggttcagtctgttcgggataagctctccgccgatattatcatcaagcagacggtgtcgctgtccccggaaatagtcctggaggaaaataccgacatagaagcatgggatgcggtcatcgaaaataacgatgaattattggaaaagtatatcgcaggagaaccaatcagccgggaaaaacttgcgcgggaggaacagcagcgggttcaagacgcctccctgttcccggtctatcatggcagcgccaaaaatggccttggcattcaaccgttgatggatgcggtgacagggctgttccaaccgattggggaacaggggggcgccgccctatgcggcagcgttttcaaggttgagtacaccgattgcggccagcggcgtgtctatctacggttatacagcggaacgctgcgcctgcgggatacggtggccctggccgggagagaaaagctgaaaatcacagagatgcgtattccatccaaaggggaaattgttcggacagacaccgcttatcagggtgaaattgttatccttcccagcgacagcgtgaggttaaacgatgtattaggggaccaaacccggctccctcgtaaaaggtggcgcgaggaccccctccccatgctgcggacgacgattgcgccgaaaacggcagcgcaaagagaacggctgctggacgctcttacgcaacttgcggatactgacccgcttttgcgttgcgaagtggattccatcacccatgagatcattctttcttttttgggccgggtgcagttggaggttgtttccgctttgctgtcggaaaaatacaagcttgaaacagtggtaaaggaaccctccgtcatttatatggagcggccgctcaaagcagccagccacaccatccatatcgaggtgccgcccaacccgttttgggcatccataggactgtctgttacaccactctcgcttggctccggtgtacaatacgagagccgggtttcgctgggatacttgaaccagagttttcaaaacgctgtcagggatggtatccgttacgggctggagcagggcttgttcggctggaacgtaacggactgtaagatttgctttgaatacgggctttattacagtccggtcagcacgccggcggacttccgctcattggccccgattgtattggaacaggcattgaaggaatcggggacgcagctgctggaaccttatctctccttcatcctctatgcgccccaggaatacctttccagggcttatcatgatgcaccgaaatactgtgccaccatcgaaacggcccaggtaaaaaaggatgaagttgtctttactggcgagattcccgcccgctgtatacaggcataccgtactgatctggccttttacaccaacgggcggagcgtatgccttacagagctgaaaggatatcaggccgctgtcggtcagccggtcatccagccccgccgtccaaacagccgcctggacaaggtgcgccatatgtttcagaaggtaatgtaa";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        expectedSeqid = "FR838949";
        expectedSeq = "atgaaaataatcaatattggaattcttgcccatgtagacgctggaaagacgaccttgacggagagcctgctatatgccagcggagccatttcagaaccggggagcgtcgaaaaagggacaacgaggacggacaccatgtttttggagcggcagcgtgggattaccattcaagcggcagtcacttccttccagtggcacagatgtaaagttaacattgtggatacgcccggccacatggattttttggcggaggtgtaccgctctttggctgttttagatggggccatcttggtgatctccgctaaagatggcgtgcaggcccagacccgtattctgttccatgccctgcggaaaatgaacattcccaccgttatctttatcaacaagatcgaccaggctggcgttgatttgcagagcgtggttcagtctgttcgggataagctctccgccgatattatcatcaagcagacggtgtcgctgtccccggaaatagtcctggaggaaaataccgacatagaagcatgggatgcggtcatcgaaaataacgatgaattattggaaaagtatatcgcaggagaaccaatcagccgggaaaaacttgcgcgggaggaacagcggcgggttcaagacgcctccctgttcccggtctattatggcagcgccaaaaagggccttggcattcaaccgttgatggatgcggtgacagggctgttccaaccgattggggaacaggggagcgccgccctatgcggcagcgttttcaaggtggagtatacagattgcggccagcggcgtgtctatctacggctatacagcggaacgctgcgcctgcgggatacggtggccctggccgggagagaaaagctgaaaatcacagagatgcgtattccatccaaaggggaaattgttcggacagacaccgcttatccgggtgaaattgttatccttcccagcgacagcgtgaggttaaacgatgtattaggggacccaacccggctccctcgtaaaaggtggcgtgaggaccccctccccatgctgcggacgtcgattgcgccgaaaacggcagcgcaaagagaacggctgctggacgctcttacgcaacttgcggatactgacccgcttttgcgctgcgaggtggattccatcacccatgagatcattctttcttttttgggccgggtgcagttggaggttgtttccgctttgctgtcggaaaaatacaagcttgaaacagtggtaaaggaacccaccgtcatttatatggagcggccgctcaaagcagccagccacaccatccatatcgaggtgccgcccaacccgttttgggcatccatcggactgtctgttacaccactcccgcttggctccggtgtacaatacgagagccgggtttcgctgggatacttgaaccagagttttcaaaacgctgtcagggatggtatccgttacgggctggagcagggcttgttcggctggaacgtaacggactgtaagatttgctttgaatacgggctttattacagtccggtcagcacgccggcggacttccgctcattggccccgattgtattggaacaggcattgaaggaatcagggacgcaactgctggaaccttatctctccttcaccctctatgcgccccgggaatatctttccagggcttatcatgatgcaccgaaatactgtgccaccatcgaaacggtccaggtaaaaaaggatgaagttgtctttactggcgagattcccgcccgctgtatacaggcataccgtactgatctggccttttacaccaacgggcagagcgtatgccttacagaactgaaagggtatcaggccgctgtcggcaagccagtcatccagccccgccgtccaaacagccgcctggacaaggtgcgccatatgtttcagaaggtaatgtaa";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        assertNull(reader.readNextSequence());
    }

    @Test
    public void testReadCompressedGenbank() throws IOException {
        SequenceReader reader = new SequenceReader(new File("test/test.genbank.gz"));
        Sequence seq;

        String expectedSeqid;
        String expectedSeq;

        expectedSeqid = "S000349357";
        expectedSeq = "gagtttgatcctggctcaggacgaacgctggcggcgtgcctaacacatgcaagccaaaggaaagtagcaatatgagtacttggcgcaagggtgcgtaatgtataggttatctacccttcggttcgggataacttcgcgaaagcgaagataataccggatattgaggagacttgaaagatttatcgccgaaggatgagcttatatcccatcaggtagttggtagggtaaaagcctaccaagcctacgacgggtagctggtctgagaggatgatcagccacactggaactgagacacggtccagactcctacgggaggcagcagtgaggaatattgctcaatgggtgaaagcctgaagcagcaacgccgcgtgaacgatgaaggtcttcggattgtaaagttcttttgcaggggacgaaaaactcgctttgcgagtctgacggtactctgcgaataagccacggctaactctgtgccagcagccgcggtgatacagaggtggcaagcgttgtccggatttactgggtgtaaagggtgcgtaggcggatttgcaagtcgggggttaaagactcttgcttaacaagagaaacgccttcgatactgcatgtctagagtgccgaagaggaaactggaatttccggtgtagcggtggaatgtgcagagatcggaaggaacaccagtggcgaaggcaggtttgtggtcggtaactgacgctgatgcacgaaagcgtggggagcaaacaggattagataccctggtagtccacgccctaaacgatggatgctagatgttggacttcggttcagtgtcgtagctaacgcagtaagcatcccacctggggagtacgcgcgcaagcgtgaaactcaaaggaattgacgggggcccgcacaagcggtggagtatgtggtttaattcgatgcaacgcgaagaaccttacctaggcttgacatgctgggtaaagcggatgaaagtccgtgtccgaaagggatccagcacaggtgctgcatggctgtcgtcagctcgtgtcgtgagatgttgggttaagtcccgcaacgagcgcaacccctattgttagttgctaccaagtaatgttgagcactctagcaagactgcctacgcaagtagagaggaaggaggggatgacgtcaagtcctcatggcccttacgcctagggccacacacgtactacaatgggtactacaatgggcgaagtcgcgagacggaggtaatcccaaaaaagcactctcagttcagatcggagtctgcaactcgactccgtgaagttggaatcgctagtaatcgcggatcagcatgccgcggtgaatacgttcccgggccttgtacacaccgcccgtcaagccatggaagtcatcagcgcccgaagacgctttgcgtttaaggcgagggtggtaactggggctaagtcgtaacaaggtaac";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        expectedSeqid = "S002189637";
        expectedSeq = "acacatgcagccaaaggaagtagcaatacgagtacttggcgtaagggtgagtaacgcataggtcatctgcccttaggttcgggataacttcgcgaaagcgaagataataccggatattgaggaaacttgaaagatttatcgcctaaggatgagcttatgtcccatcaggtagttggtagggtaaaagcctaccaagcctacgacgggtagctggtctgagaggatgatcagccacactggaactgagacacggtccagactcctacgggaggcagcagtgaggaatattgcgcaatgggtgaaagcctgacgcagcaacgccgcgtgtgcgacgaaggtcttcggattgtaaagcacttttgcaggggacgaacagcctattttatagacctgacggtaccttgcgaataagccacggctaactctgtgccagcagccgcggtgatacagaggtggcaagcgttgtccggatttactgggtataaagggtgcgtaggcggacctataagtcgagcgttaaagatcttcgcttaacgaagaaaatgcgctcgatactgttggtctagagtgttagagaggaaactggaatttccggtgtagcggtggaatgtgtagagatcggaaggaacaccagtggcgaaggcaggtttctggctaacaactgacgctgaggcacgaaagcgcgggtagcaaacaggattagataccctggtagtccgcgccctaaacgatggatgctagatgtcggacttcggttcggtgtcgcagctaacgcattaagcatcccacctgggaagtacgcgcgcaagcgtgaaactcaaaggaattgacgggggcccgcacaagcggtggagtatgtggtttaattcgatgcaacgcgaagaaccttacctaggcttgacatggtagctaaggcggatgaaagtccgcgtccgaaagggagctatcacaggtgctgcatggctgtcgtcagctcgtgtcgtgagatgttgggttaagtcccgcaacgagcgcaacccctattgttagttgctaccgggtaatgccgagcactctagcaagactgcctacgcaagtagagaggaaggaggggatgacgtcaagtcctcatggcccttacgcctagggcaacacacgtactacaatgggcattacaatgggcgaaggcgcgagccggagataatcccaaaaaagtgctctcagttcagatcggagtctgcaactcgactccgtgaagttggaatcgctagtaatcgcaggtcagcatactgcggtgaatacgttcccgggccttgtacacaccgcccgtcaagccatggaagttatcggcgcccgaagacgcattgcgt";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        assertNull(reader.readNextSequence());
    }

    private static String readFully(BufferedReader reader) throws IOException {
        String line;
        StringBuilder ret = new StringBuilder();

        while((line = reader.readLine()) != null) {
            ret.append(line).append("\n");
        }

        return ret.toString();
    }

    @Test
    public void testReadEMBLStream() throws IOException {
        String contents = readFully(new BufferedReader(new FileReader("test/test.embl")));

        SequenceReader reader = new SequenceReader(new ByteArrayInputStream(contents.getBytes()));
        Sequence seq;

        String expectedSeqid;
        String expectedSeq;

        expectedSeqid = "FR838948";
        expectedSeq = "atgaaaataatcaatattggaattcttgcccatgtagacgctggaaagacgaccttgacggagagcctgctatatgccagcggagccatttcagaaccggggagcgtcgaaaaagggacaacgaggacggacaccatgtttttggagcggcagcgtgggattaccattcaagcggcagtcacttccttccagtggcacagatgtaaagttaacattgtggatacgcccggccacatggattttttggcggaggtgtaccgctctttggctgttttagatggggccatcttggtgatctccgctaaagatggcgtgcaggcccagacccgtattctgttccatgccctgcggaaaatgaacattcccaccgttatctttatcaacaagatcgaccaggctggcgttgatttgcagagcgtggttcagtctgttcgggataagctctccgccgatattatcatcaagcagacggtgtcgctgtccccggaaatagtcctggaggaaaataccgacatagaagcatgggatgcggtcatcgaaaataacgatgaattattggaaaagtatatcgcaggagaaccaatcagccgggaaaaacttgcgcgggaggaacagcagcgggttcaagacgcctccctgttcccggtctatcatggcagcgccaaaaatggccttggcattcaaccgttgatggatgcggtgacagggctgttccaaccgattggggaacaggggggcgccgccctatgcggcagcgttttcaaggttgagtacaccgattgcggccagcggcgtgtctatctacggttatacagcggaacgctgcgcctgcgggatacggtggccctggccgggagagaaaagctgaaaatcacagagatgcgtattccatccaaaggggaaattgttcggacagacaccgcttatcagggtgaaattgttatccttcccagcgacagcgtgaggttaaacgatgtattaggggaccaaacccggctccctcgtaaaaggtggcgcgaggaccccctccccatgctgcggacgacgattgcgccgaaaacggcagcgcaaagagaacggctgctggacgctcttacgcaacttgcggatactgacccgcttttgcgttgcgaagtggattccatcacccatgagatcattctttcttttttgggccgggtgcagttggaggttgtttccgctttgctgtcggaaaaatacaagcttgaaacagtggtaaaggaaccctccgtcatttatatggagcggccgctcaaagcagccagccacaccatccatatcgaggtgccgcccaacccgttttgggcatccataggactgtctgttacaccactctcgcttggctccggtgtacaatacgagagccgggtttcgctgggatacttgaaccagagttttcaaaacgctgtcagggatggtatccgttacgggctggagcagggcttgttcggctggaacgtaacggactgtaagatttgctttgaatacgggctttattacagtccggtcagcacgccggcggacttccgctcattggccccgattgtattggaacaggcattgaaggaatcggggacgcagctgctggaaccttatctctccttcatcctctatgcgccccaggaatacctttccagggcttatcatgatgcaccgaaatactgtgccaccatcgaaacggcccaggtaaaaaaggatgaagttgtctttactggcgagattcccgcccgctgtatacaggcataccgtactgatctggccttttacaccaacgggcggagcgtatgccttacagagctgaaaggatatcaggccgctgtcggtcagccggtcatccagccccgccgtccaaacagccgcctggacaaggtgcgccatatgtttcagaaggtaatgtaa";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        expectedSeqid = "FR838949";
        expectedSeq = "atgaaaataatcaatattggaattcttgcccatgtagacgctggaaagacgaccttgacggagagcctgctatatgccagcggagccatttcagaaccggggagcgtcgaaaaagggacaacgaggacggacaccatgtttttggagcggcagcgtgggattaccattcaagcggcagtcacttccttccagtggcacagatgtaaagttaacattgtggatacgcccggccacatggattttttggcggaggtgtaccgctctttggctgttttagatggggccatcttggtgatctccgctaaagatggcgtgcaggcccagacccgtattctgttccatgccctgcggaaaatgaacattcccaccgttatctttatcaacaagatcgaccaggctggcgttgatttgcagagcgtggttcagtctgttcgggataagctctccgccgatattatcatcaagcagacggtgtcgctgtccccggaaatagtcctggaggaaaataccgacatagaagcatgggatgcggtcatcgaaaataacgatgaattattggaaaagtatatcgcaggagaaccaatcagccgggaaaaacttgcgcgggaggaacagcggcgggttcaagacgcctccctgttcccggtctattatggcagcgccaaaaagggccttggcattcaaccgttgatggatgcggtgacagggctgttccaaccgattggggaacaggggagcgccgccctatgcggcagcgttttcaaggtggagtatacagattgcggccagcggcgtgtctatctacggctatacagcggaacgctgcgcctgcgggatacggtggccctggccgggagagaaaagctgaaaatcacagagatgcgtattccatccaaaggggaaattgttcggacagacaccgcttatccgggtgaaattgttatccttcccagcgacagcgtgaggttaaacgatgtattaggggacccaacccggctccctcgtaaaaggtggcgtgaggaccccctccccatgctgcggacgtcgattgcgccgaaaacggcagcgcaaagagaacggctgctggacgctcttacgcaacttgcggatactgacccgcttttgcgctgcgaggtggattccatcacccatgagatcattctttcttttttgggccgggtgcagttggaggttgtttccgctttgctgtcggaaaaatacaagcttgaaacagtggtaaaggaacccaccgtcatttatatggagcggccgctcaaagcagccagccacaccatccatatcgaggtgccgcccaacccgttttgggcatccatcggactgtctgttacaccactcccgcttggctccggtgtacaatacgagagccgggtttcgctgggatacttgaaccagagttttcaaaacgctgtcagggatggtatccgttacgggctggagcagggcttgttcggctggaacgtaacggactgtaagatttgctttgaatacgggctttattacagtccggtcagcacgccggcggacttccgctcattggccccgattgtattggaacaggcattgaaggaatcagggacgcaactgctggaaccttatctctccttcaccctctatgcgccccgggaatatctttccagggcttatcatgatgcaccgaaatactgtgccaccatcgaaacggtccaggtaaaaaaggatgaagttgtctttactggcgagattcccgcccgctgtatacaggcataccgtactgatctggccttttacaccaacgggcagagcgtatgccttacagaactgaaagggtatcaggccgctgtcggcaagccagtcatccagccccgccgtccaaacagccgcctggacaaggtgcgccatatgtttcagaaggtaatgtaa";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        assertNull(reader.readNextSequence());
    }

    @Test
    public void testReadGenbankStream() throws IOException {
        String contents = readFully(new BufferedReader(new FileReader("test/test.genbank")));

        SequenceReader reader = new SequenceReader(new ByteArrayInputStream(contents.getBytes()));
        Sequence seq;

        String expectedSeqid;
        String expectedSeq;

        expectedSeqid = "S000349357";
        expectedSeq = "gagtttgatcctggctcaggacgaacgctggcggcgtgcctaacacatgcaagccaaaggaaagtagcaatatgagtacttggcgcaagggtgcgtaatgtataggttatctacccttcggttcgggataacttcgcgaaagcgaagataataccggatattgaggagacttgaaagatttatcgccgaaggatgagcttatatcccatcaggtagttggtagggtaaaagcctaccaagcctacgacgggtagctggtctgagaggatgatcagccacactggaactgagacacggtccagactcctacgggaggcagcagtgaggaatattgctcaatgggtgaaagcctgaagcagcaacgccgcgtgaacgatgaaggtcttcggattgtaaagttcttttgcaggggacgaaaaactcgctttgcgagtctgacggtactctgcgaataagccacggctaactctgtgccagcagccgcggtgatacagaggtggcaagcgttgtccggatttactgggtgtaaagggtgcgtaggcggatttgcaagtcgggggttaaagactcttgcttaacaagagaaacgccttcgatactgcatgtctagagtgccgaagaggaaactggaatttccggtgtagcggtggaatgtgcagagatcggaaggaacaccagtggcgaaggcaggtttgtggtcggtaactgacgctgatgcacgaaagcgtggggagcaaacaggattagataccctggtagtccacgccctaaacgatggatgctagatgttggacttcggttcagtgtcgtagctaacgcagtaagcatcccacctggggagtacgcgcgcaagcgtgaaactcaaaggaattgacgggggcccgcacaagcggtggagtatgtggtttaattcgatgcaacgcgaagaaccttacctaggcttgacatgctgggtaaagcggatgaaagtccgtgtccgaaagggatccagcacaggtgctgcatggctgtcgtcagctcgtgtcgtgagatgttgggttaagtcccgcaacgagcgcaacccctattgttagttgctaccaagtaatgttgagcactctagcaagactgcctacgcaagtagagaggaaggaggggatgacgtcaagtcctcatggcccttacgcctagggccacacacgtactacaatgggtactacaatgggcgaagtcgcgagacggaggtaatcccaaaaaagcactctcagttcagatcggagtctgcaactcgactccgtgaagttggaatcgctagtaatcgcggatcagcatgccgcggtgaatacgttcccgggccttgtacacaccgcccgtcaagccatggaagtcatcagcgcccgaagacgctttgcgtttaaggcgagggtggtaactggggctaagtcgtaacaaggtaac";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        expectedSeqid = "S002189637";
        expectedSeq = "acacatgcagccaaaggaagtagcaatacgagtacttggcgtaagggtgagtaacgcataggtcatctgcccttaggttcgggataacttcgcgaaagcgaagataataccggatattgaggaaacttgaaagatttatcgcctaaggatgagcttatgtcccatcaggtagttggtagggtaaaagcctaccaagcctacgacgggtagctggtctgagaggatgatcagccacactggaactgagacacggtccagactcctacgggaggcagcagtgaggaatattgcgcaatgggtgaaagcctgacgcagcaacgccgcgtgtgcgacgaaggtcttcggattgtaaagcacttttgcaggggacgaacagcctattttatagacctgacggtaccttgcgaataagccacggctaactctgtgccagcagccgcggtgatacagaggtggcaagcgttgtccggatttactgggtataaagggtgcgtaggcggacctataagtcgagcgttaaagatcttcgcttaacgaagaaaatgcgctcgatactgttggtctagagtgttagagaggaaactggaatttccggtgtagcggtggaatgtgtagagatcggaaggaacaccagtggcgaaggcaggtttctggctaacaactgacgctgaggcacgaaagcgcgggtagcaaacaggattagataccctggtagtccgcgccctaaacgatggatgctagatgtcggacttcggttcggtgtcgcagctaacgcattaagcatcccacctgggaagtacgcgcgcaagcgtgaaactcaaaggaattgacgggggcccgcacaagcggtggagtatgtggtttaattcgatgcaacgcgaagaaccttacctaggcttgacatggtagctaaggcggatgaaagtccgcgtccgaaagggagctatcacaggtgctgcatggctgtcgtcagctcgtgtcgtgagatgttgggttaagtcccgcaacgagcgcaacccctattgttagttgctaccgggtaatgccgagcactctagcaagactgcctacgcaagtagagaggaaggaggggatgacgtcaagtcctcatggcccttacgcctagggcaacacacgtactacaatgggcattacaatgggcgaaggcgcgagccggagataatcccaaaaaagtgctctcagttcagatcggagtctgcaactcgactccgtgaagttggaatcgctagtaatcgcaggtcagcatactgcggtgaatacgttcccgggccttgtacacaccgcccgtcaagccatggaagttatcggcgcccgaagacgcattgcgt";

        seq = reader.readNextSequence();
        assertNotNull(seq);

        assertEquals(expectedSeqid, seq.getSeqName());
        assertEquals(expectedSeq, seq.getSeqString());

        assertNull(reader.readNextSequence());
    }
}
