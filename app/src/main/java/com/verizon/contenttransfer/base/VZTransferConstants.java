package com.verizon.contenttransfer.base;

import android.os.Environment;

public interface VZTransferConstants {
    public static boolean TRANSFER_CALENDAR                         = true;//BuildConfig.BUILD_TYPE.equals("debug")?false:true;
	public static final boolean FALSE                               = false;
    public static final boolean TRUE                                = true;
	public static boolean MVM_TOOL_BAR                              = true;
	public static boolean MVM_ANALYTICS								= false;
    public static boolean SUPPORT_DOCS                              = true;
    public static boolean SUPPORT_APPS                              = true;
    public static boolean CRASH_LOGGING                             = true;
    public static boolean SITECAT_LOGGING                           = false;
    //public static boolean SUPPORT_MMS                               = true;
    public static boolean SUPPORT_QR                                = true; // NOTE: Don't use this flag directly.. instead use "QRCodeUtil.getInstance().isUsingQRCode()"
    public static String IOS_MIN_VERSION                            = "3.5.6";
    public static String AND_MIN_VERSION                            = "3.5.6";
    public static boolean HANDLE_RUNTIME_EXCEPTIONS                 = true; // Enable or Disable Runtime exception handling
    public static boolean SUPPORT_MUSIC_ON_IOS                      = true;
    public static boolean USE_NEW_DIALOG                            = true;
    public static boolean SUPPORT_PASSWORDS_DATABASE                = false;
	//public static String BUILD_APP_PACKAGE 							= "com.vzw.hss.myverizon"; //"com.verizon.contenttransfer"; //

    public static final String CONTACTS								= "CONTACTS";
    public static final String PHOTOS								= "PHOTOS";
    public static final String APPS								    = "APPS";
    public static final String VIDEOS								= "VIDEOS";
    public static final String MUSICS								= "MUSICS";
    public static final String SMS									= "SMS";
    public static final String CALLLOG								= "CALLLOGS";
    public static final String CALENDAR								= "CALENDAR";
    public static final String WIFISETTING							= "WIFISETTING";
    public static final String DOCUMENTS							= "DOCUMENTS";
    public static final String APP_ANALYTICS						= "APPANALYTICS";
    public static final String VZCLOUD_CLICKS						= "VZCLOUDCLICKS";
    public static final String INVALID_QR_CODE  					= "invalid QR code";


    public static final String STATUS									= "status";
    public static final String TOTAL_SIZE								= "totalSize";
    public static final String TOTAL_COUNT								= "totalCount";
    public static final String CONTACTS_STR								= "Contacts";//string--------------------------------------
    public static final String PHOTOS_STR							    = "Photos";
    public static final String VIDEOS_STR								= "Videos";
    public static final String AUDIO_STR								= "Audio";
    public static final String SMS_STR									= "Messages";
    public static final String CALLLOG_STR								= "Call logs";
    public static final String CALENDAR_STR							    = "Calendars";
    public static final String DOCUMENTS_STR							= "Documents";
    public static final String APPS_STR                                 = "Apps";

    public static int CONTACT_NOTIFICATION_INDEX 									= 0;
    public static int PHOTO_INDEX 										= 1;
    public static int VIDEO_INDEX 										= 2;
    public static int CALENDAR_NOTIFICATION_INDEX 									= 3;
    public static int MUSIC_INDEX 										= 4;
    public static int CALLLOG_NOTIFICATION_INDEX 									= 5;
    public static int SMS_NOTIFICATION_INDEX 										= 6;
    public static int MMS_NOTIFICATION_INDEX                                    = 7;


