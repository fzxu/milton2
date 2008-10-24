package com.bradmcevoy.http;

import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.io.XmlUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class LockInfo {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LockInfo.class);

    public enum LockScope {
        NONE,
        SHARED,
        EXCLUSIVE
    }

    public enum LockType {
        READ,
        WRITE
    }

    public enum LockDepth {
        ZERO,
        INFINITY
    }
    
    public static LockInfo parseLockInfo(Request request) throws IOException, FileNotFoundException, JDOMException  {
        InputStream in = request.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamToStream.readTo(in, out);
        String xml = new String(out.toByteArray());        
        xml = xml.trim();
        if( xml.length() > 0 ) {
            final LockInfo info = new LockInfo();
            Document doc = XmlUtils.getDomDocument(xml);

            XmlUtils.process(doc.getRootElement(), new XmlUtils.XmlNodeOperator() {
                @Override
                public void process(Element n) {
                    if( n instanceof Element ) {
                        Element el = (Element) n;
                        String nodeName = el.getName();
                        if( nodeName.endsWith("lockscope")) {
                            List ch = el.getChildren();
                            if( ch != null && ch.size()>0 ) {
                                Element n2 = (Element) el.getChildren().get(0);
                                String s = n2.getName().toLowerCase();
                                if( s.endsWith("exclusive") ) {
                                    info.scope = LockScope.EXCLUSIVE;
                                } else if( s.endsWith("shared")) {
                                    info.scope = LockScope.SHARED;
                                } else {
                                    info.scope = LockScope.NONE;
                                }                                    
                            }
                        } else if( nodeName.endsWith("locktype")) {
                            List ch = el.getChildren();
                            if( ch != null && ch.size()>0 ) {
                                Element n2 = (Element) el.getChildren().get(0);
                                String s = n2.getName().toLowerCase();
                                if( s.endsWith("read") ) {
                                    info.type = LockType.READ;
                                } else if( s.endsWith("write")) {
                                    info.type = LockType.WRITE;
                                } else {
                                    log.warn("Unknown lock type: " + s);
                                    info.type = LockType.WRITE;
                                }                            
                            }
                        } else if( nodeName.endsWith("owner")) {
                            List ch = el.getChildren();
                            if( ch != null && ch.size()>0 ) {
                                Element n2 = (Element) ch.get(0);
                                if( n2 instanceof Element ) {
                                    Element e2 = (Element) n2;
                                    info.owner = e2.getText();
                                }
                            }
                        }
                    }
                }
            });
            info.depth = LockDepth.INFINITY; // todo
            log.debug("parsed lock info: " + info);
            return info;
        } else {
            return null;
        }
        
    }
    

    public LockScope scope;
    public LockType type;
    public String owner;
    public LockDepth depth;
    
    public LockInfo(LockScope scope, LockType type, String owner, LockDepth depth) {
        this.scope = scope;
        this.type = type;
        this.owner = owner;
        this.depth = depth;
    }

    public LockInfo() {
    }
    
        
    @Override
    public String toString() {
        return "scope: " + scope.name() + ", type: " + type.name() + ", owner: " + owner + ", depth:" + depth;
    }
    
    
}
