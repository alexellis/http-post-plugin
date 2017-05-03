package jenkins.plugins.httppost;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.ListView;
import hudson.model.Result;
import hudson.model.Run;
<<<<<<< HEAD
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.Run.Summary;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
=======
import hudson.model.Run.Summary;
>>>>>>> Fork plugin to post build status, not artifacts.
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
//import org.json.JSONObject;


/**
 * Upload all {@link hudson.model.Run.Artifact artifacts} using a multipart HTTP POST call to an
 * specific URL.<br> Additional metadata will be included in the request as HTTP headers: {@code
 * Job-Name}, {@code Build-Number} and {@code Build-Timestamp} are included automatically by the
 * time writing.
 *
 * @author Christian Becker (christian.becker.1987@gmail.com)
 */
@SuppressWarnings("UnusedDeclaration") // This class will be loaded using its Descriptor.
public class HttpPostPublisher extends Notifier {

  @DataBoundConstructor
  public HttpPostPublisher() {
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    
	String buildResult = build.getResult().toString();
	  
    Descriptor descriptor = getDescriptor();
    String url = descriptor.url;
    String headers = descriptor.headers;
    if (url == null || url.length() == 0) {
      listener.getLogger().println("HTTP POST: No URL specified");
      return true;
    }

    try {
<<<<<<< HEAD
      JSONObject json = new JSONObject();
    	
      JSONArray views = getViews(build.getProject());
      JSONArray changeSets = getChangesets(build);
      
      json.accumulate("result", buildResult);
      json.accumulate("buildNumber", build.getId());
      json.accumulate("jobName", build.getProject().getName());
      json.accumulate("buildTimestamp", String.valueOf(build.getTimeInMillis()));
      json.accumulate("displayName", build.getProject().getDisplayName());
      json.accumulate("fullDisplayName", build.getProject().getFullDisplayName());
      json.accumulate("duration", build.getDurationString());
      json.accumulate("nodeName", build.getBuiltOn().getNodeName());
      
      final Jenkins jenkins = Jenkins.getInstance();

      json.accumulate("jenkinsUrl", jenkins.getUrl());
      json.accumulate("projectUrl", build.getUrl());
      
      json.accumulate("changesets", changeSets);   

      json.put("views", views);
      
      OkHttpClient client = new OkHttpClient();
      Proxy proxy = Proxy.NO_PROXY;
	  client.setProxy(proxy);
=======
      FormEncodingBuilder form = new FormEncodingBuilder();
      form.add("result", buildResult);
      form.add("buildNumber", build.getId());
      form.add("jobName", build.getProject().getName());
      form.add("buildNumber", String.valueOf(build.getNumber()));
      form.add("buildTimestamp", String.valueOf(build.getTimeInMillis()));
      
      OkHttpClient client = new OkHttpClient();
>>>>>>> Fork plugin to post build status, not artifacts.
      client.setConnectTimeout(10, TimeUnit.SECONDS);
      client.setReadTimeout(10, TimeUnit.SECONDS);

      Request.Builder builder = new Request.Builder();
      builder.url(url);
<<<<<<< HEAD
      MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
      String content = json.toString(1);
      builder.post(RequestBody.create(MEDIA_TYPE_JSON, content) );
=======

      builder.post(form.build());
>>>>>>> Fork plugin to post build status, not artifacts.

      Request request = builder.build();

      listener.getLogger().println(String.format("---> POST %s", url));
      listener.getLogger().println(request.headers());

      long start = System.nanoTime();
      Response response = client.newCall(request).execute();
      long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

      listener.getLogger()
          .println(String.format("<--- %s %s (%sms)", response.code(), response.message(), time));
      listener.getLogger().println(response.body().string());
    } catch (Exception e) {
      e.printStackTrace(listener.getLogger());
    }

    return true;
  }

  private JSONArray getChangesets(AbstractBuild build) {
	  JSONArray list = new JSONArray();
      for (Object item : build.getChangeSet().getItems())
      {
    	  ChangeLogSet.Entry entry = (ChangeLogSet.Entry)item;
    	  list.add(entry.getCommitId());
      }
      return list;
}

private JSONArray getViews(AbstractProject project) {
      final Jenkins jenkins = Jenkins.getInstance();
      JSONArray  views = new JSONArray();
      
      for (View view: jenkins.getViews()) {
          if (view instanceof ListView) {
          	
  			for (TopLevelItem item : view.getItems()) {
  				if (((AbstractItem) item).equals(project)) {
  					views.add(view.getViewName());
  				}
  			}
          }
      }
      return views;
}

@Override
  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  @Override
  public Descriptor getDescriptor() {
    return (Descriptor) super.getDescriptor();
  }

  @Extension
  public static final class Descriptor extends BuildStepDescriptor<Publisher> {

    public String url;
    public String headers;

    public Descriptor() {
      load();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "HTTP POST build status to an URL";
    }

    public FormValidation doCheckUrl(@QueryParameter String value) {
      if (value.length() == 0) {
        return FormValidation.error("URL must not be empty");
      }

      if (!value.startsWith("http://") && !value.startsWith("https://")) {
        return FormValidation.error("URL must start with http:// or https://");
      }

      try {
        new URL(value).toURI();
      } catch (Exception e) {
        return FormValidation.error(e.getMessage());
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckHeaders(@QueryParameter String value) {
      if (value.length() > 0) {
        Headers.Builder headers = new Headers.Builder();
        String[] lines = value.split("\r?\n");

        for (String line : lines) {
          int index = line.indexOf(':');
          if (index == -1) {
            return FormValidation.error("Unexpected header: " + line);
          }

          try {
            headers.add(line.substring(0, index).trim(), line.substring(index + 1).trim());
          } catch (Exception e) {
            return FormValidation.error(e.getMessage());
          }
        }
      }

      return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
      req.bindJSON(this, json.getJSONObject("http-post-status"));
      save();

      return true;
    }
  }
}
