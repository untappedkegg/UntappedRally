package com.untappedkegg.rally.news;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.data.BaseSAX;
import com.untappedkegg.rally.util.DateManager;

import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SAXNews extends BaseSAX {

    private final String uri;

    public SAXNews(final String uri) {
        this.uri = uri;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        localName = localName.toLowerCase(Locale.US);

        super.endElement(namespaceURI, localName, qName);

        if (localName.equals("pubdate") || localName.equals("date")) {
            try {
                /**
                 * Handle the news individually as necessary based on
                 * the format of the pubDate
                 */
                switch (uri) {
//                    case AppState.SOURCE_IRALLY:
//                        pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE_OFFSET.parse(buffer));
//                        shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE_OFFSET.parse(buffer));
//                        break;
                    case AppState.SOURCE_RALLY_AMERICA:
                        pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE_RA.parse(buffer));
                        shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE_RA.parse(buffer));
                        break;
                    default:
                        pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE.parse(buffer));
                        shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE.parse(buffer));
                        break;
                }
            } catch (ParseException e) {
                pubDate = buffer;
                shortDate = buffer;
                if (BuildConfig.DEBUG)
                    e.printStackTrace();
            }

            /**
             * pull the imgLink from the raw buffer for Rally America
             */
        } else if (localName.equals("description") && uri.equals(AppState.SOURCE_RALLY_AMERICA)) {

            Pattern pattern = Pattern.compile("src=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(buffer);
            if (matcher.find()) {
                imgLink = AppState.RA_BASE_URL + matcher.group(1);
            }

        } else if (localName.equals("item")) {
            if (uri.equals(AppState.SOURCE_RALLY_AMERICA)) {
                description = description.replace("{summary}", "").trim();
            }

            try {
                if (DateManager.timeBetweenInDays(DateManager.DATABASE.parse(pubDate).getTime()) < Integer.parseInt(AppState.getSettings().getString("pref_news_cutoff", "30"))) {
//                        // Filter out all Non Rally America stories form iRally
//                    if (!uri.equals(AppState.SOURCE_RALLY_AMERICA) && (title.startsWith("RA") || title.startsWith("USA"))) {
//                        DbNews.news_insert(title, link, description.trim(), pubDate, shortDate, uri, imgLink);
//                    } else if (!uri.equals(AppState.SOURCE_IRALLY)) {
                        DbNews.news_insert(title, link, description.trim(), pubDate, shortDate, uri, imgLink);
//                    } else {
//                        return;
//                    }
                }
            } catch (ParseException e) {
                if(BuildConfig.DEBUG)
                    e.printStackTrace();
            }

            imgLink = null;
        }

    }

}
