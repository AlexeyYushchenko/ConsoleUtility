package util;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ConsoleParser {

    @Argument(handler = SubCommandHandler.class)
    @SubCommands({
            @SubCommand(name = "ls", impl = Ls.class),
            @SubCommand(name = "ciphxor", impl = Ciphxor.class),
            @SubCommand(name = "grep", impl = Grep.class),
            @SubCommand(name = "find", impl = Find.class),
            @SubCommand(name = "pack-rle", impl = PackRle.class)
    })
    SubCmd subCmd;

    public static void main(final String[] arguments) {
        try {
            final ConsoleParser consoleParser = new ConsoleParser();
            consoleParser.parseArgs(arguments);
            consoleParser.subCmd.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract static class SubCmd {
        String name;

        public String getName() {
            return name;
        }

        public void execute() {
        }
    }

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

    public static class Ls extends SubCmd {

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

        private String in;

        @Option(name = "-o",
                aliases = "--output",
                usage = "[-o output.txt] - outputs information to the specified file.")
        private String out;

        @Argument
        private List<String> arguments;

        @Override
        public void execute() {
            try {
                in = arguments.get(arguments.size() - 1);
                List<File> files = getFiles(in);
                if (files.isEmpty()) return;

                List<String> report = files.stream()
                        .map(file -> {
                            StringJoiner sj = new StringJoiner("; ");
                            sj.add(file.getName());
                            try {
                                if (longFormat) {
                                    addLongFormatFileInformation(sj, file);
                                } else if (humanReadable) {
                                    addHumanReadableFileInformation(sj, file);
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

                if (out != null) {
                    Files.writeString(Path.of(out), String.join("\n", report));
                } else {
                    System.out.println(String.join("\n", report));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void addLongFormatFileInformation(StringJoiner sj, File file) throws IOException {
            sj.add("file permissions: " + getFilesPermissions(file));
            sj.add("lastModifiedTime: " + Files.getLastModifiedTime(file.toPath()));
            sj.add("size (bytes): " + Files.size(Path.of(file.getAbsolutePath())));
        }

        private void addHumanReadableFileInformation(StringJoiner sj, File file) throws IOException {
            sj.add("file permissions: " + getFilesPermissions(file));
            sj.add("lastModifiedTime: " + Files.getLastModifiedTime(file.toPath()));
            sj.add("size: " + getHumanReadableSize(file));
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

        /**
         * @param file
         * @return file permissions (read/write/execute) in "rwx" format if(humanReadable) or "xxx" (bitmask) if (longFormat).
         */

        private String getFilesPermissions(File file) {
            String result = "";
            if (humanReadable) {
                result = String.format("%s%s%s",
                        file.canRead() ?    "r" : "-",
                        file.canWrite() ?   "w" : "-",
                        file.canExecute() ? "x" : "-"
                );
            } else if (longFormat) {
                result = String.format("%s%s%s",
                        file.canRead() ?    "1" : "0",
                        file.canWrite() ?   "1" : "0",
                        file.canExecute() ? "1" : "0"
                );
            }
            return result;
        }

        /**
         * @param file we want to get size information about
         * @return file size info in a human-friendly format: "n gigabytes, n megabytes, n kilobytes, n bytes";
         * The largest unit of measure is a terabyte.
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

    /**
     * Вариант 2 -- шифрование
     * Шифрация (-c) или дешифрация (-d) указанного (в аргументе командной строки)
     * файла. Выходной файл указывается как -o filename.txt, по умолчанию имя
     * формируется из имени входного файла с добавлением расширения.
     * Алгоритм шифрации XOR. Ключ указывается после -c или -d в шестнадцатеричной
     * системе, длина ключа -- любое целое количество байт.
     * Command Line: ciphxor [-c key] [-d key] inputname.txt [-o outputname.txt]
     * Кроме самой программы, следует написать автоматические тесты к ней.
     */

    public static class Ciphxor extends SubCmd {
        @Option(name = "-c",
                usage = "[-c key] - 'cipher' option. The key is specified in hexadecimal system, the length of the key is any integer.",
                forbids = {"-d"})
        private String cKey;

        @Option(name = "-d",
                usage = "[-d key] - 'decipher' option. The key is specified in hexadecimal system, the length of the key is any integer.",
                forbids = {"-c"})
        private String dKey;

        private String in;

        @Option(name = "-o",
                aliases = "--output",
                usage = "[-o output.txt] - outputs information to the specified file.")
        private String out;

        @Argument
        private List<String> arguments;

        /**
         * Depending on the chosen option, i.e. '-c' ('cipher') or '-d' ('decipher),
         * function gets data from input file and puts info either into the output file, if option '-o' is used, or console.
         */

        @Override
        public void execute() {
            try {
                in = arguments.get(0);
                List<String> lines = Files.readAllLines(Path.of(in));
                String text = String.join("\n", lines);

                String key = cKey != null ? cKey : dKey;
                int n = Integer.parseInt(key, 16);
                String keyword = Integer.toBinaryString(n);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    sb.append((char) (text.charAt(i) ^ keyword.charAt(i % keyword.length()))); //encrypt or decrypt data via XOR cipher and key.
                }

                if (cKey != null) {
                    Path outputPath;
                    if (out != null) {
                        outputPath = Path.of(out);
                    } else {
                        outputPath = Path.of(in.substring(0, in.lastIndexOf(".")) + ".encrypted");
                    }
                    Files.writeString(outputPath, sb);
                } else if (dKey != null) {
                    if (out != null) {
                        Files.writeString(Path.of(out), sb);
                    } else {
                        System.out.println(sb);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Вариант 3 -- grep
     * Вывод на консоль указанного (в аргументе командной строки) текстового файла с
     * фильтрацией:
     * ● word задаёт слово для поиска (на консоль выводятся только содержащие его
     * строки)
     * ● -r (regex) вместо слова задаёт регулярное выражение для поиска (на консоль
     * выводятся только строки, содержащие данное выражение)
     * ● -v инвертирует условие фильтрации (выводится только то, что ему НЕ
     * соответствует)
     * ● -i игнорировать регистр слов
     * Command Line: grep [-v] [-i] [-r] word inputname.txt
     */

    public static class Grep extends SubCmd {

        @Option(name = "-v", usage = "invert the condition (outputs all EXCEPT for lines that contain the word).")
        private boolean isInverted;

        @Option(name = "-i", aliases = "--ignore", usage = "ignore the upper/lower case.")
        private boolean isCaseIgnored;

        @Option(name = "-r", aliases = "--regex", usage = "use regular expression instead of a keyword")
        private boolean isRegex;

        private String word;

        private String in;

        @Argument
        private List<String> arguments;

        @Override
        public void execute() {
            try {
                in = arguments.get(arguments.size() - 1);
                word = arguments.get(arguments.size() - 2);
                List<String> lines = Files.readAllLines(Path.of(in));
                List<String> result = lines.stream().filter(line -> {
                            if (isRegex && isCaseIgnored && isInverted) {
                                return !line.toLowerCase().matches(word.toLowerCase());
                            } else if (isRegex && isCaseIgnored) {
                                return line.toLowerCase().matches(word.toLowerCase());
                            } else if (isCaseIgnored && isInverted) {
                                return !line.toLowerCase().contains(word.toLowerCase());
                            } else if (isRegex && isInverted) {
                                return !line.matches(word);
                            } else if (isRegex) {
                                return line.matches(word);
                            } else if (isInverted) {
                                return !line.contains(word);
                            } else if ((isCaseIgnored)) {
                                return line.toLowerCase().contains(word.toLowerCase());
                            } else {
                                return line.contains(word);
                            }
                        }
                ).toList();

                result.forEach(System.out::println);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Вариант 4 -- find
     * Поиск файла(ов) с заданным в командной строке именем в указанной ключом -d
     * директории, по умолчанию в текущей директории. Ключ -r указывает на необходимость
     * поиска также во всех поддиректориях.
     * Command Line: find [-r] [-d directory] filename.txt
     * Кроме самой программы, следует написать автоматические тесты к ней.
     */

    public static class Find extends SubCmd {

        @Option(name = "-r", aliases = "--subdirectory", usage = "search in subdirectory(-ies).")
        private boolean hasSubdirectorySearch;

        @Option(name = "-d",
                usage = "[-d directory] - directory where we look for the file(-s).",
                forbids = {"-c"})
        private File directory = new File(System.getProperty("user.dir"));

        private String filename;

        @Argument
        private List<String> arguments;

        @Override
        public void execute() {
            try {
                filename = arguments.get(arguments.size() - 1);
                if (!directory.isDirectory())
                    throw new IllegalArgumentException("Wrong directory: '" + directory + "'");

                List<File> result = new ArrayList<>();
                File[] files = directory.listFiles();
                if (files != null) Arrays.stream(files).forEach(file -> checkFile(file, result));

                if (!result.isEmpty()) {
                    System.out.println("Here's what I found:");
                    result.forEach(System.out::println);
                } else {
                    System.out.println("No files found");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void checkFile(File file, List<File> result) {
            if (file.isFile() && file.getName().toLowerCase().contains(filename.toLowerCase())) {
                result.add(file);
            } else if (hasSubdirectorySearch && file.isDirectory()) {
                Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> checkFile(f, result));
            }
        }
    }

    /**
     * Вариант 5 -- pack-rle
     * Упаковывает -z или распаковывает -u указанный в аргументе командной строки
     * текстовый файл. Выходной файл указывается как -out outputname.txt, по умолчанию
     * имя формируется из входного файла с добавлением расширения.
     * Реализовать сжатие RLE (run-length encoding). Продумать алгоритм сжатия и формат
     * файла, при котором сжатие «неудачных» данных не приводит к большому увеличению
     * размера файла.
     * Command Line: pack-rle [-z|-u] [-out outputname.txt] inputname.txt
     * Кроме самой программы, следует написать автоматические тесты к ней.
     */

    public static class PackRle extends SubCmd {

        @Option(name = "-z",
                usage = "pack text file using run-length encoding.",
                forbids = {"-u"})
        private boolean pack;

        @Option(name = "-u",
                usage = "unpack data and output to a file.",
                forbids = {"-z"})
        private boolean unpack;

        private String in;

        @Option(name = "-o",
                aliases = "-out",
                usage = "[-o output.txt] - outputs information to the specified file.",
                required = true)
        private String out;

        @Argument
        private List<String> arguments;

        @Override
        public void execute() {
            try {
                in = arguments.get(arguments.size() - 1);
                StringBuilder sb = new StringBuilder();
                Files.readAllLines(Path.of(in)).forEach(sb::append);
                String result;
                if (pack) result = packRle(sb);
                else if (unpack) result = unpackRle(sb);
                else throw new IllegalStateException("No option (-z | -u) was selected.");

                Files.writeString(Path.of(out), result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String packRle(StringBuilder original) {
            StringBuilder res = new StringBuilder();
            char[] chars = original.toString().toCharArray();
            int counter = 1;
            char current = chars[0];
            for (int i = 1; i < chars.length; i++) {
                if (chars[i] == current) {
                    counter++;
                } else {
                    if (counter > 1) res.append(counter);
                    res.append(current);
                    counter = 1;
                    current = chars[i];
                }
                if (i == chars.length - 1) {
                    if (counter > 1) res.append(counter);
                    res.append(chars[i]);
                }
            }
            System.out.println(original);
            System.out.println(res);
            return res.toString();
        }

        private String unpackRle(StringBuilder original) {
            StringBuilder res = new StringBuilder();
            char[] chars = original.toString().toCharArray();
            StringBuilder digitBuilder = new StringBuilder();
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    digitBuilder.append(c);
                } else if (Character.isLetter(c)) {
                    if (digitBuilder.toString().length() > 0) {
                        res.append(String.valueOf(c).repeat(Integer.parseInt(digitBuilder.toString())));
                        digitBuilder = new StringBuilder();
                    } else {
                        res.append(c);
                    }
                }else {
                    throw new InputMismatchException("Wrong character: '" + c + "'.");
                }
            }
            System.out.println(original);
            System.out.println(res);
            return res.toString();
        }
    }

    private void parseArgs(final String[] args) {
        final CmdLineParser parser = new CmdLineParser(this);
        if (args.length < 1) {
            parser.printUsage(System.out);
            System.exit(-1);
        }
        try {
            System.out.println();
            parser.parseArgument(args);

        } catch (CmdLineException clEx) {
            System.out.println("ERROR: Unable to parse command-line options: " + clEx);
            System.err.println(clEx.getMessage());
            parser.printUsage(System.err);
        }
    }

}