    public static final String SERVER_EXTRA_KEY                     = "IsServer";
    public static final String VZTRANSFER_PSWDB_DIR                 = Environment.getExternalStorageDirectory() + "/Download/vztransfer/pswdb"; // this path is used in CT Password Manager to save password database file.
    public static final String VZTRANSFER_DIR 						= Environment.getExternalStorageDirectory() + "/Download/vztransfer/";
    public static final String tempCalendarStoragePath 				=  VZTRANSFER_DIR + "calendar";
    public static final String tempAppsStoragePath 				    =  VZTRANSFER_DIR + "apps";
    public static final String tempAppsIconsStoragePath 		    =  VZTRANSFER_DIR + "appIcons";
    public static final String VCARD_REQUEST_HEADER 				= "VZCONTENTTRANSFER_REQUEST_FOR_VCARD";
    public static final String PHOTO_REQUEST_HEADER 				= "VZCONTENTTRANSFER_REQUEST_FOR_PHOTO_";
    public static final String APPS_REQUEST_HEADER 				    = "VZCONTENTTRANSFER_REQUEST_FOR_APPS_";
    public static final String VIDEO_REQUEST_HEADER 				= "VZCONTENTTRANSFER_REQUEST_FOR_VIDEO_";
    public static final String MUSIC_REQUEST_HEADER 				= "VZCONTENTTRANSFER_REQUEST_FOR_MUSIC_";
    public static final String CALLLOG_REQUEST_HEADER 				= "VZCONTENTTRANSFER_REQUEST_FOR_CALLLOG_";
    public static final String SMS_REQUEST_HEADER 					= "VZCONTENTTRANSFER_REQUEST_FOR_SMS_";
    public static final String CALENDAR_REQUEST_HEADER 				= "VZCONTENTTRANSFER_REQUEST_FOR_CALENDAR_";
    public static final String DOCUMENTS_REQUEST_HEADER             = "VZCONTENTTRANSFER_REQUEST_FOR_DOCUMENT_";


    public static final String VZCONTENTTRANSFER_REQUEST_FOR_VPART 	= "VZCONTENTTRANSFER_REQUEST_FOR_VPART_";

    public static String DUPLICATE  								= "DUPLICATE_";
    public static String VZTRANSFER_DUPLICATE_RECEIVED              = "VZTRANSFER_DUPLICATE_RECEIVED";
    public static String DOCUMENT_LOG_FILE                          = "/storage/emulated/0/Documents/log/";
    public static String SYSTEM_HIDDEN_FILE                         = "/.";

    public static String ACTIVITY_TAG  								= "ACTIVITY_TAG";
    public static final String APPS_LIST_IOS                       = "Apps List"; //string----------------------------------------------------
    public static final String CONTENT_TRANSFER_PKG                  = "com.verizon.contenttransfer";
    //MVM GED build
    //public static final String MY_VERIZON_PKG                  = "com.vzw.hss.myverizonged";//"com.vzw.hss.myverizon";
    //MVM regular build
    public static final String MY_VERIZON_PKG                  = "com.vzw.hss.myverizon";

    public static final int SOCKET_TIMEOUT 							= 12000;
    public static final String CONTACTS_FILE 						= "contacts0.vcf";
    public static final String PHOTOS_FILE 							= "photos_list.txt";
    public static final String VIDEOS_FILE 							= "videos_list.txt";
    public static final String MUSICS_FILE 							= "musics_list.txt";
    public static final String CALLLOGS_FILE 						= "calllogs_list.txt";
    public static final String SMS_FILE 							= "message_list.txt";
    public static final String CALENDAR_FILE 						= "calendar_list.txt";
    public static final String APP_ANALYTICS_FILE 					= "app_analytics.txt";
    public static final String VZCLOUD_CLICKS_FILE 					= "banner_click_analytics.txt";
    public static final String ERROR_REPORT_FILE 					= "error_report.txt";
    public static final String DOCUMENTS_FILE                       = "documents_list.txt";
    public static final String APPS_FILE                            = "apps_list.txt";
    public static final String DEFAULT_DATABASE_FILE                = "ctpm.db";  // this name is used in CT Password Manager.

    public static final String PHOTO_LOG_REQUEST_HEADER 			= "VZCONTENTTRANSFER_REQUEST_FOR_PHOTO_LOG_FILE";
    public static final String APPS_LOG_REQUEST_HEADER 			    = "VZCONTENTTRANSFER_REQUEST_FOR_APPS_LOG_FILE";
    public static final String VIDEO_LOG_REQUEST_HEADER 			= "VZCONTENTTRANSFER_REQUEST_FOR_VIDEO_LOG_FILE";
    public static final String CALENDAR_LOG_REQUEST_HEADER 			= "VZCONTENTTRANSFER_REQUEST_FOR_CALENDAR_LOG_FILE";
    public static final String DOCUMENT_LOG_REQUEST_HEADER 			= "VZCONTENTTRANSFER_REQUEST_FOR_DOCUMENT_LOG_FILE";

