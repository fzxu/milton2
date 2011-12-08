package com.ettrema.http.caldav;

import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.values.HrefList;
import com.bradmcevoy.http.values.HrefListValueWriter;
import com.bradmcevoy.http.values.PropFindResponseListWriter;
import com.bradmcevoy.http.values.ValueWriter;
import com.bradmcevoy.http.values.ValueWriters;
import com.bradmcevoy.http.webdav.PropFindPropertyBuilder;
import com.bradmcevoy.http.webdav.PropFindXmlGenerator;
import com.bradmcevoy.http.webdav.PropFindXmlGeneratorHelper;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.property.PropertySource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

import org.jdom.Document;
import static org.easymock.EasyMock.*;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author bradm
 */
public class ExpandPropertyReportTest extends TestCase {
	
	ResourceFactory resourceFactory;
	PropFindableResource otherResource;
	PropFindPropertyBuilder propertyBuilder;
	ExpandPropertyReport rep;
	PropertySource propertySource;
	PropertySource.PropertyMetaData meta;
	List<PropertySource> propertySources;
	PropFindXmlGenerator xmlGenerator;
	PropFindXmlGeneratorHelper xmlGeneratorHelper;
	ValueWriters valueWriters;
	
	public ExpandPropertyReportTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		propertySource = createMock(PropertySource.class);
		xmlGeneratorHelper = new PropFindXmlGeneratorHelper();
		List<ValueWriter> writers = Arrays.asList(new HrefListValueWriter(), new PropFindResponseListWriter(xmlGeneratorHelper));
		valueWriters = new ValueWriters(writers);
		xmlGeneratorHelper.setValueWriters(valueWriters);
		xmlGenerator = new PropFindXmlGenerator(valueWriters);
		propertySources = Arrays.asList(propertySource);
		meta = new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.READ_ONLY, HrefList.class);
		propertyBuilder = new PropFindPropertyBuilder(propertySources);
		otherResource = createMock(PropFindableResource.class);
		resourceFactory = createMock(ResourceFactory.class);
				
		rep = new ExpandPropertyReport(resourceFactory, propertyBuilder, xmlGenerator);
		
	}

	public void testProcess() throws JDOMException, IOException, NotAuthorizedException {
		PropFindableResource pfr = createMock(PropFindableResource.class);
		SAXBuilder builder = new org.jdom.input.SAXBuilder();
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
						"<D:expand-property xmlns:D=\"DAV:\">" +
						"<D:property name=\"version-history\">" +
						" <D:property name=\"version-set\">" +
						"   <D:property name=\"creator-displayname\"/>" +
						"   <D:property name=\"activity-set\"/>" +
						" </D:property>" +
					   "</D:property>" +
					 "</D:expand-property>";
		QName qname = new QName(WebDavProtocol.DAV_URI, "version-history");
		InputStream in = new ByteArrayInputStream(xml.getBytes());
		Document doc = builder.build(in);
		
		HrefList hrefList = new HrefList();
		hrefList.add("/other");
		expect(propertySource.getPropertyMetaData(eq(qname), same(pfr))).andReturn(meta);
		expect(propertySource.getProperty(eq(qname), same(pfr))).andReturn(hrefList);
		expect(resourceFactory.getResource("host", "/other")).andReturn(otherResource);
		replay(propertySource,  resourceFactory);
		
		xml = rep.process("host", "/path",  pfr, doc);
		
		System.out.println("expand property report:");
		System.out.println(xml);
		verify(propertySource,  resourceFactory);
	}

	public void testGetName() {
	}
}
