package com.ettrema.zsync;

import com.bradmcevoy.http.Range;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class RangeListParser {
	public List<Range> parse(InputStream in) throws IOException {
		List<Range> list = new ArrayList<Range>();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
		String line = reader.readLine();
		while( line != null ) {
			Range r = Range.parse(line);
			list.add(r);
			line = reader.readLine();
		}
		return list;
	}


}