    public static final String PHOTO_TRANSFER_REQUEST_HEADER 		= "VZCONTENTTRANSFERPHOTOSTART";
    public static final String APP_TRANSFER_REQUEST_HEADER 		    = "VZCONTENTTRANSFERAPPLSSTART";
    public static final String VIDEO_TRANSFER_REQUEST_HEADER 		= "VZCONTENTTRANSFERVIDEOSTART";
    public static final String MUSIC_TRANSFER_REQUEST_HEADER 		= "VZCONTENTTRANSFERMUSICSTART";
    public static final String CALLLOG_TRANSFER_REQUEST_HEADER 		= "VZCONTENTTRANSFERCLOGSSTART";
    public static final String CALENDAR_TRANSFER_REQUEST_HEADER 	= "VZCONTENTTRANSFERCALENSTART";
    public static final String SMS_TRANSFER_REQUEST_HEADER 			= "VZCONTENTTRANSFERSMSXXSTART";
    public static final String DOCUMENT_TRANSFER_REQUEST_HEADER 	= "VZCONTENTTRANSFERDCMNTSTART";
    public static final String METADATA_TRANSFER_REQUEST_HEADER 	= "VZCONTENTTRANSFERALLFLSTART";
    public static final String VZ_CONTENTTRANSFER_MEDIA_ERROR 	    = "VZCONTENTTRANSFERMEDIAERROR";
    public static final String VCARD_TRANSFER_REQUEST_HEADER 	    = "VZCONTENTTRANSFERVCARDSTART";




    public static final String TRANSFER_FINISH_HEADER				= "VZCONTENTTRANSFER_FINISHED";


    public static final int BUFFER_SIZE_1K							= 1024;
    public static final int BUFFER_SIZE_2K							= 1024*2;
    public static final int BUFFER_SIZE_4K							= 1024*4;
    public static final int BUFFER_SIZE_8K							= 1024*8; // Used if Connection Timed out is detected, reduced the buffer size from 64k to 8k.
    public static final int BUFFER_SIZE_16K							= 1024*16; // Used receiver side received meta data buffer size.
    public static final int BUFFER_SIZE_32K							= 1024*32;
    public static final int BUFFER_SIZE_64K							= 1024*64; // Used for the first time transfer.
    //public static final int BUFFER_SIZE_64K							= BUFFER_SIZE_8K;

    public static final String IP_ADDRESS_OF_SERVER					= "ipAddressOfServer";

    public static final String AUDIO_BROAD_CAST_MESSAGE             ="VZCONTENTTRANSFER_";

    public static final String DIALOG_MSG_FIRST_PAGE_MOVE_HEADER   = "Warning"; //............................................................
    public static final String DIALOG_MSG_GO_P2P_AFTER_CANCELATION    = "OK";

    public static final String CRLF 								= "\r\n";
    public static final String CR 								= "\r";
    public static final String LF 								= "\n";

    //public static final String PHOTO_LOG_HEADER 					= "VZCONTENTTRANSFER_REQUEST_FOR_PHOTO_LOG_FILE";
    //public static final String PHOTO_HEADER 						= "VZCONTENTTRANSFERPHOTOSTART";

    //public static final String VIDEO_LOG_HEADER						= "VZCONTENTTRANSFER_REQUEST_FOR_VIDEO_LOG_FILE";
    //public static final String VIDEO_HEADER							= "VZCONTENTTRANSFERVIDEOSTART";

    public static final String END_TRANSFER 						= "BYE";
    public static final String ANDROID 						        = "AND";

    public static final String CLIENT_CONTACTS_FILE 				= "client_contacts.vcf";
    public static final String CLIENT_PHOTOS_LIST_FILE 				= "client_photos_list.txt";
    public static final String CLIENT_APPS_LIST_FILE 				= "client_apps_list.txt";
    public static final String CLIENT_VIDEOS_LIST_FILE 				= "client_videos_list.txt";
    public static final String CLIENT_MUSIC_LIST_FILE 				= "client_musics_list.txt";
    public static final String CLIENT_CALLLOG_LIST_FILE 			= "client_calllogs_list.txt";
    public static final String CLIENT_SMS_LIST_FILE 				= "client_message_list.txt";
    public static final String CLIENT_CALENDAR_FILE 			    = "client_calendar_list.txt";
    public static final String CLIENT_DOCUMENTS_FILE 			    = "client_documents_list.txt";
    public static final String CLIENT_PAYLOAD 			            = "client_payload.txt";

    public static final String COLLECTING_MEDIA			            = "Collecting...";

