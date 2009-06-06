
package com.ettrema.console;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public class Ls extends AbstractConsoleCommand{

    Ls(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        Resource cur = currentResource();
        if( cur == null ) {
            return result("current dir not found: " + cursor.getPath().toString());
        }
        CollectionResource target;
        if( args.size() > 0 ) {
            String dir = args.get(0);
            Cursor c = cursor.find( dir );
            if( !c.exists() ) {
                return result("not found: " + dir);
            } else if( !c.isFolder() ) {
                return result("not a folder: " + dir);
            }
            target = (CollectionResource) c.getResource();
        } else {
            target = currentResource();
        }
        StringBuffer sb = new StringBuffer();
        for( Resource r1 : target.getChildren() ) {
            String href = cursor.getPath().child(r1.getName()).toString();
            sb.append("<a href='").append(href).append("'>").append(r1.getName()).append("</a>").append("<br/>");
        }
        return result(sb.toString());
    }

    private String getHref(String dir, Resource r) {
        return dir + "/" + r.getName();
    }
}
