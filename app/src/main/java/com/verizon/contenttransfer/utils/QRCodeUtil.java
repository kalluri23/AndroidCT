package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.p2p.model.QRCodeVO;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


/**
 * Created by c0bissh on 1/30/2017.
 */
public class QRCodeUtil {
    private static String TAG = QRCodeUtil.class.getName();
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;


    private static QRCodeUtil instance;
    private String scannedQRCode = null;
    private boolean returnedFromQRActivity = false;
    private boolean connectingWifi = false;

    private boolean capturing = false;
    private QRCodeVO qrCodeVO = null;
    private Timer timer = null;

    BarcodeScannerCT barReader = null;


    private boolean usingQRCode = VZTransferConstants.SUPPORT_QR;
    public static boolean isScanningFailed = false;
    public static int step = 0;

    public static QRCodeUtil getInstance() {
        if (instance == null) {
            instance = new QRCodeUtil();
        }
        return instance;
    }

    public boolean isUsingQRCode() {
        //LogUtil.d(TAG,"using qr code :"+usingQRCode+"  CTGlobal.getInstance().isManualSetup() = "+CTGlobal.getInstance().isManualSetup());
        return (usingQRCode && !CTGlobal.getInstance().isManualSetup());
    }

    public void setUsingQRCode(boolean usingQRCode) {
        this.usingQRCode = usingQRCode;
    }

    public boolean isCapturing() {
        return capturing;
    }

    public void setCapturing(boolean capturing) {
        this.capturing = capturing;
    }

    public boolean isConnectingWifi() {
        return connectingWifi;
    }

    public void setConnectingWifi(boolean connectingWifi) {
        this.connectingWifi = connectingWifi;
    }

    public QRCodeVO getQrCodeVO() {
        return qrCodeVO;
    }

    public void setQrCodeVO(QRCodeVO qrCodeVO) {
        this.qrCodeVO = qrCodeVO;
    }

    public String getScannedQRCode() {
        return scannedQRCode;
    }

    public void setScannedQRCode(String scannedQRCode) {
        LogUtil.d(TAG, "Set scanned QR code :" + scannedQRCode);
        this.scannedQRCode = scannedQRCode;
    }

    public boolean isReturnedFromQRActivity() {
        return returnedFromQRActivity;
    }

    public void setReturnedFromQRActivity(boolean returnedFromQRActivity) {
        this.returnedFromQRActivity = returnedFromQRActivity;
    }


    public void launchQRCodeActivity(Activity activity) {
        QRCodeUtil.getInstance().reset();
        if (!QRCodeUtil.getInstance().isCapturing()) {
            QRCodeUtil.getInstance().setCapturing(true);
            barReader = new BarcodeScannerCT();
            barReader.setContext(activity);
            barReader.scan();
        }
    }

    public void reset() {
        scannedQRCode = null;
        returnedFromQRActivity = false;
        LogUtil.d(TAG, "set returned from qr act .. reset");
    }

