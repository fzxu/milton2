
package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cd extends AbstractConsoleCommand{

    private static final Logger log = LoggerFactory.getLogger(Cd.class);
    
    Cd(List<String> args, String host, String currentDir, ConsoleResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }


    @Override
    public Result execute() {
        log.debug("execute");
        String sPath = args.get(0);
        Path path = Path.path(sPath);
        log.debug("cd path: " + path.toString());
        Resource r;
        Cursor c = cursor.find( path );
        if( !c.exists() ) {
            return result("not found: " + path);
        } else if( !c.isFolder()) {
            return result("not a folder: " + path);
        } else {
            return new Result(c.getPath().toString(),"");
        }
    }

}
