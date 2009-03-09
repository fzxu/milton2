package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.ArrayList;
import java.util.List;

public class Cp extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Cp.class);

    Cp(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }


    public Result execute() {
        String srcPath = args.get(0);
        String destPath = args.get(1);
        log.debug("copy: " + srcPath + "->" + destPath);
        CollectionResource cur = currentResource();
        Path pSrc = Path.path(srcPath);
        Path pDest = Path.path(destPath);
        List<Resource> list = new ArrayList<Resource>();
        Result resultSearch = findWithRegex(cur, pSrc, list);
        if (resultSearch != null) {
            return resultSearch;
        }
        if (list.size() == 0) {
            return result("Source not found: " + srcPath);
        } else {

            Resource rDest = find(cur, Path.path(destPath));
            // if dest exists, must be a folder. In this case keep name
            if (rDest != null) {
                if (rDest instanceof CollectionResource) {
                    CollectionResource destFolder = (CollectionResource) rDest;
                    return copyTo(list, destFolder);
                } else {
                    return result("destination exists but is not a folder");
                }
            } else {
                // dest does not exist, so should be a file. if the path has a sinlge element it is the name to copy to.
                if (list.size() > 1) {
                    return result("No folder: " + destPath);
                }
                Resource theRes = list.get(0);
                return copySingle(cur, theRes, pDest);
            }
        }
    }


    protected Result copyTo(List<Resource> list, CollectionResource destFolder) {
        log.debug("copyTo: " + list.size() + " -> " + destFolder.getName());
        for (Resource res : list) {
            log.debug("copying: " + res.getName());
            if( res instanceof CopyableResource ) {
                CopyableResource cr = (CopyableResource) res;
                cr.copyTo(destFolder, cr.getName());
            }
        }
        return result("Copied to: " + destFolder.getName() );
    }

    
    private Result copySingle(CollectionResource curFolder, Resource theRes, Path pDest) {
        log.debug("copySingle: " + pDest);
        if( theRes instanceof CopyableResource ) {
            CopyableResource cr = (CopyableResource) theRes;
            if (pDest.getLength() == 1) { // copying within the same parent folder
                cr.copyTo(curFolder, pDest.getName());
                return result("Copied to: " + pDest.getName());
            } else { //copying to some other folder
                // is a path ending with a name. Check the parent exists
                Path pParent = pDest.getParent();
                Resource rDest = find(curFolder, pParent);
                if (rDest == null) {
                    return result("The dest path does not exist, nor does its parent");
                } else {
                    if (rDest instanceof CollectionResource) {
                        CollectionResource destFolder = (CollectionResource) rDest;
                        cr.copyTo(destFolder, pDest.getName());
                        return result("Copied to: " + pDest);
                    } else {
                        return result("The destination path does not exist, and its parent is not a folder");
                    }
                }
            }
        } else {
            return result("The specified resource does not support copying");
        }

    }
}
