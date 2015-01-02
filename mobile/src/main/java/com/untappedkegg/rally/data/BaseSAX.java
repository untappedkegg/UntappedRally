package com.untappedkegg.rally.data;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BaseSAX extends DefaultHandler {
    /* VARIABLES */
    protected String buffer;
    protected int id;
    protected String name;
    protected String title;
    protected String status;
    protected String link;
    protected String pubDate;
    protected String shortDate;
    protected String description;
    protected String author;
    protected String imgLink;

    protected String series;


    /* INHERITED METHODS */
    @Override
    public void startDocument() {
        Log.i(getClass().getSimpleName(), "Started parsing data.");
    }

    @Override
    public void endDocument() {
        Log.i(getClass().getSimpleName(), "Finished parsing data.");
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        buffer = "";
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        buffer = buffer.trim();

        if (localName.equalsIgnoreCase("id")) {
            try {
                id = Integer.parseInt(buffer);
            } catch (NumberFormatException f) {
                //Not much we can do
            }
        } else if (localName.equalsIgnoreCase("title")) {
            title = android.text.Html.fromHtml(buffer).toString();
        } else if (localName.equalsIgnoreCase("name")) {
            name = buffer;
        } else if (localName.equalsIgnoreCase("link") || localName.equalsIgnoreCase("feedburner:origLink")) {
            link = buffer;
        } else if (localName.equalsIgnoreCase("image") || localName.equalsIgnoreCase("img")) {
            imgLink = buffer;
        } else if (localName.equalsIgnoreCase("series")) {
            series = buffer;
        } else if (localName.equalsIgnoreCase("description")) {
            description = android.text.Html.fromHtml(buffer.replaceAll("<img.+?>", "")).toString();
        }
    }

    @Override
    public void characters(char[] buf, int offset, int len) {
        buffer += String.copyValueOf(buf, offset, len);
    }
}
