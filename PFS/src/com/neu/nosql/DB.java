package com.neu.nosql;

import java.util.ArrayList;

public class DB {
    private String name;
    private int suffix;
    private boolean[] bitmap;
    private ArrayList<FCB> fcbList;

    public static DB open(String dbName) throws Exception {
        return null;
    }

    public void put(String fileName) throws Exception {

    }

    public void get(String fileName) throws Exception {

    }

    public String find(String fileName, int key) throws Exception {
        return null;
    }

    public ArrayList<String> dir() throws Exception {
        return null;
    }

    public void kill(String dbName) throws Exception {

    }
}
