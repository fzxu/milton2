package com.ettrema.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mcevoyb
 */
class Utils {

    static void close(InputStream in) {
        try {
            if (in == null) {
                return;
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void close(OutputStream out) {
        try {
            if (out == null) {
                return;
            }
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void write(InputStream in, OutputStream out) throws IOException {
        byte[] arr = new byte[1024];
        int s = in.read(arr);
        while (s >= 0) {
            out.write(arr, 0, s);
            s = in.read(arr);
        }
    }

    static void processResultCode(int result, String href) throws com.ettrema.httpclient.HttpException {
        if (result >= 200 && result < 300) {
            return;
        } else if (result >= 300 && result < 400) {
            switch (result) {
                case 301:
                    throw new RedirectException(result, href);
                case 302:
                    throw new RedirectException(result, href);
                case 304:
                    break;
                default:
                    throw new RedirectException(result, href);
            }
        } else if (result >= 400 && result < 500) {
            switch (result) {
                case 400:
                    throw new BadRequestException(result, href);
                case 401:
                    throw new Unauthorized(result, href);
                case 403:
                    throw new Unauthorized(result, href);
                case 404:
                    throw new NotFoundException(result, href);
                case 405:
                    throw new MethodNotAllowedException(result, href);
                default:
                    throw new GenericHttpException(result, href);
            }
        } else if (result >= 500 && result < 600) {
            throw new InternalServerError(href, result);
        } else {
            throw new GenericHttpException(result, href);
        }

    }
}
