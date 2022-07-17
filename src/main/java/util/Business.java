package util;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

//ОСТАТОЧНЫЙ АРГУМЕНТ для directory_or_file.

public class Business {

    /**
     * Вариант 1 -- ls
     * Вывод содержимого указанной в качестве аргумента директории в виде
     * отсортированного списка имен файлов.
     * ? Флаг -l (long) переключает вывод в длинный формат, в котором, кроме имени
     * файла, указываются права на выполнение/чтение/запись в виде битовой маски
     * XXX, время последней модификации и размер в байтах.
     * ? Флаг -h (human-readable) переключает вывод в человеко-читаемый формат
     * (размер в кило-, мега- или гигабайтах, права на выполнение в виде rwx).
     * ? Флаг -r (reverse) меняет порядок вывода на противоположный.
     * ? Флаг -o (output) указывает имя файла, в который следует вывести результат;
     * если этот флаг отсутствует, результат выводится в консоль.
     * В случае, если в качестве аргумента указан файл, а не директория, следует вывести
     * информацию об этом файле.
     * <p>
     * Command Line: ls [-l] [-h] [-r] [-o output.file] directory_or_file
     * Кроме самой программы, следует написать автоматические тесты к ней.
     */

    @Option(name = "ls",
            aliases = "--ls",
            usage = "prints, depending on the options (-r, -l, -h) selected or none, the contents of the directory or file passed as an argument. " +
                    "Example: ls [-l|-h] [-r] [-o output.file] directory_or_file",
            forbids = {"ciphxor", "--ciphxor"}
            , required = true //кидает ошибку, если true
    )
    private boolean ls;
//    private String lsInputFile;

    @Option(name = "-l",
            usage = """
                    switches the output to a 'long' format, in which, in addition to the file name,
                    read/write/execute permissions are indicated in the form of a XXX bitmask,
                    the time of the last modification and the size in bytes.""",
            forbids = {"-h"})
    private boolean longFormat;

    @Option(name = "-h",
            aliases = "--human-readable",
            usage = "displays information in a human-friendly format: file size in giga-, mega-, kilo-, bytes; file permissions are denoted as 'rwx'",
            forbids = {"-l"})
    private boolean humanReadable;

    @Option(name = "-r",
            aliases = "--reverse",
            usage = "prints information in reversed order.")
    private boolean reverse;

    @Option(name = "-o",
            aliases = "--output",
            usage = "[-o output.txt] - outputs information to the specified file.")
    private String output;

    /**
     * Вариант 2 -- шифрация
     * Шифрация (-c) или дешифрация (-d) указанного (в аргументе командной строки)
     * файла. Выходной файл указывается как -o filename.txt, по умолчанию имя
     * формируется из имени входного файла с добавлением расширения.
     * Алгоритм шифрации XOR. Ключ указывается после -c или -d в шестнадцатеричной
     * системе, длина ключа -- любое целое количество байт.
     * Command Line: ciphxor [-c key] [-d key] inputname.txt [-o outputname.txt]
     * Кроме самой программы, следует написать автоматические тесты к ней.
     */

    @Option(name = "ciphxor",
            aliases = "--ciphxor",
            usage = "encrypts (-c) or decrypts (-d) the specified (in the command line argument) file. " +
                    "Command Line example: ciphxor [-c|-d key] inputname.txt [-o outputname.txt]",
            forbids = {"ls", "--ls"})
    private boolean ciphxor;
//    private String ciphxorInputFile;

    @Option(name = "-c",
            usage = "[-c key] - 'cipher' option. The key is specified in hexadecimal system, the length of the key is any integer.",
            forbids = {"-d"})
    private String cKey;

    @Option(name = "-d",
            usage = "[-d key] - 'decipher' option. The key is specified in hexadecimal system, the length of the key is any integer.",
            forbids = {"-c"})
    private String dKey;

    @Argument
    private List<String> arguments; //без этого не берет остаточный аргумент.

    String command; //from command line (ls, ciphxor).
    private String inputFile;

    private void parseArgs(final String[] args) {
        arguments = new ArrayList<>(Arrays.stream(args).toList());
        command = args[0];

        final CmdLineParser parser = new CmdLineParser(this);
        if (args.length < 1) {
            parser.printUsage(out);
            System.exit(-1);
        }
        try {
            out.println();
            parser.parseArgument(args);

            out.println("ls=" + ls + ", ciphxor=" + ciphxor);

            switch (command) {

                case "ls" -> {
                    inputFile = arguments.get(arguments.size() - 1);
                    if (inputFile == null)
                        throw new NullPointerException("Argument is missing. You need to enter a directory or a file path. " +
                                "Example: ls [-l|-h] [-r] [-o output.file] directory_or_file");
                }

                case "ciphxor" -> {
                    inputFile = arguments.get(3);
                }

                default -> {
                    out.println("Unknown command: " + command);
                }
            }


        } catch (CmdLineException clEx) {
            out.println("ERROR: Unable to parse command-line options: " + clEx);
            System.err.println(clEx.getMessage());
            parser.printUsage(System.err);
        }
    }