    public void generateQRCode(Activity activity, String connType, String deviceName, String hotspotPsw, boolean displayQrCode) {
        LogUtil.d(TAG, "Display qr code :" + displayQrCode);
        if (!shouldQrCodeGenerate() || !displayQrCode) {
            return;
        }

        ImageView barCodePreview = (ImageView) activity.findViewById(R.id.qrcodePreview_iv);
        if (null != barCodePreview) {
            try {
                QRCodeVO qrCodeVO = createQRCodeVO(activity, connType, deviceName, hotspotPsw);
                QRCodeUtil.getInstance().setQrCodeVO(qrCodeVO);
                LogUtil.d(TAG, "Generated QR code... :" + qrCodeVO.getString());
                String validatedQR = "VZWCT" + qrCodeVO.getString();
                LogUtil.d(TAG, "Validated QR code... :" + validatedQR);
                String encodedQR = Utils.getEncodedString(validatedQR);
                LogUtil.d(TAG, "Encoded QR code... :" + encodedQR);
                barCodePreview.setImageBitmap(QRCodeUtil.getInstance().encodeAsBitmap(encodedQR));
                if (displayQrCode) {
                    barCodePreview.setVisibility(View.VISIBLE);
                } else {
                    barCodePreview.setVisibility(View.GONE);
                }
            } catch (WriterException e) {
                LogUtil.e(TAG, "Bar code writer exception :" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean shouldQrCodeGenerate() {
        LogUtil.d(TAG, "is new :" + CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE));
        LogUtil.d(TAG, "is cross :" + CTGlobal.getInstance().isCross());
        if (!CTGlobal.getInstance().isDoingOneToMany()
                && CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)
                || CTGlobal.getInstance().isCross()) {
            LogUtil.d(TAG, "Should qr code generate -- condition 1.");
            return true;
        } else if (CTGlobal.getInstance().isDoingOneToMany()
                && !Utils.isReceiverDevice()
                || CTGlobal.getInstance().isCross()) {
            LogUtil.d(TAG, "Should qr code generate -- condition 2.");
            return true;
        }
        return false;
    }

    public QRCodeVO createQRCodeVO(Activity activity, String connType, String deviceName, String hotspotPsw) {

        QRCodeVO qrCodeVO = new QRCodeVO();


        qrCodeVO.setConnectionType(connType);

        try {
            qrCodeVO.setVersionName(activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(activity.getApplicationContext().WIFI_SERVICE);
        if (connType.equals(VZTransferConstants.PHONE_WIFI_CONNECTION)) {
            String ssid = wifiManager.getConnectionInfo().getSSID();
            if (ssid != null) {
                ssid = ssid.replaceAll("\"", "");
            } else {
                ssid = "";
            }
            qrCodeVO.setSsid(ssid);
        } else {
            qrCodeVO.setSsid(deviceName.trim());
        }

        //qrCodeVO.setSecurityType(getSecurityType(WifiManagerControl.getWifiConfiguration(qrCodeVO.getSsid())));
        LogUtil.d(TAG, "Current ssid =" + qrCodeVO.getSsid());
        if (qrCodeVO.getSsid().length() > 0) {
            qrCodeVO.setSecurityType(WifiManagerControl.checkSecurity(wifiManager, qrCodeVO.getSsid()));
        }
        qrCodeVO.setIpaddress(getLocalIpAddress(activity));

        qrCodeVO.setPassword(hotspotPsw);

        // qrCodeVO.setCombinationType(CTGlobal.getInstance().isCross() ? VZTransferConstants.CROSS_PLATFORM : VZTransferConstants.SAME_PLATFORM);

        if (CTGlobal.getInstance().isCross()) {
            qrCodeVO.setCombinationType(VZTransferConstants.CROSS_PLATFORM);
        } else if (CTGlobal.getInstance().isDoingOneToManyComb()) {
            qrCodeVO.setCombinationType(VZTransferConstants.ONE_TO_MANY);
        } else {
            qrCodeVO.setCombinationType(VZTransferConstants.SAME_PLATFORM);
        }
        LogUtil.d(TAG, "combination type :" + qrCodeVO.getCombinationType());
        qrCodeVO.setSetupType(CTGlobal.getInstance().getPhoneSelection()); //VZTransferConstants.OLD_PHONE / VZTransferConstants.NEW_PHONE


        return qrCodeVO;
    }

    public String getLocalIpAddress(Activity activity) {
        WifiManager wifiMan = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();

        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;
    }

    public QRCodeVO resultQRCodeVO(final Activity activity) {
        if (scannedQRCode == null) {
            return null;
        }

        LogUtil.d(TAG, "Scanned qr code :" + scannedQRCode);
        String decodedScannedQR = Utils.getDecodedString(scannedQRCode);
        LogUtil.d(TAG, "decoded Scanned qr code :" + decodedScannedQR);
        if (decodedScannedQR.startsWith("VZWCT")) {
            String validatedQR = decodedScannedQR.substring(5);
            LogUtil.d(TAG, "scanned validated QR code... :" + validatedQR);
            String[] qrCodeResult = validatedQR.split(VZTransferConstants.QR_CODE_DELIMITER);
            QRCodeVO qrCodeVO = new QRCodeVO();
            if (qrCodeResult.length > 7) {
                LogUtil.e(TAG, "Found a valid code");
                qrCodeVO.setVersionName(qrCodeResult[0].trim());
                qrCodeVO.setSecurityType(qrCodeResult[1].trim());
                qrCodeVO.setCombinationType(qrCodeResult[2].trim()); //VZTransferConstants.CROSS_PLATFORM / VZTransferConstants.SAME_PLATFORM
                qrCodeVO.setSsid(qrCodeResult[3].trim());
                qrCodeVO.setIpaddress(qrCodeResult[4].trim());
                qrCodeVO.setPassword(qrCodeResult[5].trim());
                qrCodeVO.setConnectionType(qrCodeResult[6].trim());
                qrCodeVO.setSetupType(qrCodeResult[7].trim()); // old/new
            }
            return qrCodeVO;
        } else {
            ExitContentTransferDialog.alertToExitDialog(activity, activity.getString(R.string.ct_qr_error_title), activity.getString(R.string.ct_qr_error_desc1),activity.getString(R.string.start_over));
            QRCodeVO qrVO=new QRCodeVO();
            qrVO.setStatus(VZTransferConstants.INVALID_QR_CODE);
            return qrVO;
        }
    }

    public Bitmap encodeAsBitmap(String str) throws WriterException {
        //QRCodeUtil.getInstance().getStringToPass(activity)
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 500, 500, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    private int timeCounter = 0;
    private boolean stopTimeoutTimer = false;

    public void createTimer(final int timeInSec) {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timer == null) {
            timeCounter = 0;
            stopTimeoutTimer = false;
            timer = new Timer();
            timer.scheduleAtFixedRate(
                    new TimerTask() {

                        @Override
                        public void run() {
                            if (stopTimeoutTimer) {
                                LogUtil.d(TAG, "Timeout Timer task is cancelled.");
                                return;
                            } else if (timer != null && timeCounter <= timeInSec) {
                                timeCounter++;
                            } else {
                                timer.cancel();
                                timer = null;
                                LogUtil.d(TAG, "Timer is expired after :" + timeCounter + " sec");
                                Intent timeout = new Intent(VZTransferConstants.TIMER_TIME_OUT);
                                LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(timeout);
                                timeCounter = 0;
                                return;
                            }
                        }
                    }, 0, 1000); // This timer update transfer time, so interval always need to be 1000.
        }
    }

    public void cancelTimeoutTimer() {
        if (timer != null) {
            CTDeviceIteratorModel.getInstance().unregisterTimeoutListener();
            stopTimeoutTimer = true;
            timeCounter = 0;
            timer.cancel();
            timer = null;
            LogUtil.d(TAG, "Cancelled timeout timer.");
        }
    }
}
