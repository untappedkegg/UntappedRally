package com.untappedkegg.rally.news;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.data.BaseSAX;
import com.untappedkegg.rally.util.DateManager;

import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SAXNews extends BaseSAX {

    private final String uri;

    public SAXNews(String uri) {
        this.uri = uri;

    }


    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        super.endElement(namespaceURI, localName, qName);

        if (localName.equalsIgnoreCase("pubdate") || localName.equalsIgnoreCase("date")) {
            try {
                /**
                 * Handle the news individually as necessary based on
                 * the format of the pubDate
                 */
                if (uri.equals(AppState.SOURCE_BEST_OF_RALLY)) {
                    pubDate = DateManager.formatForDatabase(DateManager.bestOfRally.parse(buffer));
                    shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.bestOfRally.parse(buffer));
                    //				} else if (uri.equals(AppState.SOURCE_WRC_COM)) {
                    //					pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE_TIMEZONE.parse(buffer));
                    //					shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE_TIMEZONE.parse(buffer));
                } else if (uri.equals(AppState.SOURCE_IRALLY) || uri.equals(AppState.SOURCE_CITROEN)) {
                    pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE_OFFSET.parse(buffer));
                    shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE_OFFSET.parse(buffer));
                } else if (uri.equals(AppState.SOURCE_RALLY_AMERICA)) {
                    //					Log.w(this.getClass().getCanonicalName(), buffer);
                    pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE_RA.parse(buffer));
                    shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE_RA.parse(buffer));
                } else {

                    pubDate = DateManager.formatForDatabase(DateManager.RSS_DATE.parse(buffer));
                    shortDate = DateManager.DAYONLY_HUMAN_READABLE.format(DateManager.RSS_DATE.parse(buffer));
                }
            } catch (ParseException e) {
                //				e.printStackTrace();
                pubDate = buffer;
                shortDate = buffer;
            }

            /**
             * pull the imgLink from the raw buffer for Rally America
             */
        } else if (localName.equalsIgnoreCase("description") && uri.equals(AppState.SOURCE_RALLY_AMERICA)) {

            Pattern pattern = Pattern.compile("src=\"(.*?)\"", Pattern.CASE_INSENSITIVE);//Pattern.compile("src=\"[^\"]*");
            Matcher matcher = pattern.matcher(buffer);
            if (matcher.find()) {
                imgLink = "http://www.rally-america.com" + matcher.group(1);
            }
            //				description = android.text.Html.fromHtml(buffer.substring(buffer.indexOf("/>", 0)+2)).toString();

        } else if (localName.equalsIgnoreCase("item")) {
            if (uri.equals(AppState.SOURCE_RALLY_AMERICA)) {
                description = description.replace("{summary}", "").trim();
            }

            try {
                if (DateManager.timeBetweenInDays(DateManager.DATABASE.parse(pubDate).getTime()) < AppState.NEWS_OLD_ITEM_CUTOFF) {
                    if ((!uri.equals(AppState.SOURCE_RALLY_AMERICA) && title.startsWith("RA")) || uri.equals(AppState.SOURCE_RALLY_AMERICA)) {
                        DbNews.news_insert(title, link, description.trim(), pubDate, shortDate, uri, imgLink);
                    }
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                //					e.printStackTrace();
                //					DbNews.news_insert(title, link, description, pubDate, shortDate, uri, imgLink);
            }

            //			DbNews.news_insert(title, link, description, pubDate, shortDate, uri, imgLink);
            imgLink = null;
        }

    }

}
