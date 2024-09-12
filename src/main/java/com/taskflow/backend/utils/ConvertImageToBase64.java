package com.taskflow.backend.utils;

import java.util.Base64;

public class ConvertImageToBase64 {

    //Recibir el objeto imagen del usuario y convertirlo a base64.
    public static String convertImageToBase64(byte[] image) {
        return Base64.getEncoder().encodeToString(image);
    }
}
