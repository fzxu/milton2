
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public class CpFactory implements ConsoleCommandFactory{
    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth,ResourceFactory resourceFactory) {
        return new Cp(args, host, currentDir, resourceFactory);
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"cp","copy"};
    }

    @Override
    public String getDescription() {
        return "Copies a file or folder to a destination file or folder";
    }

    public void setConsoleResourceFactory(ConsoleResourceFactory crf) {
        
    }
}
