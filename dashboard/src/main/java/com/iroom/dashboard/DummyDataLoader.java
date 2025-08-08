// package com.iroom.dashboard;
//
// import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Component;
//
// import java.nio.file.Files;
// import java.util.List;
//
// @Component
// @RequiredArgsConstructor
// public class DummyDataLoader {
//
// 	private final JdbcTemplate jdbcTemplate;
//
// 	@PostConstruct
// 	public void loadDummyData() throws Exception {
// 		ClassPathResource resource = new ClassPathResource("data.sql");
// 		List<String> lines = Files.readAllLines(resource.getFile().toPath());
//
// 		for (String line : lines) {
// 			if (!line.trim().isEmpty()) {
// 				jdbcTemplate.execute(line);
// 			}
// 		}
// 	}
// }
