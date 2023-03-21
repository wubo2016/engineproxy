package com.wyuansmart.phone.engine.dto.orc;

import java.util.Date;

public class DateStatDto {

    /**
     * 日期
     */
    private Date date;

    /**
     * 总线
     */
    private Long count;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
