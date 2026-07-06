package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Event;
import org.springframework.stereotype.Repository;

/**
 * Event Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface EventRepository extends CatalogAwareRepository<Event> {
}
