package com.leederedu.elastic.entity;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class Info {

    private long id;
    private String name;
    private String url;
    private String context;
    private int sortNum;

    public long getId() {
        return id;
    }

    public Info setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Info setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Info setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getContext() {
        return context;
    }

    public Info setContext(String context) {
        this.context = context;
        return this;
    }

    public int getSortNum() {
        return sortNum;
    }

    public Info setSortNum(int sortNum) {
        this.sortNum = sortNum;
        return this;
    }

    @Override
    public String toString() {
        return "Info[id=" + this.id
                + ", name=" + this.name
                + ", url=" + this.url
                + ", comtext=" + this.context
                + ", sortNum=" + this.sortNum
                + "]";
    }
}
