package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.ConflictException;
import java.io.IOException;
import java.io.InputStream;

public interface PutableResource extends CollectionResource {
    Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException;
}
