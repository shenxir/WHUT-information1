import java.util.Scanner;

public class DocumentSearch {
    public static void main(String[] args) {
        String[] docIDs = {"d1", "d2", "d3"};
        String[] contents = {
                "l like to watch the sun set with my friend.",
                "The Best Places To Watch The Sunset.",
                "My friend watch the sun come up."
        };

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input the keyword to search:");
        String keyword = scanner.nextLine().trim().toLowerCase();

        for (int i = 0; i < docIDs.length; i++) {
            if (containsSubstring(contents[i].toLowerCase(), keyword)) {
                System.out.println(docIDs[i] + ".txt");
            }
        }
        scanner.close();
    }

    public static boolean containsSubstring(String content, String keyword) {
        int contentLength = content.length();
        int keywordLength = keyword.length();

        if (keywordLength > contentLength) {
            return false;
        }

        for (int i = 0; i <= contentLength - keywordLength; i++) {
            int j = 0;
            while (j < keywordLength && content.charAt(i + j) == keyword.charAt(j)) {
                j++;
            }
            if (j == keywordLength) {
                return true;
            }
        }
        return false;
    }
}
