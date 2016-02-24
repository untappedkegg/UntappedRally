package com.untappedkegg.rally.social;

import com.untappedkegg.rally.data.BaseSAX;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Locale;

public class SaxYouTube extends BaseSAX {
    // tag names
    static final String TAG_THUMBNAIL = "thumbnail";
    static final String TAG_YT_LINK = "player";
    static final String TAG_YT_DIRECT_LINK = "content";
    static final String TAG_YOUTUBE_ROOT = "entry";
    // attribute names
    private static final String ATT_URL = "url";

    // other config
    private static final String THUMBNAIL_SUFFIX = "0.jpg";

    //	private String uri;
    private String thumbnail = "";
    private String ytLink = "";
    private String dirLink = "";

    /* ---- CONSTRUCTOR ---- */
    public SaxYouTube(String uri) {
        //		this.uri = uri;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        localName = localName.toLowerCase(Locale.US);

        super.startElement(namespaceURI, localName, qName, atts);

        switch (localName) {
            case TAG_THUMBNAIL:
                String possibleThumbnail = atts.getValue(ATT_URL);
                if (possibleThumbnail.endsWith(THUMBNAIL_SUFFIX)) {
                    thumbnail = possibleThumbnail;
                }
                break;
            case TAG_YT_LINK:
                ytLink = atts.getValue(ATT_URL);
                break;
            case TAG_YT_DIRECT_LINK:
                dirLink = atts.getValue(ATT_URL);
                break;
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        super.endElement(namespaceURI, localName, qName);

        switch (localName) {
            case "thumbnail_medium":
                thumbnail = buffer;
                break;
            case TAG_YOUTUBE_ROOT:
                DbSocial.insertVideo(title, ytLink, dirLink, thumbnail);

                break;
        }
    }
}
