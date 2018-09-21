package com.verizon.contenttransfer.feedback;

import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by desaivi on 9/24/2015.
 */
public class CTRequestParameterBean implements Serializable {

    //@SerializedName("model")
    private String model;
    //@SerializedName("app_type")
    private String app_type;
    //@SerializedName("RequestParameters")
    private String RequestParameters;
    //@SerializedName("sim_operator_code")
    private String sim_operator_code;
    //@SerializedName("support_location_services")
    private String support_location_services;

   // @SerializedName("timeZone")
    private String timeZone;
    //@SerializedName("type")
    private String type;
   // @SerializedName("app_name")
    private String app_name;
   // @SerializedName("current_app_version")
    private String current_app_version;

   // @SerializedName("device_name")
    private String device_name;
   // @SerializedName("static_cache_version")
    private String static_cache_version;
   // @SerializedName("deviceMdn")
    private String deviceMdn;
   // @SerializedName("formfactor")
    private String formfactor;

   // @SerializedName("application_id")
    private String application_id;
  //  @SerializedName("requestedPageType")
    private String requestedPageType;
   // @SerializedName("sourceID")
    private String sourceID;
   // @SerializedName("fw_version")
    private String fw_version;

   // @SerializedName("crashLogsList")
    private JSONArray crashLogsList;
    //private List<String> crashLogsList;
    //private List<CTCrashReportBean> crashLogsList;
   // @SerializedName("no_sim_present")
    private String no_sim_present;
  //  @SerializedName("os_version")
    private String os_version;
   // @SerializedName("static_cache_timestamp")
    private String static_cache_timestamp;

   // @SerializedName("upgrade_check_flag")
    private String upgrade_check_flag;
   // @SerializedName("network_operator_code")
    private String network_operator_code;
   // @SerializedName("os_name")
    private String os_name;
   // @SerializedName("apiLevel")
    private String apiLevel;

   // @SerializedName("brand")
    private String brand;
  //  @SerializedName("deviceMode")
    private String deviceMode;


   // @SerializedName("crashStack")
    private List<String> crashStack;


    public List<String> getCrashStack() {
        return crashStack;
    }

    public void setCrashStack(List<String> crashStack) {
        this.crashStack = crashStack;
    }

    public void setCrashStack(StackTraceElement[] stackTraceElements) {

        if (stackTraceElements != null && stackTraceElements.length > 0) {
            crashStack = new ArrayList<String>();
            for (StackTraceElement element : stackTraceElements) {
                if (element != null)
                    crashStack.add(element.toString());
            }
        }
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApptype() {
        return app_type;
    }

    public void setApptype(String app_type) {
        this.app_type = app_type;
    }

    public String getRequestParameters() {
        return RequestParameters;
    }

    public void setRequestParameters(String RequestParameters) {
        this.RequestParameters = RequestParameters;
    }

    public String getSimOperatorCode() {
        return sim_operator_code;
    }

    public void setSimOperatorCode(String sim_operator_code) {
        this.sim_operator_code = sim_operator_code;
    }

    public String getSupportlocationservices() {
        return support_location_services;
    }

    public void setSupportlocationservices(String support_location_services) {
        this.support_location_services = support_location_services;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppName() {
        return app_name;
    }

    public void setAppName(String app_name) {
        this.app_name = app_name;
    }

    public String getCurrentAppVersion() {
        return current_app_version;
    }

    public void setCurrentAppVersion(String current_app_version) {
        this.current_app_version = current_app_version;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDeviceName(String device_name) {
        this.device_name = device_name;
    }

    public String getStaticCacheVersion() {
        return static_cache_version;
    }

    public void setStaticCacheVersion(String static_cache_version) {
        this.static_cache_version = static_cache_version;
    }

    public String getDeviceMdn() {
        return deviceMdn;
    }

    public void setDeviceMdn(String deviceMdn) {
        this.deviceMdn = deviceMdn;
    }

    public String getFormfactor() {
        return formfactor;
    }

    public void setFormfactor(String formfactor) {
        this.formfactor = formfactor;
    }

    public String getApplicationId() {
        return application_id;
    }

    public void setApplicationId(String application_id) {
        this.application_id = application_id;
    }

    public String getRequestedPageType() {
        return requestedPageType;
    }

    public void setRequestedPageType(String requestedPageType) {
        this.requestedPageType = requestedPageType;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public String getFwVersion() {
        return fw_version;
    }

    public void setFwVersion(String fw_version) {
        this.fw_version = fw_version;
    }

    public JSONArray getCrashLogsList() {
        return crashLogsList;
    }
    public void setCrashLogsList(JSONArray crashLogsList) {
        this.crashLogsList = crashLogsList;
    }

/*    public List<String> getCrashLogsList() {
        return crashLogsList;
    }
    public void setCrashLogsList(List<String> crashLogsList) {
        this.crashLogsList = crashLogsList;
    }
    public void setCrashLogsList(StackTraceElement[] stackTraceElements) {

        if (stackTraceElements != null && stackTraceElements.length > 0) {
            crashLogsList = new ArrayList<String>();
            for (StackTraceElement element : stackTraceElements) {
                if (element != null)
                    crashLogsList.add(element.toString());
            }
        }
    }*/
/*    public List<CTCrashReportBean> getCrashLogsList() {
        return crashLogsList;
    }
    public void setCrashLogsList(List<CTCrashReportBean> crashLogsList) {
        this.crashLogsList = crashLogsList;
    }*/

    public String getNoSimPresent() {
        return no_sim_present;
    }

    public void setNoSimPresent(String no_sim_present) {
        this.no_sim_present = no_sim_present;
    }

    public String getOsVersion() {
        return os_version;
    }

    public void setOsVersion(String os_version) {
        this.os_version = os_version;
    }

    public String getStaticCacheTimestamp() {
        return static_cache_timestamp;
    }

    public void setStaticCacheTimestamp(String static_cache_timestamp) {
        this.static_cache_timestamp = static_cache_timestamp;
    }

    public String getUpgradeCheckFlag() {
        return upgrade_check_flag;
    }

    public void setUpgradeCheckFlag(String upgrade_check_flag) {
        this.upgrade_check_flag = upgrade_check_flag;
    }

    public String getNetworkOperatorCode() {
        return network_operator_code;
    }

    public void setNetworkOperatorCode(String network_operator_code) {
        this.network_operator_code = network_operator_code;
    }

    public String getOsName() {
        return os_name;
    }

    public void setOsName(String os_name) {
        this.os_name = os_name;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(String deviceMode) {
        this.deviceMode = deviceMode;
    }
}
