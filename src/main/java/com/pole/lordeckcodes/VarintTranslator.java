package com.pole.lordeckcodes;

// THIS CODE ADAPTED FROM
/*
* Made by Pole (2019)
*
VarintBitConverter: https://github.com/topas/VarintBitConverter 
Copyright (c) 2011 Tomas Pastorek, Ixone.cz. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following
    disclaimer in the documentation and/or other materials provided
    with the distribution.

THIS SOFTWARE IS PROVIDED BY TOMAS PASTOREK AND CONTRIBUTORS ``AS IS'' 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL TOMAS PASTOREK OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE. 

*/

import java.util.LinkedList;

class VarintTranslator {

    private static final byte AllButMSB = 0x7f;
    private static final int JustMSB = 0x80;

    static int popVarint(final LinkedList<Byte> bytes) {

        long result = 0;
        int currentShift = 0;
        int bytesPopped = 0;

        int i;
        for (i = 0; i < bytes.size(); i++) {

            bytesPopped++;
            long current = (long)bytes.get(i) & AllButMSB;
            result |= current << currentShift;

            if ((bytes.get(i) & JustMSB) != JustMSB) {
//                bytes = bytes.subList(bytesPopped, bytes.size());
                bytes.subList(0, bytesPopped).clear();
                return (int)result;
            }

            currentShift += 7;

        }

        throw new IllegalArgumentException("Byte array did not contain valid varints.");
    }


    private static Byte[] getVarint(long value) {

        Byte[] buff = new Byte[10];
        int currentIndex = 0;

        if (value == 0)
            return new Byte[]{0};

        while (value != 0) {

            long byteVal = value & AllButMSB;
            value >>= 7;

            if (value != 0)
                byteVal |= 0x80;

            buff[currentIndex++] = (byte)byteVal;

        }

        Byte[] result = new Byte[currentIndex];
        System.arraycopy(buff, 0, result, 0, currentIndex);

        return result;
    }

    static Byte[] getVarint(int value) {

        return getVarint((long)value);
    }
}