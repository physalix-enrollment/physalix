package hsa.awp.common.persistence;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQL10Dialect;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.spi.PersistenceUnitInfo;
import java.util.HashMap;
import java.util.Map;

public class CustomHibernateJpaVendorAdapter extends HibernateJpaVendorAdapter {
    @Override
    protected Class<?> determineDatabaseDialectClass(Database database) {
        if (database == Database.POSTGRESQL) {
            return PostgreSQL10Dialect.class;
        }
        return super.determineDatabaseDialectClass(database);
    }

    @Override
    public Map<String, Object> getJpaPropertyMap(PersistenceUnitInfo pui) {
        Map<String, Object> properties = super.getJpaPropertyMap(pui);
        properties.putAll(overrideSettings());
        return properties;
    }

    @Override
    public Map<String, Object> getJpaPropertyMap() {
        Map<String, Object> properties = super.getJpaPropertyMap();
        properties.putAll(overrideSettings());
        return properties;
    }

    private Map<String, Object> overrideSettings() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.HBM2DDL_AUTO, "update");
        return properties;
    }
}
