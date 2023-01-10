package net.openid.conformance.info;

import com.google.common.base.Strings;
import com.google.gson.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.openid.conformance.CollapsingGsonHttpMessageConverter;
import net.openid.conformance.pagination.PaginationRequest;
import net.openid.conformance.pagination.PaginationResponse;
import net.openid.conformance.testmodule.DataUtils;
import net.openid.conformance.testmodule.OIDFJSON;
import net.openid.conformance.variant.VariantSelection;
import net.openid.conformance.variant.VariantService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Tag(name = "TestPlanAPI", description = "A set of apis to query and manipulate the test plans of the user")
@RequestMapping(value = "/api")
public class TestPlanApi implements DataUtils {

    @Autowired
    private TestPlanService planService;

    @Autowired
    private TestInfoService infoService;

    @Autowired
    private SavedConfigurationService savedConfigurationService;

    @Autowired
    private VariantService variantService;

    @PostMapping(value = "/plan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create test plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created test plan successfully"),
            @ApiResponse(responseCode = "404", description = "Couldn't find test plan for provided plan name")
    })
    public ResponseEntity<Map<String, Object>> createTestPlan(
            @Parameter(description = "Plan name") @RequestParam("planName") String planName,
            @Parameter(description = "Kind of test variation") @RequestParam(value = "variant", required = false) VariantSelection variant,
            @Parameter(description = "Configuration json") @RequestBody JsonObject config,
            Model m) {

        String id = RandomStringUtils.randomAlphanumeric(13);

        VariantService.TestPlanHolder holder = variantService.getTestPlan(planName);

        if (holder == null) {
            return new ResponseEntity<>(Map.of("error", "No plan with name: " + planName + ""), HttpStatus.NOT_FOUND);
        }

        String description = null;
        if (config.has("description") && config.get("description").isJsonPrimitive()) {
            description = OIDFJSON.getString(config.get("description"));
        }

        if (config.has("alias") && config.get("alias").isJsonPrimitive()) {
            String alias = Strings.emptyToNull(OIDFJSON.getString(config.get("alias")));
            if (!alias.matches("^([a-zA-Z0-9_-]+)$")) {
                throw new RuntimeException("Invalid alias value '" + alias + "'. " +
                        "alias can only contain alphanumeric characters, _ and -.");
            }
        }

        // extract the `publish` field if available
        String publish = null;
        if (config.has("publish") && config.get("publish").isJsonPrimitive()) {
            publish = Strings.emptyToNull(OIDFJSON.getString(config.get("publish")));
        }

        // save the configuration for the test plan
        savedConfigurationService.savePlanConfigurationForCurrentUser(config, planName, variant);

        List<Plan.Module> testModules;
        if (variant != null) {
            testModules = holder.getTestModulesForVariant(variant);
        } else {
            testModules = holder.getTestModulesForVariant(VariantSelection.EMPTY);
        }

        if (testModules.isEmpty()) {
            throw new RuntimeException("No test modules in plan '" + planName + "' are applicable for specified variant");
        }

        String certProfile = holder.certificationProfileForVariant(variant);

        planService.createTestPlan(id, planName, variant, config, description, certProfile, testModules, holder.info.summary(), publish);

        Map<String, Object> map = new HashMap<>();
        map.put("name", planName);
        map.put("id", id);
        map.put("modules", testModules);

        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @GetMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of test plan instances with paging")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieved successfully")
    })
    public ResponseEntity<Object> getTestPlansForCurrentUser(
            @Parameter(description = "Published data only") @RequestParam(name = "public", defaultValue = "false") boolean publicOnly,
            PaginationRequest page) {

        PaginationResponse<?> response = publicOnly
                ? planService.getPaginatedPublicPlans(page)
                : planService.getPaginatedPlansForCurrentUser(page);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/plan/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get test plan information by plan id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Couldn't find test plan for provided plan Id")
    })
    public ResponseEntity<Object> getTestPlan(
            @Parameter(description = "Id of test plan") @PathVariable("id") String id,
            @Parameter(description = "Published data only") @RequestParam(name = "public", defaultValue = "false") boolean publicOnly) {

        Object testPlan = publicOnly ? planService.getPublicPlan(id) : planService.getTestPlan(id);

        if (testPlan == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Gson gson = CollapsingGsonHttpMessageConverter.getDbObjectCollapsingGson();
        JsonObject testPlanObj = JsonParser.parseString(gson.toJson(testPlan)).getAsJsonObject();

        JsonElement modules = testPlanObj.get("modules");

        if (modules != null && modules.isJsonArray()) {
            ((JsonArray) modules).forEach(m -> {
                String testModuleName = OIDFJSON.getString(m.getAsJsonObject().get("testModule"));
                VariantService.TestModuleHolder testModule = variantService.getTestModule(testModuleName);
                if (testModule != null) {
                    m.getAsJsonObject().addProperty("testSummary", testModule.info.summary());
                }
            });
        }

        return new ResponseEntity<>(testPlanObj, HttpStatus.OK);
    }

    @PostMapping(value = "/plan/{id}/publish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publish a test plan by plan Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Published test plan successfully"),
            @ApiResponse(responseCode = "400", description = "'publish' field is missing or its value is not JsonPrimitive"),
            @ApiResponse(responseCode = "403", description = "'publish' value is not valid or couldn't find test plan by provided plan Id")
    })
    public ResponseEntity<Object> publishTestPlan(@Parameter(description = "Id of test plan that you want publish") @PathVariable("id") String id,
                                                  @Parameter(description = "Configuration Json") @RequestBody JsonObject config) {

        String publish = null;
        if (config.has("publish") && config.get("publish").isJsonPrimitive()) {
            publish = Strings.emptyToNull(OIDFJSON.getString(config.get("publish")));
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!planService.publishTestPlan(id, publish)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("publish", publish);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping(value = "/plan/{id}/makemutable", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Make a test plan mutable again (requires administrator privileges)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Made the test plan mutable again successfully"),
            @ApiResponse(responseCode = "400", description = "Could not find plan"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<Object> makeTestPlanMutable(@Parameter(description = "Id of test plan that you want make mutable again") @PathVariable("id") String id) {
        if (!planService.changeTestPlanImmutableStatus(id, Boolean.FALSE)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "plan/info/{planName}")
    @Operation(summary = "Get information for one test plan by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Couldn't find test plan for provided plan name")
    })
    public ResponseEntity<Object> getTestPlanInfo(@Parameter(description = "Plan name, use to identify a specific TestPlan ") @PathVariable("planName") String planName) {
        VariantService.TestPlanHolder holder = variantService.getTestPlan(planName);

        if (holder != null) {

            Map<String, ?> map = args(
                    "planName", holder.info.testPlanName(),
                    "displayName", holder.info.displayName(),
                    "profile", holder.info.profile(),
                    "modules", holder.getTestModules(),
                    "configurationFields", holder.configurationFields(),
                    "hidesConfigurationFields", holder.hidesConfigurationFields(),
                    "summary", holder.info.summary(),
                    "variants", holder.getVariantSummary()
            );

            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "plan/available")
    @Operation(summary = "Get a list of available test plans and their attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully")
    })
    public ResponseEntity<Object> getAvailableTestPlans() {
        Set<Map<String, ?>> available = variantService.getTestPlans().stream()
                .map(e -> args(
                        "planName", e.info.testPlanName(),
                        "displayName", e.info.displayName(),
                        "profile", e.info.profile(),
                        "modules", e.getTestModules(),
                        "configurationFields", e.configurationFields(),
                        "hidesConfigurationFields", e.hidesConfigurationFields(),
                        "summary", e.info.summary(),
                        "variants", e.getVariantSummary()
                ))
                .collect(Collectors.toSet());

        return new ResponseEntity<>(available, HttpStatus.OK);
    }

    @DeleteMapping(value = "/plan/{id}")
    @Operation(summary = "Delete a test plan and related configuration. Requires the plan to be mutable.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Could not find a plan with the given id, belonging to the user"),
            @ApiResponse(responseCode = "405", description = "The plan is immutable and cannot be deleted")
    })
    public ResponseEntity<StreamingResponseBody> deleteMutableTestPlan(
            @Parameter(description = "Id of test plan") @PathVariable("id") String id
    ) {
        Plan testPlan = planService.getTestPlan(id);
        if (testPlan == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (testPlan.getImmutable() != null && testPlan.getImmutable()) {
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        }

        List<String> testIds = testPlan.getModules().stream().map(Plan.Module::getInstances).collect(ArrayList::new, List::addAll, List::addAll);
        infoService.deleteTests(testIds);
        planService.deleteMutableTestPlan(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
