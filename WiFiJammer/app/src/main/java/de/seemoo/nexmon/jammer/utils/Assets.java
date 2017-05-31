package de.seemoo.nexmon.jammer.utils;

import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by matthias on 31.05.17.
 */

public class Assets {
    public static int copyFileFromAsset(AssetManager assetManager, String sourcePath, String destPath) {
        byte[] buff = new byte[1024];
        int len;
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(sourcePath);
            new File(destPath).createNewFile();
            out = new FileOutputStream(destPath);
            // write file
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }
}
