package com.passwordmanager;

import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class FileEncoder {

  public static String decodeFile(File file, String password) throws InvalidPasswordException, IOException {
    try (FileReader fileReader = new FileReader(file);
         BufferedReader bufferedInputStream = new BufferedReader(fileReader)) {
      final String datas = bufferedInputStream.lines().collect(Collectors.joining());
      return decode(datas.getBytes(), password);
    }
  }

  public static void encodeFile(File file, String datas, String password) throws IOException {
    final byte[] encode = encode(datas, password);
    try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      byteArrayOutputStream.write(encode);
      byteArrayOutputStream.writeTo(fileOutputStream);
    }
  }

  public static byte[] encode(String datas, String password) {
    byte[] dataByte = Base64.encodeBase64(datas.getBytes());

    final byte[] passwordByte = Base64.encodeBase64(password.getBytes());
    byte[] targetByte = new byte[passwordByte.length + dataByte.length];
    int i = 0;
    for (byte b : passwordByte) {
      targetByte[i] = b;
      i += 2;
    }
    i = 0;
    for (byte b : dataByte) {
      if (i < passwordByte.length * 2) {
        i++;
      }
      targetByte[i++] = b;
    }
    return targetByte;
  }

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
