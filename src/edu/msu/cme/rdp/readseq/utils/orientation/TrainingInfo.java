// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TrainingInfo.java

package edu.msu.cme.rdp.readseq.utils.orientation;

import java.io.*;

// Referenced classes of package edu.msu.cme.rdp.reverseengine:
//            TrainingDataException, LogWordPriorFileParser

public class TrainingInfo
{

    private TrainingInfo()
    {
        wordPairPriorDiffArr = new float[NUM_OF_WORDS];
        try
        {
            java.net.URL aurl = getClass().getResource(dataFile);
            java.io.InputStream inStream = getClass().getResourceAsStream(dataFile);
            InputStreamReader in = new InputStreamReader(inStream);
            createLogWordPriorArr(in);
        }
        catch(IOException ex)
        {
            throw new RuntimeException(ex);
        }
        catch(TrainingDataException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static void setDataFile(String file)
    {
        dataFile = file;
    }

    public static String getDataFile()
    {
        return dataFile;
    }

    public static synchronized TrainingInfo getInfo()
    {
        if(theInfo == null)
        {
            if(dataFile == null)
                throw new IllegalStateException("Must set the data file for TrainingInfo");
            theInfo = new TrainingInfo();
        }
        return theInfo;
    }

    private void createLogWordPriorArr(Reader reader)
        throws IOException, TrainingDataException
    {
        LogWordPriorFileParser parser = new LogWordPriorFileParser();
        float logWordPriorArr[] = new float[NUM_OF_WORDS];
        parser.createLogWordPriorArr(reader, logWordPriorArr);
        int origWord[] = new int[8];
        generateWordPairDiffArr(logWordPriorArr, origWord, 0);
    }

    void generateWordPairDiffArr(float logWordPriorArr[], int word[], int beginIndex)
    {
        if(beginIndex < 0 || beginIndex > word.length)
            return;
        int origWordIndex = getWordIndex(word);
        int revWordIndex = getWordIndex(getReversedWord(word));
        float origWordPrior = logWordPriorArr[origWordIndex];
        float revWordPrior = logWordPriorArr[revWordIndex];
        wordPairPriorDiffArr[origWordIndex] = origWordPrior - revWordPrior;
        for(int i = beginIndex; i < word.length; i++)
        {
            int origBase = word[i];
            for(int j = 0; j < 4; j++)
                if(word[i] != j)
                {
                    word[i] = j;
                    generateWordPairDiffArr(logWordPriorArr, word, i + 1);
                    word[i] = origBase;
                }

        }

    }

    private static int getWordIndex(int word[])
    {
        int wordIndex = 0;
        for(int w = 0; w < word.length; w++)
        {
            wordIndex <<= 2;
            wordIndex &= 0xffff;
            wordIndex |= word[w];
        }

        return wordIndex;
    }

    private static int[] getReversedWord(int word[])
    {
        int length = word.length;
        int reverseWord[] = new int[length];
        for(int w = 0; w < length; w++)
            reverseWord[length - 1 - w] = intComplementLookup[word[w]];

        return reverseWord;
    }

    float getWordPairPriorDiff(int wordIndex)
    {
        return wordPairPriorDiffArr[wordIndex];
    }

    private static int NUM_OF_WORDS = 0x10000;
    private static final int RNA_BASES = 4;
    private static int intComplementLookup[];
    private float wordPairPriorDiffArr[];
    public static TrainingInfo theInfo = null;
    private static String dataFile = null;

    static 
    {
        intComplementLookup = new int[4];
        intComplementLookup[0] = 1;
        intComplementLookup[1] = 0;
        intComplementLookup[2] = 3;
        intComplementLookup[3] = 2;
    }
}
