package com.knowrecorder.develop.opencourse.ocimport;

import android.util.Log;

import java.io.BufferedInputStream;
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
       ZipInputStream zis = null;

        try {

            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath)));
            ZipEntry ze = null;
            int count;
            byte[] buffer = new byte[8192];

            while((ze = zis.getNextEntry()) != null){
                String fileName = ze.getName();

                if (ze.isDirectory()) {
                    File fmd = new File(unzipPath + fileName);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(unzipPath + fileName);
                try {
                    while((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0 , count);
                }finally {
                    fout.close();
                }

            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }finally {
            if(zis != null){
                try {
                    zis.close();
                }catch (IOException e){

                }
            }
        }



//        InputStream is;
//        ZipInputStream zis;
//        String filename;
//        is = new FileInputStream(zipPath);
//        zis = new ZipInputStream(new BufferedInputStream(is));
//
//        ZipEntry ze;
//        byte[] buffer = new byte[1024];
//        int count;
//
//        while ((ze = zis.getNextEntry()) != null) {
//            filename = ze.getName();
//
//            if (ze.isDirectory()) {
//                File fmd = new File(unzipPath + filename);
//                fmd.mkdirs();
//                continue;
//            }
//
//            FileOutputStream fout = new FileOutputStream(unzipPath + filename);
//
//            while ((count = zis.read(buffer)) != -1) {
//                fout.write(buffer, 0, count);
//            }
//
//            fout.close();
//            zis.closeEntry();
//        }
//
//        zis.close();
    }
}
