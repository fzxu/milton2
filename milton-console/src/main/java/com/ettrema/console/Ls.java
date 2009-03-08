
package com.ettrema.console;

import com.bradmcevoy.common.Path;
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
            return result("current dir not found: " + currentDir);
        }
        if( args.size() > 0 ) {
            String dir = args.get(0);
            cur = (CollectionResource) find(cur, Path.path(dir));
            if( cur == null ) return result("not found: " + dir);
        }
        if( cur instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource) cur;
            StringBuffer sb = new StringBuffer();
            for( Resource r1 : col.getChildren() ) {
                String href = lastPath.child(r1.getName()).toString();
                sb.append("<a href='").append(href).append("'>").append(r1.getName()).append("</a>").append("<br/>");
            }
            return result(sb.toString());
        } else {
            return result("not a collection: " + cur.getName() + " - " + cur.getClass());
        }
        
    }

    private String getHref(String dir, Resource r) {
        return dir + "/" + r.getName();
    }
}
