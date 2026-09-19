package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.Developer;
import com.workintech.s17d2.model.Experience;
import com.workintech.s17d2.model.JuniorDeveloper;
import com.workintech.s17d2.model.MidDeveloper;
import com.workintech.s17d2.model.SeniorDeveloper;
import com.workintech.s17d2.tax.Taxable;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/developers")
public class DeveloperController {
    public Map<Integer, Developer> developers;

    private final Taxable taxable;

    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    @PostConstruct
    public void init() {
        developers = new LinkedHashMap<>();
    }

    @GetMapping
    public List<Developer> getDevelopers() {
        return new ArrayList<>(developers.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Developer> getDeveloperById(@PathVariable int id) {
        Developer developer = developers.get(id);

        if (developer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(developer);
    }

    @PostMapping
    public ResponseEntity<Developer> addDeveloper(@RequestBody Developer developer) {
        Developer taxedDeveloper = createDeveloperWithTax(developer);
        developers.put(taxedDeveloper.getId(), taxedDeveloper);

        return ResponseEntity.status(HttpStatus.CREATED).body(taxedDeveloper);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Developer> updateDeveloper(@PathVariable int id, @RequestBody Developer developer) {
        if (!developers.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        Developer updatedDeveloper = createDeveloperWithTax(developer);
        updatedDeveloper.setId(id);
        developers.put(id, updatedDeveloper);

        return ResponseEntity.ok(updatedDeveloper);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Developer> deleteDeveloper(@PathVariable int id) {
        Developer removedDeveloper = developers.remove(id);

        if (removedDeveloper == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(removedDeveloper);
    }

    private Developer createDeveloperWithTax(Developer developer) {
        double salaryAfterTax = calculateSalaryAfterTax(developer.getSalary(), developer.getExperience());

        if (developer.getExperience() == Experience.JUNIOR) {
            return new JuniorDeveloper(developer.getId(), developer.getName(), salaryAfterTax);
        }

        if (developer.getExperience() == Experience.MID) {
            return new MidDeveloper(developer.getId(), developer.getName(), salaryAfterTax);
        }

        return new SeniorDeveloper(developer.getId(), developer.getName(), salaryAfterTax);
    }

    private double calculateSalaryAfterTax(double salary, Experience experience) {
        if (experience == Experience.JUNIOR) {
            return salary - (salary * taxable.getSimpleTaxRate() / 100);
        }

        if (experience == Experience.MID) {
            return salary - (salary * taxable.getMiddleTaxRate() / 100);
        }

        return salary - (salary * taxable.getUpperTaxRate() / 100);
    }
}
