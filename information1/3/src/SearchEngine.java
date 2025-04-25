import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchEngine {
    private static final String DATA_DIR = "D:\\IDEA\\information1";
    private static final String INDEX_FILE = "index.txt";
    private static Map<String, List<String>> invertedIndex = new HashMap<>();

    public static void main(String[] args) throws IOException {
        preprocessDocuments();
        searchOnline();
    }

    private static void searchOnline() throws IOException {
        loadIndexFromFile();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("请输入要搜索的关键词（多个词用空格分隔，输入 'exit' 退出）:");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("exit")) break;
            if (input.isEmpty()) continue;

            List<String> keywords = Arrays.stream(input.split("\\s+"))
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toList());

            if (keywords.isEmpty()) {
                System.out.println("请输入有效关键词");
                continue;
            }

            boolean allKeywordsExist = keywords.stream()
                    .allMatch(invertedIndex::containsKey);

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
                result.forEach(doc -> System.out.println(doc + ".txt"));
            }
        }
        scanner.close();
    }

    private static void preprocessDocuments() throws IOException {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;

        for (File file : files) {
            String docID = file.getName().replace(".txt", "");
            String content = readFileContent(file);
            indexDocument(docID, content);
        }
        saveIndexToFile();
    }

    private static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        return content.toString().trim();
    }

    private static void indexDocument(String docID, String content) {
        String[] words = content.toLowerCase().split("\\W+");
        for (String word : words) {
            invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(docID);
        }
    }

    private static void saveIndexToFile() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(INDEX_FILE))) {
            out.writeObject(invertedIndex);
        }
    }

    private static void loadIndexFromFile() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(INDEX_FILE))) {
            invertedIndex = (Map<String, List<String>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("索引文件未找到或读取失败，可能需要重新生成。");
        }
    }
}
