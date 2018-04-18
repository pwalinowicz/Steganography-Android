package com.example.pw.hideyourmessageinwav;

/**
 * Created by PW on 2018-04-17.
 */

public class Wavelet  {

    public enum WaveletType{
        HaarWavelet,
        DaubechiesD4Wavelet
    }

    WaveletType type;

    public WaveletType getType() {
        return type;
    }

    public Wavelet(){}

    public Wavelet(WaveletType waveletType){
        type = waveletType;
    }

    public void forwardTransform(double[] buffer, int n){}
    public void inverseTransform(double[] buffer, int n){}
}
