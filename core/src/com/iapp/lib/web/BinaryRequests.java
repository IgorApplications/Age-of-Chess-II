package com.iapp.lib.web;

import com.iapp.lib.util.Pair;

import java.math.BigInteger;

public final class BinaryRequests {

    public static byte[] updateAvatar(byte operation, long id, byte[] array) {
        byte[] idArray = BigInteger.valueOf(id).toByteArray();
        byte[] general = new byte[idArray.length + array.length + 2];
        general[0] = operation;
        general[1] = (byte) idArray.length;
        System.arraycopy(idArray, 0, general, 2, idArray.length);
        int margin = 2 + idArray.length;
        System.arraycopy(array, 0, general, margin, array.length);

        return general;
    }

    public static byte[] getAvatar(byte operation, long id) {
        byte[] idArray = BigInteger.valueOf(id).toByteArray();
        byte[] general = new byte[idArray.length + 2];
        general[0] = operation;
        general[1] = (byte) idArray.length;
        System.arraycopy(idArray, 0, general, 2, idArray.length);

        return general;
    }

    public static byte[] resultUpdateAvatar(byte operation, RequestStatus requestStatus) {
        return new byte[] {operation, (byte) requestStatus.ordinal()};
    }

    public static byte[] resultGetAvatar(byte operation, RequestStatus requestStatus, long id, byte[] avatar) {
        if (requestStatus != RequestStatus.DONE) {
            return new byte[] {operation, (byte) requestStatus.ordinal()};
        }
        byte[] idArray = BigInteger.valueOf(id).toByteArray();
        byte[] general = new byte[avatar.length + idArray.length + 3];
        general[0] = operation;
        general[1] = (byte) requestStatus.ordinal();
        general[2] = (byte) idArray.length;
        System.arraycopy(idArray, 0, general, 3, idArray.length);
        int margin = 3 + idArray.length;
        System.arraycopy(avatar, 0, general, margin, avatar.length);

        return general;
    }

    public static RequestStatus parseResultUpdateAvatar(byte[] array) {
        return RequestStatus.values()[array[1]];
    }

    public static Pair<RequestStatus, Pair<Long, byte[]>> parseResultGetAvatar(byte[] array) {
        RequestStatus requestStatus = RequestStatus.values()[array[1]];
        byte idSize = array[2];
        var id = new BigInteger(sub(array, 3, idSize + 3)).longValue();

        if (requestStatus != RequestStatus.DONE) {
            return new Pair<>(requestStatus, new Pair<>(id, null));
        }

        byte[] avatar = sub(array, 4, array.length);
        return new Pair<>(requestStatus, new Pair<>(id, avatar));
    }

    public static Pair<Long, byte[]> parseUpdateAvatar(byte[] message) {
        byte idSize = message[1];
        long id = new BigInteger(sub(message, 2, idSize + 2)).longValue();
        byte[] data = sub(message, idSize + 2, message.length);
        return new Pair<>(id, data);
    }

    public static long parseGetAvatar(byte[] message) {
        byte idSize = message[1];
        return new BigInteger(sub(message, 2, idSize + 2)).longValue();
    }

    private static byte[] sub(byte[] arr, int start, int end) {
        byte[] newArr = new byte[end - start];
        System.arraycopy(arr, start, newArr, 0, newArr.length);
        return newArr;
    }
}
