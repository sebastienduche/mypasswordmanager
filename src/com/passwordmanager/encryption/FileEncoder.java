package com.passwordmanager.encryption;

import com.passwordmanager.exception.InvalidPasswordException;
import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileEncoder {

    public static String decodeFile(File file, String password) throws InvalidPasswordException, IOException {
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedInputStream = new BufferedReader(fileReader)) {
            final String datas = bufferedInputStream.lines().collect(Collectors.joining());
            try {
                return EncryptorAesGcmPassword.decrypt(datas.getBytes(), password);
            } catch (InvalidPasswordException e) {
                return decode(datas.getBytes(), password);
            }
        }
    }

    public static void encodeFile(File file, String datas, String password) throws Exception {
        String encode = EncryptorAesGcmPassword.encrypt(datas.getBytes(UTF_8), password);
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byteArrayOutputStream.write(encode.getBytes(UTF_8));
            byteArrayOutputStream.writeTo(fileOutputStream);
        }
    }

    @Deprecated
    public static String decode(byte[] datas, String password) throws InvalidPasswordException {
        final byte[] passwordByte = Base64.encodeBase64(password.getBytes());
        int i = 0;
        for (byte b : passwordByte) {
            if (b != datas[i]) {
                throw new InvalidPasswordException();
            }
            i += 2;
        }
        byte[] decode = new byte[datas.length - passwordByte.length];
        i = 0;
        for (int j = 0; j < datas.length; j++) {
            if (i < passwordByte.length * 2) {
                i++;
            }
            if (j < decode.length) {
                decode[j] = datas[i++];
            }
        }
        return new String(Base64.decodeBase64(decode));
    }
}