    public static final String ACTION_HAND_SHAKE 					= "P2P.HAND_SHAKE";
    public static final int HANDSHAKE_PORT							= 8987;
    public static final int DISCOVERY_PORT 							= 8955;
    public static final int COMM_PORT 								= 8999;
    public static final int PINCODE                                 = 255;

    public static final String CT_CUSTOM_EXCEPTION                  = "CTERROR>";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS 			= "go_host";
    public static final String EXTRAS_ALT_GROUP_OWNER_ADDRESS 		= "alt_host";
    public static final String DATA_TRANSFER_CANCELLED 				= "Data transfer cancelled! Please try again."; //.........................................

    public static final String  AMAZON_MP3							= "com.amazon.";
    public static final String  GOOGLE_MP3							="com.google.";
    public static final String  HANGOUT_MESSAGE						=".ogg";

    public static String SERVICE_EXITED 							= "Service exited";
    public static String SOCKET_EXCEPTION_STATUS                    = "Socket Exception";
    public static String SOCKET_EXCEPTION_BROKEN_PIPE_STATUS        = "Broken Pipe exception";
    public static float VOLUME_PERCENT 								= 0.5f;
    public static long RECEIVER_SIDE_BUFFER_BYTES 					= 5000000L;
    public static String ACCOUNT_TYPE_PHONE 						= "phone";
    public static final String PROGRESS_START_TEXT                  = "0 Files / 0 MB"; //..........................................................
    public static final String NO_MEDIA_WITH_ZERO_COUNT_SELECTED    = "NONE";

    public static final String IN_PROGRESS							= "IN_PROGRESS";
    public static String PLEASE_ENTER_A_PIN 					    = "Please enter a valid code."; //...............................................................
    public static String INVALID_PIN                                = "Invalid code. Please try again."; //...........................................................
    public static int MAX_BULK_INSERT_COUNT 						= 100;
    public static String NEW_DEVICE_AVAILABLE_SPACE 				= "space";
    public static final String CLOSE_COMM_ON_FINISH_TRANSFER        = "Close Comm";
    public static final String DATA_TRANSFER_INTERRUPTED			= "Transfer Interrupted";
    public static final String DATA_TRANSFER_INTERRUPTED_BY_USER	= "Transfer Cancelled";
    public static final String DATA_TRANSFER_NOT_STARTED	        = "Transfer Cancelled - Not Started";
    public static final String MF_MVM_EXIT	                        = "MF back button-CT app exit";
    public static final String DEFAULT_TRANSFERRED_BYTES            = "0.1";
    //SUCCESSFULLY COMPLETED
    public static String TRANSFER_SUCCESSFULLY_COMPLETED            = "Transfer Success";
    public static String BROADCAST_DATA_TRANSFER_INTERRUPTED		= "Broadcast Data Transfer interrupted";
    public static String APP_ANALYTICS_FILE_URL 					= "https://mvm-wdev1.vzw.com/ct/CTAnalytics/CTAudit";//https://mobile-edev.vzw.com/CTAnalytics/CTAudit";
    public static String APP_ANALYTICS_FILE_PROD_URL 				= "https://mobile.vzw.com/CTAnalytics/CTAudit";
    public static String CONNECTION_FAILED 							= "Transfer Failed";
    public static String SELECT_ALL 				                = "Select All";     //...................................................................................
    public static String DESELECT_ALL 				                = "Deselect All";    //................................................................................
    public static String VZ_CLOUD_EVENT 				                = "VZcloud";


    public static String CALCULATE_DATA 							    =  "Calculate data";

    //public static String CONNECT_TO_WIFI 							=  "Connect to wifi";

    public static String ANALYTICS_URL								= "https://rdd.vzw.com/upload/appAnalytics";
    public static String ANALYTICS_DEVURL							= "https://rdd-dev-w.vzw.com/upload/appAnalytics";
    public static String CRASH_REPORT_PROD_URL						= "http://mobile.vzw.com/mvmrc/mvm/logCrashReport";
    public static String CRASH_REPORT_DEV_URL					    = "http://mobile-wdev.vzw.com/dev03/mvmrc/mvm/logCrashReport";

    public static String APP_NAME									= "ContentTransferApp";
    public static String UNKNOWN_SSID									= "<unknown ssid>";
    public static String GOOGLE 									= "google";

