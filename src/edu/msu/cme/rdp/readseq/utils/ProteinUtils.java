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

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.utils.gbregion.Extends;
import edu.msu.cme.rdp.readseq.utils.gbregion.SingleSeqRegion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author fishjord
 */
public class ProteinUtils {

    public static final char UNKNOWN_AA = 'X';

    public static class AminoAcid {

        private Character aminoAcid;
        private boolean initiator;

        public AminoAcid(Character aminoAcid, boolean initiator) {
            this.aminoAcid = Character.toLowerCase(aminoAcid);
            this.initiator = initiator;
        }

        public Character getAminoAcid() {
            return aminoAcid;
        }

        public Character getAminoAcid(int index) {
            return aminoAcid;
        }

        public boolean isInitiator() {
            return initiator;
        }

        public boolean matches(char c) {
            return aminoAcid == Character.toLowerCase(c);
        }
    }
    private Map<Integer, AminoAcid[][][]> translationTableMap = new LinkedHashMap();
    private static final Pattern tableIdPattern = Pattern.compile("transl_table=(\\d+)");
    private static final Pattern tableEntryPattern = Pattern.compile("([A-Z]{3})\\s+([A-Z*]{1})\\s+[A-Z][a-z]{2}(\\s+i)?");

    private static final byte[] asciiMap = new byte[127];
    static {
        Arrays.fill(asciiMap, (byte)-1);
        asciiMap['A'] = asciiMap['a'] = 0;
        asciiMap['C'] = asciiMap['c'] = 1;
        asciiMap['G'] = asciiMap['g'] = 2;
        asciiMap['T'] = asciiMap['t'] = asciiMap['U'] = asciiMap['u'] = 3;
    }

    private static class ProteinUtilsHolder {

        private static ProteinUtils holder = new ProteinUtils();
    }

    private static class TranslateResult {

        int errors;
        String translStr;
    }

    private ProteinUtils() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data/protein_coding_tables.txt")));
            String line;
            Integer currTable = -1;
            AminoAcid[][][] translationTable = new AminoAcid[4][4][4];

            while ((line = reader.readLine()) != null) {
                Matcher m = tableIdPattern.matcher(line);

                if (m.find()) {
                    if (currTable != -1) {
                        translationTableMap.put(currTable, translationTable);
                    }

                    currTable = new Integer(m.group(1));
                    translationTable = new AminoAcid[4][4][4];
                } else {
                    m = tableEntryPattern.matcher(line);

                    while (m.find()) {
                        String strCodon = m.group(1).toLowerCase();
                        AminoAcid aa = new AminoAcid(m.group(2).charAt(0), m.group().endsWith("i"));
                        Set<String> codons = new HashSet();
                        codons.add(strCodon.toLowerCase());

                        if (strCodon.contains("t")) {
                            codons.add(strCodon.replace("t", "u"));
                        }
                        if (strCodon.contains("u")) {
                            codons.add(strCodon.replace("u", "t"));
                        }

                        for (String c : codons) {
                            char[] codon = strCodon.toCharArray();

                            if (codon.length != 3) {
                                throw new IOException("Unexpected codon length in line " + line);
                            }

                            translationTable[asciiMap[codon[0]]][asciiMap[codon[1]]][asciiMap[codon[2]]] = aa;
                        }
                    }
                }
            }

