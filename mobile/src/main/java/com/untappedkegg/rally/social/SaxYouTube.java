package com.untappedkegg.rally.social;

import com.untappedkegg.rally.data.BaseSAX;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SaxYouTube extends BaseSAX {
    // tag names
    static final String TAG_USERNAME = "username";
    static final String TAG_TYPE = "type";
    static final String TAG_PRIORITY = "priority";
    static final String TAG_THUMBNAIL = "thumbnail";
    static final String TAG_YT_LINK = "player";
    static final String TAG_YT_DIRECT_LINK = "content";
    static final String TAG_TWITTER_ROOT = "status";
    static final String TAG_YOUTUBE_ROOT = "entry";
    static final String TAG_MENU_ROOT = "item";
    static final String TAG_IMAGE = "image";
    static final String TAG_ARTIST = "artist";
    static final String TAG_SONG_TITLE = "track";
    static final String TAG_VIMEO_ROOT = "video";

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
        super.startElement(namespaceURI, localName, qName, atts);

        if (localName.equalsIgnoreCase(TAG_THUMBNAIL)) {
            String possibleThumbnail = atts.getValue(ATT_URL);
            if (possibleThumbnail.endsWith(THUMBNAIL_SUFFIX)) {
                thumbnail = possibleThumbnail;
            }
        } else if (localName.equalsIgnoreCase(TAG_YT_LINK)) {
            ytLink = atts.getValue(ATT_URL);
        } else if (localName.equalsIgnoreCase(TAG_YT_DIRECT_LINK)) {
            dirLink = atts.getValue(ATT_URL);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        super.endElement(namespaceURI, localName, qName);

        if (localName.equalsIgnoreCase("title")) {
            title = buffer;
            //		} else if(localName.equalsIgnoreCase("player")) {
            //			ytLink = buffer;
            //			Log.e(getClass().getCanonicalName(), buffer);
        } else if (localName.equalsIgnoreCase("thumbnail_medium")) {
            thumbnail = buffer;
        } else if (localName.equalsIgnoreCase(TAG_YOUTUBE_ROOT)) {
            DbSocial.insertVideo(title, ytLink, dirLink, thumbnail);

        }
    }
}
