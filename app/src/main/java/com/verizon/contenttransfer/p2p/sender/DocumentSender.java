package com.verizon.contenttransfer.p2p.sender;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by rahiahm on 6/9/2016.
 */
public class DocumentSender {

    private static final String TAG = DocumentSender.class.getName();
    private static final String documentLogHeader = "VZCONTENTTRANSFERDOCLOGAND";

    public static void generateDocumentsListFile() {

        LogUtil.d(TAG, "Starting Document file list generation...");

        MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
        fileListGenerator.getDocFileList( CTGlobal.getInstance().getContentTransferContext() );

        LogUtil.d(TAG, "Document file created ...");
    }

    public static void sendDocumentsFileList(Socket iosClientSocket) {
        try {
            File documentsFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.DOCUMENTS_FILE);
            InputStream documentsStream = new FileInputStream( documentsFile );   //getContentResolver().openInputStream( selectedFileUri );
            OutputStream out = iosClientSocket.getOutputStream();

            long documentsFileSize= documentsFile.length();
            PrintWriter writer = new PrintWriter( out ,true );
            out.flush();

            String filesize = Long.toString( documentsFileSize );
            LogUtil.d(TAG, "File size : " + filesize );

            int numDigits = filesize.length();
            LogUtil.d(TAG, "Num of digits : " +  numDigits);

            if ( numDigits < 10 ) {
                for (int k=0; k < (10-numDigits); k++ ) {
                    filesize ="0"+filesize;
                }
            }

            LogUtil.d(TAG, "File size : " + filesize );
            writer.print( documentLogHeader + filesize );
            writer.flush();
            LogUtil.d(TAG, "Document Header sent");

            byte[] buffer = new byte[4096];
            int len = documentsStream.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                LogUtil.d(TAG, "Documents file List transfer in progress... size " +  len );
                len = documentsStream.read(buffer);
            }

            out.flush();

            LogUtil.d(TAG, "Documents File List transfer complete");


        } catch (FileNotFoundException e) {
            LogUtil.d(TAG, "Documents" + e.getMessage());
        } catch (IOException e) {
            LogUtil.d(TAG, "Failed to send documents file" + e.getMessage());
        }
    }


}
