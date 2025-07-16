package io.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import io.dashboard.model.Unit;
import io.dashboard.repository.UnitRepository;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner unitDataGenerator(@Autowired UnitRepository unitRepository) {
		return args -> {
			if (unitRepository.count() == 0) {
				List<Unit> units = Arrays.asList(
					// Currencies
					Unit.builder().code("EUR").description("Euro").group("Currency").build(),
					Unit.builder().code("USD").description("US Dollar").group("Currency").build(),
					Unit.builder().code("GBP").description("British Pound").group("Currency").build(),
					Unit.builder().code("CHF").description("Swiss Franc").group("Currency").build(),
					Unit.builder().code("JPY").description("Japanese Yen").group("Currency").build(),
					// Count
					Unit.builder().code("number").description("Number").group("Count").build(),
					Unit.builder().code("count").description("Count").group("Count").build(),
					Unit.builder().code("projects").description("Number of Projects").group("Count").build(),
					// Percent
					Unit.builder().code("%").description("Percent").group("Percent").build(),
					// Time
					Unit.builder().code("s").description("Seconds").group("Time").build(),
					Unit.builder().code("min").description("Minutes").group("Time").build(),
					Unit.builder().code("h").description("Hours").group("Time").build(),
					Unit.builder().code("d").description("Days").group("Time").build(),
					Unit.builder().code("y").description("Years").group("Time").build(),
					// Length
					Unit.builder().code("m").description("Meter").group("Length").build(),
					Unit.builder().code("km").description("Kilometer").group("Length").build(),
					Unit.builder().code("mi").description("Mile").group("Length").build(),
					// Weight
					Unit.builder().code("g").description("Gram").group("Weight").build(),
					Unit.builder().code("kg").description("Kilogram").group("Weight").build(),
					Unit.builder().code("t").description("Ton").group("Weight").build(),
					// Area
					Unit.builder().code("m²").description("Square Meter").group("Area").build(),
					Unit.builder().code("km²").description("Square Kilometer").group("Area").build(),
					// Volume
					Unit.builder().code("l").description("Liter").group("Volume").build(),
					Unit.builder().code("m³").description("Cubic Meter").group("Volume").build()
				);
				unitRepository.saveAll(units);
			}
		};
	}
}
