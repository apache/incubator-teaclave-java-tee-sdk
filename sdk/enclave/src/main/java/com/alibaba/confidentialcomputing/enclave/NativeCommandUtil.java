package com.alibaba.confidentialcomputing.enclave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class NativeCommandUtil {
    public static final Path GRAALVM_HOME = Paths.get(System.getenv("GRAALVM_HOME"));

    public static int executeNewProcess(List<String> command, Path workDir) {
        if (command == null || command.isEmpty()) {
            throw new RuntimeException("Didn't provide any execution command.");
        }
        ProcessBuilder pb = new ProcessBuilder(command).directory(workDir.toFile());
        pb.redirectErrorStream(true);
        String oneLineCommand = command.stream().collect(Collectors.joining(" "));
        System.out.println(oneLineCommand);
        Process p = null;
        try {
            p = pb.start();
            InputStreamReader inst = new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8);
            try (BufferedReader br = new BufferedReader(inst)) {
                String res;
                while ((res = br.readLine()) != null) {
                    System.out.println(res);
                }
            }
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to execute command:\n " + oneLineCommand +
                        "\n Working directory is :" + workDir.toString() + "\n The exit code is " + exitCode+
                        "\n");
            }
            return 0;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute command:\n " + oneLineCommand, e);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }
}
