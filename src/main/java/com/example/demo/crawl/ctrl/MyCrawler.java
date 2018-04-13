package com.example.demo.crawl.ctrl;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

public class MyCrawler extends WebCrawler {

    private CrawlStat crawlStat;

    public MyCrawler() {
        crawlStat = new CrawlStat();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (ParseZiRoom.FILTERS.matcher(href).matches() || !href.startsWith(ParseZiRoom.URL_PREFIX)) {
            return false;
        }
        return true;
//        return !ParseZiRoom.FILTERS.matcher(href).matches() && href.startsWith(ParseZiRoom.URL_PREFIX);
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("-----------爬取路径：" + url);
        crawlStat.incProcessedPages();

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            //count
            crawlStat.incTotalLinks(links.size());
            try {
                crawlStat.incTotalTextSize(htmlParseData.getText().getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException ignored) {
                // Do nothing
            }
            //document parse
            Document doc = Jsoup.parse(html);
            ParseZiRoom.parseHtmlAndWriteCSV(doc);
        }
    }

    /**
     * This function is called by controller to get the local data of this crawler when job is
     * finished
     */
    @Override
    public Object getMyLocalData() {
        return crawlStat;
    }

    /**
     * This function is called by controller before finishing the job.
     * You can put whatever stuff you need here.
     */
    @Override
    public void onBeforeExit() {
        dumpMyData();
    }

    public void dumpMyData() {
        int id = getMyId();
        // You can configure the log to output to file
        logger.info("Crawler {} > Processed Pages: {}", id, crawlStat.getTotalProcessedPages());
        logger.info("Crawler {} > Total Links Found: {}", id, crawlStat.getTotalLinks());
        logger.info("Crawler {} > Total Text Size: {}", id, crawlStat.getTotalTextSize());
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        String URL_PREFIX = "http://sh.ziroom.com/z/nl/z3.html";
        Document doc = Jsoup.connect(URL_PREFIX).get();

        Set<String> stringSet = ParseZiRoom.getDocumentURL(doc);
        System.out.println("totalSize:" + stringSet.size());

        Iterator iterator = stringSet.iterator();
        while (iterator.hasNext()) {
            String url = (String) iterator.next();
            System.out.println("distinct地址:" + url);

            doc = Jsoup.connect(url).get();
            ParseZiRoom.parseHtmlAndWriteCSV(doc);
        }
        System.out.println("总耗时(ms):" + (System.currentTimeMillis() - startTime));
    }

}
