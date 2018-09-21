package com.verizon.contenttransfer.utils;

//import android.util.Log;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adapter.P2PContentListAdapter;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.model.ContentSelectionVO;
import com.verizon.contenttransfer.view.CTSelectContentView;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class CTSelectContentUtil {

    private static final String TAG = "CTSelectContentViewUtil";
    private static CTSelectContentUtil instance;

    public static CTSelectContentUtil getInstance() {
        if (instance == null) {
            instance = new CTSelectContentUtil();
        }
        return instance;
    }

    public ArrayList<ContentSelectionVO> getContentList(Activity activity) {
        // Content List
        ArrayList<ContentSelectionVO> contentList = new ArrayList<ContentSelectionVO>();
        ContentSelectionVO contactsCS = new ContentSelectionVO();
        contactsCS.setContentType(VZTransferConstants.CONTACTS_STR);
        contactsCS.setUImedia(activity.getString(R.string.contacts_str));
        contactsCS.setContentflag(false);
        contactsCS.setPermissionFlag(CTGlobal.getInstance().isContactsPermitted());

        ContentSelectionVO photosCS = new ContentSelectionVO();
        photosCS.setContentType(VZTransferConstants.PHOTOS_STR);
        photosCS.setContentflag(false);
        photosCS.setUImedia(activity.getString(R.string.photos_str));
        photosCS.setPermissionFlag(true);

        ContentSelectionVO videosCS = new ContentSelectionVO();
        videosCS.setContentType(VZTransferConstants.VIDEOS_STR);
        videosCS.setUImedia(activity.getString(R.string.videos_str));
        videosCS.setContentflag(false);
        videosCS.setPermissionFlag(true);

        ContentSelectionVO calendarCS = new ContentSelectionVO();
        calendarCS.setContentType(VZTransferConstants.CALENDAR_STR);
        calendarCS.setUImedia(activity.getString(R.string.calendars_str));
        calendarCS.setContentflag(false);
        calendarCS.setPermissionFlag(CTGlobal.getInstance().isCalendarPermitted());

        ContentSelectionVO audioCS = new ContentSelectionVO();
        audioCS.setContentType(VZTransferConstants.AUDIO_STR);
        audioCS.setUImedia(activity.getString(R.string.musics_str));
        audioCS.setContentflag(false);
        audioCS.setPermissionFlag(true);

        ContentSelectionVO smsCS = new ContentSelectionVO();
        smsCS.setContentType(VZTransferConstants.SMS_STR);
        smsCS.setUImedia(activity.getString(R.string.messages_str));
        smsCS.setContentflag(false);
        smsCS.setPermissionFlag(CTGlobal.getInstance().isSmsPermitted());

        ContentSelectionVO callLogsCS = new ContentSelectionVO();
        callLogsCS.setContentType(VZTransferConstants.CALLLOG_STR);
        callLogsCS.setUImedia(activity.getString(R.string.callLogs_str));
        callLogsCS.setContentflag(false);
        callLogsCS.setPermissionFlag(CTGlobal.getInstance().isCalllogsPermitted());

        ContentSelectionVO documentCS = new ContentSelectionVO();
        documentCS.setContentType(VZTransferConstants.DOCUMENTS_STR);
        documentCS.setUImedia(activity.getString(R.string.documents_str));
        documentCS.setContentflag(false);
        documentCS.setPermissionFlag(true);

        ContentSelectionVO appsCS = new ContentSelectionVO();
        appsCS.setContentType(VZTransferConstants.APPS_STR);
        appsCS.setUImedia(activity.getString(R.string.apps_str));
        appsCS.setContentflag(false);
        appsCS.setPermissionFlag(true);


        String[] mediaTypeArray =  CTGlobal.getInstance().getMediaTypeArray();
        for(int i=0;i<mediaTypeArray.length;i++) {
            if(mediaTypeArray[i].equals(VZTransferConstants.CONTACTS_STR)){
                contentList.add(contactsCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.PHOTOS_STR)){
                contentList.add(photosCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.VIDEOS_STR)){
                contentList.add(videosCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.AUDIO_STR)){
                contentList.add(audioCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.CALENDAR_STR)){
                contentList.add(calendarCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.SMS_STR)){
                contentList.add(smsCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.CALLLOG_STR)){
                contentList.add(callLogsCS);
            } else if(mediaTypeArray[i].equals(VZTransferConstants.DOCUMENTS_STR)){
                if (VZTransferConstants.SUPPORT_DOCS) {
                    contentList.add(documentCS);
                }
            } else if(mediaTypeArray[i].equals(VZTransferConstants.APPS_STR)){
                if (VZTransferConstants.SUPPORT_APPS) {
                    contentList.add(appsCS);
                }
            }
        }
        return contentList;
    }


    public void setLogAnalytics() {
        //log analytics
        HashMap<String, Object> eventMap = new HashMap<String, Object>();
        eventMap.put(ContentTransferAnalyticsMap.DEVICE_UUID, CTGlobal.getInstance().getDeviceUUID());
        eventMap.put(ContentTransferAnalyticsMap.CONTACTS, Utils.getContentSelection(VZTransferConstants.CONTACTS_STR).getContentflag());
        eventMap.put(ContentTransferAnalyticsMap.PHOTOS, Utils.getContentSelection(VZTransferConstants.PHOTOS_STR).getContentflag());
        eventMap.put(ContentTransferAnalyticsMap.VIDEOS, Utils.getContentSelection(VZTransferConstants.VIDEOS_STR).getContentflag());
        eventMap.put(ContentTransferAnalyticsMap.CALENDAR, Utils.getContentSelection(VZTransferConstants.CALENDAR_STR).getContentflag());
        eventMap.put(ContentTransferAnalyticsMap.APPS, Utils.getContentSelection(VZTransferConstants.APPS_STR).getContentflag());
        eventMap.put(ContentTransferAnalyticsMap.TRANSFER_START_TIME, Utils.getTimeInMillis());
        if (P2PContentListAdapter.contentselctionlist.size() > 5) {
            eventMap.put(ContentTransferAnalyticsMap.MUSIC, Utils.getContentSelection(VZTransferConstants.AUDIO_STR).getContentflag());
            eventMap.put(ContentTransferAnalyticsMap.SMS,Utils.getContentSelection(VZTransferConstants.SMS_STR).getContentflag());
            eventMap.put(ContentTransferAnalyticsMap.CALLLOGS, Utils.getContentSelection(VZTransferConstants.CALLLOG_STR).getContentflag());
        }
        Utils.logEvent(CTSelectContentView.getInstance().getTransferdata(), eventMap, "TransferScreen");
    }

    public boolean isAnyItemSelected() {
        ArrayList<ContentSelectionVO> p2pContentList = P2PContentListAdapter.contentselctionlist;
        for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
            if (P2PContentListAdapter.contentselctionlist.get(i).getContentflag()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyZeroFileSelected(Activity activity) {
        ContentSelectionVO contentSelectionVO = new ContentSelectionVO();
        for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
            contentSelectionVO = P2PContentListAdapter.contentselctionlist.get(i);
            if (contentSelectionVO.getContentflag()){
                if(contentSelectionVO.getContentsize() == 0
                        && !contentSelectionVO.getContentStorage().equals(activity.getString(R.string.ct_content_tv))) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSelectedMediaNameWithZeroCount() {
        boolean isZeroCount = false;
        String zeroCountMediaType = "";
        ContentSelectionVO contentSelectionVO = new ContentSelectionVO();
        for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
            contentSelectionVO = P2PContentListAdapter.contentselctionlist.get(i);

            if (contentSelectionVO.getContentflag()){
                if(contentSelectionVO.getContentsize() == 0) {
                    contentSelectionVO.setContentflag(false);
                    isZeroCount = true;
                    zeroCountMediaType += contentSelectionVO.getContentType()+",";
                }
            }
        }
        if(isZeroCount){
            if(zeroCountMediaType.length()>0){
                zeroCountMediaType = zeroCountMediaType.substring(0,zeroCountMediaType.lastIndexOf(','));
            }
            LogUtil.d(TAG,"zero count media types are :"+zeroCountMediaType);
            return zeroCountMediaType;
        }
        return VZTransferConstants.NO_MEDIA_WITH_ZERO_COUNT_SELECTED;
    }
    public void resetOndestroy() {
        CTSelectContentView.getInstance().killInstance();
        instance = null;
    }
    public void updateMediaCount(String mediaType, boolean isCollecting, Activity activity) {
        if(mediaType.equals(VZTransferConstants.CONTACTS_STR)) {
            TextView cloudContentStatusText = (TextView) CTSelectContentModel.getInstance().getActivity().findViewById(R.id.ct_cloud_contact_can_not_transfer_id);
            ContentSelectionVO contactVO = Utils.getContentSelection(VZTransferConstants.CONTACTS_STR);
            contactVO.setContentsize(MediaFileListGenerator.TOT_CONTACTS);
            if(MediaFileListGenerator.TOT_CLOUD_CONTACTS > 0) {
                contactVO.setCloudContent(MediaFileListGenerator.TOT_CLOUD_CONTACTS + CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_cloud_contact_text));

                if(cloudContentStatusText != null) {
                    cloudContentStatusText.setVisibility(View.VISIBLE);
                }
            }else {
                contactVO.setCloudContent("");
                if(cloudContentStatusText != null) {
                    cloudContentStatusText.setVisibility(View.GONE);
                }
            }
            if(isCollecting) {
                contactVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            } else {
                contactVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_CONTACTS,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalContactsBytes()),mediaType));
            }
        } else if(mediaType.equals(VZTransferConstants.PHOTOS_STR)) {
            ContentSelectionVO photoVO = (ContentSelectionVO) Utils.getContentSelection(VZTransferConstants.PHOTOS_STR);
            photoVO.setContentsize(MediaFileListGenerator.TOT_PHOTOS);
            if(isCollecting) {
                photoVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            }else {
                photoVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_PHOTOS,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalPhotosBytes()),mediaType));
            }

        }else if(mediaType.equals(VZTransferConstants.VIDEOS_STR)) {
            ContentSelectionVO videoVO = Utils.getContentSelection(VZTransferConstants.VIDEOS_STR);
            videoVO.setContentsize(MediaFileListGenerator.TOT_VIDEOS);
            if(isCollecting) {
                videoVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            }else {
                videoVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_VIDEOS,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalVideosBytes()),mediaType));
            }

        }else if(mediaType.equals(VZTransferConstants.CALENDAR_STR)) {
            ContentSelectionVO calendarVO = (ContentSelectionVO) Utils.getContentSelection(VZTransferConstants.CALENDAR_STR);
            calendarVO.setContentsize(MediaFileListGenerator.TOT_CALENDAR);
            if(isCollecting) {
                calendarVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            }else {
                calendarVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_CALENDAR,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalCalendarBytes()),mediaType));
            }

        } else if (mediaType.equals(VZTransferConstants.AUDIO_STR)) {
            ContentSelectionVO musicVO = (ContentSelectionVO) Utils.getContentSelection(VZTransferConstants.AUDIO_STR);
            musicVO.setContentsize(MediaFileListGenerator.TOT_MUSICS);
            if(isCollecting) {
                musicVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            }else {
                musicVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_MUSICS,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalMusicBytes()),mediaType));
            }

        }else if(mediaType.equals(VZTransferConstants.CALLLOG_STR)) {
            ContentSelectionVO calllogVO = Utils.getContentSelection(VZTransferConstants.CALLLOG_STR);
            calllogVO.setContentsize(MediaFileListGenerator.TOT_CALLLOGS);
            if(isCollecting) {
                calllogVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            }else {
                calllogVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_CALLLOGS,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalCalllogsBytes()),mediaType));
            }

        }else if(mediaType.equals(VZTransferConstants.SMS_STR)) {
            ContentSelectionVO smsVO = Utils.getContentSelection(VZTransferConstants.SMS_STR);
            int contentSize =  MediaFileListGenerator.TOT_MESSAGES;
            smsVO.setContentsize(contentSize);
            if(isCollecting) {
                smsVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
            }else {
                smsVO.setContentStorage(Utils.findContentStorage(contentSize,
                        Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalSMSBytes()),mediaType));
            }

        } else if (mediaType.equals(VZTransferConstants.DOCUMENTS_STR)) {
            if (VZTransferConstants.SUPPORT_DOCS) {
                ContentSelectionVO documentVO = Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR);
                documentVO.setContentsize(MediaFileListGenerator.TOT_DOCS);
                if(isCollecting) {
                    documentVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
                }else {
                    documentVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_DOCS,
                            Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalDocumentsBytes()), mediaType));
                }
            }
        } else if (mediaType.equals(VZTransferConstants.APPS_STR)) {
            if (VZTransferConstants.SUPPORT_APPS) {
                ContentSelectionVO appVO = Utils.getContentSelection(VZTransferConstants.APPS_STR);
                appVO.setContentsize(MediaFileListGenerator.TOT_APPS);
                if (!CTGlobal.getInstance().isCross()) {
                    if (isCollecting) {
                        appVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
                    } else {
                        appVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_APPS,
                                Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalAppsBytes()),mediaType));
                    }


                } else {
                    if (isCollecting) {
                        appVO.setContentStorage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv));
                    } else {
                        appVO.setContentStorage(Utils.findContentStorage(MediaFileListGenerator.TOT_APPS,
                                Utils.bytesToMeg(CTSelectContentModel.getInstance().getTotalAppIconsBytes()),mediaType));
                    }

                }
            }
        }
    }


    public void handleCancelTransfer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.writeToAllCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SocketUtil.disconnectAllSocket();
            }
        }).start();
        LogUtil.d(TAG, "closeSendingSockets callled ...");
    }

}
