package com.svenj.tools.pdf;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static SimpleDateFormat sDateFormat = null;

    public static String buildSubtitle(Date time, long size) {
        if (sDateFormat == null) {
            sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(sDateFormat.format(time));
        sb.append(" | ");
        sb.append(getFormatSize(size));

        return sb.toString();
    }

    private static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte(s)";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * Parse the DateFormat(D:YYYYMMDDHHmmSSOHH'mm') of PDF metadata, such as:
     * D:20140327195230+05'00'
     * @param metaDateString string for metadata date
     * @return date
     */
    public static Date parsePdfMetaDate(String metaDateString) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ", Locale.getDefault());
            // skip start : D:
            String dateString = metaDateString.substring(2);
            // remove '
            dateString = dateString.replaceAll("'", "");

            return sdf.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

}
