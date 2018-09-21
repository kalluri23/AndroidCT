package com.verizon.contenttransfer.adobe;

/**
 * Created by duggipr on 3/2/2016.
 */
public final class CTSiteCatConstants {

    /*
     *  Site cat global map.
     */
    public static final String SITECAT_GLOBAL_KEY_CATEGORY      = "vzwi.mvmapp.Category";
    public static final String SITECAT_GLOBAL_KEY_LOB           ="vzwi.mvmapp.LOB";
    public static final String SITECAT_GLOBAL_KEY_LANGUAGE      ="vzwi.mvmapp.language";
    public static final String SITECAT_GLOBAL_KEY_APPVERSION    ="vzwi.mvmapp.appVersion";
    public static final String SITECAT_GLOBAL_KEY_SDKVERSION    ="vzwi.mvmapp.sdkVersion";

    public static final String SITECAT_GLOBAL_VALUE_CT          = "/ct";
    public static final String SITECAT_GLOBAL_VALUE_CONSUMER    ="consumer";
    public static final String SITECAT_GLOBAL_VALUE_ENGLISH     ="english";


    public static final String SITECAT_KEY_PAGENAME             ="vzwi.mvmapp.pageName";
    public static final String SITECAT_KEY_TRANSACTIONID        ="vzwi.mvmapp.transactionId";
    public static final String SITECAT_KEY_SENDERRECEIVER       ="vzwi.mvmapp.senderReceiver";

    public static final String SITECAT_KEY_LINKNAME             ="vzwi.mvmapp.LinkName";
    public static final String SITECAT_KEY_PAGELINK             ="vzwi.mvmapp.pageLink";

    /*
     *  Site cat specific map - phone select.
     */
    public static final String SITECAT_VALUE_PHONE_SELECT       ="/ct/phone select";
    public static final String SITECAT_VALUE_NEW_PRESS          ="click button new on phone select screen";
    public static final String SITECAT_VALUE_OLD_PRESS          ="click button old on phone select screen";
    public static final String SITECAT_VALUE_SENDER             ="sender";
    public static final String SITECAT_VALUE_RECEIVER           ="receiver";


    /*
     *  Site cat specific map - phone combination.
     */
    public static final String SITECAT_VALUE_PHONE_COMBINATION          ="/ct/phone combination";
    public static final String SITECAT_VALUE_ANDROID_TO_ANDROID         ="select android to android";
    public static final String SITECAT_VALUE_ANDROID_TO_IOS             ="select android to ios";
    public static final String SITECAT_VALUE_IOS_TO_ANDROID             ="select ios to android";


    /*
     *  Site cat specific map - phone widi select.
     */
    public static final String SITECAT_KEY_FLOW_INITIATED               ="vzwi.mvmapp.flowinitiated";
    public static final String SITECAT_KEY_FLOWNAME                     ="vzwi.mvmapp.flowName";

    public static final String SITECAT_VALUE_PHONE_WIDI_SELECT          ="/ct/phone widi select";
    public static final String SITECAT_VALUE_WIFI_DIRECT_SELECTED       ="wifi direct selected";
    public static final String SITECAT_VALUE_FLOW_INITIATED             ="1";
    public static final String SITECAT_VALUE_PAIRING_OF_SENDER_RECEIVER ="pairing of sender and receiver";
    public static final String SITECAT_VALUE_HOTSPOT_SELECTED           ="use hotspot button selected";


    /*
     *  Site cat specific map - phone widi hotspot.
     */
    public static final String SITECAT_VALUE_PHONE_WIDI_HOTSPOT         ="/ct/phone widi hotspot";
    public static final String SITECAT_VALUE_HOTSPOT_WIFI               ="yes button hotspot wifi";


    /*
     *  Site cat specific map - phone wifi select.
     */
    public static final String SITECAT_VALUE_PHONE_WIFI_SELECT          ="/ct/phone wifi select";
    public static final String SITECAT_VALUE_SAME_WIFI                  ="yes button phone wifi";


