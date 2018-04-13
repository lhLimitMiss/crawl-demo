package com.example.demo.crawl.ctrl;

import com.csvreader.CsvWriter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ParseZiRoom {

    private final static String CSV_PATH = "/data/crawl/data.csv";

    public static final String regex = "^([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[fF][tT][pP]:/*)(([A-Za-z0-9-~]+).)+(" +
            "[A-Za-z0-9-~\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)$";
    /**
     * 爬取匹配原则
     */
    public static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|ico"
            + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    public static final String URL_PREFIX = "http://gz.ziroom.com/z/nl/";

    private File csv;

    public ParseZiRoom() {
        csv = new File(CSV_PATH);
        if (!csv.exists()) {
            try {
                csv.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveCSV(List<String[]> arrList) {
        try {
            CsvWriter cw;
            BufferedWriter out = new BufferedWriter(new
                    OutputStreamWriter(new FileOutputStream(CSV_PATH, true), "GBK"), 1024);
//            cw = new CsvWriter(CSV_PATH, ',', Charset.forName("GBK"));
            cw = new CsvWriter(out, ',');
            for (String[] arr : arrList) {
                cw.writeRecord(arr);
            }
            out.close();
            cw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析document文档，写入csv文件
     *
     * @param doc
     */
    public static void parseHtmlAndWriteCSV(Document doc) {
        List<String[]> csvList = new ArrayList<>();
        String[] arrs = null;
        Elements contents = doc.select("li[class=clearfix]");
        for (Element c : contents) {
            // 图片
            String img = c.select(".img img").first().attr("_src");
            System.out.println("图片：" + img);

            // 地址
            Element txt = c.select("div[class=txt]").first();
            String arr1 = txt.select("h3 a").first().text();
            String arr2 = txt.select("h4 a").first().text();
            String arr3 = txt.select("div[class=detail]").first().text();

            String address = arr1.concat(arr1 + ",").concat(arr2 + ",").concat(arr3);
            System.out.println("地址：" + address);

            // 说明
            String rank = txt.select("p").first().text();
            String[] ranks = rank.split("\\|");
            String area = ranks[0].trim();
            String floor = ranks[1].trim();
            String doorModel = ranks[2].trim();
            System.out.println("area:" + area + ",floor:" + floor + ",doorModel:" + doorModel);

            // 价格
            String pirce = c.select("p[class=price]").first().text();
            System.out.println("价格：" + pirce);
            //save csv
            arrs = new String[]{img, pirce, area, floor, doorModel, address};
            csvList.add(arrs);
        }

        if (csvList != null) {
            saveCSV(csvList);
        }
    }

    public static boolean chkVisitorWebUrl(String href) {
        return !FILTERS.matcher(href).matches() && href.startsWith(URL_PREFIX);
    }

    /**
     * 获取查询区域的url,经过匹配规则和去重过滤
     *
     * @param doc
     * @return
     */
    public static Set<String> getDocumentURL(Document doc) {
        Elements newsHeadlines = doc.select(".clearfix .filterList a");
        Set<String> hrefSet = new HashSet<>();
        for (Element c : newsHeadlines) {
            String hrefValue = c.select("a").attr("href");
            if (StringUtils.isNotBlank(hrefValue) && hrefValue.indexOf("http:") == -1) {
                hrefValue = "http:" + hrefValue;
            }
            Pattern pattern = Pattern.compile(regex);
            if (pattern.matcher(hrefValue).matches() && chkVisitorWebUrl(hrefValue)) {
                hrefSet.add(hrefValue);
            }
            System.out.println(hrefValue);
        }
        return hrefSet;
    }
}
