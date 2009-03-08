
package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public class Cd extends AbstractConsoleCommand{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Cd.class);
    
    Cd(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }


    @Override
    public Result execute() {
        log.debug("execute");
        String sPath = args.get(0);
        Path path = Path.path(sPath);
        log.debug("cd path: " + path.toString());
        Resource r;
        if( path.isRelative() ) {
            log.debug("relative");
            CollectionResource cur = currentResource();
            if( cur == null ) {
                return result("current directory not found: " + currentDir);
            }
            r = find(cur,path);
        } else {
            log.debug("abs");
            CollectionResource h = (CollectionResource) host();
            if( h == null ) {
                log.warn("didnt find current host: " + host);
                return result("root folder for host not found");
            }
            r = find(h,path);
        }
        if( r == null ) {
            return result("not found123: " + path);
        }
        return new Result(lastPath.toString(),"");
    }

}
