package com.example.demo.servlet;

import com.example.demo.TestResultListener;
import com.example.demo.controller.*;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.service.*;
import com.example.demo.service.impl.*;
import com.example.demo.servlet.HelloServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.Optional;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Listeners(TestResultListener.class)
public class MenuProfitabilityApplicationTests {

    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;
    @Mock
    private ProfitCalculationRecordRepository profitCalculationRecordRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetailsService userDetailsService;

    private IngredientService ingredientService;
    private CategoryService categoryService;
    private MenuItemService menuItemService;
    private RecipeIngredientService recipeIngredientService;
    private ProfitCalculationServiceImpl profitCalculationService;
    private UserService userService;

    private IngredientController ingredientController;
    private CategoryController categoryController;
    private MenuItemController menuItemController;
    private RecipeIngredientController recipeIngredientController;
    private ProfitCalculationController profitCalculationController;
    private AuthController authController;

    @BeforeClass
    public void init() {
        MockitoAnnotations.openMocks(this);

        ingredientService = new IngredientServiceImpl(ingredientRepository);
        categoryService = new CategoryServiceImpl(categoryRepository);
        menuItemService = new MenuItemServiceImpl(menuItemRepository, recipeIngredientRepository, categoryRepository);
        recipeIngredientService = new RecipeIngredientServiceImpl(recipeIngredientRepository, ingredientRepository, menuItemRepository);
        profitCalculationService = new ProfitCalculationServiceImpl(menuItemRepository, recipeIngredientRepository,
                ingredientRepository, profitCalculationRecordRepository);
        userService = new UserServiceImpl(userRepository, passwordEncoder);

        ingredientController = new IngredientController(ingredientService);
        categoryController = new CategoryController(categoryService);
        menuItemController = new MenuItemController(menuItemService);
        recipeIngredientController = new RecipeIngredientController(recipeIngredientService);
        profitCalculationController = new ProfitCalculationController(profitCalculationService);
        authController = new AuthController(authenticationManager, jwtTokenProvider, userService);
    }

    // Helpers
    private MenuItem createSampleMenuItem() {
        MenuItem item = new MenuItem();
        item.setSellingPrice(BigDecimal.valueOf(200));
        item.setName("Burger");
        item.setDescription("Test burger");
        item.setActive(true);
        return item;
    }

    private Ingredient createSampleIngredient() {
        Ingredient ing = new Ingredient();
        ing.setName("Cheese");
        ing.setUnit("grams");
        ing.setCostPerUnit(BigDecimal.valueOf(5));
        ing.setActive(true);
        return ing;
    }

    /* ==========================================================
     * 1. Develop and deploy a simple servlet using Tomcat Server
     *    (8 tests)
     * ========================================================== */

    @Test(priority = 1, groups = "servlet")
    public void testServletRespondsWithHello() throws Exception {
        HelloServlet servlet = new HelloServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        writer.flush();
        Assert.assertTrue(stringWriter.toString().contains("Hello from servlet"));
    }

    @Test(priority = 1, groups = "servlet")
    public void testServletContentType() throws Exception {
        HelloServlet servlet = new HelloServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doGet(request, response);

        verify(response).setContentType("text/plain");
    }

    @Test(priority = 1, groups = "servlet")
    public void testServletHandlesNullRequest() throws Exception {
        HelloServlet servlet = new HelloServlet();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doGet(null, response);

        verify(response).getWriter();
    }

    @Test(priority = 1, groups = "servlet")
    public void testServletMultipleCalls() throws Exception {
        HelloServlet servlet = new HelloServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        for (int i = 0; i < 5; i++) {
            servlet.doGet(request, response);
        }

        verify(response, times(5)).getWriter();
    }

  

    @Test(priority = 1, groups = "servlet")
    public void testServletNullResponseThrowsException() {
        HelloServlet servlet = new HelloServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        Assert.assertThrows(Exception.class, () -> servlet.doGet(request, null));
    }

    @Test(priority = 1, groups = "servlet")
    public void testServletWriterIsClosedGracefully() throws Exception {
        HelloServlet servlet = new HelloServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        servlet.doGet(request, response);
        verify(writer).write("Hello from servlet");
    }

