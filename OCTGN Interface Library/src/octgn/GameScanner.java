/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package octgn;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class GameScanner {
    /**
     * org.w3c.dom.Document document
     */
    org.w3c.dom.Document document;

    /**
     * Create new GameScanner with org.w3c.dom.Document.
     * @param document 
     */
    public GameScanner(org.w3c.dom.Document document) {
        this.document = document;
    }

    /**
     * Scan through org.w3c.dom.Document document.
     */
    public void visitDocument() {
        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals("game")) {//change to game for a match on first element
            visitElementGame(element);
        }
        if ((element != null) && element.getTagName().equals("xs:simpleType")) {
            visitElement_xs_simpleType(element);
        }
        if ((element != null) && element.getTagName().equals("xs:annotation")) {
            visitElement_xs_annotation(element);
        }
        if ((element != null) && element.getTagName().equals("xs:documentation")) {
            visitElement_xs_documentation(element);
        }
        if ((element != null) && element.getTagName().equals("xs:restriction")) {
            visitElement_xs_restriction(element);
        }
        if ((element != null) && element.getTagName().equals("xs:pattern")) {
            visitElement_xs_pattern(element);
        }
        if ((element != null) && element.getTagName().equals("xs:enumeration")) {
            visitElement_xs_enumeration(element);
        }
        if ((element != null) && element.getTagName().equals("xs:element")) {
            visitElement_xs_element(element);
        }
        if ((element != null) && element.getTagName().equals("xs:complexType")) {
            visitElement_xs_complexType(element);
        }
        if ((element != null) && element.getTagName().equals("xs:sequence")) {
            visitElement_xs_sequence(element);
        }
        if ((element != null) && element.getTagName().equals("xs:attribute")) {
            visitElement_xs_attribute(element);
        }
        if ((element != null) && element.getTagName().equals("xs:choice")) {
            visitElement_xs_choice(element);
        }
        if ((element != null) && element.getTagName().equals("xs:complexContent")) {
            visitElement_xs_complexContent(element);
        }
        if ((element != null) && element.getTagName().equals("xs:extension")) {
            visitElement_xs_extension(element);
        }
        if ((element != null) && element.getTagName().equals("xs:group")) {
            visitElement_xs_group(element);
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:schema.
     */
    void visitElementGame(org.w3c.dom.Element element) {
        // <xs:schema>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("xmlns:xs")) {
                // <xs:schema xmlns:xs="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:simpleType")) {
                        visitElement_xs_simpleType(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:element")) {
                        visitElement_xs_element(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:complexType")) {
                        visitElement_xs_complexType(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:group")) {
                        visitElement_xs_group(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:simpleType.
     */
    void visitElement_xs_simpleType(org.w3c.dom.Element element) {
        // <xs:simpleType>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("name")) {
                // <xs:simpleType name="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:annotation")) {
                        visitElement_xs_annotation(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:restriction")) {
                        visitElement_xs_restriction(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:annotation.
     */
    void visitElement_xs_annotation(org.w3c.dom.Element element) {
        // <xs:annotation>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:documentation")) {
                        visitElement_xs_documentation(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:documentation.
     */
    void visitElement_xs_documentation(org.w3c.dom.Element element) {
        // <xs:documentation>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
                case org.w3c.dom.Node.TEXT_NODE:
                    // ((org.w3c.dom.Text)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:restriction.
     */
    void visitElement_xs_restriction(org.w3c.dom.Element element) {
        // <xs:restriction>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("base")) {
                // <xs:restriction base="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:pattern")) {
                        visitElement_xs_pattern(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:enumeration")) {
                        visitElement_xs_enumeration(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:pattern.
     */
    void visitElement_xs_pattern(org.w3c.dom.Element element) {
        // <xs:pattern>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("value")) {
                // <xs:pattern value="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:enumeration.
     */
    void visitElement_xs_enumeration(org.w3c.dom.Element element) {
        // <xs:enumeration>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("value")) {
                // <xs:enumeration value="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:element.
     */
    void visitElement_xs_element(org.w3c.dom.Element element) {
        // <xs:element>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("type")) {
                // <xs:element type="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("maxOccurs")) {
                // <xs:element maxOccurs="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("minOccurs")) {
                // <xs:element minOccurs="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("name")) {
                // <xs:element name="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:annotation")) {
                        visitElement_xs_annotation(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:complexType")) {
                        visitElement_xs_complexType(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:complexType.
     */
    void visitElement_xs_complexType(org.w3c.dom.Element element) {
        // <xs:complexType>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("abstract")) {
                // <xs:complexType abstract="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("name")) {
                // <xs:complexType name="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:annotation")) {
                        visitElement_xs_annotation(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:sequence")) {
                        visitElement_xs_sequence(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:attribute")) {
                        visitElement_xs_attribute(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:choice")) {
                        visitElement_xs_choice(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:complexContent")) {
                        visitElement_xs_complexContent(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:group")) {
                        visitElement_xs_group(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:sequence.
     */
    void visitElement_xs_sequence(org.w3c.dom.Element element) {
        // <xs:sequence>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:element")) {
                        visitElement_xs_element(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:choice")) {
                        visitElement_xs_choice(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:group")) {
                        visitElement_xs_group(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:attribute.
     */
    void visitElement_xs_attribute(org.w3c.dom.Element element) {
        // <xs:attribute>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("default")) {
                // <xs:attribute default="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("use")) {
                // <xs:attribute use="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("type")) {
                // <xs:attribute type="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("name")) {
                // <xs:attribute name="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:simpleType")) {
                        visitElement_xs_simpleType(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:annotation")) {
                        visitElement_xs_annotation(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:choice.
     */
    void visitElement_xs_choice(org.w3c.dom.Element element) {
        // <xs:choice>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("maxOccurs")) {
                // <xs:choice maxOccurs="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("minOccurs")) {
                // <xs:choice minOccurs="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:element")) {
                        visitElement_xs_element(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:complexContent.
     */
    void visitElement_xs_complexContent(org.w3c.dom.Element element) {
        // <xs:complexContent>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:extension")) {
                        visitElement_xs_extension(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:extension.
     */
    void visitElement_xs_extension(org.w3c.dom.Element element) {
        // <xs:extension>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("base")) {
                // <xs:extension base="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:attribute")) {
                        visitElement_xs_attribute(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:choice")) {
                        visitElement_xs_choice(nodeElement);
                    }
                    if (nodeElement.getTagName().equals("xs:group")) {
                        visitElement_xs_group(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }

    /**
     * Scan through org.w3c.dom.Element named xs:group.
     */
    void visitElement_xs_group(org.w3c.dom.Element element) {
        // <xs:group>
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("name")) {
                // <xs:group name="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("maxOccurs")) {
                // <xs:group maxOccurs="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("minOccurs")) {
                // <xs:group minOccurs="???">
                System.out.println(attr.getValue());
            }
            if (attr.getName().equals("ref")) {
                // <xs:group ref="???">
                System.out.println(attr.getValue());
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.CDATA_SECTION_NODE:
                    // ((org.w3c.dom.CDATASection)node).getData();
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals("xs:choice")) {
                        visitElement_xs_choice(nodeElement);
                    }
                    break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                    // ((org.w3c.dom.ProcessingInstruction)node).getTarget();
                    // ((org.w3c.dom.ProcessingInstruction)node).getData();
                    break;
            }
        }
    }
    
}