    /*
     *  Site cat specific map - phone pin.
     */
    public static final String SITECAT_VALUE_PHONE_PIN                  ="/ct/phone pin";
    public static final String SITECAT_VALUE_CONNECT_USING_PIN          ="connect using pin";

    /*
     *  Site cat specific map - pairing fails.
     */
    public static final String SITECAT_KEY_ERRORCODE                            ="vzwi.mvmapp.errorCode";

    public static final String SITECAT_VALUE_POPUP_PAIRING_FAILED               ="/ct/pop up pairing failed";
    public static final String SITECAT_VALUE_WIFI_DIRECT_PAIRING_FAILED         ="wifi direct pairing failed";
    public static final String SITECAT_VALUE_WIFI_DIRECT_HOTSPOT_PAIRING_FAILED ="wifi direct hotspot pairing failed";
    public static final String SITECAT_VALUE_WIFI_HOTSPOT_PAIRING_FAILED        ="wifi hotspot pairing failed";
    public static final String SITECAT_VALUE_INVALID_PIN                        ="invalid pin";


    /*
     *  Site cat specific map - phone transfer.
     */
    public static final String SITECAT_KEY_FLOW_COMPLETED           ="vzwi.mvmapp.flowcompleted";
    public static final String SITECAT_KEY_TYPES_MEDIA_SELECTED     ="vzwi.mvmapp.typesMediaSelected";
    public static final String SITECAT_KEY_NB_CONTACTS_TO_TRANSFER  ="vzwi.mvmapp.nbContactsToTransfer";
    public static final String SITECAT_KEY_NB_PHOTOS_TO_TRANSFER    ="vzwi.mvmapp.nbPhotosToTransfer";
    public static final String SITECAT_KEY_NB_VIDEOS_TO_TRANSFER    ="vzwi.mvmapp.nbVideosToTransfer";
    public static final String SITECAT_KEY_NB_AUDIOS_TO_TRANSFER    ="vzwi.mvmapp.nbAudiosToTransfer";
    public static final String SITECAT_KEY_NB_CALLLOGS_TO_TRANSFER  ="vzwi.mvmapp.nbCallLogsToTransfer";
    public static final String SITECAT_KEY_NB_SMS_TO_TRANSFER       ="vzwi.mvmapp.nbSmsToTransfer";
    public static final String SITECAT_KEY_NB_CALENDARS_TO_TRANSFER ="vzwi.mvmapp.nbCalendarsToTransfer";

    public static final String SITECAT_VALUE_PHONE_TRANSFER         ="/ct/phone transfer";
    public static final String SITECAT_VALUE_FLOW_COMPLETED         ="1";
    public static final String SITECAT_VALUE_TRANSFER_LINK_NAME     ="metadata file read";
    public static final String SITECAT_VALUE_TRANSFER_TO_RECEIVER   ="transfer to receiver";
    public static final String SITECAT_VALUE_CANCEL_BEFORE_START    ="cancel transfer before beginning";


    /*
     *  Site cat specific map - phone processing.
     */
    public static final String SITECAT_KEY_CLICK_CANCEL_BUTTON_TRANSFER             ="vzwi.mvmapp.cancelTransfer";
    public static final String SITECAT_VALUE_CLICK_CANCEL_BUTTON_TRANSFER           ="1";

    public static final String SITECAT_VALUE_PHONE_PROCESSING                       ="/ct/phone processing";


    /*
     *  Site cat specific map - phone finish.
     */

