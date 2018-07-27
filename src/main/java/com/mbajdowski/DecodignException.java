package com.mbajdowski;

public class DecodignException extends Exception {

    public DecodignException(String param, int value){
        super("Error while decoding! "+param+" has invalid value of: "+value);
    }
}
