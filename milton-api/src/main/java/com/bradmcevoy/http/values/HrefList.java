package com.bradmcevoy.http.values;

import java.util.ArrayList;

/**
 * Holds a list of href values which will be written as a list of <href> elements
 *
 * See HrefListValueWriter
 *
 * @author brad
 */
public class HrefList extends ArrayList<String> {

    private static final long serialVersionUID = 1L;

    public static HrefList asList(String... items) {
        HrefList l = new HrefList();
        for (String s : items) {
            l.add(s);
        }
        return l;
    }
}