    public static final String SITECAT_KEY_DATA_VOLUME_TRANSFERRED              ="vzwi.mvmapp.dataVolumeTransferred";
    public static final String SITECAT_KEY_TRANSFER_DURATION                    ="vzwi.mvmapp.transferDuration";
    public static final String SITECAT_KEY_TRANSFER_SPEED                       ="vzwi.mvmapp.transferSpeed";
    public static final String SITECAT_KEY_CLICK_CLOSE_BUTTON                   ="vzwi.mvmapp.ClickCloseButton";
    public static final String SITECAT_KEY_MDN                                  ="vzwi.mvmapp.MDN";
    public static final String SITECAT_VALUE_PHONE_FINISH                       ="/ct/phone finish";
    public static final String SITECAT_VALUE_CONTACTS                           ="contacts";
    public static final String SITECAT_VALUE_VIDEOS                             ="videos";
    public static final String SITECAT_VALUE_PHOTOS                             ="photos";
    public static final String SITECAT_VALUE_AUDIOS                             ="audios";
    public static final String SITECAT_VALUE_CALLLOGS                           ="calllogs";
    public static final String SITECAT_VALUE_SMS                                ="sms";
    public static final String SITECAT_VALUE_CALENDARS                          ="calendars";
    public static final String SITECAT_VALUE_APPS                               ="apps";
    public static final String SITECAT_VALUE_DOCUMENTS                          ="documents";
    public static final String SITECAT_VALUE_DEVICESETTINGS                     ="devicesettings";
    public static final String SITECAT_VALUE_SNOTES                             ="snotes";
    public static final String SITECAT_VALUE_ALARMS                             ="alarms";
    public static final String SITECAT_VALUE_WIFI                               ="wifis";
    public static final String SITECAT_VALUE_WALLPAPER                          ="wallpapers";
    public static final String SITECAT_VALUE_VOICERECORDING                     ="voicerecordings";
    public static final String SITECAT_VALUE_RINGTONES                          ="ringtones";
    public static final String SITECAT_VALUE_REMINDERS                          ="reminders";
    public static final String SITECAT_VALUE_DELIMITER                          ="|";
    public static final String SITECAT_VALUE_EVENT_NOT_FIRED                    ="event not fired";

    public static final String SITECAT_VALUE_CANCEL_CLICKED                      ="cancel transfer";





    /*
     *  Site cat specific map - phone finish.
     */

    public static final String SITECAT_VALUE_TRANSFER_CRASHED                             ="/ct/phone crash";
    /*
     *  Site cat specific map - Future types of media available for transfer.
     */
    public static final String SITECAT_KEY_NBAPPSTOTRANSFER                     ="vzwi.mvmapp.nbAppsToTransfer";
    public static final String SITECAT_KEY_NBDOCUMENTSTOTRANSFER                ="vzwi.mvmapp.nbDocumentsToTransfer";
    public static final String SITECAT_KEY_NBDEVICESETTINGSTOTRANSFER           ="vzwi.mvmapp.nbDeviceSettingsToTransfer";
    public static final String SITECAT_KEY_NBSNOTESTOTRANSFER                   ="vzwi.mvmapp.nbSNotesToTransfer";
    public static final String SITECAT_KEY_NBALARMSTOTRANSFER                   ="vzwi.mvmapp.nbAlarmsToTransfer";
    public static final String SITECAT_KEY_NBWIFISTOTRANSFER                    ="vzwi.mvmapp.nbWifisToTransfer";
    public static final String SITECAT_KEY_NBWALLPAPERSTOTRANSFER               ="vzwi.mvmapp.nbWallpapersToTransfer";
    public static final String SITECAT_KEY_NBVOICERECORDINGSTOTRANSFER          ="vzwi.mvmapp.nbVoiceRecordingsToTransfer";
    public static final String SITECAT_KEY_NBRINGTONESTOTRANSFER                ="vzwi.mvmapp.nbRingtonesToTransfer";


    public static final String SITECAT_KEY_NBCALENDARSTRANSFERRED                    ="vzwi.mvmapp.nbCalendarsTransferred";
    public static final String SITECAT_KEY_NBCALLLOGSTRANSFERRED                    ="vzwi.mvmapp.nbCallLogsTransferred";
    public static final String SITECAT_KEY_NBSMSTRANSFERRED                    ="vzwi.mvmapp.nbSmsTransferred";

