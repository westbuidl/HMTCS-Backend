package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.models.ExampleCase;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class CaseController {

    @GetMapping(value = "/get-example-case", produces = "application/json")
    public ResponseEntity<ExampleCase> getExampleCase() {
        return ok(new ExampleCase(1, "ABC12345", "Case Title",
                                  "Case Description", "Case Status", LocalDateTime.now()));
    }

    @GetMapping(value = "/get-all-cases", produces = "application/json")
    public ResponseEntity<List<ExampleCase>> getAllCases() {
        LocalDateTime now = LocalDateTime.now();
        List<ExampleCase> cases = Arrays.asList(
            new ExampleCase(1, "ABC12345", "Case 1", "Desc 1", "Open", now),
            new ExampleCase(2, "DEF67890", "Case 2", "Desc 2", "Closed", now)
        );
        return ok(cases);
    }
}