    public static int WIDI_CONNECTION_TIMEOUT                       = 30;
    public static int BATTERY_LEVEL							    	= 25;
    public static String WAITING_FOR_MEDIA_STATE_UPDATE 			= "Waiting for Media state update..";
    public static String p2pStartupActivity 						= "activity.P2PStartupActivity"; // Home page
    public static String p2pSetupActivity 							= "activity.P2PSetupActivity"; // Setup Old or New page
    public static String wiFiDirectActivity 						= "activity.WiFiDirectActivity"; // Display WifiDirect available device list page. - HotSpot btn
    public static String p2PWifiSetupActivity 						= "activity.CTWifiSetupActivity"; // HotSpot WifiSetup confirmation Page.
    public static String activityDiscoveryNew						= "activity.CTSenderPinActivity"; // Enter Pin Page
    public static String p2PReceiverActivity 						= "activity.CTReceiverPinActivity"; // Paring... New Phone Pin page - display pin number
    public static String iOSTransferActivity 						= "activity.CTSelectContentActivity"; //  Transfer activity with Transfer btn.
    public static String iOSSenderActivity 							= "activity.CTSenderActivity"; // Sending... Page
    public static String iOSGettingReadyReceiverActivity 			= "activity.CTGettingReadyReceiverActivity"; // Receiving Page.
    public static String iOSReceiverActivity 						= "activity.CTReceiverActivity"; // Receiving Page.
    public static String p2PFinishActivity 							= "activity.P2PFinishActivity"; // Finish page.
    public static String CT_DEVICE_COMBO_ACTIVITY                   = "activity.CTDeviceComboActivity"; // Select Phone combination.
    public static String transferSummaryActivity 					= "activity.TransferSummaryActivity"; // Finish page transfer summary page.
    public static String ctTransferStatusActivity 					= "activity.CTTransferStatusActivity"; // Finish page recap transfer summary status page.
    public static String TncActvity                                 = "activity.TNCActivity"; // TNC
    public static String landingActivity                            = "activity.CTLandingActivity"; // home page .. start transfer.
    public static String transferInterruptActivity                  = "activity.CTTransferInterruptActivity";
    public static String ctErrorMsgActivity                         = "activity.CTErrorMsgActivity"; // error page
    public static String ctMultiPhoneTransferActivity               = "activity.CTMultiPhoneTransferActivity"; // advance option page for one to many transfer
    public static final String RECEIVING_FILE_MSG      			    = "Receiving ";   //.........................................................
    public static final String PROCESSING_DUPLICATE 		        = "Duplicate ";
    public static final String INVALID_HEADER_RECEIVED 				= "Invalid header received";
    public static String MB 										= " MB";
    public static String MBPS 										= " Mbps";
    public static String LESS_THAN_1								= "Less than 1";   //.......................................................................
    public static String MOTOROLA_BRAND								= "motorola";
    public static String HTC_BRAND									= "htc";
    public static String SAMSUNG_BRAND								= "samsung";
    public static String GOOGLE_PIXEL                               = "google";
    public static String CT_DIR                                     = "CT";
    public static final String MESSAGE_KEY                          =  "message";

