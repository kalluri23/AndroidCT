package com.verizon.contenttransfer.p2p.receiver;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileNameGenerator;
import com.verizon.contenttransfer.utils.SocketUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by rahiahm on 6/15/2016.
 */
public class DocumentReceiver {
    //
    private static final String TAG = DocumentReceiver.class.getName();

    public static void startProcessingDocumentList(Socket iosServerSocket) throws IOException {
        LogUtil.d(TAG, "startProcessingDocumentList Begin");
        CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(iosServerSocket.getInetAddress().getHostAddress());

        File client_document_file = new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.CLIENT_DOCUMENTS_FILE);
        MediaFetchingService mediaService = new MediaFetchingService();
        String documentFileName = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(client_document_file));
            while ((documentFileName = reader.readLine()) != null) {
                LogUtil.d(TAG, "Document to be received : " + documentFileName);
            }

            JSONParser parser = new JSONParser();
            JSONArray documentListArray = (JSONArray) parser.parse(new FileReader(client_document_file));

            for (Object document : documentListArray) {
                JSONObject jsonObject = (JSONObject) document;
                String fileSize = (String) jsonObject.get("Size");
                LogUtil.d(TAG, "Size of the document File : " + fileSize);
                mediaService.fetchMedia(iosServerSocket, VZTransferConstants.DOCUMENTS, jsonObject, fileSize, MediaFileNameGenerator.isDuplicate(VZTransferConstants.DOCUMENTS, jsonObject));



            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        LogUtil.d(TAG, "startProcessingDocumentList End");
    }

}
