import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    private static Path srcPath;
    private static Path desPath;

    public static void main(String[] args) {
        String sPath = "D:\\JavaProjects\\Tasks\\SiteSrc";
        String dPath = "D:\\JavaProjects\\Tasks\\SiteDst";
        srcPath = Paths.get(sPath);
        desPath = Paths.get(dPath);
        shtmlFinder(srcPath);
    }

    private static void shtmlFinder(Path path) {
        File folder = new File(path.toString());

        try {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    //файл найден, если файл после последней точки содержит shtml, лезем в него
                    if (file.getName().substring(file.getName().lastIndexOf(".")).contains("shtml") ||
                            file.getName().substring(file.getName().lastIndexOf(".")).contains("html")) {

                        Path newPath = Paths.get(desPath.toString() + "\\" + srcPath.relativize(file.toPath()));
                        String newPathString = newPath.toString();
                        if(file.getName().substring(file.getName().lastIndexOf(".")).contains("html")){
                            newPathString = newPath.toString().replace(".html", ".shtml");
                        }
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newPathString), "UTF8"));
                        bufferedWriter.write(parser(file));
                        bufferedWriter.flush();

                    } else {
                        Files.copy(file.toPath(), new File(desPath.toString() + "\\" + srcPath.relativize(file.toPath())).toPath());
                    }
                } else {
                    Path subDir = desPath.resolve(srcPath.relativize(file.toPath()));
                    Files.createDirectories(subDir);
                    shtmlFinder(file.toPath());
                }
            }
        } catch (Exception ex) {
            ex.getStackTrace();
        }

    }

    private static String parser(File file) {
        Document doc = new Document("");
        try {

            ArrayList<String> lines = utfDecoder(file);

            String fileText = "";
            for (String line : lines) {
                if (line.contains("</header>")) {
                    line += "\n<!--#include virtual=\"/aside/main_nav.ssi\" -->\n\n";
                } else if (line.contains("div class=\"divfooter\"")) {
                    line = "<!--#include virtual=\"/aside/footer.ssi\" -->" + line;
                } else if (line.contains("<meta charset=\"windows-1251\" />")){
                    line = line.replace("windows-1251", "UTF-8");
                }
                fileText += line;
            }

            doc = Jsoup.parse(fileText);

            Element meta = doc.selectFirst("meta[charset]");
            meta.attr("charset", "UTF-8");
            Element logotype = doc.selectFirst("div.logotype");
            logotype.remove();
            Element logoline = doc.selectFirst("div.logoline");
            logoline.remove();
            Element footer = doc.selectFirst("div.divfooter");
            footer.remove();

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (!link.attr("href").substring(0, 4).equals("http") &&
                        link.attr("href").substring(link.attr("href").lastIndexOf(".")).equals(".html")) {
                    String newLink = link.attr("href").replace(".html", ".shtml");
                    link.attr("href", newLink);
                }
            }

        } catch (Exception ex) {
            ex.getStackTrace();
        }
        return doc.toString();

    }

    private static ArrayList<String> utfDecoder (File file){
        ArrayList<String> lines;

        try {
            lines = (ArrayList<String>)Files.readAllLines(file.toPath());

        } catch (Exception ex) {
            ex.getStackTrace();
            lines = cpDecoder(file);
        }

        return lines;
    }

    private static ArrayList<String> cpDecoder (File file){
        ArrayList<String> lines = new ArrayList<>();
        try {

            lines = (ArrayList<String>) Files.readAllLines(Paths.get(String.valueOf(file)), Charset.forName("windows-1251"));


        } catch (Exception ex) {
            System.out.println("Чозанах?!");
            ex.getStackTrace();
        }

        return lines;
    }
}
