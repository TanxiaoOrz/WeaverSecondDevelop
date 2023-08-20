package com.customization.commons;


import weaver.conn.RecordSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义工具类
 * @author
 */
public class CustomUtil {

	public static String dateFormat(String format) {
		return dateFormat(new Date(), format);
	}

	public static String dateFormat(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	public static Double getDouble(String v) {
		return isBlank(v) ? null : getDouble(v, -1.0);
	}

    /**
     * 20200220121212转换2020-02-20 12-12
     *
     * @param s
     * @return
     */
    public synchronized static String getDateofhour(String s) {
        String r = "";
        Date d = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            d = sdf.parse(s);
            r = sdf1.format(d);
        } catch (Exception e) {
            // 格式不正确
        }
        return r;
    }

    /**
     * 20200220转换2020-02-20
     *
     * @param s
     * @return
     */
    public synchronized static String stringFormatToString(String s) {
        String r = "";
        Date d = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            d = sdf.parse(s);
            r = sdf1.format(d);
        } catch (Exception e) {
            // 格式不正确
        }
        System.out.println("格式化结果 = " + r);
        return r;
    }

	public static Double getDouble(String v, Double defValue) {
		try {
			return Double.parseDouble(v);
		} catch (Exception e) {
			return defValue;
		}
	}

	/**
	 * 如果字符串为空返回Null，否则转换成Integer返回，如果转换失败则抛出异常。
	 * @param v
	 * @return
	 */
	public static Integer getInteger(String v) {
		return isBlank(v) ? null : Integer.parseInt(v);
	}

	/**
	 * 将对象转换成字符串。
	 * @param value
	 * @return
	 */
	public static Integer getInteger(Object value, Integer defValue) {
		try {
			return Integer.parseInt(getString(value));
		} catch (Exception e) {
		}
		return defValue;
	}

	/**
	 * 将字符串转换为整型，如果转化失败返回-1。
	 * @param v
	 * @return
	 */
	public static int getInt(String v) {
		return getInt(v, -1);
	}

	/**
	 * 将字符串转换成整数，如果转换失败返回默认值。
	 * @param v	字符串
	 * @param defValue	默认值
	 * @return	转换后的整数
	 */
	public static int getInt(String v, Integer defValue) {
		try {
			return Integer.parseInt(v);
		} catch (Exception e) {
			return defValue;
		}
	}

	public static boolean isBlank(String v) {
		return v == null || v.trim().length() == 0;
	}

	public static boolean isNotBlank(String v) {
		return !isBlank(v);
	}

	public static String getDBValue(String val) {
		if (val != null) {
			val = "'" + val + "'";
		}
		return val;
	}

	public static String getString(Object obj) {
		return getString(obj, null);
	}

	public static String getString(Object obj, String defValue) {
		if (obj == null) {
			return defValue;
		}
		return obj.toString();
	}

	public static String getStringLeft(String str, int len) {
		if (isBlank(str)) {
			return "";
		}
		len = str.length() > len ? len : str.length();
		return str.substring(0, len);
	}

	public static void checkStringEmpty(String str, String errMessage) {
		if (str == null || str.isEmpty()) {
			throw new RuntimeException(errMessage);
		}
	}

	public static void checkNull(Object o, String errMessage) {
		if (o == null) {
			throw new RuntimeException(errMessage);
		}
	}

	public static void checkArrayEmpty(Object o, String errMessage) {
		if (o == null) {
			throw new RuntimeException(errMessage);
		}
	}

	public static List<String> getDeclares(String str) {
		Pattern p = Pattern.compile("(?<=\\u007B).*?(?=\\u007D)");
		Matcher m = p.matcher(str);
		List<String> result = new ArrayList<String>();
		while (m.find()){
			if (m.group() != null){
				result.add(m.group());
			}
		}
		return result;
	}

	public static void stdout(StackTraceElement[] stackTrace, Object... args){
		StackTraceElement s = stackTrace[1];
		String str = String.format("[%s] %s.%s(Line: %d) ", CustomUtil.dateFormat(new Date(), "HH:mm:ss.SSS"),
				s.getClassName(), s.getMethodName(), s.getLineNumber());
		for (Object obj : args) {
			str += ", " + obj;
		}
		System.out.println(str);
	}

	public static String getStringDate(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	public static String getStringDate(String format, String date) throws ParseException {

		String olddate=date;


		if(date.isEmpty()||date.equals("00000000")){
			return  "";
		}
		if(date.contains("-")){
			date=date.replace("-","");
		}

		if(format.contains("-")){
			Date rq = new SimpleDateFormat("yyyyMMdd").parse(date);
			SimpleDateFormat sdf =  new SimpleDateFormat(format);
			date=sdf.format(rq);
		}

		return date;
	}

	public static String upperFirstWord(String str) {
		String firstWord = str.substring(0, 1).toUpperCase();
		return firstWord + str.substring(1);
	}

	public static String replace(String str){
		str=str.replaceAll("'","''");
		str=str.trim();
		return str;
	}

	public static char getSeparator() {
		return '\002';
	}

	public static String  MerchantCode2name(String code){
		if(code.isEmpty())return "";

		String  name="";
		RecordSet rs= new RecordSet();
		rs.execute("select  NAME_ORG1 from uf_Merchant where  PARTNER= '" + code + "'");
		if (rs.next()) {
			name = rs.getString("NAME_ORG1");
		}
		return  name;
	}

	public static void main(String[] args) {
		try {
			System.out.println(getStringDate("yyyyMMdd",null));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
