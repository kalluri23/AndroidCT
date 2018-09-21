package com.verizon.contenttransfer.utils;

import com.verizon.contenttransfer.base.VZTransferConstants;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by kommisu on 6/16/2016.
 */
public final class PinManager {

    private static final String TAG = "PinManager";
    private static ArrayList<Integer> randCount = new ArrayList<Integer>();

    public final int encodePin( final int devicePin ) {
        return encode( devicePin );
    }

    public final String encodePin( final String devicePin ) {
        return encode( devicePin );
    }

    public final int decodePin( final int devicePin ) {
        return decode( devicePin );
    }

    public final String decodePin( final String devicePin ) {
        return decode( devicePin );
    }

    private final String encode (final String pin ) {
        int encodedPin = encode( Integer.parseInt( pin ) );
        return Integer.toString( encodedPin );
    }

    private final int encode (final int pin ) {
        int localPin = 0;
        int rnum = getRandomNumber();
        //If this random number was already used get another one
        if ( randCount.contains( Integer.valueOf(rnum) ) ) {
            rnum = getRandomNumber();
            //If all the 37 random numbers are used then reset the arrayList
            if ( randCount.size() == 37 ) {
                randCount.clear();
                randCount = new ArrayList<Integer>();
            }
        } else {
            //If random numbser was not used then add it to list
            randCount.add(Integer.valueOf(rnum));
        }
        localPin = pin + (VZTransferConstants.PINCODE * rnum );
        //String encodedPin = Base64.encodeToString( Integer.toString( localPin).getBytes(), Base64.NO_PADDING );
        LogUtil.d(TAG,  "Encoded Pin : " + localPin );
        return localPin;
    }

    private final String decode (final String pin ) {
        int decodedPin = decode( Integer.parseInt( pin ) );
        return Integer.toString( decodedPin );
    }

    private final int decode (final int pin ) {
        int localPin = 0;
        localPin = pin % VZTransferConstants.PINCODE;
        //String encodedPin = Base64.encodeToString( Integer.toString( localPin).getBytes(), Base64.NO_PADDING );
        LogUtil.d(TAG,  "Decoded Pin : " + localPin );
        return localPin;
    }

    private final int getRandomNumber() {
        Random rand = new Random();
        int value = rand.nextInt(34) + 4;
        return value;
    }
}
