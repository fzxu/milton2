
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;

public class HelpFactory implements ConsoleCommandFactory {

    private ConsoleResourceFactory consoleResourceFactory;

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth,ResourceFactory resourceFactory) {
        return new Help(args, host, currentDir, resourceFactory, consoleResourceFactory);
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"help"};
    }

    @Override
    public String getDescription() {
        return "Help. Display all commands";
    }

    public void setConsoleResourceFactory(ConsoleResourceFactory crf) {
        this.consoleResourceFactory = crf;
    }
    
    

}
