package github.users.eirikma.iteratorgenerators;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Created by emaus on 24.10.2016.
 */
public class CsvReader {


    public static List<Map<String, String>> readCsvInputFromClasspathRef(String classpathRef) {
        return readCsvInput(Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathRef));
    }

    public static List<Map<String,String>> readCsvFile(File file) {
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            return readCsvInput(input);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<String> findFieldNamesFromHeader(InputStream input) {
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(input, Charset.defaultCharset()));
            String headerLine = lineNumberReader.readLine();
            char separator = findMostUsedSymbol(headerLine, new char[]{',', ';', '\t'});
            return Arrays.asList(headerLine.split(Pattern.quote("" + separator)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, String>> readCsvInput(InputStream input) {
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(input, Charset.defaultCharset()));
            String headerLine = lineNumberReader.readLine();
            char separator = findMostUsedSymbol(headerLine, new char[]{',', ';', '\t'});
            String[] split = headerLine.split(Pattern.quote("" + separator));
            return readCsvInput(lineNumberReader, separator, asList(split));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Map<String, String>> readCsvInput(LineNumberReader lineNumberReader, char separator, List<String> fieldNames) {
        try {
            ArrayList<Map<String, String>> result = new ArrayList<>();
            String splittingToken = Pattern.quote("" + separator);
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                String[] split = line.split(splittingToken);
                HashMap<String, String> row = new HashMap<>(fieldNames.size());
                for (int i = 0; i < fieldNames.size(); i++) {
                    String fieldName = fieldNames.get(i);
                    if (split.length > i) {
                        row.put(fieldName, split[i]);
                    }
                }
                //  handling rows with extra contents here:
                 if (split.length > fieldNames.size()) {
                     row.put("___REMAINING___", join(asList(Arrays.copyOfRange(split, fieldNames.size(), split.length)), "" + separator));
                 }
                 result.add(row);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static char findMostUsedSymbol(String line, char[] candidates) {
        int highestCount = 0;
        char bestSeparator = candidates[0];
        for (char candidate : candidates) {
            int occurrences = StringUtils.countMatches(line, ""+candidate);
            if (occurrences > bestSeparator) {
                bestSeparator = candidate;
            }
        }
        return bestSeparator;
    }

}
