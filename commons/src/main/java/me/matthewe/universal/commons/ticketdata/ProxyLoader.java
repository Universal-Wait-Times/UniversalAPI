package me.matthewe.universal.commons.ticketdata;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProxyLoader {

    public static List<Proxy> fetchHttpsProxies() throws Exception {
        List<Proxy> proxyList = new ArrayList<>();
        Document doc = Jsoup.connect("https://www.us-proxy.org/").get();

        Elements rows = doc.select("table#proxylisttable tbody tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 7) continue;

            String ip = cols.get(0).text();
            int port = Integer.parseInt(cols.get(1).text());
            String https = cols.get(6).text();

            if ("yes".equalsIgnoreCase(https)) {
                proxyList.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
            }
        }

        return proxyList;
    }


    public static Proxy getRandomProxy(List<Proxy> proxies) {
        return proxies.get(new Random().nextInt(proxies.size()));
    }
}