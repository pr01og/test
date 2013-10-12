package com.probojnik.terminal.data.synchronization;

import android.util.Xml;
import com.probojnik.terminal.data.sqlite.DataHelper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stanislav Shamji
 */
class SyncParser {
    private static final String ns = null;


    public SyncEntry parseToDB(InputStream in, DataHelper context) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            SyncEntry result = readResponse(parser, context);
            result.closeDB();
            return result;
        } finally {
            in.close();
        }
    }

    private SyncEntry readResponse(XmlPullParser parser, DataHelper context) throws XmlPullParserException, IOException {
        SyncEntry entry = new SyncEntry(context);

        parser.require(XmlPullParser.START_TAG, ns, "request");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("status-code")) {
                entry.setStatusCode(readContent(parser, "status-code"));
            } else if (name.equals("status-detail")) {
                entry.setStatusDetail(readContent(parser, "status-detail"));
            } else if (name.equals("time")) {
                entry.setTime(readContent(parser, "time"));
            } else if (name.equals("script")) {
                entry.addRow(readAttribute(parser, "script", "row"));
            }
        }
        return entry;
    }

    private String readContent(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return title;
    }

    private String readAttribute(XmlPullParser parser, String name, String attr) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String link = parser.getAttributeValue(null, attr);

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, name);
        return link;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
