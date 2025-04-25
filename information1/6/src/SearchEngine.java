import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SearchEngine{
    private static final String DATA_DIR = "D:\\study\\idea\\information1\\information1";
    private static final String INDEX_FILE = "D:\\study\\idea\\information1\\information1\\tec\\index.txt";
    private static final String DICTIONARY_FILE = "D:\\study\\idea\\information1\\information1\\tec\\dictionary.txt";
    private static final int MAX_WORD_LENGTH = 6;
    private static Set<String> dictionary = new HashSet<>();
    private static Map<String, Set<String>> invertedIndex = new HashMap<>();
    private static Map<String, Set<String>> documentWords = new HashMap<>();

    public static void main(String[] args) throws IOException {
        loadDictionary();
        preprocessDocuments();
        searchWithJaccard();
    }

    private static void loadDictionary() throws IOException {
        File file = new File(DICTIONARY_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                dictionary.add(line.trim());

            }
        }
        System.out.println("词典加载完成，词典大小: " + dictionary.size());
    }

    private static void preprocessDocuments() throws IOException {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            System.out.println("文档目录不存在：" + DATA_DIR);
            return;
        }
        File[] files = dir.listFiles((d, name) ->
                name.endsWith(".txt") &&
                        !name.equals(DICTIONARY_FILE) &&
                        !name.equals(INDEX_FILE)
        );
        if (files == null || files.length == 0) {
            System.out.println("没有找到任何 .txt 文件！");
            return;
        }
        for (File file : files) {
            String docID = file.getName().replace(".txt", "");
            String content = readFileContent(file);
            List<String> words = segmentText(content);
            documentWords.put(docID, new HashSet<>(words));
            for (String word : words) {
                invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(docID);
            }
        }
    }

    private static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        return content.toString().trim();
    }

    private static List<String> segmentText(String text) {
        List<String> words = new ArrayList<>();
        int len = text.length();
        int index = 0;
        while (index < len) {
            String word = null;
            char currentChar = text.charAt(index);
            if (isChinese(currentChar)) {
                int maxPossibleLength = Math.min(MAX_WORD_LENGTH, len - index);
                for (int end = index + maxPossibleLength; end > index; end--) {
                    String sub = text.substring(index, end);
                    if (dictionary.contains(sub)) {
                        word = sub;
                        break;
                    }
                }
                if (word != null) {
                    words.add(word);
                    index += word.length();
                    continue;
                }
                String singleChar = text.substring(index, index + 1);
                if (dictionary.contains(singleChar)) {
                    words.add(singleChar);
                }
                index++;
            } else if (Character.isLetter(currentChar)) {
                int end = index;
                while (end < len && Character.isLetter(text.charAt(end))) {
                    end++;
                }
                word = text.substring(index, end).toLowerCase();
                words.add(word);
                index = end;
            } else {
                index++;
            }
        }
        return words;
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private static void searchWithJaccard() {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (true) {
            System.out.println("请输入要搜索的关键词（多个词用空格分隔，输入 'exit' 退出）：");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) break;
            if (input.isEmpty()) continue;

            List<String> keywords = segmentText(input);

            if (keywords.isEmpty()) {
                System.out.println("请输入有效关键词");
                continue;
            }
            Set<String> querySet = new HashSet<>(keywords);

            Map<String, Double> scores = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : documentWords.entrySet()) {
                String docID = entry.getKey();
                Set<String> docSet = entry.getValue();
                Set<String> intersection = new HashSet<>(docSet);
                intersection.retainAll(querySet);
                Set<String> union = new HashSet<>(docSet);
                union.addAll(querySet);
                double jaccard = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
                if (jaccard > 0) {
                    scores.put(docID, jaccard);
                }
            }

            if (scores.isEmpty()) {
                System.out.println("未找到匹配文档");
            } else {
                System.out.println("匹配文档（按Jaccard相似度排序）：");
                scores.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .forEach(e -> System.out.printf("%s.txt (%.3f)%n", e.getKey(), e.getValue()));
            }
        }
        scanner.close();
    }
}