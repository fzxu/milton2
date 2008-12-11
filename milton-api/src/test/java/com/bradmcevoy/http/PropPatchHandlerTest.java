package com.bradmcevoy.http;

import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 */
public class PropPatchHandlerTest extends TestCase {



    public void testParseContent_MSOffice() throws Exception  {
        InputStream in = this.getClass().getResourceAsStream("proppatch_request_msoffice.xml");
        assertNotNull(in);
        PropPatchHandler.Fields fields = PropPatchHandler.parseContent(in);
        assertNotNull(fields);
        assertEquals(3, fields.setFields.size());
        assertEquals(0, fields.removeFields.size());
        assertEquals("Win32LastAccessTime",fields.setFields.get(0).name);
        assertEquals("Win32LastModifiedTime",fields.setFields.get(1).name);
        assertEquals("Win32FileAttributes",fields.setFields.get(2).name);

        assertEquals("Wed, 10 Dec 2008 21:55:22 GMT",fields.setFields.get(0).value);
        assertEquals("Wed, 10 Dec 2008 21:55:22 GMT",fields.setFields.get(1).value);
        assertEquals("00000020",fields.setFields.get(2).value);

    }

    public void testParseContent_Spec() throws Exception  {
        InputStream in = this.getClass().getResourceAsStream("proppatch_request_spec.xml");
        assertNotNull(in);
        PropPatchHandler.Fields fields = PropPatchHandler.parseContent(in);
        assertNotNull(fields);
        assertEquals(2, fields.setFields.size());
        assertEquals(1, fields.removeFields.size());
    }
}
