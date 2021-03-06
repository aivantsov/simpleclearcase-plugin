/**
 * The MIT License
 * 
 * Copyright (c) 2011, Sun Microsystems, Inc., Sam Tavakoli
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jenkins.plugins.simpleclearcase;

import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jenkins.plugins.simpleclearcase.util.DateUtil;
import jenkins.plugins.simpleclearcase.util.DebugHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class SimpleClearCaseChangeLogParser extends ChangeLogParser {
    private static final String XML_VERSION              = "1.0";
    private static final String XML_ENCODING             = "UTF-8";
    private static final String XML_INDENT               = "yes";
    private static final String XML_INDENT_SPACE         = "2";
    private static final String XML_INDENT_SPACE_SETTING = "{http://xml.apache.org/xslt}indent-amount";

    private static final String CHANGELOG         = "changelog";
    private static final String VERSION           = "version";
    private static final String DATE              = "date";
    private static final String ENTRY             = "entry";
    private static final String USER              = "user";
    private static final String COMMENT           = "comment";
    private static final String ITEMS             = "items";
    private static final String ITEM              = "item";
    private static final String OPERATION         = "operation";
    private static final String EVENT_DESCRIPTION = "eventdescription";

    private DateUtil dateUtil;
    
    public SimpleClearCaseChangeLogParser() {
        dateUtil = new DateUtil();
    }
    public boolean writeChangeLog(File file, SimpleClearCaseChangeLogSet set,
                                                               TaskListener listener) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;

        try {
            doc = factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            DebugHelper.error(listener, "Couldn't create a new DocumentBuilder, ExceptionMsg: e: %s",
                                                                                         ex.getMessage());
            return false;
        }

        doc.setXmlVersion(XML_VERSION);

        Element changelog = doc.createElement(CHANGELOG);

        for (SimpleClearCaseChangeLogEntry e : set.getEntries()) {
            Element entry = doc.createElement(ENTRY);

            Element date = doc.createElement(DATE);
            date.setTextContent(dateUtil.formatDate(e.getDate()));

            Element user = doc.createElement(USER);
            user.setTextContent(e.getUser());

            Element comment = doc.createElement(COMMENT);
            comment.setTextContent(e.getComment());

            Element version = doc.createElement(VERSION);
            version.setTextContent(e.getVersion());

            Element operation = doc.createElement(OPERATION);
            operation.setTextContent(e.getOperation());

            Element eventDescription = doc.createElement(EVENT_DESCRIPTION);
            eventDescription.setTextContent(e.getEventDescription());

            Element items = doc.createElement(ITEMS);
            // TODO when changing Entry such that file elements contain versions
            // then this must be also changed
            for (String filePath : e.getAffectedPaths()) {
                Element item = doc.createElement(ITEM);
                item.setTextContent(filePath);
                items.appendChild(item);
            }

            entry.appendChild(date);
            entry.appendChild(user);
            entry.appendChild(operation);
            entry.appendChild(eventDescription);
            entry.appendChild(version);
            entry.appendChild(comment);
            entry.appendChild(items);

            changelog.appendChild(entry);
        }

        doc.appendChild(changelog);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t;
        try {
            t = tf.newTransformer();

            t.setOutputProperty(OutputKeys.ENCODING, XML_ENCODING);
            t.setOutputProperty(OutputKeys.INDENT, XML_INDENT);
            t.setOutputProperty(XML_INDENT_SPACE_SETTING, XML_INDENT_SPACE);

            DOMSource src = new DOMSource(doc);
            StreamResult res = new StreamResult(new FileOutputStream(file));

            t.transform(src, res);
        } catch (TransformerException ex) {
            DebugHelper.error(listener, 
                               "Couldn't create transform and write to resultStream, ExceptionMsg: e: %s",
                                                                                         ex.getMessage());
            return false;
        }
        return true;
    }

    public List<SimpleClearCaseChangeLogEntry> readChangeLog(File file) throws 
                                                 IOException, ParserConfigurationException, SAXException {
        return readChangeLog(new FileInputStream(file));
    }

    public List<SimpleClearCaseChangeLogEntry> readChangeLog(InputStream is) throws 
                                                 IOException, ParserConfigurationException, SAXException {
        List<SimpleClearCaseChangeLogEntry> ret = new ArrayList<SimpleClearCaseChangeLogEntry>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        // proper change log should only contain a CHANGE LOG element
        Element changelog = (Element) doc.getElementsByTagName(CHANGELOG).item(0);

        // fetch all the entries from changelog
        NodeList entries = changelog.getElementsByTagName(ENTRY);

        // as NodeList doesn't implement iterable...
        for (int i = 0; i < entries.getLength(); i++) {
            Element elemEntry = (Element) entries.item(i);

            String date = elemEntry.getElementsByTagName(DATE).item(0).getTextContent().trim();
            String user = elemEntry.getElementsByTagName(USER).item(0).getTextContent().trim();
            String version = elemEntry.getElementsByTagName(VERSION).item(0).getTextContent().trim();
            String operation = elemEntry.getElementsByTagName(OPERATION).item(0).getTextContent().trim();
            String eventDescription = elemEntry.getElementsByTagName(EVENT_DESCRIPTION).item(0).getTextContent().trim();
            String comment = elemEntry.getElementsByTagName(COMMENT).item(0).getTextContent().trim();

            // we create the entry without any file path reference
            SimpleClearCaseChangeLogEntry entry = new SimpleClearCaseChangeLogEntry(
                           dateUtil.parseDate(date), user, version, eventDescription, operation, comment);
            // adding all available file paths to entry
            addFilePathsToEntry(elemEntry.getElementsByTagName(ITEM), entry);
            ret.add(entry);
        }
        return ret;
    }

    private void addFilePathsToEntry(NodeList items, SimpleClearCaseChangeLogEntry entry) {
        for (int i = 0; i < items.getLength(); i++) {
            Element elemItem = (Element) items.item(i);
            entry.addPath(elemItem.getTextContent().trim());
        }
    }

    @Override
    public ChangeLogSet<? extends Entry> parse(@SuppressWarnings("rawtypes") AbstractBuild build,
                                                    File changelogFile) throws IOException, SAXException {
        List<SimpleClearCaseChangeLogEntry> entries;
        try {
            entries = readChangeLog(changelogFile);
        } catch (ParserConfigurationException e) {
            return null;
        }
        return new SimpleClearCaseChangeLogSet(build, entries);
    }
}
