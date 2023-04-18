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

package org.apache.teaclave.javasdk.tool.optimize;

import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import org.apache.teaclave.javasdk.common.annotations.EnclaveService;
import org.graalvm.collections.EconomicMap;
import org.graalvm.compiler.core.common.CompilationIdentifier;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.graph.NodeSourcePosition;
import org.graalvm.compiler.hotspot.HotSpotGraalCompiler;
import org.graalvm.compiler.java.GraphBuilderPhase;
import org.graalvm.compiler.nodes.InvokeWithExceptionNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.cfg.Block;
import org.graalvm.compiler.nodes.cfg.ControlFlowGraph;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugins;
import org.graalvm.compiler.options.OptionKey;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.OptimisticOptimizations;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.graalvm.compiler.debug.DebugOptions.Dump;

public class ECallInLoopDetector {

    private static HotSpotGraalCompiler compiler;
    private static MetaAccessProvider accessProvider;
    private static final boolean DEBUG;
    private static OptionValues options;

    static {
        DEBUG = Boolean.parseBoolean(System.getProperty("optimization.debug", "false"));
        HotSpotJVMCIRuntime jvmciRuntime = HotSpotJVMCIRuntime.runtime();
        compiler = (HotSpotGraalCompiler) jvmciRuntime.getCompiler();
        accessProvider = jvmciRuntime.getHostJVMCIBackend().getMetaAccess();
        options = compiler.getGraalRuntime().getOptions();
        if (DEBUG) {
            Dump.update((EconomicMap<OptionKey<?>, Object>) options.getMap(), ":3");
        }
    }

    /**
     * Check if there is Ecall inside loop in the given method. See the test case {@code ECallInLoopDetectorTest} for
     * example illustration.
     *
     * @param m method to check
     * @return a list of inside-loop-Ecall positions in the method, empty if none.
     */
    public static List<NodeSourcePosition> check(Method m) {
        StructuredGraph graph = getGraph(m);
        List<NodeSourcePosition> ret = new ArrayList<>();
        ControlFlowGraph cfg = ControlFlowGraph.computeForSchedule(graph);
        graph.getNodes(InvokeWithExceptionNode.TYPE).forEach(n -> {
            Block block = cfg.blockFor(n);
            if (block != null && block.getLoop() != null) {
                ResolvedJavaMethod method = n.getTargetMethod();
                if (isEnclaveService(method.getDeclaringClass())) {
                    ret.add(n.getNodeSourcePosition());
                }
            }
        });
        if (!ret.isEmpty()) {
            int len = ret.size();
            System.out.println("Detected " + len + " enclave service calls inside loop.");
            for (int i = 0; i < len; i++) {
                System.out.println((i + 1) + ". " + ret.get(i));
            }
        }
        return ret;
    }

    private static StructuredGraph getGraph(Method method) {
        ResolvedJavaMethod javaMethod = accessProvider.lookupJavaMethod(method);
        DebugContext debug = new DebugContext.Builder(options, compiler.getDebugHandlersFactories()).build();

        StructuredGraph graph = compiler.createGraph(javaMethod, -1, null,
                CompilationIdentifier.INVALID_COMPILATION_ID, options, debug);
        try (DebugContext.Scope s = debug.scope("detectECallInLoop", graph)) {
            InvocationPlugins plugins = new InvocationPlugins();
            GraphBuilderConfiguration graphBuilderConfig = GraphBuilderConfiguration.getDefault(new GraphBuilderConfiguration.Plugins(plugins))
                    .withNodeSourcePosition(true)
                    .withEagerResolving(true)
                    .withRetainLocalVariables(true);

            GraphBuilderPhase.Instance graphBuilder = new GraphBuilderPhase.Instance(compiler.getGraalRuntime().getHostProviders(), graphBuilderConfig, OptimisticOptimizations.NONE, null);
            graphBuilder.apply(graph);
            if (DEBUG) {
                debug.dump(3, graph, "");
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return graph;
    }

    /**
     * Scan the entire class hierarchy tree to find if it implements any interface
     * annotated with @EnclaveService
     */
    private static boolean isEnclaveService(ResolvedJavaType type) {
        Deque<ResolvedJavaType> workList = new ArrayDeque<>();
        workList.push(type);
        while (!workList.isEmpty()) {
            ResolvedJavaType top = workList.pop();
            if (top.getAnnotation(EnclaveService.class) != null) {
                return true;
            } else {
                for (ResolvedJavaType anInterface : top.getInterfaces()) {
                    workList.push(anInterface);
                }
            }
        }
        return false;
    }
}
