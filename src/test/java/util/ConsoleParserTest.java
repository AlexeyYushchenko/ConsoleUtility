package util;

import org.junit.jupiter.api.Test;

class ConsoleParserTest {

    final String LS = "ls";
    final String CIPHXOR = "ciphxor";
    final String GREP = "grep";
    final String FIND = "find";
    final String PACK_RLE = "pack-rle";
    String FILEPATH = "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\secret.txt";
    String FOLDER_PATH = "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\test";
    String ENCRYPTED_TEXT = "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\secret.encrypted";
    String OUTPUT = "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\ciphxorTestOutput.txt";
    String GREP_FILE = "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\grepTestFile.txt";

    @Test
    void testGrep() {
        String word = "finish";
        System.out.println(word);
        ConsoleParser.main(new String[]{GREP, word, GREP_FILE});
        System.out.println();

        System.out.println("-i, " + word);
        ConsoleParser.main(new String[]{GREP, "-i", word, GREP_FILE});
        System.out.println();

        System.out.println("-r, " + word);
        ConsoleParser.main(new String[]{GREP, "-r", word, GREP_FILE});
        System.out.println();

        System.out.println("-v, " + word);
        ConsoleParser.main(new String[]{GREP, "-v", word, GREP_FILE});
        System.out.println();

        System.out.println("-i -r -v, " + word);
        ConsoleParser.main(new String[]{GREP, "-i", "-r", "-v", word, GREP_FILE});
        System.out.println();
    }

    @Test
    void testLS() {
        System.out.println("LS TESTS:");
        System.out.println("TEST FILE");
        System.out.print("no options");
        ConsoleParser.main(new String[]{LS, FILEPATH});
        System.out.println();
        System.out.print("-l");
        ConsoleParser.main(new String[]{LS, "-l", FILEPATH});
        System.out.println();
        System.out.print("-h");
        ConsoleParser.main(new String[]{LS, "-h", FILEPATH});
        System.out.println();

        System.out.println("TEST FOLDER");
        System.out.print("w/o options");
        ConsoleParser.main(new String[]{LS, FOLDER_PATH});
        System.out.println();
        System.out.print("-l");
        ConsoleParser.main(new String[]{LS, "-l", FOLDER_PATH});
        System.out.println();
        System.out.print("-r");
        ConsoleParser.main(new String[]{LS, "-r", FOLDER_PATH});
        System.out.println();
        System.out.print("-h -r");
        ConsoleParser.main(new String[]{LS, "-h", "-r", FOLDER_PATH});
        System.out.println();
    }

    @Test
    void testCiphxor() {
        System.out.println("CIPHXOR TESTS:");
        System.out.print("-c E10F (no output)");
        ConsoleParser.main(new String[]{CIPHXOR, "-c", "E10F", FILEPATH});
        System.out.print("-d E10F");
        ConsoleParser.main(new String[]{CIPHXOR, "-d", "E10F", ENCRYPTED_TEXT});
        ConsoleParser.main(new String[]{CIPHXOR, "-d", "E10F", ENCRYPTED_TEXT, "-o", OUTPUT});
    }

    @Test
    void testFind1() {
        System.out.println("Find test:");
        ConsoleParser.main(new String[]{FIND, "-r", "-d", "C:\\Users\\User\\IdeaProjects\\ConsoleUtility", "secret"});
    }

    @Test
    void testFind2() {
        System.out.println("Find test:");
        ConsoleParser.main(new String[]{FIND, "-r", "-d", "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar", "secret"});
    }

    @Test
    void testFindWithErrorInDirectory() {
        System.out.println("Find test:");
        ConsoleParser.main(new String[]{FIND, "-r", "-d", "ciphxorTestOutput.txt", "secret"});
    }

    @Test
    void testPackRLE(){
        System.out.println("Pack-RLE -z");
        ConsoleParser.main(new String[]{PACK_RLE, "-z", "-o", "output4PackRle.txt", "input.txt"});
    }

    @Test
    void testUnpackRLE(){
        System.out.println("Pack-RLE -u");
        ConsoleParser.main(new String[]{PACK_RLE, "-u", "-o", "output4PackRle.txt", "input.txt"});
    }
}














