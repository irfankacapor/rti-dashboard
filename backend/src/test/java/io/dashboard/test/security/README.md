# Test Security Annotations

This package provides custom test annotations to easily mock different user roles in your Spring Boot tests.

## Available Annotations

### `@WithMockAdmin`
Mocks a user with `ADMIN` role and username "admin".

### `@WithMockManager`
Mocks a user with `MANAGER` role and username "manager".

### `@WithMockRegularUser`
Mocks a user with `USER` role and username "user".

## Usage

### Method-level usage
```java
@Test
@WithMockAdmin
void testAdminOnlyEndpoint() throws Exception {
    // Test with admin user context
}

@Test
@WithMockManager
void testManagerEndpoint() throws Exception {
    // Test with manager user context
}

@Test
@WithMockRegularUser
void testUserEndpoint() throws Exception {
    // Test with regular user context
}
```

### Class-level usage
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockAdmin  // All tests in this class will use admin context
class AdminControllerTest {
    // All test methods will have admin user context
}
```

## Dependencies

Make sure you have the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

## How it works

These annotations are built on top of Spring Security's `@WithMockUser` annotation and provide a convenient way to test your application with different user roles without having to manually set up authentication for each test.

The annotations automatically:
1. Create a mock user with the specified role
2. Set up the security context for the test
3. Clean up after the test completes

## Example

```java
@SpringBootTest
@AutoConfigureMockMvc
class AreaControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockAdmin
    void createArea_shouldReturn201_andArea() throws Exception {
        // This test will run with admin user context
        mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"A1\",\"name\":\"Area 1\"}"))
                .andExpect(status().isCreated());
    }
    
    @Test
    @WithMockManager
    void getAllAreas_shouldReturnList() throws Exception {
        // This test will run with manager user context
        mockMvc.perform(get("/api/v1/areas"))
                .andExpect(status().isOk());
    }
}
```

## Troubleshooting

If you're still getting 403 errors, make sure:

1. Your controller methods have proper `@PreAuthorize` annotations
2. The test class is properly configured with `@SpringBootTest` and `@AutoConfigureMockMvc`
3. The Spring Security Test dependency is included in your pom.xml
4. Your security configuration allows method-level security to handle authorization 