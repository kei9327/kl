package com.knowrecorder.develop.opencourse.ocexport;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by we160303 on 2017-03-20.
 */

public class ZipUtils {

    private static final int COMPRESSION_LEVEL = 8;

    private static final int BUFFER_SIZE = 1024 * 2;


    /**
     * 지정된 폴더를 Zip 파일로 압축한다.
     * @param sourcePath - 압축 대상 디렉토리
     * @param output - 저장 zip 파일 이름
     * @throws Exception
     */
    public static void zip(String sourcePath, String output, String parentDirectory) throws Exception {

        // 압축 대상(sourcePath)이 디렉토리나 파일이 아니면 리턴한다.
        File sourceFile = new File(sourcePath);
        if (!sourceFile.isFile() && !sourceFile.isDirectory()) {
            throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
        }

        // output 의 확장자가 zip이 아니면 리턴한다.
        //if (!(StringUtils.substringAfterLast(output, ".")).equalsIgnoreCase("zip")) {
        //    throw new Exception("압축 후 저장 파일명의 확장자를 확인하세요");
        //}

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(output); // FileOutputStream
            bos = new BufferedOutputStream(fos); // BufferedStream
            zos = new ZipOutputStream(bos); // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8

            zos.putNextEntry(new ZipEntry(parentDirectory+"/"));

            zipEntry(sourceFile, sourcePath, zos, parentDirectory+"/"); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 압축
     * @param sourceFile
     * @param sourcePath
     * @param zos
     * @throws Exception
     */
    private static void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos, String parentDirectory) throws Exception {
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if (sourceFile.isDirectory()) {
            if (sourceFile.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일 리스트
            for (int i = 0; i < fileArray.length; i++) {
                String parentFolder = "";
                if(fileArray[i].isDirectory()){
                    StringTokenizer tok = new StringTokenizer(fileArray[i].getAbsolutePath(),"/");
                    int tok_len = tok.countTokens();
                    parentFolder = tok.toString()+"/";
                    while(tok_len != 0){
                        tok_len--;
                        parentFolder = tok.nextToken()+"/";
                    }
                    zos.putNextEntry(new ZipEntry(parentDirectory + parentFolder));
                }

                zipEntry(fileArray[i], sourcePath, zos, parentDirectory + parentFolder); // 재귀 호출

            }
        } else { // sourcehFile 이 디렉토리가 아닌 경우
            BufferedInputStream bis = null;
            try {
                String sFilePath = sourceFile.getPath();
                Log.i("ZipUtils", "file Path : " + sFilePath);
                //String zipEntryName = sFilePath.substring(sourcePath.length() + 1, sFilePath.length());
                StringTokenizer tok = new StringTokenizer(sFilePath,"/");

                int tok_len = tok.countTokens();
                String zipEntryName=tok.toString();
                while(tok_len != 0){
                    tok_len--;
                    zipEntryName = tok.nextToken();
                }
                bis = new BufferedInputStream(new FileInputStream(sourceFile));

                ZipEntry zentry = new ZipEntry(parentDirectory + zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);

                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;

                while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            } finally {
                if (bis != null) {
                    bis.close();
                }
            }
        }
    }

}
