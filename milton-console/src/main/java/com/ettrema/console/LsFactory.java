
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public class LsFactory implements ConsoleCommandFactory {

    @Override
    public String[] getCommandNames() {
        return new String[]{"ls"};
    }

    
    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth,ResourceFactory resourceFactory) {
        return new Ls(args,host,currentDir,resourceFactory);
    }

    @Override
    public String getDescription() {
        return "List. List contents of the current or a specified directory";
    }

    
}
