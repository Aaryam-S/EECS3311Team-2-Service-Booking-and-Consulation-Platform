package service;

import java.util.ArrayList;
import java.util.List;
import model.Service;

public class CatalogService {
    // Singleton Pattern
    private static CatalogService instance;
    private List<Service> availableServices;

    private CatalogService() {
        availableServices = new ArrayList<>();
        // Seed data for demonstration
        availableServices.add(new Service("Software Consulting", 150.00, 60));
        availableServices.add(new Service("Career Coaching", 100.00, 45));
        availableServices.add(new Service("Legal Advice", 200.00, 30));
    }

    public static synchronized CatalogService getInstance() {
        if (instance == null) {
            instance = new CatalogService();
        }
        return instance;
    }

    public List<Service> getAllServices() {
        return availableServices;
    }

    public void addService(Service service) {
        availableServices.add(service);
    }

    public Service findServiceByName(String name) {
        for (Service s : availableServices) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}