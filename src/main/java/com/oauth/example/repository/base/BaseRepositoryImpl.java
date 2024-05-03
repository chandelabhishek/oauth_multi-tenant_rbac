package com.oauth.example.repository.base;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoRepositoryBean
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {
    private static final String DELETED_FIELD = "deletedAt";
    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager em;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
        this.entityInformation = entityInformation;
        this.domainClass = entityInformation.getJavaType();
    }

    private static <T> Specification<T> notDeleted() {
        return Specification.where(new DeletedIsNUll<>());
    }

    @Override
    @NonNull
    public List<T> findAll() {
        return super.findAll(notDeleted());
    }

    @Override
    @NonNull
    public List<T> findAll(@NonNull Sort sort) {
        return super.findAll(notDeleted(), sort);
    }

    @Override
    @NonNull
    public Page<T> findAll(@NonNull Pageable page) {
        return super.findAll(notDeleted(), page);
    }

    @Override
    @NonNull
    public Optional<T> findById(@NonNull ID id) {
        return super.findOne(Specification.where(new ByIdSpecification<>(entityInformation, id)).and(notDeleted()));
    }

    @Override
    @Modifying
    @Transactional
    public void deleteById(@NonNull ID id) {
        softDelete(id, java.sql.Timestamp.from(Instant.now()));
    }

    @Override
    @Modifying
    @Transactional
    public void delete(@NonNull T entity) {
        softDelete(entity, Timestamp.from(Instant.now()));
    }

    public void hardDelete(T entity) {
        super.delete(entity);
    }

    private void softDelete(ID id, Timestamp currentTime) {
        Assert.notNull(id, "The given id must not be null!");

        Optional<T> entity = findById(id);

        if (entity.isEmpty())
            throw new EmptyResultDataAccessException(
                    String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1);

        softDelete(entity.get(), currentTime);
    }

    private void softDelete(T entity, Timestamp currentTime) {
        Assert.notNull(entity, "The entity must not be null!");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(entityInformation.getJavaType());
        Root<T> root = update.from(domainClass);
        update.set(DELETED_FIELD, currentTime);
        update.where(
                cb.equal(
                        root.<ID>get(Objects.requireNonNull(entityInformation.getIdAttribute()).getName()),
                        entityInformation.getId(entity)
                )
        );
        em.createQuery(update).executeUpdate();
    }

    private record ByIdSpecification<T, ID>(JpaEntityInformation<T, ?> entityInformation,
                                            ID id) implements Specification<T> {

        @Override
        public Predicate toPredicate(Root<T> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.equal(root.<ID>get(Objects.requireNonNull(entityInformation.getIdAttribute()).getName()), id);
        }
    }

    private static final class DeletedIsNUll<T> implements Specification<T> {
        @Override
        public Predicate toPredicate(Root<T> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            return criteriaBuilder.isNull(root.<Timestamp>get(DELETED_FIELD));
        }
    }
}
