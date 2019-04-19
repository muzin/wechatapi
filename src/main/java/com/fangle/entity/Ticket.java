package com.fangle.entity;

import com.fangle.util.StringUtils;

import java.util.Date;

public class Ticket {

    private String ticket;

    private Long expireTime;

    public Ticket(String ticket, Long expireTime){
        this.ticket = ticket;
        this.expireTime = expireTime;
    }

    /**
     * 设置 票据
     * Examples:
     * ```
     * token.setTicket("...");
     * ```
     * @param ticket
     * @return Ticket
     */
    public Ticket setTicket(String ticket){
        this.ticket = ticket;
        return this;
    }

    /**
     * 获取 ticket
     * Examples:
     * ```
     * String token = token.getTicket();
     * ```
     * @return String
     */
    public String getTicket(){
        return this.ticket;
    }

    /**
     * 获取过期时间
     * Examples:
     * ```
     * Long time = token.getExpireTime();
     * ```
     * @return String
     */
    public Long getExpireTime(){
        return this.expireTime;
    }

    /**
     * 设置 过期时间
     * Examples:
     * ```
     * token.setExpireTime(123456789);
     * ```
     * @param expireTime 过期时间
     * @return AccessToken
     */
    public Ticket setExpireTime(Long expireTime){
        this.expireTime = expireTime;
        return this;
    }


    /**
     * 检查ticket是否有效，检查规则为当前时间和过期时间进行对比
     * Examples:
     * ```
     * token.isValid();
     * ```
     */
    public boolean isValid () {
        return StringUtils.notEmpty(this.ticket)
                && new Date().getTime() < this.expireTime;
    }

}
