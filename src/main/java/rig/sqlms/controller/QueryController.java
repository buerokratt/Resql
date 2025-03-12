package rig.sqlms.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;
import rig.sqlms.service.QueryService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@Slf4j
public class QueryController {
    private final QueryService queryService;

    private static final Pattern urlPattern = Pattern.compile("(/.+?)(/.+)");

    /**
     * @return JSON array containing the results
     */
    @Operation(description = "Initiates previously configured POST queries")
    @PostMapping(value = "/{project}/**")
    public List<Map<String, Object>> execute(@PathVariable String project,
                                             @RequestBody(required = false) Map<String, Object> parameters,
                                             HttpServletRequest request) {
        Matcher urlMatcher = urlPattern.matcher(request.getRequestURI());
        if (! urlMatcher.find())
            throw new NotFoundException(request.getRequestURI() + " not found");
        String name = urlMatcher.group(2);
        log.debug("INPUT POST: {}", name);
        return queryService.execute(project, "POST", name, parameters);
    }

    /**
     * @return JSON array containing the results
     */
    @Operation(description = "Initiates previously configured GET queries")
    @GetMapping(value = "/{project}/**")
    public List<Map<String, Object>> execute(@PathVariable String project,
                                             @RequestParam(required = false) MultiValueMap<String, Object> parameters,
                                             HttpServletRequest request) {
        Matcher urlMatcher = urlPattern.matcher(request.getRequestURI());
        if (! urlMatcher.find())
            throw new NotFoundException(request.getRequestURI() + " not found");
        String name = urlMatcher.group(2);
        log.debug("INPUT GET: {}", name);
        return queryService.execute(project, "GET", name, parameters.toSingleValueMap());
    }

    /**
     * @return Json array of result arrays
     */
    @Operation(description = "Initiates previously configured queries in batch mode")
    @PostMapping("/{name}/batch")
    public List<List<Map<String, Object>>> executeBatch(@PathVariable String name, @RequestBody BatchRequest batchRequest) {
        return batchRequest.queries.stream()
                .map(parameters -> queryService.executePost(name, parameters))
                .toList();
    }

    record BatchRequest(List<Map<String, Object>> queries) {
        public BatchRequest {
            Objects.requireNonNull(queries);
        }
    }
}
