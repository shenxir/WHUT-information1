import java.util.Scanner;

public class Search {
    public static void main(String[] args) {
        String[] docIDs = {"d1", "d2", "d3"};
        String[] contents = {
                "I like to watch the sun set with my friend.",
                "The Best Places To Watch The Sunset.",
                "My friend watch the sun come up."
        };

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input the keyword to search:");
        String keyword = scanner.nextLine().trim().toLowerCase();

        for (int i = 0; i < docIDs.length; i++) {
            if (containsWholeWord(contents[i].toLowerCase(), keyword)) {
                System.out.println(docIDs[i] + ".txt");
            }
        }
        scanner.close();
    }

    public static boolean containsWholeWord(String content, String keyword) {
        String regex = "\\b" + keyword + "\\b";
        return content.matches(".*" + regex + ".*");
    }
}

