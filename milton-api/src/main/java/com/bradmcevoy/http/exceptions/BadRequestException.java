package com.bradmcevoy.http.exceptions;

import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class BadRequestException extends MiltonException {
    private static final long serialVersionUID = 1L;

    public BadRequestException(Resource r) {
        super(r);
    }

}
