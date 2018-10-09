package com.fleemer.web.controller;

import com.fleemer.model.Person;
import com.fleemer.model.mongodb.AccessStats;
import com.fleemer.service.AccessLogService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua_parser.Client;
import ua_parser.Parser;

@Controller
@RequestMapping("/options")
public class OptionsController {
    private static final String ACCESS_STATS_VIEW = "access_stats";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String SERIALIZE_VIEW = "serialize";

    private final AccessLogService accessLogService;

    @Autowired
    public OptionsController(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @GetMapping("/locale")
    public String localize(@RequestParam("url") String backUrl) {
        return "redirect:" + backUrl;
    }

    @GetMapping("/serialize")
    public String serialize() {
        return SERIALIZE_VIEW;
    }

    @GetMapping("/requests/stats")
    public String requestsStats(HttpSession session, Model model) throws IOException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        LocalDate till = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        LocalDate from = till.minusMonths(2).withDayOfMonth(1);
        List<AccessStats> statsList = accessLogService.findAll(person, from, till);
        modifyAccessStatsList(statsList);
        model.addAttribute("stats", statsList);
        return ACCESS_STATS_VIEW;
    }

    private static void modifyAccessStatsList(List<AccessStats> statsList) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM YYYY", LocaleContextHolder.getLocale());
        Map<String, String> cache = new HashMap<>();
        for (AccessStats stats : statsList) {
            LocalDate date = LocalDate.parse(stats.getDate());
            stats.setDate(formatter.format(date));
            String agentInfo = getAgentInfo(cache, stats.getAgent());
            stats.setAgent(agentInfo);
        }
    }

    private static String getAgentInfo(Map<String, String> cache, String agentInfo) throws IOException {
        String cachedAgentInfo = cache.get(agentInfo);
        if (cachedAgentInfo != null) {
            return cachedAgentInfo;
        } else {
            String modifiedAgentInfo = getModifiedAgentInfo(agentInfo);
            cache.put(agentInfo, modifiedAgentInfo);
            return modifiedAgentInfo;
        }
    }

    private static String getModifiedAgentInfo(String agentInfo) throws IOException {
        Parser parser = new Parser();
        Client client = parser.parse(agentInfo);
        return client.userAgent.family + ' ' + client.userAgent.major + '.' + client.userAgent.minor + " (" +
                client.os.family + ' ' + client.os.major + '.' + client.os.minor + ')';
    }
}
