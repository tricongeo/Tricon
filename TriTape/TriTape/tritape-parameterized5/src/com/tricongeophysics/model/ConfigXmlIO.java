package com.tricongeophysics.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads/writes SegyConfig and SegdConfig (byte offsets + trace-header field
 * mapping) as XML, so a user's parameterization of the readers/writers can be
 * saved and reloaded instead of re-entered by hand. Values are always
 * stored/loaded in the same 0-based form used internally everywhere else -
 * the GUI's 1-based display is purely cosmetic and has no bearing on the file
 * format.
 */
public final class ConfigXmlIO
{
    private ConfigXmlIO() {}

    // ---------------- SEG-Y ----------------

    public static void saveSegyConfig(SegyConfig config, File file) throws IOException
    {
        try
        {
            Document doc = newDocument();
            Element root = doc.createElement("segyConfig");
            doc.appendChild(root);

            appendInt(doc, root, "textualHeaderBytes", config.textualHeaderBytes);
            appendInt(doc, root, "binaryHeaderBytes", config.binaryHeaderBytes);
            appendInt(doc, root, "traceHeaderBytes", config.traceHeaderBytes);
            appendInt(doc, root, "sampleRateByteOffset", config.sampleRateByteOffset);
            appendInt(doc, root, "samplesPerTraceByteOffset", config.samplesPerTraceByteOffset);
            appendInt(doc, root, "formatCodeByteOffset", config.formatCodeByteOffset);
            appendInt(doc, root, "numSamplesThisTraceByteOffset", config.numSamplesThisTraceByteOffset);
            appendInt(doc, root, "coordinateScalarByteOffset", config.coordinateScalarByteOffset);
            appendInt(doc, root, "elevationScalarByteOffset", config.elevationScalarByteOffset);
            appendSchema(doc, root, config.traceHeaderSchema);

            writeDocument(doc, file);
        }
        catch (ParserConfigurationException | TransformerException ex)
        {
            throw new IOException("Failed to write SEG-Y settings XML: " + ex.getMessage(), ex);
        }
    }

    public static SegyConfig loadSegyConfig(File file) throws IOException
    {
        try
        {
            Element root = parseRoot(file);
            SegyConfig config = new SegyConfig();
            config.textualHeaderBytes = readInt(root, "textualHeaderBytes", config.textualHeaderBytes);
            config.binaryHeaderBytes = readInt(root, "binaryHeaderBytes", config.binaryHeaderBytes);
            config.traceHeaderBytes = readInt(root, "traceHeaderBytes", config.traceHeaderBytes);
            config.sampleRateByteOffset = readInt(root, "sampleRateByteOffset", config.sampleRateByteOffset);
            config.samplesPerTraceByteOffset = readInt(root, "samplesPerTraceByteOffset", config.samplesPerTraceByteOffset);
            config.formatCodeByteOffset = readInt(root, "formatCodeByteOffset", config.formatCodeByteOffset);
            config.numSamplesThisTraceByteOffset = readInt(root, "numSamplesThisTraceByteOffset", config.numSamplesThisTraceByteOffset);
            config.coordinateScalarByteOffset = readInt(root, "coordinateScalarByteOffset", config.coordinateScalarByteOffset);
            config.elevationScalarByteOffset = readInt(root, "elevationScalarByteOffset", config.elevationScalarByteOffset);
            HeaderSchema schema = readSchema(root);
            if (schema != null) config.traceHeaderSchema = schema;
            return config;
        }
        catch (ParserConfigurationException | SAXException ex)
        {
            throw new IOException("Failed to read SEG-Y settings XML: " + ex.getMessage(), ex);
        }
    }

    // ---------------- SEG-D ----------------

    public static void saveSegdConfig(SegdConfig config, File file) throws IOException
    {
        try
        {
            Document doc = newDocument();
            Element root = doc.createElement("segdConfig");
            doc.appendChild(root);

            appendInt(doc, root, "generalHeaderBlockBytes", config.generalHeaderBlockBytes);
            appendInt(doc, root, "traceHeaderBytes", config.traceHeaderBytes);
            appendInt(doc, root, "traceHeaderExtensionBytes", config.traceHeaderExtensionBytes);
            appendInt(doc, root, "fileNumberByteOffset", config.fileNumberByteOffset);
            appendInt(doc, root, "formatCodeByteOffset", config.formatCodeByteOffset);
            appendInt(doc, root, "channelSetsPerScanTypeByteOffset", config.channelSetsPerScanTypeByteOffset);
            appendInt(doc, root, "additionalGeneralHeaderBlocksByteOffset", config.additionalGeneralHeaderBlocksByteOffset);
            appendInt(doc, root, "baseScanIntervalByteOffset", config.baseScanIntervalByteOffset);
            appendInt(doc, root, "traceHeaderExtensionCountByteOffset", config.traceHeaderExtensionCountByteOffset);
            appendInt(doc, root, "extendedHeaderBlocksByteOffsetInHeader2", config.extendedHeaderBlocksByteOffsetInHeader2);
            appendInt(doc, root, "externalHeaderBlocksByteOffsetInHeader2", config.externalHeaderBlocksByteOffsetInHeader2);
            appendInt(doc, root, "samplesFieldByteOffsetInChannelSetDescriptor", config.samplesFieldByteOffsetInChannelSetDescriptor);
            appendSchema(doc, root, config.traceHeaderSchema);

            writeDocument(doc, file);
        }
        catch (ParserConfigurationException | TransformerException ex)
        {
            throw new IOException("Failed to write SEG-D settings XML: " + ex.getMessage(), ex);
        }
    }

