package com.keggemeyer.rallyamerica.schedule;

import com.keggemeyer.rallyamerica.data.BaseSAX;
import com.keggemeyer.rallyamerica.util.DateManager;

import org.xml.sax.SAXException;

import java.text.ParseException;

public class SAXSchedule extends BaseSAX {

    private String dates;
    private String location;
    private String sequence;
    private String website;
    private String startDate = "";
    private String endDate = "";
    private String year;
    private String[] datesArray;
    private String from;
    private String to;
    private String eventSite = "";
    private String fromTo = "";

    public SAXSchedule() {
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        super.endElement(namespaceURI, localName, qName);
        if (localName.equalsIgnoreCase("dates")) {
            this.dates = buffer;
        } else if (localName.equalsIgnoreCase("seq")) {
            this.sequence = buffer;
        } else if (localName.equalsIgnoreCase("ws")) {
            this.website = buffer;
        } else if (localName.equalsIgnoreCase("wsevent")) {
            this.eventSite = buffer;
        } else if (localName.equalsIgnoreCase("year")) {
            this.year = buffer;
        } else if (localName.equalsIgnoreCase("loc")) {
            this.location = buffer;
        } else if (localName.equalsIgnoreCase("event")) {


            if ("TBD".equalsIgnoreCase(dates) || "CANCELLED".equalsIgnoreCase(dates)) {
                startDate = endDate = dates;
            } else {
                try {
                    datesArray = dates.split(" ");
                    if (datesArray.length > 3) {
                        startDate = DateManager.ISO8601_DATEONLY.format(DateManager.RALLY_AMERICA.parse(datesArray[0] + " " + datesArray[1] + ", " + year));
                        endDate = DateManager.ISO8601_DATEONLY.format(DateManager.RALLY_AMERICA.parse(datesArray[0] + " " + datesArray[3] + " " + year));
                    } else {
                        endDate = startDate = DateManager.ISO8601_DATEONLY.format(DateManager.RALLY_AMERICA.parse(dates));
                    }
                    //					Log.w(this.getClass().getCanonicalName(), String.format("Name = %s, Start = %s, end = %s, Year = %s, Website = %s, Dates = %s", name, startDate, endDate, year, website, dates));
                } catch (Exception e) {
                    startDate = endDate = "TBD";
                    //					e.printStackTrace();
                }
            }

            //			String title, startDate, endDate, fromTo, country, series, year;
            //			Log.w(this.getClass().getCanonicalName(), String.format("Name = %s, Start = %s, end = %s, Year = %s, Website = %s, Dates = %s", name, startDate, endDate, year, website, dates));
            try {
                from = DateManager.SCHED_FROM.format(DateManager.ISO8601_DATEONLY.parse(startDate));

                fromTo = from;
            } catch (ParseException e) {
                fromTo = !startDate.equals(endDate) ? "TBD" : startDate;
            }
            try {
                to = DateManager.SCHED_TO.format(DateManager.ISO8601_DATEONLY.parse(endDate));
                //				startDate.equalsIgnoreCase(endDate) ? fromTo = to : fromTo += " to " + to;
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

        }
    }
}
