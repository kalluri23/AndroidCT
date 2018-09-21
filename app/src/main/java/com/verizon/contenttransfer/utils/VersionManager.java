package com.verizon.contenttransfer.utils;

import com.verizon.contenttransfer.base.VZTransferConstants;

/**
 * Created by kommisu on 6/28/2016.
 */
public final class VersionManager {

    private static final String TAG = VersionManager.class.getName();
    private static final String minSupportedVersion = VZTransferConstants.IOS_MIN_VERSION;
    private static final String minAndSupportedVersion = VZTransferConstants.AND_MIN_VERSION;
    private static final VersionManager versionManager = new VersionManager();

    /**
     * Provides local version
     * @return
     */
    public static final String getVersion() {
        return CTGlobal.getInstance().getBuildVersion();
    }

    public static final String getMinSupportedVersion() {
        if(CTGlobal.getInstance().isCross()){
            return minSupportedVersion;
        }else{
            return minAndSupportedVersion;
        }
    }

    /**
     * Checks the versions provided against local versions
     * @param version
     * @return
     */
    public static final int compareVersion( String version, String minVersion, String platform ) {

/*        if(platform.equals("AND")){
            return versionManager.compare(version, getVersion());
        } else {*/
            int meetsCrossMin = versionManager.compare(getVersion(), minVersion);
            LogUtil.e(TAG, "meetsCrossMin = " + meetsCrossMin);
            if(meetsCrossMin >= 0)
                return versionManager.compare(version, getMinSupportedVersion())>=0?0:-1;
            else {
                //If this device doesn't meet cross platform minimum, report the cross platform version as HIGHER
                return 1;
            }
/*        }*/
    }

    /**
     * Checks one version against another
     * @param version
     * @return
     *      0 means both version are equal
     *      -1 means version being checked is lower than the other version
     *      1 means version being checked is higher than the other version
     *      -99 means version provided for validation is in incorrect format
     */

    private final int compare(final String version, final String compVersion){
        int status = -99;

        final String[] vers = version.trim().split("[.]");
        final String[] compVers = compVersion.trim().split("[.]");

        if (vers.length == 3 && compVers.length == 3) {
            final int majorVer = Integer.parseInt(vers[0]);
            final int minorVer = Integer.parseInt(vers[1]);
            final int buildVer = Integer.parseInt(vers[2]);
            final int compMajor = Integer.parseInt(compVers[0]);
            final int compMinor = Integer.parseInt(compVers[1]);
            final int compBuild = Integer.parseInt(compVers[2]);

            if (majorVer == compMajor) {
                if (minorVer == compMinor) {
                    if (buildVer == compBuild) {
                        status = 0;
                    } else {
                        status = isLower(buildVer, compBuild);
                    }
                } else {
                    status = isLower(minorVer, compMinor);
                }
            } else {
                status = isLower(majorVer, compMajor);
            }

        }
        return status;
    }

    private final int isLower( final int ver, final int localVer ) {
        if ( ver < localVer ) {
            return -1;
        } else {
            return 1;
        }
    }
}
