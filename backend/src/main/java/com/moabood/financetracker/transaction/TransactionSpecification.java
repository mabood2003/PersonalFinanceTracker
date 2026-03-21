package com.moabood.financetracker.transaction;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(Long userId, TransactionFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getAccountId() != null) {
                predicates.add(cb.equal(root.get("account").get("id"), filter.getAccountId()));
            }
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getEndDate()));
            }
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                Predicate merchantLike = cb.like(cb.lower(root.get("merchant")), pattern);
                predicates.add(cb.or(descLike, merchantLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
