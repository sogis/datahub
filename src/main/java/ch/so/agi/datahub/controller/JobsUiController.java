package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.model.JobSearch;
import ch.so.agi.datahub.service.JobResponseService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@ConditionalOnWebApplication
public class JobsUiController {
  private final JobResponseService jobs;

  public JobsUiController(JobResponseService jobs) {
    this.jobs = jobs;
  }

  @GetMapping({"/", "/jobs"})
  public ModelAndView page(
      @RequestParam Map<String, String> params, jakarta.servlet.http.HttpServletRequest request) {
    return view(params, "jobs", request.getContextPath());
  }

  @GetMapping("/jobs/table")
  public ModelAndView table(
      @RequestParam Map<String, String> params,
      jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response) {
    response.setHeader(
        "HX-Push-Url",
        request.getContextPath()
            + "/jobs"
            + (request.getQueryString() == null ? "" : "?" + request.getQueryString()));
    return view(params, "jobs-table", request.getContextPath());
  }

  @GetMapping({"/web/jobs.xhtml", "/web/index.xhtml"})
  public String legacy() {
    return "redirect:/jobs";
  }

  private ModelAndView view(Map<String, String> params, String template, String context) {
    int page = 0;
    try {
      page = Integer.parseInt(params.getOrDefault("page", "0"));
    } catch (NumberFormatException ignored) {
    }
    var search =
        new JobSearch(
            params.get("organisation"),
            params.get("theme"),
            params.get("operat"),
            params.get("status"),
            params.get("validationStatus"),
            page,
            params.get("sort"));
    var result = jobs.search(search, 25);
    var model = new ModelAndView(template);
    model.addObject("result", result);
    model.addObject("search", search);
    model.addObject("context", context);
    model.addObject("previous", context + link(params, Math.max(0, search.page() - 1)));
    model.addObject("next", context + link(params, search.page() + 1));
    return model;
  }

  private static String link(Map<String, String> params, int page) {
    Map<String, String> values = new TreeMap<>();
    for (String key :
        List.of("organisation", "theme", "operat", "status", "validationStatus", "sort"))
      if (params.get(key) != null) values.put(key, params.get(key));
    values.put("page", Integer.toString(page));
    return "/jobs?"
        + values.entrySet().stream()
            .map(
                e ->
                    URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(java.util.stream.Collectors.joining("&"));
  }
}
