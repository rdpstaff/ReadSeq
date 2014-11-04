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

import edu.msu.cme.rdp.readseq.readers.core.SFFCore;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore.CommonHeader;
import edu.msu.cme.rdp.readseq.readers.core.SFFCore.ReadBlock;
import edu.msu.cme.rdp.readseq.writers.SFFWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class SFFSplitter {

    public static void splitInto(File sffFile, Map<String, String> barcodeToSample, File directory) throws IOException {
        SFFCore core = new SFFCore(sffFile);
        CommonHeader ch = core.getCommonHeader();

        Map<String, SFFWriter> outputMap = new HashMap();
        Map<String, PrintWriter> idOutputMap = new HashMap();

        for (String sample : new HashSet<String>(barcodeToSample.values())) {
            File outFile = new File(directory, sample + ".sff");
            outputMap.put(sample, new SFFWriter(outFile, ch, core.getManifest()));
            // allow to append to existing seqid files if multiple tags belong to the same sample            
            idOutputMap.put(sample, new PrintWriter(new FileWriter(new File(directory, sample + "_seqids.txt"), true)));
        }

        try {
            ReadBlock rb;
            for (int index = 0; index < ch.getNumReads(); index++) {

                rb = core.readReadBlock();
                int left = Math.max(rb.getClipAdapterLeft(), rb.getClipQualLeft());
                String trimmedSeq = rb.getSeq().substring(left - 1).toLowerCase(); //Biologists REALLY don't like zeros...

                //System.out.println(rb.getName() + "\t" + trimmedSeq.substring(0, 25) + "\t" + left);

                for (String barcode : barcodeToSample.keySet()) {
                    //System.out.println("\t\t" + barcode);
                    if (trimmedSeq.startsWith(barcode)) {
                        String sample = barcodeToSample.get(barcode);

                        outputMap.get(sample).writeReadBlock(rb);
                        idOutputMap.get(sample).println(rb.getName());                            
                        break;
                    }
                }
            }

        } finally {
            for (SFFWriter os : outputMap.values()) {
                os.close();
            }
            for (PrintWriter out : idOutputMap.values()) {
                out.close();
            }
        }
    }

    public static void dumpIndex(File f) throws Exception {
        SFFCore core = new SFFCore(f);
        RandomAccessFile tmp = new RandomAccessFile(f, "r");
        tmp.seek(core.getCommonHeader().getIndexOffset());

        int mftMagicNumber = 778921588;
        int srtMagicNumber = 779317876;
        int acceptedVersionMagicNumber = 825110576;

        int magicNumber = tmp.readInt();

        if (magicNumber == mftMagicNumber) {
            System.out.println("Found an index header!");
            int version = tmp.readInt();

            if (version == acceptedVersionMagicNumber) {
                System.out.println("We've got a version we understand!");

                int xmlSize = tmp.readInt();
                int dataSize = tmp.readInt();
                System.out.println("XML Size=" + xmlSize + ", dataSize=" + dataSize);
                byte[] xml = new byte[xmlSize];
                tmp.read(xml);
                System.out.println(new String(xml));
            }
        } else if (magicNumber == srtMagicNumber) {
            System.out.println("Found an index header!");
            int version = tmp.readInt();

            if (version == acceptedVersionMagicNumber) {
                System.out.println("We've got a version we understand!");
                if (tmp.read() != 0) {
                    throw new IOException("GAH NULL BYTE NOT FOUND OMG");
                }
            }
        } else {
            throw new IOException("No supported index found");
        }

        List<Integer> currIndex = new ArrayList();
        while (tmp.getFilePointer() != tmp.length()) {
            int b = tmp.readUnsignedByte();
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
                System.out.println(name + "\t" + indexLoc);

                currIndex.clear();
            } else {
                currIndex.add(b);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //dumpIndex(new File("/scratch/fishjord/test_sff/tuo.sff"));
        //dumpIndex(new File("/work/fishjord/other_projects/sra_submission/titanium_04222010/data/tmp/buk_region1.sff"));

        
        if (args.length != 3) {
            System.err.println("USAGE: SFFSplitter <sff_file> <barcode_mapping> <output_dir> \n" 
                    + "Caution: The results will be appended the same files in the ouput_dir to if multiple barcodes map to the same sample.");
            return;
        }

        File inSff = new File(args[0]);
        File barcodeMappingFile = new File(args[1]);
        File outputDir = new File(args[2]);

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Failed to make output dir " + outputDir);
                return;
            }
        }

        BufferedReader reader = new BufferedReader(new FileReader(barcodeMappingFile));
        String line = "";

        Map<String, String> barcodeMap = new HashMap();
        while ((line = reader.readLine()) != null) {
            String[] lexemes = line.trim().split("\\s+");

            if (lexemes.length == 2) {
                if (barcodeMap.containsKey(lexemes[0])) {
                    System.err.println("Warning: Duplicate mapping for " + lexemes[0] + " " + barcodeMap.get(lexemes[0]) + "," + lexemes[1]);
                }
                barcodeMap.put(lexemes[0].toLowerCase(), lexemes[1]);
            }
        }

        SFFSplitter.splitInto(inSff, barcodeMap, outputDir);

        /*
        //File inFile = new File("/work/fishjord/user_files/vamps_v6/clone43_culled3/derep/Clone43culled3.sff");
        File inFile = new File("/scratch/fishjord/test_sff/tuo.sff");
        //File inFile = new File("/scratch/fishjord/test_sff/out.sff");
        //File inFile = new File("/scratch/wangqion/qiong_titanium/titanium_run_022010/sff/GB4XUSJ01.sff");
        File outFile = new File("/scratch/fishjord/test_sff/out.sff");

        long l = 55555;
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(l);
        
        for(byte b : bb.array()) {
        System.out.print(Integer.toHexString(b & 0xff) + " ");
        }
        System.out.println();

        bb.rewind();
        System.out.println(bb.getLong());

        dumpIndex(inFile);
        //new SFFSplitter(inFile, new HashMap()).copyYo(outFile);
        //new SFFSplitter(inFile, new HashMap()).dumpIndex();*/
    }
}