    public static SegdConfig loadSegdConfig(File file) throws IOException
    {
        try
        {
            Element root = parseRoot(file);
            SegdConfig config = new SegdConfig();
            config.generalHeaderBlockBytes = readInt(root, "generalHeaderBlockBytes", config.generalHeaderBlockBytes);
            config.traceHeaderBytes = readInt(root, "traceHeaderBytes", config.traceHeaderBytes);
            config.traceHeaderExtensionBytes = readInt(root, "traceHeaderExtensionBytes", config.traceHeaderExtensionBytes);
            config.fileNumberByteOffset = readInt(root, "fileNumberByteOffset", config.fileNumberByteOffset);
            config.formatCodeByteOffset = readInt(root, "formatCodeByteOffset", config.formatCodeByteOffset);
            config.channelSetsPerScanTypeByteOffset = readInt(root, "channelSetsPerScanTypeByteOffset", config.channelSetsPerScanTypeByteOffset);
            config.additionalGeneralHeaderBlocksByteOffset = readInt(root, "additionalGeneralHeaderBlocksByteOffset", config.additionalGeneralHeaderBlocksByteOffset);
            config.baseScanIntervalByteOffset = readInt(root, "baseScanIntervalByteOffset", config.baseScanIntervalByteOffset);
            config.traceHeaderExtensionCountByteOffset = readInt(root, "traceHeaderExtensionCountByteOffset", config.traceHeaderExtensionCountByteOffset);
            config.extendedHeaderBlocksByteOffsetInHeader2 = readInt(root, "extendedHeaderBlocksByteOffsetInHeader2", config.extendedHeaderBlocksByteOffsetInHeader2);
            config.externalHeaderBlocksByteOffsetInHeader2 = readInt(root, "externalHeaderBlocksByteOffsetInHeader2", config.externalHeaderBlocksByteOffsetInHeader2);
            config.samplesFieldByteOffsetInChannelSetDescriptor = readInt(root, "samplesFieldByteOffsetInChannelSetDescriptor", config.samplesFieldByteOffsetInChannelSetDescriptor);
            HeaderSchema schema = readSchema(root);
            if (schema != null) config.traceHeaderSchema = schema;
            return config;
        }
        catch (ParserConfigurationException | SAXException ex)
        {
            throw new IOException("Failed to read SEG-D settings XML: " + ex.getMessage(), ex);
        }
    }

    // ---------------- shared helpers ----------------

    private static void appendSchema(Document doc, Element parent, HeaderSchema schema)
    {
        Element schemaEl = doc.createElement("traceHeaderSchema");
        parent.appendChild(schemaEl);
        for (HeaderFieldDef f : schema.getFields())
        {
            Element fieldEl = doc.createElement("field");
            appendText(doc, fieldEl, "name", f.getName());
            appendText(doc, fieldEl, "byteOffset", String.valueOf(f.getByteOffset()));
            appendText(doc, fieldEl, "type", f.getType().name());
            appendText(doc, fieldEl, "scalarType", f.getScalarType().name());
            schemaEl.appendChild(fieldEl);
        }
    }

    private static HeaderSchema readSchema(Element root)
    {
        NodeList schemaNodes = root.getElementsByTagName("traceHeaderSchema");
        if (schemaNodes.getLength() == 0) return null;
        Element schemaEl = (Element) schemaNodes.item(0);
        NodeList fieldNodes = schemaEl.getElementsByTagName("field");
        List<HeaderFieldDef> fields = new ArrayList<HeaderFieldDef>();
        for (int i = 0; i < fieldNodes.getLength(); i++)
        {
            Element fieldEl = (Element) fieldNodes.item(i);
            String name = childText(fieldEl, "name", "FIELD_" + i);
            int offset = parseIntSafe(childText(fieldEl, "byteOffset", "0"), 0);
            HeaderFieldDef.FieldType type = parseEnumSafe(HeaderFieldDef.FieldType.class,
                childText(fieldEl, "type", "INT32"), HeaderFieldDef.FieldType.INT32);
            HeaderFieldDef.ScalarType scalar = parseEnumSafe(HeaderFieldDef.ScalarType.class,
                childText(fieldEl, "scalarType", "NONE"), HeaderFieldDef.ScalarType.NONE);
            fields.add(new HeaderFieldDef(name, offset, type, scalar));
        }
        return new HeaderSchema(fields);
    }

    private static Document newDocument() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private static Element parseRoot(File file) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        return doc.getDocumentElement();
    }

    private static void writeDocument(Document doc, File file) throws TransformerException, IOException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(doc), new StreamResult(file));
    }

    private static void appendInt(Document doc, Element parent, String tag, int value)
    {
        appendText(doc, parent, tag, String.valueOf(value));
    }

    private static void appendText(Document doc, Element parent, String tag, String value)
    {
        Element el = doc.createElement(tag);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    private static int readInt(Element root, String tag, int fallback)
    {
        return parseIntSafe(childText(root, tag, null), fallback);
    }

    private static String childText(Element parent, String tag, String fallback)
    {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return fallback;
        Node node = nodes.item(0);
        String text = node.getTextContent();
        return text == null ? fallback : text.trim();
    }

    private static int parseIntSafe(String text, int fallback)
    {
        if (text == null || text.isEmpty()) return fallback;
        try { return Integer.parseInt(text.trim()); }
        catch (NumberFormatException ex) { return fallback; }
    }

    private static <E extends Enum<E>> E parseEnumSafe(Class<E> type, String text, E fallback)
    {
        if (text == null) return fallback;
        try { return Enum.valueOf(type, text.trim()); }
        catch (IllegalArgumentException ex) { return fallback; }
    }
}