    @Test(priority = 1, groups = "servlet")
    public void testServletHandlesIOExceptionGracefully() throws Exception {
        HelloServlet servlet = new HelloServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IOException("Error"));

        try {
            servlet.doGet(request, response);
            Assert.fail("Expected IOException");
        } catch (IOException ex) {
            Assert.assertEquals(ex.getMessage(), "Error");
        }
    }

    /* ==========================================================
     * 2. Implement CRUD operations using Spring Boot and REST APIs
     *    (10 tests)
     * ========================================================== */

    @Test(priority = 2, groups = "crud")
    public void testCreateIngredientSuccess() {
        Ingredient ing = createSampleIngredient();
        when(ingredientRepository.findByNameIgnoreCase("Cheese")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(i -> {
            Ingredient saved = i.getArgument(0);
            return saved;
        });

        ResponseEntity<Ingredient> response = ingredientController.createIngredient(ing);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        Assert.assertEquals(response.getBody().getName(), "Cheese");
    }

    @Test(priority = 2, groups = "crud")
    public void testCreateIngredientDuplicateName() {
        Ingredient ing = createSampleIngredient();
        when(ingredientRepository.findByNameIgnoreCase("Cheese")).thenReturn(Optional.of(ing));

        Assert.assertThrows(BadRequestException.class, () -> ingredientService.createIngredient(ing));
    }

    @Test(priority = 2, groups = "crud")
    public void testGetIngredientByIdNotFound() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.empty());
        Assert.assertThrows(ResourceNotFoundException.class, () -> ingredientService.getIngredientById(1L));
    }

    @Test(priority = 2, groups = "crud")
    public void testUpdateIngredientCost() {
        Ingredient existing = createSampleIngredient();
        existing.setCostPerUnit(BigDecimal.valueOf(5));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ingredientRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Ingredient updated = createSampleIngredient();
        updated.setCostPerUnit(BigDecimal.valueOf(10));

        Ingredient result = ingredientService.updateIngredient(1L, updated);
        Assert.assertEquals(result.getCostPerUnit(), BigDecimal.valueOf(10));
    }

    @Test(priority = 2, groups = "crud")
    public void testDeactivateIngredient() {
        Ingredient existing = createSampleIngredient();
        when(ingredientRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ingredientController.deactivateIngredient(2L);
        Assert.assertFalse(existing.getActive());
    }

    @Test(priority = 2, groups = "crud")
    public void testCreateMenuItemInvalidPrice() {
        MenuItem item = createSampleMenuItem();
        item.setSellingPrice(BigDecimal.valueOf(-1));

        Assert.assertThrows(BadRequestException.class, () -> menuItemService.createMenuItem(item));
    }

    @Test(priority = 2, groups = "crud")
    public void testCreateMenuItemSuccess() {
        MenuItem item = createSampleMenuItem();
        when(menuItemRepository.findByNameIgnoreCase("Burger")).thenReturn(Optional.empty());
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<MenuItem> response = menuItemController.createMenuItem(item);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        Assert.assertEquals(response.getBody().getName(), "Burger");
    }

    @Test(priority = 2, groups = "crud")
    public void testDeactivateMenuItem() {
        MenuItem existing = createSampleMenuItem();
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(menuItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        menuItemController.deactivateMenuItem(1L);
        Assert.assertFalse(existing.getActive());
    }

    @Test(priority = 2, groups = "crud")
    public void testGetAllIngredientsListEmpty() {
        when(ingredientRepository.findAll()).thenReturn(Collections.emptyList());
        ResponseEntity<List<Ingredient>> response = ingredientController.getAllIngredients();
        Assert.assertTrue(response.getBody().isEmpty());
    }

    @Test(priority = 2, groups = "crud")
    public void testGetAllMenuItemsList() {
        when(menuItemRepository.findAll()).thenReturn(List.of(createSampleMenuItem()));
        ResponseEntity<List<MenuItem>> response = menuItemController.getAllMenuItems();
        Assert.assertEquals(response.getBody().size(), 1);
    }

    /* ==========================================================
     * 3. Configure and perform Dependency Injection and IoC
     *    using Spring Framework (8 tests)
     * ========================================================== */

    @Test(priority = 3, groups = "di")
    public void testIngredientServiceInjectedIntoController() {
        Assert.assertNotNull(ingredientController);
    }

    @Test(priority = 3, groups = "di")
    public void testMenuItemServiceInjectedIntoController() {
        Assert.assertNotNull(menuItemController);
    }

    @Test(priority = 3, groups = "di")
    public void testProfitCalculationServiceInjectedIntoController() {
        Assert.assertNotNull(profitCalculationController);
    }

    @Test(priority = 3, groups = "di")
    public void testUserServiceInjectedIntoAuthController() {
        Assert.assertNotNull(authController);
    }

    @Test(priority = 3, groups = "di")
    public void testIngredientServiceUsesRepositoryBean() {
        Ingredient ing = createSampleIngredient();
        when(ingredientRepository.findByNameIgnoreCase("Cheese")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(i -> i.getArgument(0));
        Ingredient saved = ingredientService.createIngredient(ing);
        Assert.assertEquals(saved.getName(), "Cheese");
    }

    @Test(priority = 3, groups = "di")
    public void testCategoryServiceUsesRepositoryBean() {
        Category category = new Category();
        category.setName("Appetizers");
        when(categoryRepository.findByNameIgnoreCase("Appetizers")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        Category saved = categoryService.createCategory(category);
        Assert.assertEquals(saved.getName(), "Appetizers");
    }

    @Test(priority = 3, groups = "di")
    public void testDIWithMockedRepositoriesNegative() {
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());
        Assert.assertThrows(ResourceNotFoundException.class, () -> ingredientService.getIngredientById(999L));
    }

    @Test(priority = 3, groups = "di")
    public void testIoCContainerCanSwapMocks() {
        IngredientRepository repoMock = mock(IngredientRepository.class);
        IngredientService tempService = new IngredientServiceImpl(repoMock);
        when(repoMock.findAll()).thenReturn(List.of(createSampleIngredient()));
        Assert.assertEquals(tempService.getAllIngredients().size(), 1);
    }

    /* ==========================================================
     * 4. Implement Hibernate configurations, generator classes,
     *    annotations, and CRUD operations (9 tests)
     * ========================================================== */

   

    @Test(priority = 4, groups = "hibernate")
    public void testProfitCalculationZeroIngredients() {
        MenuItem item = createSampleMenuItem();
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recipeIngredientRepository.findByMenuItemId(1L)).thenReturn(Collections.emptyList());

        Assert.assertThrows(BadRequestException.class, () -> profitCalculationService.calculateProfit(1L));
    }

   

    @Test(priority = 4, groups = "hibernate")
    public void testRecipeIngredientQuantityValidation() {
        RecipeIngredient ri = new RecipeIngredient();
        ri.setQuantity(-1.0);
        Ingredient ing = new Ingredient();
        ing.setId(1L);
        MenuItem item = new MenuItem();
        item.setId(2L);
        ri.setIngredient(ing);
        ri.setMenuItem(item);

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(createSampleIngredient()));
        when(menuItemRepository.findById(2L)).thenReturn(Optional.of(createSampleMenuItem()));

        Assert.assertThrows(BadRequestException.class, () -> recipeIngredientService.addIngredientToMenuItem(ri));
    }

    // @Test(priority = 4, groups = "hibernate")
  
    @Test(priority = 4, groups = "hibernate")
    public void testGetTotalQuantityOfIngredient() {
        when(recipeIngredientRepository.getTotalQuantityByIngredientId(1L)).thenReturn(50.0);
        Double total = recipeIngredientService.getTotalQuantityOfIngredient(1L);
        Assert.assertEquals(total, 50.0);
    }

    @Test(priority = 4, groups = "hibernate")
    public void testProfitCalculationRecordRetrieval() {
        ProfitCalculationRecord record = new ProfitCalculationRecord();
        when(profitCalculationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        Assert.assertNotNull(profitCalculationService.getCalculationById(1L));
    }

    @Test(priority = 4, groups = "hibernate")
    public void testProfitCalculationRecordNotFound() {
        when(profitCalculationRecordRepository.findById(100L)).thenReturn(Optional.empty());
        Assert.assertThrows(ResourceNotFoundException.class, () -> profitCalculationService.getCalculationById(100L));
    }

    /* ==========================================================
     * 5. Perform JPA mapping with normalization (1NF, 2NF, 3NF)
     *    (9 tests)
     * ========================================================== */

    @Test(priority = 5, groups = "jpa")
    public void testCategoryNameUniqueness() {
        Category cat = new Category();
        cat.setName("Desserts");
        when(categoryRepository.findByNameIgnoreCase("Desserts")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Category created = categoryService.createCategory(cat);
        Assert.assertEquals(created.getName(), "Desserts");
    }

    @Test(priority = 5, groups = "jpa")
    public void testCategoryDuplicateName() {
        Category cat = new Category();
        cat.setName("Beverages");
        when(categoryRepository.findByNameIgnoreCase("Beverages")).thenReturn(Optional.of(cat));
        Assert.assertThrows(BadRequestException.class, () -> categoryService.createCategory(cat));
    }

    @Test(priority = 5, groups = "jpa")
    public void testIngredientNormalizationNoDuplicateFields() {
        Ingredient ing = createSampleIngredient();
        Assert.assertNotNull(ing.getName());
        Assert.assertNotNull(ing.getCostPerUnit());
    }

    @Test(priority = 5, groups = "jpa")
    public void testMenuItemHasCategoriesCollection() {
        MenuItem item = new MenuItem();
        item.setCategories(new HashSet<>());
        Assert.assertNotNull(item.getCategories());
    }

    @Test(priority = 5, groups = "jpa")
    public void testCategoryHasMenuItemsCollection() {
        Category category = new Category();
        category.setMenuItems(new HashSet<>());
        Assert.assertNotNull(category.getMenuItems());
    }

    @Test(priority = 5, groups = "jpa")
    public void testDeactivateCategory() {
        Category cat = new Category();
        cat.setName("Specials");
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        categoryService.deactivateCategory(10L);
        Assert.assertFalse(cat.getActive());
    }

    @Test(priority = 5, groups = "jpa")
    public void testGetCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        Assert.assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    @Test(priority = 5, groups = "jpa")
    public void testMenuItemCannotBeActiveWithoutRecipeIngredients() {
        MenuItem existing = createSampleMenuItem();
        existing.setActive(false);
        existing.setSellingPrice(BigDecimal.valueOf(200));
        existing.setName("Burger");
        existing.setDescription("d");
        existing.setCategories(new HashSet<>());

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(menuItemRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(existing));
        when(recipeIngredientRepository.existsByMenuItemId(1L)).thenReturn(false);

        MenuItem updated = createSampleMenuItem();
        updated.setActive(true);

        Assert.assertThrows(BadRequestException.class, () -> menuItemService.updateMenuItem(1L, updated));
    }

   
    /* ==========================================================
     * 6. Create Many-to-Many relationships and test associations
     *    in Spring Boot (9 tests)
     * ========================================================== */

    @Test(priority = 6, groups = "manyToMany")
    public void testAssignActiveCategoryToMenuItem() {
        MenuItem item = createSampleMenuItem();
        item.setId(1L);
        Category cat = new Category();
        cat.setId(2L);
        cat.setActive(true);

        when(menuItemRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(menuItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(recipeIngredientRepository.existsByMenuItemId(anyLong())).thenReturn(true);

        item.setCategories(Set.of(cat));
        MenuItem created = menuItemService.createMenuItem(item);
        Assert.assertEquals(created.getCategories().size(), 1);
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testAssignInactiveCategoryToMenuItemFails() {
        MenuItem item = createSampleMenuItem();
        item.setId(1L);
        Category cat = new Category();
        cat.setId(2L);
        cat.setActive(false);

        when(menuItemRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));

        item.setCategories(Set.of(cat));
        Assert.assertThrows(BadRequestException.class, () -> menuItemService.createMenuItem(item));
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testCategoryMenuItemsAssociation() {
        MenuItem item = createSampleMenuItem();
        Category cat = new Category();
        cat.setName("Appetizers");

        item.getCategories().add(cat);
        cat.getMenuItems().add(item);

        Assert.assertTrue(item.getCategories().contains(cat));
        Assert.assertTrue(cat.getMenuItems().contains(item));
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testMenuItemCanHaveMultipleCategories() {
        MenuItem item = createSampleMenuItem();
        Category c1 = new Category();
        c1.setName("Appetizers");
        Category c2 = new Category();
        c2.setName("Vegetarian");

        item.getCategories().add(c1);
        item.getCategories().add(c2);

        Assert.assertEquals(item.getCategories().size(), 2);
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testCategoryCanHaveMultipleMenuItems() {
        Category cat = new Category();
        cat.setName("Desserts");
        MenuItem m1 = createSampleMenuItem();
        MenuItem m2 = createSampleMenuItem();
        m1.setName("Cake");
        m2.setName("Ice Cream");

        cat.getMenuItems().add(m1);
        cat.getMenuItems().add(m2);

        Assert.assertEquals(cat.getMenuItems().size(), 2);
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testAssignCategoryDuringUpdateMenuItem() {
        MenuItem existing = createSampleMenuItem();
        existing.setId(3L);

        Category category = new Category();
        category.setId(4L);
        category.setActive(true);

        when(menuItemRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category));
        when(menuItemRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(existing));
        when(recipeIngredientRepository.existsByMenuItemId(3L)).thenReturn(true);
        when(menuItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MenuItem updated = createSampleMenuItem();
        updated.setCategories(Set.of(category));
        MenuItem result = menuItemService.updateMenuItem(3L, updated);

        Assert.assertEquals(result.getCategories().size(), 1);
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testAssignCategoryToNonExistingMenuItemFails() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());
        MenuItem updated = createSampleMenuItem();
        Assert.assertThrows(ResourceNotFoundException.class, () -> menuItemService.updateMenuItem(99L, updated));
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testMenuItemAndCategoryAssociationConsistency() {
        MenuItem item = createSampleMenuItem();
        item.setCategories(new HashSet<>());
        Category cat = new Category();
        cat.setMenuItems(new HashSet<>());

        item.getCategories().add(cat);
        cat.getMenuItems().add(item);

        Assert.assertTrue(item.getCategories().iterator().next().getMenuItems().contains(item));
    }

    @Test(priority = 6, groups = "manyToMany")
    public void testRemoveCategoryMaintainsNormalization() {
        MenuItem item = createSampleMenuItem();
        Category cat = new Category();
        item.getCategories().add(cat);
        Assert.assertFalse(item.getCategories().isEmpty());
        item.getCategories().clear();
        Assert.assertTrue(item.getCategories().isEmpty());
    }

    /* ==========================================================
     * 7. Implement basic security controls and JWT token-based
     *    authentication (9 tests)
     * ========================================================== */

    @Test(priority = 7, groups = "security")
    public void testRegisterUserSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        req.setRole("ROLE_ADMIN");

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<User> response = authController.register(req);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        Assert.assertEquals(response.getBody().getEmail(), "user@example.com");
    }

    @Test(priority = 7, groups = "security")
    public void testRegisterUserDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        User existing = new User();
        existing.setEmail("user@example.com");

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(existing));

        Assert.assertThrows(BadRequestException.class, () -> userService.register(req));
    }

    @Test(priority = 7, groups = "security")
    public void testLoginGeneratesToken() {
        AuthRequest auth = new AuthRequest();
        auth.setEmail("user@example.com");
        auth.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole("ROLE_USER");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(), eq(user))).thenReturn("jwt-token");

        ResponseEntity<com.example.demo.dto.AuthResponse> response = authController.login(auth);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody().getToken(), "jwt-token");
    }

    @Test(priority = 7, groups = "security")
    public void testLoginInvalidCredentials() {
        AuthRequest auth = new AuthRequest();
        auth.setEmail("wrong@example.com");
        auth.setPassword("bad");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        try {
            authController.login(auth);
            Assert.fail("Expected BadCredentialsException");
        } catch (BadCredentialsException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad credentials");
        }
    }

  
    @Test(priority = 7, groups = "security")
    public void testUserDetailsLoadedByEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("pass");
        user.setRole("ROLE_USER");
        user.setActive(true);

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        org.springframework.security.core.userdetails.UserDetails details =
                service.loadUserByUsername("user@example.com");

        Assert.assertEquals(details.getUsername(), "user@example.com");
    }

    @Test(priority = 7, groups = "security")
    public void testUserDetailsNotFound() {
        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        Assert.assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));
    }

    /* ==========================================================
     * 8. Use HQL and HCQL to perform advanced data querying
     *    (8 tests)
     * ========================================================== */

    @Test(priority = 8, groups = "hql")
    public void testFindByProfitMarginGreaterThanEqual() {
        ProfitCalculationRecord r1 = new ProfitCalculationRecord();
        ProfitCalculationRecord r2 = new ProfitCalculationRecord();
        when(profitCalculationRecordRepository.findByProfitMarginGreaterThanEqual(20.0))
                .thenReturn(List.of(r1, r2));
        List<ProfitCalculationRecord> results =
                profitCalculationRecordRepository.findByProfitMarginGreaterThanEqual(20.0);
        Assert.assertEquals(results.size(), 2);
    }

    @Test(priority = 8, groups = "hql")
    public void testFindByProfitMarginGreaterThanEqualNoResults() {
        when(profitCalculationRecordRepository.findByProfitMarginGreaterThanEqual(90.0))
                .thenReturn(Collections.emptyList());
        List<ProfitCalculationRecord> results =
                profitCalculationRecordRepository.findByProfitMarginGreaterThanEqual(90.0);
        Assert.assertTrue(results.isEmpty());
    }

    @Test(priority = 8, groups = "hql")
    public void testCriteriaQueryMarginBetween() {
        ProfitCalculationRecord record = new ProfitCalculationRecord();
        List<ProfitCalculationRecord> mocked = List.of(record);

        ProfitCalculationServiceImpl spyService = Mockito.spy(profitCalculationService);
        doReturn(mocked).when(spyService).findRecordsWithMarginBetween(10.0, 30.0);

        List<ProfitCalculationRecord> results = spyService.findRecordsWithMarginBetween(10.0, 30.0);
        Assert.assertEquals(results.size(), 1);
    }

    @Test(priority = 8, groups = "hql")
    public void testAdvancedQueryNegativeRange() {
        ProfitCalculationServiceImpl spyService = Mockito.spy(profitCalculationService);
        doReturn(Collections.emptyList()).when(spyService).findRecordsWithMarginBetween(-10.0, -5.0);
        List<ProfitCalculationRecord> results = spyService.findRecordsWithMarginBetween(-10.0, -5.0);
        Assert.assertTrue(results.isEmpty());
    }

    @Test(priority = 8, groups = "hql")
    public void testFindByMenuItemIdUsesJPA() {
        ProfitCalculationRecord r = new ProfitCalculationRecord();
        when(profitCalculationRecordRepository.findByMenuItemId(1L)).thenReturn(List.of(r));
        List<ProfitCalculationRecord> records = profitCalculationService.getCalculationsForMenuItem(1L);
        Assert.assertEquals(records.size(), 1);
    }

    @Test(priority = 8, groups = "hql")
    public void testGetAllCalculationsUsesJpaFindAll() {
        when(profitCalculationRecordRepository.findAll()).thenReturn(List.of(new ProfitCalculationRecord()));
        List<ProfitCalculationRecord> records = profitCalculationService.getAllCalculations();
        Assert.assertEquals(records.size(), 1);
    }

    @Test(priority = 8, groups = "hql")
    public void testIngredientRepositoryCustomQueryTotalQuantity() {
        when(recipeIngredientRepository.getTotalQuantityByIngredientId(5L)).thenReturn(123.0);
        Double total = recipeIngredientService.getTotalQuantityOfIngredient(5L);
        Assert.assertEquals(total, 123.0);
    }

    @Test(priority = 8, groups = "hql")
    public void testMenuItemRepositoryFindAllActiveWithCategories() {
        when(menuItemRepository.findAllActiveWithCategories()).thenReturn(List.of(createSampleMenuItem()));
        List<MenuItem> list = menuItemRepository.findAllActiveWithCategories();
        Assert.assertEquals(list.size(), 1);
    }
}
