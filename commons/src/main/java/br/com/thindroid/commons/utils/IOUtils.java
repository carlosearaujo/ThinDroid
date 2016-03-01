package br.com.thindroid.commons.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by thiagosilva-trix on 06/04/15.
 */
public class IOUtils {

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void saveFile(File file, byte[] data) throws IOException {
        try {
            file.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data, 0, data.length);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            Log.w(IOUtils.class.getSimpleName(), "Error code: 3321", e);
            throw new IOException("Ocorreram erros ao persistir aplicação na memória externa. " +
                    "Verifique se existe memória disponível");
        }
    }
}
