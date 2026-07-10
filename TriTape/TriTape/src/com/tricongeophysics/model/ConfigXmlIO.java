package com.tricongeophysics.model;

import org.w3c.dom.Comment;
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
 * saved and reloaded instead of re-entered by hand.
 *
 * Byte OFFSETS are written/read as 1-based (byte 1 = the first byte), matching
 * what's shown in the GUI, so the XML is human-readable/editable without
 * having to mentally shift everything by one. Byte LENGTHS/counts (e.g.
 * traceHeaderBytes, textualHeaderBytes) are plain counts and are not shifted.
 * Internally, SegyConfig/SegdConfig/HeaderFieldDef always stay 0-based - the
 * +1/-1 conversion happens only at this file boundary.
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
            root.appendChild(offsetsAreOneBasedComment(doc));

            appendLength(doc, root, "textualHeaderBytes", config.textualHeaderBytes);
            appendLength(doc, root, "binaryHeaderBytes", config.binaryHeaderBytes);
            appendLength(doc, root, "traceHeaderBytes", config.traceHeaderBytes);
            appendOffset(doc, root, "sampleRateByteOffset", config.sampleRateByteOffset);
            appendOffset(doc, root, "samplesPerTraceByteOffset", config.samplesPerTraceByteOffset);
            appendOffset(doc, root, "formatCodeByteOffset", config.formatCodeByteOffset);
            appendOffset(doc, root, "numSamplesThisTraceByteOffset", config.numSamplesThisTraceByteOffset);
            appendOffset(doc, root, "coordinateScalarByteOffset", config.coordinateScalarByteOffset);
            appendOffset(doc, root, "elevationScalarByteOffset", config.elevationScalarByteOffset);
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
            config.textualHeaderBytes = readLength(root, "textualHeaderBytes", config.textualHeaderBytes);
            config.binaryHeaderBytes = readLength(root, "binaryHeaderBytes", config.binaryHeaderBytes);
            config.traceHeaderBytes = readLength(root, "traceHeaderBytes", config.traceHeaderBytes);
            config.sampleRateByteOffset = readOffset(root, "sampleRateByteOffset", config.sampleRateByteOffset);
            config.samplesPerTraceByteOffset = readOffset(root, "samplesPerTraceByteOffset", config.samplesPerTraceByteOffset);
            config.formatCodeByteOffset = readOffset(root, "formatCodeByteOffset", config.formatCodeByteOffset);
            config.numSamplesThisTraceByteOffset = readOffset(root, "numSamplesThisTraceByteOffset", config.numSamplesThisTraceByteOffset);
            config.coordinateScalarByteOffset = readOffset(root, "coordinateScalarByteOffset", config.coordinateScalarByteOffset);
            config.elevationScalarByteOffset = readOffset(root, "elevationScalarByteOffset", config.elevationScalarByteOffset);
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
            root.appendChild(offsetsAreOneBasedComment(doc));

            appendLength(doc, root, "generalHeaderBlockBytes", config.generalHeaderBlockBytes);
            appendLength(doc, root, "traceHeaderBytes", config.traceHeaderBytes);
            appendLength(doc, root, "traceHeaderExtensionBytes", config.traceHeaderExtensionBytes);
            appendText(doc, root, "version", config.version.name());
            appendOffset(doc, root, "fileNumberByteOffset", config.fileNumberByteOffset);
            appendOffset(doc, root, "formatCodeByteOffset", config.formatCodeByteOffset);
            appendOffset(doc, root, "channelSetsPerScanTypeByteOffset", config.channelSetsPerScanTypeByteOffset);
            appendOffset(doc, root, "additionalGeneralHeaderBlocksByteOffset", config.additionalGeneralHeaderBlocksByteOffset);
            appendOffset(doc, root, "baseScanIntervalByteOffset", config.baseScanIntervalByteOffset);
            appendOffset(doc, root, "traceHeaderExtensionCountByteOffset", config.traceHeaderExtensionCountByteOffset);
            appendOffset(doc, root, "extendedHeaderBlocksByteOffsetInHeader2", config.extendedHeaderBlocksByteOffsetInHeader2);
            appendOffset(doc, root, "externalHeaderBlocksByteOffsetInHeader2", config.externalHeaderBlocksByteOffsetInHeader2);
            appendOffset(doc, root, "rev3AdditionalBlocksCountByteOffsetInHeader2", config.rev3AdditionalBlocksCountByteOffsetInHeader2);
            appendOffset(doc, root, "rev3DominantSamplingIntervalByteOffsetInHeader2", config.rev3DominantSamplingIntervalByteOffsetInHeader2);
            appendOffset(doc, root, "rev3ExtendedHeaderBlocksByteOffsetInHeader2", config.rev3ExtendedHeaderBlocksByteOffsetInHeader2);
            appendOffset(doc, root, "rev3ExternalHeaderBlocksByteOffsetInHeader2", config.rev3ExternalHeaderBlocksByteOffsetInHeader2);
            appendOffset(doc, root, "rev3HeaderSizeByteOffsetInHeader3", config.rev3HeaderSizeByteOffsetInHeader3);
            appendOffset(doc, root, "rev3TraceHeaderExtensionCountByteOffset", config.rev3TraceHeaderExtensionCountByteOffset);
            appendOffset(doc, root, "rev3NumSamplesByteOffsetInTraceHeaderExt1", config.rev3NumSamplesByteOffsetInTraceHeaderExt1);
            appendOffset(doc, root, "samplesFieldByteOffsetInChannelSetDescriptor", config.samplesFieldByteOffsetInChannelSetDescriptor);
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
            config.generalHeaderBlockBytes = readLength(root, "generalHeaderBlockBytes", config.generalHeaderBlockBytes);
            config.traceHeaderBytes = readLength(root, "traceHeaderBytes", config.traceHeaderBytes);
            config.traceHeaderExtensionBytes = readLength(root, "traceHeaderExtensionBytes", config.traceHeaderExtensionBytes);
            config.version = parseEnumSafe(SegdVersion.class, childText(root, "version", null), config.version);
            config.fileNumberByteOffset = readOffset(root, "fileNumberByteOffset", config.fileNumberByteOffset);
            config.formatCodeByteOffset = readOffset(root, "formatCodeByteOffset", config.formatCodeByteOffset);
            config.channelSetsPerScanTypeByteOffset = readOffset(root, "channelSetsPerScanTypeByteOffset", config.channelSetsPerScanTypeByteOffset);
            config.additionalGeneralHeaderBlocksByteOffset = readOffset(root, "additionalGeneralHeaderBlocksByteOffset", config.additionalGeneralHeaderBlocksByteOffset);
            config.baseScanIntervalByteOffset = readOffset(root, "baseScanIntervalByteOffset", config.baseScanIntervalByteOffset);
            config.traceHeaderExtensionCountByteOffset = readOffset(root, "traceHeaderExtensionCountByteOffset", config.traceHeaderExtensionCountByteOffset);
            config.extendedHeaderBlocksByteOffsetInHeader2 = readOffset(root, "extendedHeaderBlocksByteOffsetInHeader2", config.extendedHeaderBlocksByteOffsetInHeader2);
            config.externalHeaderBlocksByteOffsetInHeader2 = readOffset(root, "externalHeaderBlocksByteOffsetInHeader2", config.externalHeaderBlocksByteOffsetInHeader2);
            config.rev3AdditionalBlocksCountByteOffsetInHeader2 = readOffset(root, "rev3AdditionalBlocksCountByteOffsetInHeader2", config.rev3AdditionalBlocksCountByteOffsetInHeader2);
            config.rev3DominantSamplingIntervalByteOffsetInHeader2 = readOffset(root, "rev3DominantSamplingIntervalByteOffsetInHeader2", config.rev3DominantSamplingIntervalByteOffsetInHeader2);
            config.rev3ExtendedHeaderBlocksByteOffsetInHeader2 = readOffset(root, "rev3ExtendedHeaderBlocksByteOffsetInHeader2", config.rev3ExtendedHeaderBlocksByteOffsetInHeader2);
            config.rev3ExternalHeaderBlocksByteOffsetInHeader2 = readOffset(root, "rev3ExternalHeaderBlocksByteOffsetInHeader2", config.rev3ExternalHeaderBlocksByteOffsetInHeader2);
            config.rev3HeaderSizeByteOffsetInHeader3 = readOffset(root, "rev3HeaderSizeByteOffsetInHeader3", config.rev3HeaderSizeByteOffsetInHeader3);
            config.rev3TraceHeaderExtensionCountByteOffset = readOffset(root, "rev3TraceHeaderExtensionCountByteOffset", config.rev3TraceHeaderExtensionCountByteOffset);
            config.rev3NumSamplesByteOffsetInTraceHeaderExt1 = readOffset(root, "rev3NumSamplesByteOffsetInTraceHeaderExt1", config.rev3NumSamplesByteOffsetInTraceHeaderExt1);
            config.samplesFieldByteOffsetInChannelSetDescriptor = readOffset(root, "samplesFieldByteOffsetInChannelSetDescriptor", config.samplesFieldByteOffsetInChannelSetDescriptor);
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

    private static Comment offsetsAreOneBasedComment(Document doc)
    {
        return doc.createComment(" byte offsets below are 1-based (byte 1 = the first byte); "
            + "*Bytes/*Length fields are plain counts, not offsets ");
    }

    private static void appendSchema(Document doc, Element parent, HeaderSchema schema)
    {
        Element schemaEl = doc.createElement("traceHeaderSchema");
        parent.appendChild(schemaEl);
        for (HeaderFieldDef f : schema.getFields())
        {
            Element fieldEl = doc.createElement("field");
            appendText(doc, fieldEl, "name", f.getName());
            appendText(doc, fieldEl, "byteOffset", String.valueOf(f.getByteOffset() + 1)); // 1-based
            appendText(doc, fieldEl, "type", f.getType().name());
            appendText(doc, fieldEl, "scalarType", f.getScalarType().name());
            appendText(doc, fieldEl, "scaleDivisor", String.valueOf(f.getScaleDivisor()));
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
            int oneBasedOffset = parseIntSafe(childText(fieldEl, "byteOffset", "1"), 1);
            int offset = Math.max(0, oneBasedOffset - 1); // stored 1-based -> internal 0-based
            HeaderFieldDef.FieldType type = parseEnumSafe(HeaderFieldDef.FieldType.class,
                childText(fieldEl, "type", "INT32"), HeaderFieldDef.FieldType.INT32);
            HeaderFieldDef.ScalarType scalar = parseEnumSafe(HeaderFieldDef.ScalarType.class,
                childText(fieldEl, "scalarType", "NONE"), HeaderFieldDef.ScalarType.NONE);
            double scaleDivisor = parseDoubleSafe(childText(fieldEl, "scaleDivisor", "1.0"), 1.0);
            fields.add(new HeaderFieldDef(name, offset, type, scalar, scaleDivisor));
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

    /** writes a plain byte-count (length) field, unshifted */
    private static void appendLength(Document doc, Element parent, String tag, int value)
    {
        appendText(doc, parent, tag, String.valueOf(value));
    }

    /** writes a 0-based internal offset as 1-based text */
    private static void appendOffset(Document doc, Element parent, String tag, int zeroBasedValue)
    {
        appendText(doc, parent, tag, String.valueOf(zeroBasedValue + 1));
    }

    private static void appendText(Document doc, Element parent, String tag, String value)
    {
        Element el = doc.createElement(tag);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    /** reads a plain byte-count (length) field, unshifted */
    private static int readLength(Element root, String tag, int fallback)
    {
        return parseIntSafe(childText(root, tag, null), fallback);
    }

    /** reads a 1-based offset from XML and converts it back to the internal 0-based form */
    private static int readOffset(Element root, String tag, int fallbackZeroBased)
    {
        String text = childText(root, tag, null);
        if (text == null) return fallbackZeroBased;
        int oneBased = parseIntSafe(text, fallbackZeroBased + 1);
        return Math.max(0, oneBased - 1);
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

    private static double parseDoubleSafe(String text, double fallback)
    {
        if (text == null || text.isEmpty()) return fallback;
        try { return Double.parseDouble(text.trim()); }
        catch (NumberFormatException ex) { return fallback; }
    }

    private static <E extends Enum<E>> E parseEnumSafe(Class<E> type, String text, E fallback)
    {
        if (text == null) return fallback;
        try { return Enum.valueOf(type, text.trim()); }
        catch (IllegalArgumentException ex) { return fallback; }
    }
}
