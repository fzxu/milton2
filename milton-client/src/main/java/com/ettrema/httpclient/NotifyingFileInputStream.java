package com.ettrema.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class NotifyingFileInputStream extends FileInputStream {

    final ProgressListener listener;
    final Throttle throttle;
    final String fileName;
    long pos;
    long totalLength;
    // the system time we last notified the progress listener
    long timeLastNotify;
    long bytesSinceLastNotify;

    public NotifyingFileInputStream(File f, ProgressListener listener, Throttle throttle) throws FileNotFoundException {
        super(f);
        this.throttle = throttle;
        this.listener = listener;
        this.totalLength = f.length();
        this.fileName = f.getAbsolutePath();
        this.timeLastNotify = System.currentTimeMillis();
    }

    @Override
    public int read() throws IOException {
        increment(1);
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        increment(b.length);
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        increment(len);
        return super.read(b, off, len);
    }

    private void increment(int len) {
        pos += len;
        notifyListener(len);
        if (throttle != null) {
            throttle.onRead(len);
        }
    }

    void notifyListener(int numBytes) {
        bytesSinceLastNotify += numBytes;
        if (bytesSinceLastNotify < 1000) {
            //                log.trace( "notifyListener: not enough bytes: " + bytesSinceLastNotify);
            return;
        }
        int timeDiff = (int) (System.currentTimeMillis() - timeLastNotify);
        if (timeDiff > 10) {
            timeLastNotify = System.currentTimeMillis();
            //                log.trace("notifyListener: name: " + fileName);
            if (totalLength <= 0) {
                listener.onProgress(100, fileName);
            } else {
                int percent = (int) ((pos * 100 / totalLength));
                if (percent > 100) {
                    percent = 100;
                }
                listener.onProgress(percent, fileName);
            }
            bytesSinceLastNotify = 0;
        }
    }
}
