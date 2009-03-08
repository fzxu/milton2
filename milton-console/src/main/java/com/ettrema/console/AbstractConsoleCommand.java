
package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConsoleCommand implements ConsoleCommand{
    private static final Logger log = LoggerFactory.getLogger(AbstractConsoleCommand.class);
    
    protected final List<String> args;
    protected final String host;
    protected Path currentDir;
    protected final ResourceFactory resourceFactory;

    Path lastPath;

    AbstractConsoleCommand(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        this.args = args;
        this.host = host;
        this.currentDir = Path.path(currentDir);
        lastPath = Path.path(currentDir);
        this.resourceFactory = resourceFactory;
    }    
    
    
    protected Resource find(Path p) {
        return resourceFactory.getResource(host, p.toString());
    }

    /**
     * The current resource must be a collection
     *
     * @return
     */
    protected CollectionResource currentResource() {
        return (CollectionResource) resourceFactory.getResource(host, currentDir.toString());
    }
    
    protected Result result(String msg) {
        return new Result(currentDir.toString(),msg);
    }    
    
    protected Resource find(Resource cur, Path path) {
        log.debug("find: " + path);
        if( path.isRoot() && !path.isRelative() ) {
            return host();
        }
        if( cur == null ) return null;
        Path parent = path.getParent();
        String moveTo = path.getName();
        
        if( parent == null ) {
            return move(cur,moveTo);
        } else {
            Resource r = find(cur,parent);
            return move(r,moveTo);
        }
    }

    protected Resource move(Resource cur, String p) {
        log.debug("move: lastPath is: " + lastPath.toString() );
        if( p.equals("..")) {
            lastPath = lastPath.getParent();
            return resourceFactory.getResource(host, lastPath.toString());
        } else if( p.equals(".") ) {
            return cur;
        } else {            
            if( cur instanceof CollectionResource ) {
                CollectionResource col = (CollectionResource)cur;
                Resource child = col.child(p);
                lastPath = Path.path(lastPath,child.getName());
                return child;
            } else {
                throw new RuntimeException("Not a folder: " + cur.getName());
            }
        }
    }
    
    protected Resource host() {
        return (Resource) find(Path.root);
    }    
    

    /**
     * Note that only the last part of the path is matched against a regular expression
     * 
     * @param cur
     * @param path
     * @param list
     * @return
     */
    protected Result findWithRegex(CollectionResource cur, Path path, List<Resource> list) {
        log.debug("findWithWildCard");
        CollectionResource start = cur;
        if (path.getLength() > 1) {
            Path pathToStart = path.getParent();
            Resource resStart = find(cur, pathToStart);
            if (resStart == null) {
                return result("Couldnt find path: " + pathToStart);
            }
            if (resStart instanceof CollectionResource) {
                start = (CollectionResource) resStart;
            } else {
                return result("is not a folder: " + pathToStart);
            }
        }
        Pattern pattern = null;
        try {
            log.debug("findWithWildCard: compiling " + path.getName());
            pattern = Pattern.compile(path.getName());
        } catch (Exception e) {
            return result("Couldnt compile regular expression: " + path.getName());
        }
        for (Resource res : start.getChildren()) {
                Matcher m = pattern.matcher(res.getName());
                if (m.matches()) {
                    log.debug("findWithWildCard: matches: " + res.getName());
                    list.add(res);
                } else {
                    log.debug("findWithWildCard: does not match: " + res.getName());
                }
        }
        return null;
    }    
}
