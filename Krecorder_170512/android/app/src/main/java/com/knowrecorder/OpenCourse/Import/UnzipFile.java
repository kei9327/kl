package com.knowrecorder.OpenCourse.Import;

import com.knowrecorder.develop.file.FilePath;

//import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
//import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipFile {
    private String unzipPath;
    private String zipPath;

    public UnzipFile(String unzipPath, String zipPath) {
        this.unzipPath = unzipPath;
        this.zipPath = zipPath;
    }

    public void unzip() throws IOException {

        InputStream is;
        ZipInputStream zis;
        String filename;
        is = new FileInputStream(zipPath);
        zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();

            if(filename.contains("/")) {

                String[] directory = filename.split("/");
                String path = "";

                FilePath.setViewersRealmFolder(directory[0]);

                for(int i = 0; i < directory.length - 1 ; i++)
                    path += directory[i]+"/";

                File fmd  = new File(unzipPath + path);

                if(!fmd.exists()) {
                    fmd.mkdirs();
                }
            }

            if (ze.isDirectory()) {
                File fmd = new File(unzipPath + filename);
                fmd.mkdirs();
                continue;
            }

            FileOutputStream fout = new FileOutputStream(unzipPath + filename);

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            zis.closeEntry();
        }

        zis.close();
    }
}
