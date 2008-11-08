package com.bradmcevoy.http;



public class Range {
    final long start;
    final long finish;
    
    public Range(long start, long finish) {
        this.start = start;
        this.finish = finish;
    }

    public long getStart() {
        return start;
    }

    public long getFinish() {
        return finish;
    }        
}