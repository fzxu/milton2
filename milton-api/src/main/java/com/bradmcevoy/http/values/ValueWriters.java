package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class ValueWriters {

    private final List<ValueWriter> writers;

    public ValueWriters(List<ValueWriter> valueWriters) {
        this.writers = valueWriters;
    }

    public ValueWriters() {
        writers = new ArrayList<ValueWriter>();
        writers.add(new LockTokenValueWriter());
        writers.add(new SupportedLockValueWriter());
        writers.add(new ModifiedDateValueWriter());
        writers.add(new DateValueWriter());
        writers.add(new ResourceTypeValueWriter());
        writers.add(new BooleanValueWriter());
        writers.add(new CDataValueWriter());
        writers.add(new CDataValueWriter());
        writers.add(new UUIDValueWriter());
        writers.add(new ToStringValueWriter());
    }

    public void writeValue(XmlWriter writer, QName qname, String prefix, ValueAndType vat, String href, Map<String, String> nsPrefixes) {
        for (ValueWriter vw : writers) {
            if (vw.supports(qname.getNamespaceURI(), qname.getLocalPart(), vat.getType())) {
                vw.writeValue(writer, qname.getNamespaceURI(), prefix, qname.getLocalPart(), vat.getValue(), href, nsPrefixes);
                break;
            }
        }
    }

    public List<ValueWriter> getValueWriters() {
        return writers;
    }

    public Object parse(QName qname, Class valueType, String value) {
        for (ValueWriter vw : writers) {
            if (vw.supports(qname.getNamespaceURI(), qname.getLocalPart(), valueType)) {
                return vw.parse(qname.getNamespaceURI(), qname.getLocalPart(), value);
            }
        }
        return null;
    }
}
