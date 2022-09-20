// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.enclave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class NativeCommandUtil {
    public static final Path GRAALVM_HOME = Paths.get(System.getenv("GRAALVM_HOME"));

    public static int executeNewProcess(List<String> command, Path workDir) {
        if (command == null || command.isEmpty()) {
            throw new RuntimeException("Didn't provide any execution command.");
        }
        ProcessBuilder pb = new ProcessBuilder(command).directory(workDir.toFile());
        pb.redirectErrorStream(true);
        String oneLineCommand = String.join(" ", command);
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
                        "\n Working directory is :" + workDir + "\n The exit code is " + exitCode+
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
