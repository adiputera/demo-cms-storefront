package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Event;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CatalogAwareRepository<Event> {
}
