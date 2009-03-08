package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Rm extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Cd.class);

    Rm(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        String sPath = args.get(0);

        Path path = Path.path(sPath);
        List<Resource> list = new ArrayList<Resource>();
        CollectionResource curFolder = currentResource();
        Result resultSearch = findWithRegex(curFolder, path, list);
        if (resultSearch != null) {
            return resultSearch;
        }

        if (list.size() == 0) {
            return result("not found: " + sPath);
        }
        StringBuffer sb = new StringBuffer();
        for (Resource r : list) {
            if( r instanceof DeletableResource ) {
                DeletableResource dr = (DeletableResource) r;
                dr.delete();
                sb.append(r.getName()).append(",");
            }
        }
        return result("deleted " + sb.toString());

    }
}
