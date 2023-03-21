package com.wyuansmart.phone.engine.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringParseUtil {

    static  final int IDENTITYCODE_OLD = 15;
    static  final int IDENTITYCODE_NEW = 18;

    /****************************
     * 检查字符串是否包含中文
     * @param str
     * @return 如果含中文，返回true
     */
    public static boolean checkIfContainCH(String str) {
        if (StringUtils.isEmpty(str)){
            return false;
        }
        Pattern p = Pattern.compile("[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】]");
        Matcher m = p.matcher(str);
        if(m.find()){
            return true;
        }else {
            return false;
        }
        /*for (char c : str.toCharArray()) {
            if (c >= 0x4E00 &&  c <= 0x9FA5){
                return true;
            }
        }
        return false;*/
    }

    /*******************************
     * 粗略判断身份证号码是否正确。
     *
     * @param code
     *            身份证号码。
     * @return 如果身份证号码正确，则返回true，否则返回false。
     */
    public static boolean isIdentityCode(String code) {

        if (null == code ||code.isEmpty()) {
            return false;
        }


        String birthDay = "";
        code = code.trim().toUpperCase();

        // 长度只有15和18两种情况
        if ((code.length() != IDENTITYCODE_OLD)
                && (code.length() != IDENTITYCODE_NEW)) {
            return false;
        }

        // 身份证号码必须为数字(18位的新身份证最后一位可以是x)
        Pattern pt = Pattern.compile("(^\\d{15}$)|(\\d{17}(?:\\d|x|X)$)");
        Matcher mt = pt.matcher(code);
        if (!mt.find()) {
            return false;
        }
        return true;
    }


    /*******************************
     * 粗略判断身份证号码是否正确。
     *
     * @param code
     *            身份证号码。
     * @return 如果身份证号码正确，则返回true，否则返回false。
     */
    public static Integer getGender(String code) {
        if (StringUtils.isEmpty(code)) {
            return 0;
        }
        try {
            if (code.length() == IDENTITYCODE_OLD) {
                if (Integer.parseInt(code.substring(14, 15)) % 2 == 0) {
                    return 2;
                } else {
                    return 1;
                }
                //18位身份证号
            } else if (code.length() == IDENTITYCODE_NEW) {
                // 判断性别
                if (Integer.parseInt(code.substring(16).substring(0, 1)) % 2 == 0) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }catch (Exception ex){
            return 0;
        }
        return 0;
    }

    public static Integer getBirth(String code) {
        if (StringUtils.isEmpty(code)) {
            return 0;
        }
        try {
            if (code.length() == IDENTITYCODE_OLD) {
                if (Integer.parseInt(code.substring(14, 15)) % 2 == 0) {
                    return 2;
                } else {
                    return 1;
                }
                //18位身份证号
            } else if (code.length() == IDENTITYCODE_NEW) {
                // 判断性别
                if (Integer.parseInt(code.substring(16).substring(0, 1)) % 2 == 0) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }catch (Exception ex){
            return 0;
        }
        return 0;
    }


    /**
     * 获取出生日期  yyyy-MM-dd
     * @param IDCard
     * @return
     */
    public static String getBirthday(String IDCard){
        String birthday="";
        String year="";
        String month="";
        String day="";
        if (StringUtils.isNotBlank(IDCard)){
            //15位身份证号
            if (IDCard.length() == IDENTITYCODE_OLD){
                // 身份证上的年份(15位身份证为1980年前的)
                year = "19" + IDCard.substring(6, 8);
                //身份证上的月份
                month = IDCard.substring(8, 10);
                //身份证上的日期
                day= IDCard.substring(10, 12);
                //18位身份证号
            }else if(IDCard.length() == IDENTITYCODE_NEW){
                // 身份证上的年份
                year = IDCard.substring(6).substring(0, 4);
                // 身份证上的月份
                month = IDCard.substring(10).substring(0, 2);
                //身份证上的日期
                day=IDCard.substring(12).substring(0,2);
            }
            birthday=year+"-"+month+"-"+day;
        }
        return birthday;
    }


    public static Date parseBirthDateString(String datestr)
    {
        Date date = null;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date =  sf.parse(datestr);
        } catch (ParseException e) {
            return null;
        }
        return date;

    }




    public static boolean isLetterOrDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }


    /*********************************
     * 获取一个随机的带时间搓的文件名
     * @param filename 带后缀的文件名
     * @return
     */
    public static String getNewFileNameWithTimeStr(String filename)
    {
        String fileExt = FilenameUtils.getExtension(filename);
        String randomStr = String.valueOf(Math.round(Math.random() * 1000000));
        String fileNameAppendix
                = new SimpleDateFormat("yyyyMMdd-HH-mm-ss.SSS").format(new Date()) +"_"+randomStr+ "." + fileExt;
        return fileNameAppendix;

    }

    /*********************************
     * 获取一个随机的带时间搓的目录名
     * @return
     */
    public static String getNewDirectoryWithTimeStr()
    {
        String randomStr = String.valueOf(Math.round(Math.random() * 100000));
        String fileNameAppendix
                = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +"-"+randomStr;
        return fileNameAppendix;

    }


    public static Date parseStdTimeString(String datestr)
    {
        Date date = null;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date =  sf.parse(datestr);
        } catch (ParseException e) {
            return null;
        }
        return date;

    }


}