            if (!translationTableMap.containsKey(currTable)) {
                translationTableMap.put(currTable, translationTable);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AminoAcid[][][] getTranslationTable(int table) {
        return translationTableMap.get(table);
    }

    public int proteinTables() {
        return translationTableMap.size();
    }

    public static ProteinUtils getInstance() {
        return ProteinUtilsHolder.holder;
    }

    public String translateToProtein(String unalignedNucSeq, boolean dontAllowInitiators, int translTable) {
        AminoAcid[][][] proteinMapping = translationTableMap.get(translTable);
        if (proteinMapping == null) {
            throw new IllegalArgumentException("No such protein translation table " + translTable);
        }

        StringBuilder ret = new StringBuilder();
        char[] bases = unalignedNucSeq.toLowerCase().toCharArray();

        for (int index = 0; index + 3 <= bases.length; index += 3) {
            AminoAcid aa = null;
            if(asciiMap[bases[index]] != -1 && asciiMap[bases[index + 1]] != -1 && asciiMap[bases[index + 2]] != -1) {
                aa = proteinMapping[asciiMap[bases[index]]][asciiMap[bases[index + 1]]][asciiMap[bases[index + 2]]];
            }
            if (aa == null) {
                ret.append(UNKNOWN_AA);
            } else {
                if (index == 0 && !dontAllowInitiators && aa.isInitiator()) {
                    ret.append('m');
                } else {
                    ret.append(aa.getAminoAcid());
                }
            }

        }

        return ret.toString();
    }

    private TranslateResult backTranslate(String protSeq, String unalignedNucSeq, boolean dontAllowInitiators, int translTable) {
        AminoAcid[][][] proteinMapping = translationTableMap.get(translTable);
        if (proteinMapping == null) {
            throw new IllegalArgumentException("No such protein translation table " + translTable);
        }

        int unalignedIndex = 0;
        int errors = 0;
        char[] bases = unalignedNucSeq.toLowerCase().toCharArray();
        StringBuilder nucSeq = new StringBuilder();

        for (int index = 0; index < protSeq.length(); index++) {
            char protBase = protSeq.charAt(index);

            if (Character.isLetter(protBase)) {
                int nucSeqIndex = unalignedIndex * 3;
                char p = protBase;

                if (nucSeqIndex + 3 > unalignedNucSeq.length()) {
                    break;
                }
                AminoAcid aa = null;
                if(asciiMap[bases[nucSeqIndex]] != -1 && asciiMap[bases[nucSeqIndex + 1]] != -1 && asciiMap[bases[nucSeqIndex + 2]] != -1) {
                    aa = proteinMapping[asciiMap[bases[nucSeqIndex]]][asciiMap[bases[nucSeqIndex + 1]]][asciiMap[bases[nucSeqIndex + 2]]];
                }

                if (aa != null) {

                    char aminoAcid = aa.getAminoAcid();
                    if (index == 0 && !dontAllowInitiators && aa.isInitiator()) {
                        aminoAcid = 'm';
                    }

                    if (aa.matches('*') && index + 1 != protSeq.length()) {
                        if (index + 1 != protSeq.length()
                                && Character.toLowerCase(p) != 'u') {
                            errors++;
                        }
                    } else if (Character.toUpperCase(aminoAcid) != Character.toUpperCase(p)) {
                        errors++;
                    }
                } else {
                    errors++;
                }

                if (Character.isUpperCase(protBase)) {
                    nucSeq.append(Character.toUpperCase(bases[index])).append(Character.toUpperCase(bases[index + 1])).append(Character.toUpperCase(bases[index + 2]));
                } else {
                    nucSeq.append(Character.toLowerCase(bases[index])).append(Character.toLowerCase(bases[index + 1])).append(Character.toLowerCase(bases[index + 2]));
                }

                unalignedIndex++;
            } else if (protBase == '-') {
                nucSeq.append("---");
            } else if (protBase == '.') {
                nucSeq.append("...");
            }
        }

        TranslateResult result = new TranslateResult();
        result.errors = errors;
        result.translStr = nucSeq.toString();
        return result;
    }

    public String getAlignedNucSeq(String alignedProtSeq, String unalignedNucSeq, SingleSeqRegion r, int translTable) {
        return backTranslate(alignedProtSeq, unalignedNucSeq, (r.getExtends() == Extends.BEYOND_BEGIN || r.getExtends() == Extends.BEYOND_BOTH), translTable).translStr;
    }

    public float getTranslScore(String proteinSeq, String nucSeq, SingleSeqRegion r, int translTable) {
        int errors = backTranslate(proteinSeq, nucSeq, (r.getExtends() == Extends.BEYOND_BEGIN || r.getExtends() == Extends.BEYOND_BOTH), translTable).errors;
        return ((proteinSeq.length() - errors) / (float) proteinSeq.length());
    }

    public List<Sequence> allTranslate(Sequence seq) {
        String seqString = seq.getSeqString();
        List<Sequence> ret = new ArrayList();

        if (seqString.length() < 3) {
            return ret;
        }

        for (int frame = 0; frame < 3; frame++) {
            String frameSeq = seqString.substring(frame);
            String reverseFrameSeq = IUBUtilities.reverseComplement(seqString).substring(frame);

            if (frameSeq.length() < 3) {
                continue;
            }

            ret.add(new Sequence(seq.getSeqName() + "_" + (frame + 1), "", translateToProtein(frameSeq, true, 11)));
            ret.add(new Sequence(seq.getSeqName() + "_-" + (frame + 1), "", translateToProtein(reverseFrameSeq, true, 11)));
        }

        return ret;
    }

    public List<Sequence> allFrames(Sequence seq) {
        String seqString = seq.getSeqString();
        List<Sequence> ret = new ArrayList();

        if (seqString.length() < 3) {
            return ret;
        }

        for (int frame = 0; frame < 3; frame++) {
            String frameSeq = seqString.substring(frame);
            String reverseFrameSeq = IUBUtilities.reverseComplement(seqString).substring(frame);

            if (frameSeq.length() < 3) {
                continue;
            }

            ret.add(new Sequence(seq.getSeqName() + "_" + (frame + 1), "", frameSeq));
            ret.add(new Sequence(seq.getSeqName() + "_-" + (frame + 1), "", reverseFrameSeq));
        }

        return ret;
    }
}
