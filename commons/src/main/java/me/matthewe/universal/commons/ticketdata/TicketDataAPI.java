package me.matthewe.universal.commons.ticketdata;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
@Log
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

//
//    URLConnection connection = new URL(url).openConnection();
//    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
//    connection.setConnectTimeout(7000);
//    connection.setReadTimeout(10000);
    File file = new File("cache.txt"); // or provide full path
    log.info("FOUND CACHE FILE " + file.getAbsolutePath());

    try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
//    try (Reader r = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
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
              System.getenv("CAPACITY_ENDPOINT"),
              "All Ticket Data Combined Availability",
            1// first table after that heading
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
//    System.out.println(tableHtml);
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


//      System.out.println(cells);

      if (cells.isEmpty())continue;
      String date = cells.get(0);
      if (date.equalsIgnoreCase("Date")) continue;
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
    if (ticketData==null)return;
    int capacity  = ticketData.getCapacity();
    int available = ticketData.getAvailable();
    int sold      = capacity - available;


// compute percentage sold
    double percentSold = ((double) sold / capacity) * 100;

// print it, escaping the % sign as %% in the format string

    String dayOfWeek =  getDayOfWeek(ticketData.getDate());
    String msg = String.format(
            "Capacity on %s (%s): capacity=%d, available=%d, sold=%d (%.2f%% sold)",
            ticketData.getDate(),
            dayOfWeek,
            capacity,
            available,
            sold,
            percentSold
    );

    if (dayOfWeek.equalsIgnoreCase("Saturday")) {
      msg+="\n";
    }
    System.out.println(msg);
  }

  public static void main(String[] args) throws Exception {
    TicketDataAPI ticketDataAPI = new TicketDataAPI();

    Map<String, TicketData> data = ticketDataAPI.pullTicketTable();


    ticketDataAPI.printCool( data.get("05-23-2025"));
    ticketDataAPI.printCool( data.get("05-24-2025"));
    ticketDataAPI.printCool( data.get("05-25-2025"));
    ticketDataAPI.printCool( data.get("05-26-2025"));
    ticketDataAPI.printCool( data.get("05-27-2025"));
    ticketDataAPI.printCool( data.get("05-28-2025"));
    ticketDataAPI.printCool( data.get("05-29-2025"));
    ticketDataAPI.printCool( data.get("05-30-2025"));
    ticketDataAPI.printCool( data.get("05-31-2025"));
    ticketDataAPI.printCool(data.get("06-01-2025"));
    ticketDataAPI.printCool(data.get("06-02-2025"));
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
    ticketDataAPI.printCool(data.get("06-17-2025"));
    ticketDataAPI.printCool(data.get("06-18-2025"));
    ticketDataAPI.printCool(data.get("06-19-2025"));
    ticketDataAPI.printCool(data.get("06-20-2025"));
    ticketDataAPI.printCool(data.get("06-21-2025"));
    ticketDataAPI.printCool(data.get("06-22-2025"));
    ticketDataAPI.printCool(data.get("06-23-2025"));
    ticketDataAPI.printCool(data.get("06-24-2025"));
    ticketDataAPI.printCool(data.get("06-25-2025"));
    ticketDataAPI.printCool(data.get("06-26-2025"));
    ticketDataAPI.printCool(data.get("06-27-2025"));
    ticketDataAPI.printCool(data.get("06-28-2025"));
    ticketDataAPI.printCool(data.get("06-29-2025"));
    ticketDataAPI.printCool(data.get("06-30-2025"));
  }

  public String getDayOfWeek(String dateStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    LocalDate date = LocalDate.parse(dateStr, formatter);
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US);
  }
}