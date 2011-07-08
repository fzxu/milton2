package com.bradmcevoy.http;
 


public class Range {
    private final long start;
    private final long finish;
    
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

    @Override
    public String toString() {
        return "bytes " + start + "-" + finish;
    }

    /**
     * Returns range in String format ("start-end"), ready to be put into
     * HTTP range request
     * @return Range of data in stream
     */
    public String getRange(){
        return start + "-" + finish;
    }	

}
