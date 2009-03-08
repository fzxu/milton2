
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public class RmFactory implements ConsoleCommandFactory {

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth,ResourceFactory resourceFactory) {
        return new Rm(args, host, currentDir, resourceFactory);
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"rm","delete","del"};
    }

    @Override
    public String getDescription() {
        return "Remove. Removes a file or folder by path or name, including regular expressions";
    }
}
