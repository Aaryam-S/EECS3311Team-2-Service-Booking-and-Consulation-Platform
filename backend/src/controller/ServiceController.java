package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import model.Service;
import service.CatalogService;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllServices() {
        List<Service> services = CatalogService.getInstance().getAllServices();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Service service : services) {
            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put("id", service.getId());
            serviceMap.put("name", service.getName());
            serviceMap.put("consultantName", service.getConsultantName());
            serviceMap.put("price", service.getBasePrice());
            response.add(serviceMap);
        }
        return ResponseEntity.ok(response);
    }
}