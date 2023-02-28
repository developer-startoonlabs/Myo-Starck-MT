package com.startoonlabs.apps.multipleble.utils;

import android.util.Log;

public class ValueBasedColorOperations {

    public static byte[] getParticularDataToPheeze(int body_orientation, int muscle_index, int exercise_index, int bodypart_index,
                                                   int orientation_position){
        Log.i("VALUEORIENTATION", String.valueOf(orientation_position));
        byte[] b = new byte[6];
        if(bodypart_index==8){
            bodypart_index = 1;
        }
        String ae = "AE";
        byte[] b1 = ByteToArrayOperations.hexStringToByteArray("AE");
//        try {
//            b[0] = Byte.parseByte(String.format("%040x", new BigInteger(1, ae.getBytes("UTF-16"))));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        b[0] = b1[0];
        b[1] = (byte) bodypart_index;
        b[2] = (byte) exercise_index;
        b[3] = (byte) muscle_index;
        b[4] = (byte) body_orientation;
        b[5] = (byte) orientation_position;

//        b = ByteToArrayOperations.hexStringToByteArray(ae);
        return b;
//
//
//        Log.i("bodypart", String.valueOf(bodypart_index));
//        byte[] b;
//        if(bodypart_index!=6) {
//            b = ByteToArrayOperations.hexStringToByteArray("AA0" + (bodypart_index + 3));
//            Log.i("value","AA0"+(bodypart_index+3));
//        }
//        else {
//            b = ByteToArrayOperations.hexStringToByteArray("AA04");
//        }
//        return b;
    }
}