    public static final String SAVING_CONTENT_NOTIFICATION 			= "Saving call logs";
    public static final String SAVING_MESSAGES_NOTIFICATION         = "Saving Messages";
    public static final String SAVING_COMPLETED_MESSAGE_NOTIFICATION= "Saving Message Completed";
    public static final String SAVING_CALENDAR_NOTIFICATION 	    = "Saving Calendars";
    public static final String MVM_BACK_TO_EXIT			 	        = "MVM Back to exit";
    public static final String SOCKET_TIMEOUT_EXCEPTION 			= "Socket Timeout Exception";
    public static final String NULL_POINTER_EXCEPTION 				= "Null Pointer Exception";
    public static final String WIFIDIRECT_CONNECTION_DISCONNECTED   = "Wifi direct connection disconnected";
    public static final String NEW_PHONE                            = "Receiver";
    public static final String OLD_PHONE                            = "Sender";
    public static final String NOT_SELECTED                         = "Not Selected";
    public static final String CRASH_LOG_DELIMITER                  = "EndOfLog";
    public static final String IS_CT_FLOW_STARTED                   = "isCTFlowStarted";
    public static final String IS_WIFI_CONNECTED                    = "IS_WIFI_CONNECTED";
    public static final String STANDALONE                           = "STANDALONE";
    public static final String MVM                                  = "MVM";
    public static final String TRANSFER_SUMMARY_DUPLICATE_FILE      = "Duplicate File";
    public static final String TRANSFER_SUMMARY_ERROR_FILE          = "Error File";
    public static final String VZTRANSFER_CANCEL                    = "VZTRANSFER_CANCEL";
    public static final String WIFI_DIRECT_CONNECTION               = "wifi direct";
    public static final String HOTSPOT_WIFI_CONNECTION              = "hotspot wifi";
    public static final String PHONE_WIFI_CONNECTION                = "router";
    public static final String LOW_BATTERY                          = "low_battery";
    public static final String WIDI_ERROR        				 	= "widi_error";
    public static final String ERROR        				 	    = "ERROR";
    public static final String INSUFFICIENT_STORAGE					= "insufficient_storage";
    public static final String SERVER_COMM_SOCKET                   = "Server Comm Socket";
    public static final String CLIENT_COMM_SOCKET                   = "Client Comm Socket";
    public static final String DIRECT_WIFI                          = "DIRECT";
    public static final String DEFAULT_TRANSFER_DURATION            = "0";//"0 Hr:0 Min:1 Sec";
    public static final String READY_TO_ACCEPT_CONNECTION           = "ready-to-accept-connection";
    public static final String UPDATE_TOTAL_CONNECTION_COUNT        = "TOTAL_CONNECTION_COUNT";
    public static final String UPDATE_ONE_TO_MANY_PROGRESS          = "UPDATE_ONE_TO_MANY_PROGRESS";
    public static final String UPDATE_SAVING_MEDIA_PROGRESS         = "UPDATE_SAVING_MEDIA_PROGRESS";
    public static final String ENABLE_WIFI_BROADCAST                = "enable_wifi";
    public static final String STANDARD_FLOW                        = "standard";
    public static final String RELAUNCH_FLOW                        = "relaunch";
    public static final String CONTACTS_STATE                       = "contactState";
    public static final String CONTACTS_COUNT                       = "contactCount";
    public static final String SMS_STATE                            = "smsState";
    public static final String CALLLOGS_STATE                       = "calllogState";
    public static final String CALENDAR_STATE                       = "calendarState";
    public static final String FLOW                                 = "flow";
    public static final String STATUS_MSG                           = "statusMsg";

    public static enum CTPorts {
        //Please check open port numbers before assigning port numbers to avoid conflict
        //https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
        DATA (8988), COMM (8999);
        //DATA (8988), COMM (8999), PHOTOS(9500), VIDEO(9501), Music (9502), DOCS (9503), OTHER(9504);
        private int ctPort;

        CTPorts(int port) {
            this.ctPort = port;
        }

        public int value() {
            return ctPort;
        }
    };

    public static final String SENDER                               = "SENDER";
    public static final String RECEIVER                             = "RECEIVER";
    public static final boolean IS_SECURE                           = false;

