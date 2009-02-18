package com.ettrema.http.fs;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 */
public class FsDirectoryResource extends FsResource implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource,  MoveableResource, PropFindableResource{
    
    public FsDirectoryResource(FileSystemResourceFactory factory, File dir) {
        super(factory, dir);
        if( !dir.exists() ) throw new IllegalArgumentException("Directory does not exist: " + dir.getAbsolutePath());
        if( !dir.isDirectory() ) throw new IllegalArgumentException("Is not a directory: " + dir.getAbsolutePath());
    }

    public CollectionResource createCollection(String name) {
        File fnew = new File(file,name);
        boolean ok = fnew.mkdir();
        if( !ok ) throw new RuntimeException("Failed to create: " + fnew.getAbsolutePath());
        return new FsDirectoryResource(factory, fnew);
    }

    public Resource child(String name) {
        File fchild = new File(file,name);
        return factory.resolveFile(fchild);
        
    }

    public List<? extends Resource> getChildren() {
        ArrayList<FsResource> list = new ArrayList<FsResource>();
        for( File fchild : this.file.listFiles()) {
            FsResource res = factory.resolveFile(fchild);
            list.add(res);
        }
        return list;
    }


    public String checkRedirect(Request request) {
        return request.getAbsoluteUrl() + "/index.html";
    }

    public Resource createNew(String name, InputStream in, Long length, String contentType) throws IOException {
        File dest = new File(this.getFile(), name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
            // todo: ignores contentType
            return factory.resolveFile(dest);

    }

    @Override
    protected void doCopy(File dest) {
        try {
            FileUtils.copyDirectory(this.getFile(), dest);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to copy to:" + dest.getAbsolutePath() , ex);
        }
    }
    
    
    

}
