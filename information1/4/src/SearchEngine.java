import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

public class SearchEngine {
    private static final String DATA_DIR = "D:\\IDEA\\information1";
    private static final String INDEX_FILE = "index.txt";
    private static final String DICTIONARY_FILE = "dictionary.txt";
    private static final int MAX_WORD_LENGTH = 6;  // **调整为6以支持更长词语**
    private static Set<String> dictionary = new HashSet<>();
    private static Map<String, List<String>> invertedIndex = new HashMap<>();

    public static void main(String[] args) throws IOException {
        loadDictionary();
        preprocessDocuments();
        searchOnline();
    }

    private static void loadDictionary() throws IOException {
        File file = new File(DICTIONARY_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.trim());
            }
        }
        System.out.println("词典加载完成，词典大小: " + dictionary.size());
    }

    private static void searchOnline() throws IOException {
        loadIndexFromFile();
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
            boolean allKeywordsExist = keywords.stream().allMatch(invertedIndex::containsKey);
            if (!allKeywordsExist) {
                System.out.println("未找到匹配文档");
                continue;
            }
            List<Set<String>> documentSets = keywords.stream()
                    .map(keyword -> new HashSet<>(invertedIndex.get(keyword)))
                    .collect(Collectors.toList());
            Set<String> result = documentSets.stream()
                    .reduce((set1, set2) -> {
                        set1.retainAll(set2);
                        return set1;
                    })
                    .orElse(Collections.emptySet());
            if (result.isEmpty()) {
                System.out.println("未找到匹配文档");
            } else {
                System.out.println("找到以下匹配文档：");
                result.forEach(doc -> System.out.println(doc + ".txt"));  // **正确输出文档**
            }
        }
        scanner.close();
    }

    private static void preprocessDocuments() throws IOException {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            System.out.println("文档目录不存在：" + DATA_DIR);
            return;
        }
        File[] files = dir.listFiles((d, name) ->
                name.endsWith(".txt") &&
                        !name.equals(DICTIONARY_FILE) &&   // **新增排除条件**
                        !name.equals(INDEX_FILE)           // **排除index.txt**
        );
        if (files == null || files.length == 0) {
            System.out.println("没有找到任何 .txt 文件！");
            return;
        }
        for (File file : files) {
            String docID = file.getName().replace(".txt", "");
            String content = readFileContent(file);
            indexDocument(docID, content);
        }
        saveIndexToFile();
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

    private static void indexDocument(String docID, String content) {
        List<String> words = segmentText(content);
        for (String word : words) {
            invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(docID);
        }
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

    private static void saveIndexToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(INDEX_FILE), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, List<String>> entry : invertedIndex.entrySet()) {
                writer.write(entry.getKey() + ":" + String.join(",", entry.getValue()));
                writer.newLine();
            }
        }
    }

    private static void loadIndexFromFile() throws IOException {
        File file = new File(INDEX_FILE);
        if (!file.exists()) return;
        invertedIndex.clear();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int colonIndex = line.indexOf(':');
                if (colonIndex == -1) continue;
                String keyword = line.substring(0, colonIndex);
                List<String> docs = Arrays.asList(line.substring(colonIndex + 1).split(","));
                invertedIndex.put(keyword, new ArrayList<>(docs));
            }
        }
    }
}