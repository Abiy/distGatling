package com.walmart.grafana;

/**
 * Created by ahailem on 4/27/16.
 */
public class FileFinder {
    public String getFile(String fileName){
        return  this.getClass().getResource(fileName).getFile();
    }
}
