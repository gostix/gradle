/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.base.plugins;

import org.gradle.api.*;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.Delete;
import org.gradle.language.base.internal.plugins.CleanRule;
import org.gradle.language.base.internal.tasks.AssembleBinariesTask;
import org.gradle.util.DeprecationLogger;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * <p>A {@link org.gradle.api.Plugin} which defines a basic project lifecycle.</p>
 */
@Incubating
public class LifecycleBasePlugin implements Plugin<ProjectInternal> {
    public static final String CLEAN_TASK_NAME = "clean";
    public static final String ASSEMBLE_TASK_NAME = "assemble";
    public static final String CHECK_TASK_NAME = "check";
    public static final String BUILD_TASK_NAME = "build";
    public static final String BUILD_GROUP = "build";
    public static final String VERIFICATION_GROUP = "verification";
    public static final String CUSTOM_LIFECYCLE_TASK_DEPRECATION_MSG = "Defining custom ‘%s’ task is deprecated when using standard lifecycle plugin";

    public void apply(ProjectInternal project) {
        addClean(project);
        addCleanRule(project);
        addAssemble(project);
        addCheck(project);
        addBuild(project);
        addDeprecationWarningsAboutCustomLifecycleTasks(project);
    }

    private void addClean(final Project project) {
        Delete clean = project.getTasks().create(CLEAN_TASK_NAME, Delete.class);
        clean.setDescription("Deletes the build directory.");
        clean.setGroup(BUILD_GROUP);
        clean.delete(new Callable<File>() {
            public File call() throws Exception {
                return project.getBuildDir();
            }
        });
    }

    private void addCleanRule(Project project) {
        project.getTasks().addRule(new CleanRule(project.getTasks()));
    }

    private void addAssemble(Project project) {
        Task assembleTask = project.getTasks().create(ASSEMBLE_TASK_NAME, AssembleBinariesTask.class);
        assembleTask.setDescription("Assembles the outputs of this project.");
        assembleTask.setGroup(BUILD_GROUP);
    }

    private void addCheck(final ProjectInternal project) {
        project.getTasks().addPlaceholderAction(CHECK_TASK_NAME, DefaultTask.class, new Action<TaskInternal>() {
            @Override
            public void execute(TaskInternal checkTask) {
                checkTask.setDescription("Runs all checks.");
                checkTask.setGroup(VERIFICATION_GROUP);
            }
        });
    }

    private void addBuild(final ProjectInternal project) {
        project.getTasks().addPlaceholderAction(BUILD_TASK_NAME, DefaultTask.class, new Action<DefaultTask>() {
            @Override
            public void execute(DefaultTask buildTask) {
                buildTask.setDescription("Assembles and tests this project.");
                buildTask.setGroup(BUILD_GROUP);
                buildTask.dependsOn(ASSEMBLE_TASK_NAME);
                buildTask.dependsOn(CHECK_TASK_NAME);
            }
        });
    }

    private void addDeprecationWarningsAboutCustomLifecycleTasks(ProjectInternal project) {
        project.getTasks().all(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (task.getName().equals(BUILD_TASK_NAME)) {
                    DeprecationLogger.nagUserOfDeprecated(String.format(CUSTOM_LIFECYCLE_TASK_DEPRECATION_MSG, BUILD_TASK_NAME));
                }
                if (task.getName().equals(CHECK_TASK_NAME)) {
                    DeprecationLogger.nagUserOfDeprecated(String.format(CUSTOM_LIFECYCLE_TASK_DEPRECATION_MSG, CHECK_TASK_NAME));
                }
            }
        });
    }
}
