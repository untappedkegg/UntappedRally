package com.untappedkegg.rally.schedule;

import com.untappedkegg.rally.data.BaseSAX;
import com.untappedkegg.rally.util.DateManager;

import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.Locale;

public class SAXSchedule extends BaseSAX {

    private String dates;
    private String location;
    private String sequence;
    private String website;
    private String startDate = "";
    private String endDate = "";
    private String year;
    private String from;
    private String to;
    private String eventSite = "";
    private String fromTo = "";
    private String series;
    protected String name;

    public SAXSchedule() {
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        localName = localName.toLowerCase(Locale.US);

        super.endElement(namespaceURI, localName, qName);
        switch (localName) {
            case "dates":
            case "date":
                this.dates = buffer;
                break;
            case "seq":
                this.sequence = buffer;
                break;
            case "ws":
                this.website = buffer;
                break;
            case "series":
            case "ser":
                series = buffer;
                break;
            case "wsevent":
            case "wse":
                this.eventSite = buffer;
                break;
            case "year":
            case "yr":
                this.year = buffer;
                break;
            case "loc":
                this.location = buffer;
                break;
            case "name":
                name = buffer;
                break;
            case "event":
            case "evt":


                if ("TBD".equalsIgnoreCase(dates) || "CANCELLED".equalsIgnoreCase(dates)) {
                    startDate = endDate = dates;
                } else {
                    try {
                        final String[] datesArray = dates.split(" ");
                        if (datesArray.length > 3) {
                            startDate = DateManager.ISO8601_DATEONLY.format(DateManager.RALLY_AMERICA.parse(datesArray[0] + " " + datesArray[1] + ", " + year));
                            endDate = DateManager.ISO8601_DATEONLY.format(DateManager.RALLY_AMERICA.parse(datesArray[0] + " " + datesArray[3] + " " + year));
                        } else {
                            endDate = startDate = DateManager.ISO8601_DATEONLY.format(DateManager.RALLY_AMERICA.parse(dates));
                        }

                    } catch (Exception e) {
                        startDate = endDate = "TBD";
                    }
                }

                try {
                    from = DateManager.SCHED_FROM.format(DateManager.ISO8601_DATEONLY.parse(startDate));

                    fromTo = from;
                } catch (ParseException e) {
                    fromTo = !startDate.equals(endDate) ? "TBD" : startDate;
                }
                try {
                    to = DateManager.SCHED_TO.format(DateManager.ISO8601_DATEONLY.parse(endDate));

                    if (!startDate.equalsIgnoreCase(endDate)) {
                        fromTo += " to " + to;
                    } else {
                        fromTo = to;
                    }
                } catch (ParseException e) {
                    //				e.printStackTrace();
                }
                final String[] yearActual = eventSite.split("/");
                DbSchedule.insert_schedule(name, startDate, endDate, fromTo, imgLink, series, year, website, sequence, eventSite, location, yearActual[yearActual.length - 2]);
                website = "";
                eventSite = "";
                location = "";

                break;
        }
    }
}
