
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public interface ConsoleCommandFactory {

    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth,ResourceFactory resourceFactory);
    public String[] getCommandNames();
    public String getDescription();
    
}
