package com.wyuansmart.phone.engine.util;


import com.wyuansmart.phone.common.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataConversionUtil {

    /**
     * 2000年1月1日这个最小时间
     */
    public static final long MIN_TIME = 946656000000L;
    /**
     * 整数转ip
     * @param ip
     * @return
     */
    public static String int2Ip(long ip) {
        StringBuilder builder = new StringBuilder(String.valueOf(ip >>> 24));
        builder.append(".");
        builder.append(String.valueOf((ip & 0X00FFFFFF) >>> 16));
        builder.append(".");
        builder.append(String.valueOf((ip & 0X0000FFFF) >>> 8));
        builder.append(".");
        builder.append(String.valueOf(ip & 0X000000FF));
        return builder.toString();
    }

    /**
     * 整数转ip
     * @param ip
     * @return
     */
    public static String int2Ip(Object ip) {
        if (ip == null){
            return null;
        }

        if (StringUtils.isEmpty(ip.toString())
                || !StringUtils.isNumeric(ip.toString())){
            return null;
        }

        return int2Ip(Long.parseLong(ip.toString()));
    }

    /**
     * 时间整理为毫秒
     * 比如 输入： 1536217895 返回：1536217895
     * 输入： 1536217895183 返回：1536217895183
     * @param time 时间（可以是秒 或 毫秒）
     * @return 统一为时间毫秒
     */
    public static long timeTrimToMsec(long time){
        if (time < 0xffffffffL){
            //需要x1000
            return time*1000;
        }else {
            return time;
        }
    }

    /**
     * 时间整理为毫秒
     * 比如 输入： 1536217895 返回：1536217895
     * 输入： 1536217895183 返回：1536217895183
     * @param time 时间（可以是秒 或 毫秒）
     * @return 统一为时间毫秒
     */
    public static long timeTrimToMsec(Object time){
        if (time == null){
            return 0;
        }
        if (StringUtils.isEmpty(time.toString())
                || !StringUtils.isNumeric(time.toString())){
            return 0;
        }
        return timeTrimToMsec(Long.parseLong(time.toString()));
    }

    /**
     * 获取开始时间，结束时间之间的 年月范围, 开始大于结束 返回当前时间的年月值比如 当前时间为2019-2-19 返回 201902
     * @param startTime 开始时间的毫秒
     * @param endTime 结束时间的
     * @return
     */
    public static List<Integer> getYearMonthRangeList(long startTime,long endTime){
        List<Integer> result = new ArrayList<>();
        if (startTime <= 0 || endTime <= 0){
            Calendar calendar = Calendar.getInstance();
            int year =calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH)+1;
            result.add(year*100+month);
            return result;
        }

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(startTime);
        endCalendar.setTimeInMillis(endTime);

        if (startCalendar.after(endCalendar)) {
            //开始时间小时
            Calendar calendar = Calendar.getInstance();
            int year =calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH)+1;
            result.add(year*100+month);
            return result;
        }

        int lastTime = 0;
        for (; !startCalendar.after(endCalendar); ){
            int year =startCalendar.get(Calendar.YEAR);
            int month = startCalendar.get(Calendar.MONTH)+1;
            lastTime = year*100+month;
            result.add(lastTime);
            startCalendar.add(Calendar.MONTH,1);
        }

        int year =endCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH)+1;
        int temp = year*100+month;
        if (lastTime < temp){
            result.add(lastTime);
        }
        return result;
    }

    public static String gpsToString(Double latitude, Double longitude){
        if (latitude == null || longitude == null){
            return "";
        }

        if(  Math.abs(latitude.doubleValue()) > 90 || Math.abs(longitude.doubleValue()) >180 ){
            return "";
        }

        String string = "";
        string+=longitude.toString();
        string+=",";
        string+=latitude.toString();
        string+="";
        return string;
    }

    /**
     * 将“20190112” 这种格式的字符串转化为时间
     * @return
     */
    public static Date dateStrToDate(String strDate){
        String strMark = "yyyyMMdd";
        try {
             Date date = DateUtil.convertStringToDate(strMark,strDate);
             return date;
        }catch (Exception e){

        }
        return null;
    }


    public static Date getDateTime(String time) {
        if(StringUtils.isEmpty(time)){
            return new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
        return date;
    }


    public static boolean isTimeBeyond(String time,int days) {
        if(StringUtils.isEmpty(time)){
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
        if(null == date){
            return true;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0-days);
        if(date.getTime() < calendar.getTime().getTime()){
            return true;
        }
        return false;

    }


    public static Date getDateTimeByDay(String time) {
        if(StringUtils.isEmpty(time)){
            return new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
        return date;
    }

    public static Date getDateTimeByTimeInMs(Long time) {
        if(null == time || time < 1f){
            return new Date();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        return calendar.getTime();
    }


    /***
     * 将yyyy-MM-dd HH:mm:ss.SSS格式的字符串转成yyyyMMdd格式
     * @param time
     * @return
     */
    public static String convertTimeStringDate(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyyMMdd");
        String timeStr = null;
        Date sDate = new Date();
        try {
            if(null != time) {
                sDate = sdf.parse(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sDate = new Date();
        }
        timeStr = dateSdf.format(sDate);
        return timeStr;
    }


    /***
     * 将yyyy-MM-dd HH:mm:ss.SSS格式的字符串转成yyyyMMdd格式
     * @param time
     * @return
     */
    public static String convertTime2EsDate(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timeStr = null;
        Date sDate = new Date();
        try {
            if(null != time) {
                sDate = sdf.parse(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sDate = new Date();
        }
        timeStr = dateSdf.format(sDate);
        return timeStr;
    }


    public static String getStdTimeString(Date sDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(sDate);
        return timeStr;
    }


    public static String addOneSecond(Date sDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sDate);
        calendar.add(Calendar.SECOND,1);
        Date nDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(nDate);
        return timeStr;
    }


    /***
     * 时间增加1秒
     * @param sDate
     * @return
     */
    public static String addOneSecondFromTime(String sDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = sdf.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND,1);
        Date nDate = calendar.getTime();
        String timeStr = sdf.format(nDate);
        return timeStr;
    }



    public static String getStdTimeDate(Date sDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timeStr = sdf.format(sDate);
        return timeStr;
    }


    public static Date convert2StdDate(String sDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = null;
        try {
            time = sdf.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return  null;
        }
        return time;
    }

    public static List<String> getStdTimeHourList(){
        List<String> dateList = new ArrayList<>();
        for (int i=0;i<=23;i++){
            dateList.add(String.valueOf(i));
        }
        return dateList;
    }


    public static List<String> getStdTimeDateList(Date startDate,Date endDate){
        List<String> dateList = new ArrayList<>();
        Date start = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        dateList.add(getStdTimeDate(startDate));
        for (int i=0;i<90;i++){
            calendar.add(Calendar.DATE,1);
            if(calendar.getTime().getTime() > endDate.getTime()){
                break;
            }
            else{
                dateList.add(getStdTimeDate(calendar.getTime()));
            }
        }
        return dateList;
    }

}
