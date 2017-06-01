package de.seemoo.nexmon.jammer.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by matthias on 31.05.17.
 */

public class Assets {
    public static int copyFileFromAsset(Context context, String sourcePath, String destPath) {
        byte[] buff = new byte[1024];
        int len;
        InputStream in;
        OutputStream out;
        try {
            in = context.getAssets().open(sourcePath);
            File tmpFile = File.createTempFile("tmp", "file", context.getCacheDir());
            out = new FileOutputStream(tmpFile);
            // write file
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            in.close();
            out.flush();
            out.close();

            Shell.SU.run("cp " + tmpFile.getAbsolutePath() + " " + destPath);
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }

    public static String getAssetsPath(Context context, String filename) {
        byte[] buff = new byte[1024];
        int len;
        InputStream in;
        OutputStream out;

        String filesPath = context.getFilesDir().getPath();

        try {
            in = context.getAssets().open(filename);
            File outFile = new File(filesPath, filename);

            out = new FileOutputStream(outFile);

            // write file
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }

            in.close();
            out.flush();
            out.close();

            Shell.SU.run("chmod 777 " + filesPath + "/" + filename);

            return filesPath + "/" + filename;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
