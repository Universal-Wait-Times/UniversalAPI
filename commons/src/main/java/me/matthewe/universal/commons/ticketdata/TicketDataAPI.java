package me.matthewe.universal.commons.ticketdata;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketDataAPI {

  private String fetchTableAfterHeading(
          String url,
          String headingText,
          int tableIndex) throws IOException {
    final String tableOpen = "<table class=\"calendar-table sortable\"";
    final String tableClose = "</table>";

    enum State {
      SEARCH_HEADING,
      SEARCH_TABLE_OPEN,
      CAPTURING
    }
    State state = State.SEARCH_HEADING;
    int seenTables = 0;

    StringBuilder outBuf = new StringBuilder();
    StringBuilder carry = new StringBuilder();

    try (Reader r = new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8)) {
      char[] chunk = new char[8192];
      int len;
      while ((len = r.read(chunk)) != -1) {
        carry.append(chunk, 0, len);

        boolean again;
        do {
          again = false;
          String buf = carry.toString();

          switch (state) {
            case SEARCH_HEADING:
              int hi = buf.indexOf(headingText);
              if (hi != -1) {
                // drop everything up through the heading
                carry = new StringBuilder(buf.substring(hi + headingText.length()));
                state = State.SEARCH_TABLE_OPEN;
                again = true; // re‐evaluate in new state
              }
              break;

            case SEARCH_TABLE_OPEN:
              int ti = buf.indexOf(tableOpen);
              if (ti != -1) {
                seenTables++;
                if (seenTables == tableIndex) {
                  // start capturing from the '<' of that table
                  state = State.CAPTURING;
                  carry = new StringBuilder(buf.substring(ti));
                  outBuf.append(carry);
                } else {
                  // skip past this open‐tag and keep looking
                  carry = new StringBuilder(buf.substring(ti + 1));
                  again = true;
                }
              }
              break;

            case CAPTURING:
              int ci = buf.indexOf(tableClose);
              if (ci != -1) {
                // include the closing tag, then return
                outBuf.append(buf, 0, ci + tableClose.length());
                return outBuf.toString();
              } else {
                // no close yet—append all and clear carry
                outBuf.append(buf);
                carry.setLength(0);
              }
              break;
          }
        } while (again);

        // keep only the tail end of carry to handle broken tags
        int maxKeep = Math.max(headingText.length(),
                Math.max(tableOpen.length(), tableClose.length()));
        if (carry.length() > maxKeep) {
          carry = new StringBuilder(
                  carry.substring(carry.length() - maxKeep)
          );
        }
      }
    }

    throw new IOException(
            "Never saw heading or the " + tableIndex + "ᵗʰ table after it"
    );
  }


  private String pullTicketData() {
    try {
      String snippet = fetchTableAfterHeading(
              "https://www.thrill-data.com/epic-universe-tickets/#",
              "All Ticket Data Combined Availability",
              1 // first table after that heading
      );
      return snippet;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Parses your extracted table HTML and returns a list of rows,
   * each row itself being a list of cell‐texts.
   */
  private Map < String, TicketData > parseTicketTable(String tableHtml) throws IOException {
    Map < String, TicketData > map = new HashMap < > ();

    Document doc = Jsoup.parseBodyFragment(tableHtml);
    Element table = doc.selectFirst("table.calendar-table.sortable");
    if (table == null) {
      throw new IOException("No table.calendar-table.sortable found in snippet");
    }

    for (Element rowEl: table.select("tr")) {
      List < String > cells = new ArrayList < > ();
      // grab both <th> and <td>
      for (Element cellEl: rowEl.select("th, td")) {
        cells.add(cellEl.text().trim());
      }


      String date = cells.get(0);
      int available = parseNumber(cells.get(1));
      int capacity = parseNumber(cells.get(2));
      int ticketTypesRemaining = parseNumber(cells.get(4));
      map.put(date, new TicketData(date, available, capacity, ticketTypesRemaining));
    }
    return map;
  }

  private int parseNumber(String input) {
    try {
      return Integer.parseInt(input);
    } catch (Exception e) {
      return -1;
    }
  }

  public Map < String, TicketData > pullTicketTable() {
    String s = pullTicketData();
    Map < String, TicketData > res = new HashMap < > ();
    if (s == null) return res;
    try {
      return parseTicketTable(s);
    } catch (IOException e) {
      e.printStackTrace();
      return res;
    }
  }
  private void printCool(TicketData ticketData) {
    int capacity  = ticketData.getCapacity();
    int available = ticketData.getAvailable();
    int sold      = capacity - available;


// compute percentage sold
    double percentSold = ((double) sold / capacity) * 100;

// print it, escaping the % sign as %% in the format string
    System.out.println(String.format(
            "Capacity on %s: capacity=%d, available=%d, sold=%d (%.2f%% sold)",
            ticketData.getDate(),
            capacity,
            available,
            sold,
            percentSold
    ));
  }

  public static void main(String[] args) throws Exception {
    TicketDataAPI ticketDataAPI = new TicketDataAPI();

    Map<String, TicketData> data = ticketDataAPI.pullTicketTable();


    ticketDataAPI.printCool( data.get("05-23-2025"));
    ticketDataAPI.printCool( data.get("05-24-2025"));
    ticketDataAPI.printCool( data.get("05-26-2025"));
    ticketDataAPI.printCool( data.get("05-27-2025"));
    ticketDataAPI.printCool(data.get("06-03-2025"));
    ticketDataAPI.printCool(data.get("06-04-2025"));
    ticketDataAPI.printCool(data.get("06-05-2025"));
    ticketDataAPI.printCool(data.get("06-06-2025"));
    ticketDataAPI.printCool(data.get("06-07-2025"));
    ticketDataAPI.printCool(data.get("06-08-2025"));
    ticketDataAPI.printCool(data.get("06-09-2025"));
    ticketDataAPI.printCool(data.get("06-10-2025"));
    ticketDataAPI.printCool(data.get("06-11-2025"));
    ticketDataAPI.printCool(data.get("06-12-2025"));
    ticketDataAPI.printCool(data.get("06-13-2025"));
    ticketDataAPI.printCool(data.get("06-14-2025"));
    ticketDataAPI.printCool(data.get("06-15-2025"));
    ticketDataAPI.printCool(data.get("06-16-2025"));
  }
}