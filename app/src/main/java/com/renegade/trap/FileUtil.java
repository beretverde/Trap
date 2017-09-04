package com.renegade.trap;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FileUtil {
    public static File getAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!file.mkdirs()) {
            file.mkdir();
            Log.e("FileUtil", "Directory not created");
        }
        return file;
    }

}
