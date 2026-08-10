package com.example.food.delivery.service;

import com.example.food.delivery.entity.*;
import com.example.food.delivery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializerService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Always ensure sample partner owner account exists
        if (!userRepository.existsByEmail("owner@foodexpress.com")) {
            User owner = User.builder()
                    .name("Rajesh Sharma (Partner Owner)")
                    .email("owner@foodexpress.com")
                    .password(passwordEncoder.encode("owner123"))
                    .phone("+91 9876543212")
                    .role("ROLE_RESTAURANT_OWNER")
                    .build();
            userRepository.save(owner);

            // Link any existing unassigned restaurants to this sample owner
            List<Restaurant> restaurants = restaurantRepository.findAll();
            for (Restaurant r : restaurants) {
                if (r.getOwner() == null) {
                    r.setOwner(owner);
                    restaurantRepository.save(r);
                }
            }
        }

        if (userRepository.count() > 1) {
            return; // Full data already initialized
        }

        // 1. Users
        User admin = User.builder()
                .name("FoodExpress Admin")
                .email("admin@foodexpress.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("+91 9876543210")
                .role("ROLE_ADMIN")
                .build();
        userRepository.save(admin);

        User customer = User.builder()
                .name("Alex Johnson")
                .email("customer@foodexpress.com")
                .password(passwordEncoder.encode("user123"))
                .phone("+91 9876543211")
                .role("ROLE_CUSTOMER")
                .build();
        userRepository.save(customer);

        User owner = User.builder()
                .name("Rajesh Sharma (Partner Owner)")
                .email("owner@foodexpress.com")
                .password(passwordEncoder.encode("owner123"))
                .phone("+91 9876543212")
                .role("ROLE_RESTAURANT_OWNER")
                .build();
        userRepository.save(owner);

        // 2. Default Address
        Address defaultAddr = Address.builder()
                .user(customer)
                .fullName("Alex Johnson")
                .phone("+91 9876543211")
                .houseNo("Flat 402, Sunshine Heights")
                .addressLine("MG Road, Indiranagar")
                .city("Bengaluru")
                .state("Karnataka")
                .pincode("560038")
                .landmark("Near Metro Station")
                .isDefault(true)
                .build();
        addressRepository.save(defaultAddr);

        // 3. Categories
        FoodCategory catPizza = foodCategoryRepository.save(FoodCategory.builder().name("Pizza").description("Delicious oven-baked pizzas").icon("bi-pie-chart-fill").imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500").build());
        FoodCategory catBurger = foodCategoryRepository.save(FoodCategory.builder().name("Burger").description("Juicy handcrafted burgers").icon("bi-badge-ad-fill").imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500").build());
        FoodCategory catBiryani = foodCategoryRepository.save(FoodCategory.builder().name("Biryani").description("Aromatic authentic rice dishes").icon("bi-fire").imageUrl("https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500").build());
        FoodCategory catChinese = foodCategoryRepository.save(FoodCategory.builder().name("Chinese").description("Sizzling noodles and dim sums").icon("bi-box-seam-fill").imageUrl("https://images.unsplash.com/photo-1585032226651-759b368d7246?w=500").build());
        FoodCategory catSouth = foodCategoryRepository.save(FoodCategory.builder().name("South Indian").description("Crispy dosas and soft idlis").icon("bi-sun-fill").imageUrl("https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?w=500").build());
        FoodCategory catDessert = foodCategoryRepository.save(FoodCategory.builder().name("Desserts").description("Sweet treats and ice creams").icon("bi-cup-straw").imageUrl("https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=500").build());
        FoodCategory catBeverage = foodCategoryRepository.save(FoodCategory.builder().name("Beverages").description("Refreshing drinks and shakes").icon("bi-cup-hot-fill").imageUrl("https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500").build());
        FoodCategory catFast = foodCategoryRepository.save(FoodCategory.builder().name("Fast Food").description("Quick bites and fries").icon("bi-lightning-charge-fill").imageUrl("https://images.unsplash.com/photo-1561758033-d89a9ad46330?w=500").build());

        // 4. Restaurants
        Restaurant r1 = restaurantRepository.save(Restaurant.builder()
                .owner(owner)
                .name("Spice Garden")
                .description("Authentic Indian Mughlai & Tandoori Delicacies")
                .cuisine("North Indian, Biryani, Mughlai")
                .address("12 Park Street, Indiranagar")
                .city("Bengaluru")
                .rating(4.7)
                .deliveryTimeMinutes(25)
                .priceRange("₹350 for two")
                .imageUrl("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600")
                .active(true)
                .operationalStatus(Restaurant.OperationalStatus.OPEN)
                .build());

        Restaurant r2 = restaurantRepository.save(Restaurant.builder()
                .owner(owner)
                .name("Burger Hub")
                .description("Gourmet Artisan Burgers & Loaded Fries")
                .cuisine("American, Burgers, Fast Food")
                .address("45 Commercial Street")
                .city("Bengaluru")
                .rating(4.5)
                .deliveryTimeMinutes(20)
                .priceRange("₹250 for two")
                .imageUrl("https://images.unsplash.com/photo-1550547660-d9450f859349?w=600")
                .active(true)
                .build());

        Restaurant r3 = restaurantRepository.save(Restaurant.builder()
                .name("Chennai Biryani House")
                .description("Authentic Seeraga Samba & Hyderabadi Dum Biryani")
                .cuisine("Biryani, South Indian")
                .address("88 Residency Road")
                .city("Bengaluru")
                .rating(4.8)
                .deliveryTimeMinutes(30)
                .priceRange("₹400 for two")
                .imageUrl("https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=600")
                .active(true)
                .build());

        Restaurant r4 = restaurantRepository.save(Restaurant.builder()
                .name("Pizza Corner")
                .description("Authentic Wood-fired Neapolitan & New York Pizzas")
                .cuisine("Italian, Pizza, Pastas")
                .address("102 Koramangala 5th Block")
                .city("Bengaluru")
                .rating(4.6)
                .deliveryTimeMinutes(35)
                .priceRange("₹450 for two")
                .imageUrl("https://images.unsplash.com/photo-1579751626657-72bc17010498?w=600")
                .active(true)
                .build());

        Restaurant r5 = restaurantRepository.save(Restaurant.builder()
                .name("South Indian Kitchen")
                .description("Traditional Filter Coffee, Dosas, & Ghee Roast Specialities")
                .cuisine("South Indian, Chettinad")
                .address("14 Jayanagar 4th Block")
                .city("Bengaluru")
                .rating(4.9)
                .deliveryTimeMinutes(15)
                .priceRange("₹200 for two")
                .imageUrl("https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=600")
                .active(true)
                .build());

        Restaurant r6 = restaurantRepository.save(Restaurant.builder()
                .name("Chinese Wok")
                .description("Wok-tossed Hakka Noodles & Manchurian Bowls")
                .cuisine("Chinese, Asian, Dim Sum")
                .address("67 HSR Layout Sector 1")
                .city("Bengaluru")
                .rating(4.4)
                .deliveryTimeMinutes(25)
                .priceRange("₹300 for two")
                .imageUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600")
                .active(true)
                .build());

        Restaurant r7 = restaurantRepository.save(Restaurant.builder()
                .name("Dessert House")
                .description("Decadent Sundaes, Cheesecakes & Waffles")
                .cuisine("Desserts, Bakery, Ice Cream")
                .address("21 MG Road")
                .city("Bengaluru")
                .rating(4.7)
                .deliveryTimeMinutes(20)
                .priceRange("₹250 for two")
                .imageUrl("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600")
                .active(true)
                .build());

        Restaurant r8 = restaurantRepository.save(Restaurant.builder()
                .name("Fresh Bowl")
                .description("Healthy Green Bowls, Smoothies & Salads")
                .cuisine("Healthy, Salads, Juice")
                .address("55 Whitefield Main Rd")
                .city("Bengaluru")
                .rating(4.6)
                .deliveryTimeMinutes(20)
                .priceRange("₹350 for two")
                .imageUrl("https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600")
                .active(true)
                .build());

        // 5. Food Items
        List<FoodItem> items = new ArrayList<>();

        // Spice Garden
        items.add(FoodItem.builder().name("Paneer Butter Masala").description("Rich & creamy cottage cheese gravy cooked in butter and aromatic spices").price(new BigDecimal("280.00")).category(catPizza).restaurant(r1).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=500").build());
        items.add(FoodItem.builder().name("Butter Naan").description("Soft fluffy Indian bread baked in tandoor with fresh butter").price(new BigDecimal("50.00")).category(catFast).restaurant(r1).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1601050690597-df0568f70950?w=500").build());
        items.add(FoodItem.builder().name("Chicken Tikka Masala").description("Tender grilled chicken pieces in spicy tomato cream sauce").price(new BigDecimal("340.00")).category(catBiryani).restaurant(r1).isVegetarian(false).available(true).imageUrl("https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=500").build());
        items.add(FoodItem.builder().name("Dal Makhani").description("Slow cooked black lentils with cream and butter").price(new BigDecimal("220.00")).category(catPizza).restaurant(r1).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=500").build());

        // Burger Hub
        items.add(FoodItem.builder().name("Classic Cheese Burger").description("Juicy veg patty topped with melted cheddar, lettuce & house sauce").price(new BigDecimal("180.00")).category(catBurger).restaurant(r2).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500").build());
        items.add(FoodItem.builder().name("Crispy Chicken Smash Burger").description("Double crispy fried chicken fillet with pickles and smoky BBQ mayo").price(new BigDecimal("240.00")).category(catBurger).restaurant(r2).isVegetarian(false).available(true).imageUrl("https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=500").build());
        items.add(FoodItem.builder().name("Peri Peri Loaded Fries").description("Crispy fries dusted with spicy peri-peri mix and cheese dip").price(new BigDecimal("140.00")).category(catFast).restaurant(r2).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=500").build());
        items.add(FoodItem.builder().name("Chocolate Lava Shake").description("Thick creamy chocolate milkshake with fudge brownie pieces").price(new BigDecimal("160.00")).category(catBeverage).restaurant(r2).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500").build());

        // Chennai Biryani House
        items.add(FoodItem.builder().name("Special Mutton Dum Biryani").description("Seeraga samba rice cooked with tender mutton pieces and spices").price(new BigDecimal("380.00")).category(catBiryani).restaurant(r3).isVegetarian(false).available(true).imageUrl("https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500").build());
        items.add(FoodItem.builder().name("Hyderabadi Chicken Biryani").description("Fragrant basmati rice dum cooked with marinated spicy chicken").price(new BigDecimal("310.00")).category(catBiryani).restaurant(r3).isVegetarian(false).available(true).imageUrl("https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500").build());
        items.add(FoodItem.builder().name("Paneer Veg Dum Biryani").description("Aromatic biryani rice layered with paneer cubes and veggies").price(new BigDecimal("260.00")).category(catBiryani).restaurant(r3).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1642821373181-696a14044e76?w=500").build());

        // Pizza Corner
        items.add(FoodItem.builder().name("Margherita Supreme Pizza").description("Classic mozzarella cheese, fresh basil leaves & san marzano tomato sauce").price(new BigDecimal("290.00")).category(catPizza).restaurant(r4).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500").build());
        items.add(FoodItem.builder().name("Pepperoni Passion Pizza").description("Loaded spicy pepperoni slices with extra melted mozzarella").price(new BigDecimal("420.00")).category(catPizza).restaurant(r4).isVegetarian(false).available(true).imageUrl("https://images.unsplash.com/photo-1628840042765-356cda07504e?w=500").build());
        items.add(FoodItem.builder().name("Garlic Breadsticks with Dip").description("Freshly baked breadsticks brushed with garlic butter and herbs").price(new BigDecimal("130.00")).category(catFast).restaurant(r4).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1619535860434-ba1d8fa12536?w=500").build());

        // South Indian Kitchen
        items.add(FoodItem.builder().name("Masala Dosa").description("Golden crispy dosa stuffed with spiced potato filling served with chutneys").price(new BigDecimal("110.00")).category(catSouth).restaurant(r5).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?w=500").build());
        items.add(FoodItem.builder().name("Ghee Roast Idli (4 Pcs)").description("Steamed soft rice cakes tossed in aromatic pure desi ghee & podi").price(new BigDecimal("120.00")).category(catSouth).restaurant(r5).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500").build());
        items.add(FoodItem.builder().name("Kumbakonam Filter Coffee").description("Authentic traditional South Indian strong filter coffee").price(new BigDecimal("45.00")).category(catBeverage).restaurant(r5).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500").build());

        // Chinese Wok
        items.add(FoodItem.builder().name("Veg Hakka Noodles").description("Wok-tossed noodles with crunchy vegetables, soy sauce & sesame").price(new BigDecimal("190.00")).category(catChinese).restaurant(r6).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1585032226651-759b368d7246?w=500").build());
        items.add(FoodItem.builder().name("Chilli Chicken Gravy").description("Tender chicken bites tossed in dark soy, green chillies and garlic").price(new BigDecimal("270.00")).category(catChinese).restaurant(r6).isVegetarian(false).available(true).imageUrl("https://images.unsplash.com/photo-1525755662778-989d0524087e?w=500").build());
        items.add(FoodItem.builder().name("Steamed Veg Dim Sum (6 Pcs)").description("Delicate dumplings stuffed with minced vegetables served with spicy dip").price(new BigDecimal("160.00")).category(catChinese).restaurant(r6).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=500").build());

        // Dessert House
        items.add(FoodItem.builder().name("Sizzling Brownie with Ice Cream").description("Hot fudge brownie topped with vanilla ice cream and hot chocolate sauce").price(new BigDecimal("180.00")).category(catDessert).restaurant(r7).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=500").build());
        items.add(FoodItem.builder().name("New York Cheesecake Slice").description("Classic baked cheesecake on graham cracker crust with berry compote").price(new BigDecimal("220.00")).category(catDessert).restaurant(r7).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500").build());

        // Fresh Bowl
        items.add(FoodItem.builder().name("Avocado Grain Salad Bowl").description("Quinoa, fresh avocado, cherry tomatoes, cucumbers & lemon vinaigrette").price(new BigDecimal("290.00")).category(catFast).restaurant(r8).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500").build());
        items.add(FoodItem.builder().name("Berry Blast Smoothie Bowl").description("Blended wild berries topped with chia seeds, granola & fresh banana").price(new BigDecimal("240.00")).category(catBeverage).restaurant(r8).isVegetarian(true).available(true).imageUrl("https://images.unsplash.com/photo-1590301157890-4810ed352733?w=500").build());

        foodItemRepository.saveAll(items);

        // 6. Coupons
        couponRepository.save(Coupon.builder()
                .code("WELCOME50")
                .description("Get 50% OFF up to ₹100 on your first order")
                .discountPercentage(50)
                .minOrderAmount(new BigDecimal("199.00"))
                .maxDiscountAmount(new BigDecimal("100.00"))
                .expiryDate(LocalDate.now().plusMonths(6))
                .active(true)
                .build());

        couponRepository.save(Coupon.builder()
                .code("FOODEXPRESS100")
                .description("Flat ₹100 OFF on orders above ₹499")
                .discountAmount(new BigDecimal("100.00"))
                .minOrderAmount(new BigDecimal("499.00"))
                .expiryDate(LocalDate.now().plusMonths(3))
                .active(true)
                .build());

        couponRepository.save(Coupon.builder()
                .code("SUPER20")
                .description("20% OFF up to ₹150 on all orders")
                .discountPercentage(20)
                .minOrderAmount(new BigDecimal("299.00"))
                .maxDiscountAmount(new BigDecimal("150.00"))
                .expiryDate(LocalDate.now().plusMonths(2))
                .active(true)
                .build());

        // 7. Seed Initial Order
        Order sampleOrder = Order.builder()
                .orderNumber("ORD-882194-A1B2")
                .user(customer)
                .restaurant(r1)
                .deliveryAddress(defaultAddr)
                .subtotal(new BigDecimal("620.00"))
                .deliveryFee(new BigDecimal("40.00"))
                .tax(new BigDecimal("31.00"))
                .discountAmount(new BigDecimal("100.00"))
                .grandTotal(new BigDecimal("591.00"))
                .paymentMethod(Order.PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .orderStatus(Order.OrderStatus.PREPARING)
                .couponCode("WELCOME50")
                .items(new ArrayList<>())
                .build();
        Order savedOrder = orderRepository.save(sampleOrder);

        OrderItem oi1 = OrderItem.builder()
                .order(savedOrder)
                .foodItem(items.get(0))
                .foodName("Paneer Butter Masala")
                .price(new BigDecimal("280.00"))
                .quantity(1)
                .build();
        OrderItem oi2 = OrderItem.builder()
                .order(savedOrder)
                .foodItem(items.get(2))
                .foodName("Chicken Tikka Masala")
                .price(new BigDecimal("340.00"))
                .quantity(1)
                .build();

        orderItemRepository.save(oi1);
        orderItemRepository.save(oi2);
        savedOrder.getItems().add(oi1);
        savedOrder.getItems().add(oi2);

        System.out.println(">>> FoodExpress Sample Seed Data Initialized Successfully <<<");
    }
}
