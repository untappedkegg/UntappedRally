package com.untappedkegg.rally.data;

import android.util.Log;

import com.untappedkegg.rally.BuildConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BaseSAX extends DefaultHandler {
    /* VARIABLES */
    protected String buffer;
    protected int id;
    protected String title;
    protected String status;
    protected String link;
    protected String pubDate;
    protected String shortDate;
    protected String description;
    protected String author;
    protected String imgLink;



    /* INHERITED METHODS */
    @Override
    public void startDocument() {
        if(BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(), "Started parsing data.");
        }
    }

    @Override
    public void endDocument() {
        if(BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(), "Finished parsing data.");
        }
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        buffer = "";
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        buffer = buffer.trim();

        // localName should be lower case
        switch (localName) {
            case "id":
                try {
                    id = Integer.parseInt(buffer);
                } catch (NumberFormatException f) {
                    //Not much we can do
                }
                break;
            case "title":
                title = android.text.Html.fromHtml(buffer).toString();
                break;
            case "link":
            case "feedburner:origlink":
                link = buffer;
                break;
            case "image":
            case "img":
                imgLink = buffer;
                break;
            case "description":
            case "desc":
                description = android.text.Html.fromHtml(buffer.replaceAll("<img.+?>", "")).toString();
                break;
        }
    }

    @Override
    public void characters(char[] buf, int offset, int len) {
        buffer += String.copyValueOf(buf, offset, len);
    }
}
