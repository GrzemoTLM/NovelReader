package org.core.novelreader_client;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubMetadataExtractor {
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";

    public static BookMetadataInfo extractMetadata(Path epubFilePath) throws IOException {
        try (ZipFile zipFile = new ZipFile(epubFilePath.toFile())) {
            String opfPath = findOpfPath(zipFile);
            if (opfPath == null) {
                throw new IOException("Nie znaleziono pliku OPF w archiwum EPUB");
            }

            ZipEntry opfEntry = zipFile.getEntry(opfPath);
            if (opfEntry == null) {
                throw new IOException("Plik OPF nie znaleziony: " + opfPath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(zipFile.getInputStream(opfEntry));

            String title = extractTitle(doc);
            String author = extractAuthor(doc);
            String description = extractDescription(doc);

            return new BookMetadataInfo(title, author, description);
        } catch (Exception e) {
            throw new IOException("Nie udało się wyciągnąć metadanych z pliku EPUB", e);
        }
    }

    private static String findOpfPath(ZipFile zipFile) throws IOException {
        ZipEntry containerEntry = zipFile.getEntry("META-INF/container.xml");
        if (containerEntry == null) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(zipFile.getInputStream(containerEntry));

            NodeList rootfiles = doc.getElementsByTagName("rootfile");
            if (rootfiles.getLength() > 0) {
                Element rootfile = (Element) rootfiles.item(0);
                String fullPath = rootfile.getAttribute("full-path");
                if (!fullPath.isEmpty()) {
                    return fullPath;
                }
            }
        } catch (Exception e) {
        }

        return "OEBPS/content.opf";
    }

    private static String extractTitle(Document doc) {
        NodeList titles = doc.getElementsByTagNameNS(DC_NAMESPACE, "title");
        if (titles.getLength() > 0) {
            String text = titles.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        titles = doc.getElementsByTagName("title");
        if (titles.getLength() > 0) {
            String text = titles.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        return "";
    }

    private static String extractAuthor(Document doc) {
        NodeList creators = doc.getElementsByTagNameNS(DC_NAMESPACE, "creator");
        if (creators.getLength() > 0) {
            String text = creators.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        creators = doc.getElementsByTagName("creator");
        if (creators.getLength() > 0) {
            String text = creators.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        NodeList contributors = doc.getElementsByTagNameNS(DC_NAMESPACE, "contributor");
        if (contributors.getLength() > 0) {
            String text = contributors.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        contributors = doc.getElementsByTagName("contributor");
        if (contributors.getLength() > 0) {
            String text = contributors.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        return "";
    }

    private static String extractDescription(Document doc) {
        NodeList descriptions = doc.getElementsByTagNameNS(DC_NAMESPACE, "description");
        if (descriptions.getLength() > 0) {
            String text = descriptions.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        descriptions = doc.getElementsByTagName("description");
        if (descriptions.getLength() > 0) {
            String text = descriptions.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        NodeList subjects = doc.getElementsByTagNameNS(DC_NAMESPACE, "subject");
        if (subjects.getLength() > 0) {
            String text = subjects.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        subjects = doc.getElementsByTagName("subject");
        if (subjects.getLength() > 0) {
            String text = subjects.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }

        return "";
    }

    public static class BookMetadataInfo {
        public final String title;
        public final String author;
        public final String description;

        public BookMetadataInfo(String title, String author, String description) {
            this.title = title != null ? title.trim() : "";
            this.author = author != null ? author.trim() : "";
            this.description = description != null ? description.trim() : "";
        }
    }
}

