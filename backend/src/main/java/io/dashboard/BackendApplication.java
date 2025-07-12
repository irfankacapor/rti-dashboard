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
					new Unit(null, "EUR", "Euro", "Currency", null, null),
					new Unit(null, "USD", "US Dollar", "Currency", null, null),
					new Unit(null, "GBP", "British Pound", "Currency", null, null),
					new Unit(null, "CHF", "Swiss Franc", "Currency", null, null),
					new Unit(null, "JPY", "Japanese Yen", "Currency", null, null),
					// Count
					new Unit(null, "number", "Number", "Count", null, null),
					new Unit(null, "count", "Count", "Count", null, null),
					new Unit(null, "projects", "Number of Projects", "Count", null, null),
					// Percent
					new Unit(null, "%", "Percent", "Percent", null, null),
					// Time
					new Unit(null, "s", "Seconds", "Time", null, null),
					new Unit(null, "min", "Minutes", "Time", null, null),
					new Unit(null, "h", "Hours", "Time", null, null),
					new Unit(null, "d", "Days", "Time", null, null),
					new Unit(null, "y", "Years", "Time", null, null),
					// Length
					new Unit(null, "m", "Meter", "Length", null, null),
					new Unit(null, "km", "Kilometer", "Length", null, null),
					new Unit(null, "mi", "Mile", "Length", null, null),
					// Weight
					new Unit(null, "g", "Gram", "Weight", null, null),
					new Unit(null, "kg", "Kilogram", "Weight", null, null),
					new Unit(null, "t", "Ton", "Weight", null, null),
					// Area
					new Unit(null, "m²", "Square Meter", "Area", null, null),
					new Unit(null, "km²", "Square Kilometer", "Area", null, null),
					// Volume
					new Unit(null, "l", "Liter", "Volume", null, null),
					new Unit(null, "m³", "Cubic Meter", "Volume", null, null)
				);
				unitRepository.saveAll(units);
			}
		};
	}
}
