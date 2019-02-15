package com.mycomp.jenkins.plugins.buildproject;

import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;

/**
 * This plugin can be added as a buildstep to any pipeline.
 * @author Matthias Sommer
 */
@Extension
public class ProjectBuilder extends Builder implements SimpleBuildStep, Serializable {

    private final String project;

    public ProjectBuilder() {
        this.project = "";
    }

    // Fields in config.jelly must match parameter names
    @DataBoundConstructor
    public ProjectBuilder(String project) {
        this.project = project;
    }

    public final String getProject() {
        return project;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("[NODE] Start plugin.");

        if (this.project == null || this.project.isEmpty()) {
            throw new IllegalArgumentException("You did not select a project. Please go to your Jenkins job and select one from the dropdown.");
        }

        try {
            // Get a "channel" to the build machine and run the task there
            // launcher.getChannel().call(new BootstrapLoader(binFolder, listener));

            runCommand("echo test", run, workspace, launcher, listener);
        } catch (IOException | InterruptedException e) {
            listener.getLogger().println(e);
        }
    }

    /**
     * ProcStarter will run the command at the slave node specified by the
     * launcher instance.
     */
    private static int runCommand(String commandStr, Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        ArgumentListBuilder command = new ArgumentListBuilder();
        command.addTokenized(commandStr);

        Launcher.ProcStarter proc = launcher.new ProcStarter();
        proc = proc.cmds(command);
        proc = proc.stdout(ps);
        proc = proc.stderr(ps);
        proc = proc.pwd(workspace).envs(run.getEnvironment(listener));

        int exitCode = launcher.launch(proc).join();

        String output = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        listener.getLogger().println(output);

        if (exitCode != 0) {
            throw new IOException("Process failed with exit code " + exitCode);
        }

        return exitCode;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * This allows to add this plugin as a build step to a pipeline. Is
     * reflected by config.jelly for the view.
     */
    @Extension
    @Symbol("integrationPlugin")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private transient Collection<String> projects = new ArrayList<>();

        /**
         * load the persisted global configuration.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // We are always OK with someone adding this as a build step for their job
            return true;
        }

        public FormValidation doCheckProject(@QueryParameter("project") String project)
                throws IOException, ServletException {
            if (project == null || project.isEmpty()) {
                return FormValidation.error("Select a project.");
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillProjectItems() {
            ListBoxModel model = new ListBoxModel();

            projects = this.loadProjectsFromBackend();
            projects.forEach(prj -> {
                model.add(new Option(prj));
            });

            return model;
        }

        private Collection<String> loadProjectsFromBackend() {
            List<String> projects = new ArrayList<>();
            projects.add("project1");
            projects.add("project2");
            return projects;
        }

        /**
         * What you see as the plugin name in the config view.
         *
         * @return if configuration was successful
         */
        @Override
        public String getDisplayName() {
            return "Project Builder";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
            save();
            return super.configure(req, formData);
        }
    }
}
