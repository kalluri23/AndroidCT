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
 * Created by yempasu on 3/31/2017.
 */
public class AppSender {

    private static final String TAG = AppSender.class.getName();
    private static final String appsLogHeader = "VZCONTENTTRANSFERAPPSLOGAND";


    public static void generateAppsListFile() {

        LogUtil.d(TAG, "Starting Apps file list generation...");

        MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
        fileListGenerator.getAppsList( CTGlobal.getInstance().getContentTransferContext() );
        LogUtil.d(TAG, "Apps file created ...");
    }

    public static void sendAppsFileList(Socket iosClientSocket) {
        try {

            File appsFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.APPS_FILE);
            InputStream appsStream = new FileInputStream( appsFile );
            OutputStream out = iosClientSocket.getOutputStream();

            long appsFileSize= appsFile.length();
            PrintWriter writer = new PrintWriter( out ,true );
            out.flush();

            String filesize = Long.toString( appsFileSize );
            LogUtil.d(TAG, "File size : " + filesize );

            int numDigits = filesize.length();
            LogUtil.d(TAG, "Numb of digists : " +  numDigits);

            if ( numDigits < 10 ) {
                for (int k=0; k < (10-numDigits); k++ ) {
                    filesize ="0"+filesize;
                }
            }

            LogUtil.d(TAG, "File size : " + filesize );
            writer.print( appsLogHeader + filesize );
            writer.flush();
            LogUtil.d(TAG, "Apps Header sent");

            byte[] buffer = new byte[4096];
            int len = appsStream.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                LogUtil.d(TAG, "Apps file List transfer in progress... size " +  len );
                len = appsStream.read(buffer);
            }

            out.flush();

            LogUtil.d(TAG, "Apps File List transfer complete");

        } catch (FileNotFoundException e) {
            LogUtil.d(TAG, "Apps" + e.getMessage());
        } catch (IOException e) {
            LogUtil.d(TAG, "Failed to send Apps file" + e.getMessage());
        }
    }

}
