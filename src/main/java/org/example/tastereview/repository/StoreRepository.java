package org.example.tastereview.repository;

import java.util.List;
import java.util.Optional;
import org.example.tastereview.domain.Store;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 가게 조회 (REQ-FUNC-002~004). 비교용 정규화 = 공백 제거 + 소문자. */
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByNameAndAddress(String name, String address);

    List<Store> findByNameContainingIgnoreCaseOrderByNameAsc(String nameFragment);

    @Query("select s from Store s"
            + " where lower(function('replace', s.name, ' ', ''))"
            + " like lower(concat('%', function('replace', :query, ' ', ''), '%'))"
            + " order by s.name asc")
    List<Store> searchByNormalizedName(@Param("query") String query, Pageable pageable);
}
