package com.xodbs126.dropcommerce;

import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.storeapplication.entity.ApplicationStatus;
import com.xodbs126.dropcommerce.storeapplication.entity.StoreApplication;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static com.xodbs126.dropcommerce.productdrop.entity.DropStatus.PENDING;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "logging.level.org.hibernate.SQL=debug",
        "logging.level.org.hibernate.orm.jdbc.bind=trace"
})
@Transactional
public class EntityInsertTest extends IntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void saveStoreApplication() {

        AppUser user = new AppUser("test1", "password1");
        em.persist(user);

        em.persist(new StoreApplication(user.getId(), "test1", "testDescription1", now()));

        StoreApplication storeApplication = em.find(StoreApplication.class,1L);

        assertThat(storeApplication.getStatus()).isEqualTo(ApplicationStatus.PENDING);

    }

    @Test
    public void saveProduct() {
        AppUser user = new AppUser("test1", "password1");
        em.persist(user);

        em.persist(new Store(user.getId(), "test1"));
        Store store = em.find(Store.class, 1L);
        em.persist(new Product(store, "test1", "test2", 10000, ProductCategory.BOOK));

        Product targetProduct = em.find(Product.class, 1L);
        assertThat(targetProduct.getStock()).isZero();
        assertThat(targetProduct.getDeletedAt()).isNull();
    }

    @Test
    public void saveProductDrop() {
        AppUser user = new AppUser("test1", "password1");
        em.persist(user);

        em.persist(new Store(user.getId(), "test1"));
        Store store = em.find(Store.class, 1L);
        em.persist(new Product(store, "test1", "test2", 10000, ProductCategory.BOOK));
        Product product = em.find(Product.class, 1L);

        em.persist(new ProductDrop(product, 50, 2, now(), now()));

        ProductDrop targetDrop = em.find(ProductDrop.class, 1L);
        assertThat(targetDrop.getStatus()).isEqualTo(PENDING);
        assertThat(targetDrop.isReturned()).isFalse();
        assertThat(targetDrop.getApprovedAt()).isNull();
    }


}