    public static final String SITECAT_KEY_NBAUDIOSTRANSFERRED                  ="vzwi.mvmapp.nbAudioTransferred";
    public static final String SITECAT_KEY_NBVIDEOSTRANSFERRED                  ="vzwi.mvmapp.nbVideosTransferred";
    public static final String SITECAT_KEY_NBPHOTOSTRANSFERRED                  ="vzwi.mvmapp.nbPhotosTransferred";
    public static final String SITECAT_KEY_NBCONTACTSTRANSFERRED                ="vzwi.mvmapp.nbContactsTransferred";
    public static final String SITECAT_KEY_NBAPPSTRANSFERRED                    ="vzwi.mvmapp.nbAppsTransferred";
    public static final String SITECAT_KEY_NBDOCUMENTSTRANSFERRED               ="vzwi.mvmapp.nbDocumentsTransferred";
    public static final String SITECAT_KEY_NBDEVICESETTINGSTRANSFERRED          ="vzwi.mvmapp.nbDeviceSettingsTransferred";
    public static final String SITECAT_KEY_NBSNOTESTRANSFERRED                  ="vzwi.mvmapp.nbSNotesTransferred";
    public static final String SITECAT_KEY_NBALARMSTRANSFERRED                  ="vzwi.mvmapp.nbAlarmsTransferred";
    public static final String SITECAT_KEY_NBWIFISTRANSFERRED                   ="vzwi.mvmapp.nbWifisTransferred";
    public static final String SITECAT_KEY_NBWALLPAPERSTRANSFERRED              ="vzwi.mvmapp.nbWallpapersTransferred";
    public static final String SITECAT_KEY_NBVOICERECORDINGSTRANSFERRED         ="vzwi.mvmapp.nbVoiceRecordingsTransferred";
    public static final String SITECAT_KEY_NBRINGTONESTRANSFERRED               ="vzwi.mvmapp.nbRingtonesTransferred";
    public static final String SITECAT_KEY_TRANSFERSPEED                        ="vzwi.mvmapp.transferSpeed";
    public static final String SITECAT_VALUE_SENDERID                           ="vwwi.mvmapp.senderReceiverId";

    public static final String SITECAT_KEY_ERROR_MESSAGE                        ="vzwi.mvmapp.errorMessage";
    public static final String SITECAT_VALUE_CLICKCLOSEBUTTON                   ="click on close button finished screen";
    public static final String SITECAT_VALUE_CLICKHOMEBUTTON                    ="click on home button finished screen";


    public static final String SITECAT_VALUE_ACTION_SELECTCONTINUE                          ="select continue";
    public static final String SITECAT_VALUE_ACTION_PHONESELECT                          ="phone selected";

    public static final String SITECAT_VALUE_ACTION_USEHOTSPOT                          ="use hotspot";
    public static final String SITECAT_VALUE_ACTION_YES_PHONEWIDI_HOTSPOT                          ="yes phone widi hotspot";
    public static final String SITECAT_VALUE_ACTION_YES_PHONEWIFI_SELECT                         ="yes phone wifi select";
    public static final String SITECAT_VALUE_ACTION_CONNECT                          ="connect";

    public static final String SITECAT_VALUE_ACTION_PAIRINGFAILED                          ="pairing failed";
    public static final String SITECAT_VALUE_ACTION_TRANSFER                          ="transfer";
        public static final String SITECAT_VALUE_ACTION_CANCELTRANSFER                        ="transfer cancelled";
    public static final String SITECAT_VALUE_ACTION_CLOSE                          ="close";

    public static final String SITECAT_VALUE_ACTION_TRANSFERFAILED                      ="transfer failed";
    public static final String SITECAT_VALUE_ACTION_TRANSFERINTERRUPT                   ="data transfer interrupted";
    public static final String SITECAT_VALUE_ACTION_APPCRASHED                          ="app crashed";

}
