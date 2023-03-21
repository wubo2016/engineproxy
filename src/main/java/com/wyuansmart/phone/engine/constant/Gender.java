package com.wyuansmart.phone.engine.constant;

public class Gender {
    /**
     * 性别男中文
     */
    public final static String SEX_MAN = "男";

    /**
     * 性别女中文
     */
    public final static String SEX_WOMAN = "女";

    /**
     * 男 在数据库中的值1
     */
    public final static int MAN = 1;

    /**
     * 女 在数据库中的值2
     */
    public final static int WOMAN = 2;

    /**
     *未知的性别
     */
    public final static int UNKNOWN = 0;

    public static int toGender(String gender){
        if(SEX_MAN.equals(gender)){
            return MAN;
        }else if(SEX_WOMAN.equals(gender)){
            return WOMAN;
        }else {
            return UNKNOWN;
        }
    }
}
