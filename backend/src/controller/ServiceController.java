package controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import service.CatalogService;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final CatalogService catalogService;

    public ServiceController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllServices() {
        return ResponseEntity.ok(catalogService.getAllServices());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addService(@RequestBody Map<String, Object> body) {
        Object nameObj = body.get("name");
        Object consultantNameObj = body.get("consultantName");
        Object priceObj = body.get("price");

        if (nameObj == null || consultantNameObj == null || priceObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "name, consultantName, and price are required."
            ));
        }

        Integer consultantId = body.get("consultantId") == null
                ? null
                : Integer.parseInt(body.get("consultantId").toString());

        Integer duration = body.get("duration") == null
                ? 60
                : Integer.parseInt(body.get("duration").toString());

        Double price = Double.parseDouble(priceObj.toString());

        Map<String, Object> created = catalogService.addService(
            nameObj.toString(),
            consultantId,
            consultantNameObj.toString(),
            duration,
            price
        );

        return ResponseEntity.ok(created);
    }
}