    /**
     * Executable function demonstrating Args4j command-line processing.
     *
     * @param arguments Command-line arguments to be processed with Args4j.
     */
    public static void main(final String[] arguments) {
//        String[] testCiphxorC = {"ciphxor", "-c", "E10F", "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\secret.txt"};
//        String[] testCiphxorD = {"ciphxor", "-d", "E10F", "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\secret.encrypted"};
//        String[] testLS = {"ls", "-h", "-r", "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\test"};
        String[] testLS = {"-ls", "-ciphxor", "-h", "C:\\Users\\User\\IdeaProjects\\ConsoleUtility\\out\\artifacts\\ConsoleUtility_jar\\test"};

//        Как писать автотесты?
//        тест для пустой директории
        try {
            final Business business = new Business();
            business.parseArgs(testLS);
            business.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        switch (command) {
            case "ls" -> ls();
            case "ciphxor" -> ciphxor();
        }
    }

    /**
     * //     * @param sourceFile with the message to cipher.
     * //     * @param key is a hexadecimal number (digits from 0 to 9 and letters from A(10) to F(16)) used to cipher the message.
     * //     * @param outputFile will store encrypted data. If null data will be stored in 'source file' + '.encrepted' file.
     * //     * @param command
     */

    public void ciphxor() { //String sourceFile, String key, String outputFile, String command
        try {
            List<String> lines = Files.readAllLines(Path.of(inputFile)); //getting message (either encrypted data or message to cipher)
            String text = String.join("\n", lines);

            String key = cKey != null ? cKey : dKey; //getting KEY
            int n = Integer.parseInt(key, 16);
            String keyword = Integer.toBinaryString(n);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                sb.append((char) (text.charAt(i) ^ keyword.charAt(i % keyword.length()))); //encrypt or decrypt data via XOR cipher and key.
            }

            Path outputPath;
            if (output != null) {
                outputPath = Path.of(output);
            } else {
                outputPath = Path.of(inputFile.substring(0, inputFile.lastIndexOf(".")) + ".encrypted");
            }
            Files.writeString(outputPath, sb);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<File> getFiles(String inputFile) {
        File file = new File(inputFile);
        List<File> files = new ArrayList<>();
        if (file.isFile()) {

            files.add(file);
        } else if (file.isDirectory()) {
            files = Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .filter(Objects::nonNull)
                    .filter(File::isFile)
                    .toList();
        }
        return files;
    }

    public void ls() throws IOException {
        List<File> files = getFiles(inputFile);
        if (files.isEmpty()) return;

        List<String> report = files.stream()
                .map(file -> {
                    StringJoiner sj = new StringJoiner("; ");
                    String filename = file.getName();
                    sj.add(filename);
                    try {
                        if (longFormat) {
                            sj.add("file permissions: " + getFilesPermissions(file));
                            sj.add("lastModifiedTime: " + Files.getLastModifiedTime(file.toPath()));
                            sj.add("size (bytes): " + Files.size(Path.of(file.getAbsolutePath())));
                        } else if (humanReadable) {
                            sj.add("file permissions: " + getFilesPermissions(file));
                            sj.add("lastModifiedTime: " + Files.getLastModifiedTime(file.toPath()));
                            sj.add("size: " + getHumanReadableSize(file));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return sj.toString();
                })
                .collect(Collectors.toList());

        if (reverse) {
            Collections.reverse(report);
        }

        if (output != null) {
            Files.writeString(Path.of(output), String.join("\n", report));
        } else {
            out.println(String.join("\n", report));
        }
    }

    /**
     * @param file
     * @return file permissions (read/write/execute) in "rwx" format if(humanReadable) or "xxx" (bitmask) if (longFormat).
     */

    private String getFilesPermissions(File file) {
        String result = "";
        if (humanReadable) {
            result = String.format("%s%s%s",
                    file.canRead() ? "r" : "-",
                    file.canWrite() ? "w" : "-",
                    file.canExecute() ? "x" : "-");
        } else if (longFormat) {
            result = String.format("%s%s%s",
                    file.canRead() ? "1" : "0",
                    file.canWrite() ? "1" : "0",
                    file.canExecute() ? "1" : "0");
        }
        return result;
    }

    /**
     * @param file
     * @return file size in a human-friendly format: "n gigabytes, n megabytes, n kilobytes, n bytes";
     */

    private String getHumanReadableSize(File file) throws IOException {

        Path path = Paths.get(file.getAbsolutePath());
        long size = Files.size(path);

        long bytes = 1;
        long kilobytes = bytes * 1024;
        long megabytes = kilobytes * 1024;
        long gigabytes = megabytes * 1024;
        long terabytes = gigabytes * 1024;

        StringJoiner sj = new StringJoiner(", ");
        long tb = size / terabytes;
        if (tb > 0) {
            sj.add(tb + " terabyte(s)");
            size -= terabytes * tb;
        }
        long gb = size / gigabytes;
        if (gb > 0) {
            sj.add(gb + " gigabyte(s)");
            size -= gigabytes * gb;
        }
        long mb = size / megabytes;
        if (mb > 0) {
            sj.add(mb + " megabyte(s)");
            size -= megabytes * mb;
        }
        long kb = size / kilobytes;
        if (kb > 0) {
            sj.add(kb + " kilobyte(s)");
            size -= kilobytes * kb;
        }
        if (size >= 0) {
            sj.add(size + " byte(s)");
        }
        return sj.toString();
    }

}