    public static final String CONNECTION_TIMED_OUT_DURING_TRANSFER = "etimeout";
    public static final String TNC_STATUS                          = "TNC_STATUS";
    public static final String EULA_FILE                            = "eula.txt";
    public static final String CONTENT_TRANSFER_STARTED             = "com.verizon.contenttransfer.base.CONTENT_TRANSFER_STARTED";
    public static final String CONTENT_TRANSFER_STOPPED             = "com.verizon.contenttransfer.base.CONTENT_TRANSFER_STOPPED";
    public static final String ALL_DONE_BROADCAST                   = "alldone";
    public static final String FINISHED_APP_INSTALL                 = "FINISHED APP INSTALL";
    public static final String INIT_REVIEW                          = "showReview";
    public static final String TRANSFER_FINISHED_MSG = "app_transfer_finished";
    public static final String SAVING_FINISHED_MSG = "saving finished";
    public static final String DB_PARING_DEVICE_INFO = "DB_PARING_DEVICE_INFO";
    public static final String DB_DEVICE_ID = "DEVICE_ID";
    public static final String DB_PAIRING_DEVICE_ID = "PAIRING_DEVICE_ID";
    public static final String DB_DEVICE_MODEL = "DEVICE_MODEL";
    public static final String DB_PAIRING_MODEL = "PAIRING_MODEL";
    public static final String DB_DEVICE_OS_VERSION = "DEVICE_OS_VERSION";
    public static final String DB_PAIRING_OS_VERSION = "PAIRING_OS_VERSION";
    public static final String DB_DEVICE_TYPE = "DEVICE_TYPE";
    public static final String DB_PAIRING_DEVICE_TYPE = "PAIRING_DEVICE_TYPE";
    public static final String DB_PAIRING_TYPE = "PAIRING_TYPE";
    public static final String DB_STATUS = "STATUS";
    public static final String DB_ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String DB_CONTACTS = "CONTACTS";
    public static final String DB_PHOTOS = "PHOTOS";
    public static final String DB_VIDEOS = "VIDEOS";
    public static final String DB_SMS = "SMS";
    public static final String DB_AUDIO = "AUDIO";
    public static final String DB_CALLLOG = "CALL_LOGS";
    public static final String DB_DOCUMENTS = "DOCUMENTS";
    public static final String DB_CALENDAR = "CALENDAR";
    public static final String DB_WIFI_SETTINGS = "WIFI_SETTINGS";
    public static final String DB_DEVICE_SETTINGS = "DEVICE_SETTINGS";
    public static final String DB_DEVICE_APPS = "DEVICE_APPS";
    public static final String DB_ALARM = "ALARM";
    public static final String DB_WALLPAPER = "WALLPAPER";
    public static final String DB_VOICE_RECORDINGS = "VOICE_RECORDINGS";
    public static final String DB_RINGTONES = "RINGTONES";
    public static final String DB_SNOTE = "SNOTE";
    public static final String DB_TRANSFER_TYPE = "TRANSFER_TYPE";
    public static final String DB_DATA_TRANSFERRED = "DATA_TRANSFERRED";
    public static final String DB_TRANSFER_SPEED = "TRANSFER_SPEED";
    public static final String DB_DURATION = "DURATION";
    public static final String DB_BUILD_VERSION = "BUILD_VERSION";
    public static final String DB_DESCRIPTION = "DESCRIPTION";
    public static final String DB_TRANSFER_DATE = "TRANSFER_DATE";
    public static final String DB_CREATED_DATE = "CREATED_DATE";
    public static final String CT_CONNECTION_ESTABLISHED	    = "Connection Established";
    public static final String CT_TRANSFER_STARTED 			    = "Transfer Started";
    public static final String CT_TRANSFER_FINISH 			    = "Transfer Finished";
    public static final String CT_CONNECTION_CLOSED			    = "Connection Closed";
    public static final String VERIZON_GUEST_WIFI 			    = "Verizon Guest Wi-Fi";

    public static final String QR_CODE_DELIMITER                = "#";
    public static final String CROSS_PLATFORM                   = "cross platform";
    public static final String SAME_PLATFORM                    = "same platform";
    public static final String ONE_TO_MANY                      = "one to many";
    public static final String CLIENT_KEY_UPDATE_BROADCAST      = "clientkeyupdate";
    public static final String CONNECTING_TO_WIFI_PLEASE_WAIT   = "Connecting to WiFi. Please wait..."; // ......................................
    public static final String CT_ANALYTIC_METADATA_RECEIVED    = "Metadata received";
    public static final String CT_ANALYTIC_TRANSFER_MEDIA       = "Transfer media: ";
    public static final String CT_ANALYTIC_TRANSFER_FINISHED    = "Transfer finished";
    public static final String  SOCKET_CLOSED                   = "Socket closed";
    public static final String  BAD_FILE_DESCRIPTOR             = "EBADF";
    public static final String  CONNECTION_TIMED_OUT             = "ETIMEDOUT";
    public static final String  GLOBAL_UUID                   = "globalUUID";
    // one to many.

    public static final String CLIENT_CONNECTED                 = "One To Many Client Connected";
    public static final String HOTSPOT_IP                       = "192.168.49.1";
    public static final String VERSION_CHECK_SUCCESS            = "version-check-success";
    public static final String VERSION_CHECK_FAILED             = "version-check-failed";
    public static final String TIMER_TIME_OUT                   = "Timer timeout";
    public static final String CONNECTION_MESSAGE_PREFIX        = "VZCONTENTTRANSFERSECURITYKEYFROMSENAND#";		//--TODO add "AND" to represent android platform
    public static final int MAX_HOTSPOT_CONNECTION_COUNT        = 7;
    public static final int MAX_ROUTER_CONNECTION_COUNT         = 7;
    public static final int MAX_SAMSUNG_CONNECTION_COUNT        = 4;
    public static final String  ACCESS_POINT_UPDATE             = "accessPointUpdate";


}
