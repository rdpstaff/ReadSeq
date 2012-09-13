// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OrientationChecker.java
package edu.msu.cme.rdp.readseq.utils.orientation;

// Referenced classes of package edu.msu.cme.rdp.reverseengine:
//            WordGenerator, TrainingInfo
public class OrientationChecker {

    private OrientationChecker() {
        trainInfo = TrainingInfo.getInfo();
    }

    public static OrientationChecker getChecker() {
        if (TrainingInfo.getDataFile() == null) {
            TrainingInfo.setDataFile(dataFile);
        }
        OrientationChecker checker = new OrientationChecker();
        return checker;
    }

    public boolean isSeqReversed(String seqString) {
        WordGenerator generator = new WordGenerator(seqString);
        boolean reverse = false;
        float priorDiff;
        int wordIndex;
        for (priorDiff = 0.0F; generator.hasNext(); priorDiff += trainInfo.getWordPairPriorDiff(wordIndex)) {
            wordIndex = generator.next();
        }

        if (priorDiff < 0.0F) {
            reverse = true;
        }
        return reverse;
    }
    TrainingInfo trainInfo;
    private static String dataFile = "/data/classifier/logWordPrior.txt";

    public static void main(String[] args) {

    }
}
