
package com.ettrema.console;

import com.bradmcevoy.http.ResourceFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Help extends AbstractConsoleCommand {

    private ConsoleResourceFactory consoleResourceFactory;

    Help(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, ConsoleResourceFactory consoleResourceFactory) {
        super(args, host, currentDir, resourceFactory);
        this.consoleResourceFactory = consoleResourceFactory;
    }
    
    @Override
    public Result execute() {
        StringBuffer sb = new StringBuffer();
        List<ConsoleCommandFactory> list = new ArrayList<ConsoleCommandFactory>();
        list.addAll(consoleResourceFactory.factories );
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                ConsoleCommandFactory f1 = (ConsoleCommandFactory)o1;
                ConsoleCommandFactory f2 = (ConsoleCommandFactory)o2;
                return f1.getCommandNames()[0].compareTo(f2.getCommandNames()[0]);
            }
        });
        for( ConsoleCommandFactory f : list ) {
            sb.append("<b>");
            for( String s : f.getCommandNames() ) {
                sb.append(s).append(" ");
            }
            sb.append("</b>");
            sb.append("<br/>").append("\n");
            sb.append("<br/>").append(f.getDescription());
            sb.append("<br/>").append("\n");
            sb.append("<br/>").append("\n");
        }        
        return new Result(this.lastPath.toString(), sb.toString());
    }

}
