// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WordGenerator.java

package edu.msu.cme.rdp.readseq.utils.orientation;


public class WordGenerator
{

    public WordGenerator(String seq)
    {
        nextWord = 0;
        baseCount = 1;
        position = 0;
        invalid = false;
        seqString = seq;
    }

    public boolean hasNext()
    {
        int charIndex = 0;
        for(baseCount--; baseCount < 8 && position < seqString.length();)
        {
            char nextBase = seqString.charAt(position++);
            charIndex = charLookup[nextBase];
            if(charIndex == -1)
            {
                baseCount = -1;
                charIndex = 0;
            }
            baseCount++;
            nextWord <<= 2;
            nextWord &= 0xffff;
            nextWord |= charIndex;
        }

        if(baseCount < 8)
        {
            invalid = true;
            return false;
        } else
        {
            return true;
        }
    }

    public int next()
    {
        if(invalid)
            throw new IllegalStateException("Attempt to call WordGenerator.next() when no more words.");
        else
            return nextWord;
    }

    public static final int WORD_SIZE = 8;
    public static final int MAX_NUM_WORD = 0x10000;
    public static final int MASK = 65535;
    private static final int MAX_ASCII = 128;
    private String seqString;
    private int nextWord;
    private int baseCount;
    private int position;
    private boolean invalid;
    private static int charLookup[];

    static 
    {
        charLookup = new int[128];
        for(int i = 0; i < 128; i++)
            charLookup[i] = -1;

        charLookup[65] = 0;
        charLookup[84] = 1;
        charLookup[85] = 1;
        charLookup[71] = 2;
        charLookup[67] = 3;
        charLookup[97] = 0;
        charLookup[116] = 1;
        charLookup[117] = 1;
        charLookup[103] = 2;
        charLookup[99] = 3;
    }
}
