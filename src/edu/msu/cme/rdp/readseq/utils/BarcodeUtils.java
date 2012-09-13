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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fishjord
 */
public class BarcodeUtils {

    public static class BarcodeInvalidException extends Exception {

        public BarcodeInvalidException(String str) {
            super(str);
        }
    }

    public static Map<String, String> readBarcodeFile(File barcodeFile) throws IOException, BarcodeInvalidException {
        return readBarcodeFile(new FileReader(barcodeFile));
    }

    public static Map<String, String> readBarcodeFile(InputStream is) throws IOException, BarcodeInvalidException {
        return readBarcodeFile(new InputStreamReader(is));
    }

    /**
     * Reads a barcode (tag file) and returns a map of barcodes to sample names
     * 
     * @param barcodeFile barcode fiel to read
     * @return map of barcodes to sample names
     * @throws IOException
     * @throws edu.msu.cme.rdp.readseq.utils.BarcodeUtils.BarcodeInvalidException if there are duplicate barcodes or a barcode is a prefix to another
     */
    public static Map<String, String> readBarcodeFile(Reader r) throws IOException, BarcodeInvalidException {
        Map<String, String> ret = new HashMap();

        BufferedReader reader = null;
        if (reader instanceof BufferedReader) {
            reader = (BufferedReader) r;
        } else {
            reader = new BufferedReader(r);
        }

        String line;

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != 2 && tokens.length != 3) {
                continue;
            }

            String tag = tokens[0].toLowerCase();

            if (ret.containsKey(tag)) {
                throw new BarcodeInvalidException("Barcode for " + ret.get(tag) + " and " + tokens[1] + " are both " + tag);
            }

            ret.put(tag, tokens[1]);
        }

        reader.close();

        List<String> barcodes = new ArrayList(ret.keySet());
        for (int row = 0; row < barcodes.size(); row++) {
            String barcode1 = barcodes.get(row);
            for (int col = row + 1; col < barcodes.size(); col++) {
                String barcode2 = barcodes.get(col);
                if (barcode1.startsWith(barcode2)) {
                    throw new BarcodeInvalidException("Barcode " + barcode2 + " (" + ret.get(barcode2) + ") is a prefix of barcode " + barcode1 + " (" + ret.get(barcode1) + ")");
                }
                if (barcode2.startsWith(barcode1)) {
                    throw new BarcodeInvalidException("Barcode " + barcode1 + " (" + ret.get(barcode1) + ") is a prefix of barcode " + barcode2 + " (" + ret.get(barcode2) + ")");
                }
            }
        }


        return ret;
    }
}
