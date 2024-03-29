package com.ll.synergarette.boundedContext.order.repository;

import com.ll.synergarette.boundedContext.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
//    @Query("SELECT o FROM Order o JOIN FETCH o.member WHERE o.member.id = :memberId AND o.paidCheck = 1")
//    List<Order> findPaidOrdersByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT o FROM Order o JOIN FETCH o.member WHERE o.member.id = :memberId AND o.isPaid = true")
    List<Order> findPaidOrdersByMemberId(@Param("memberId") Long memberId);

//    Order findByIdAndPaid(Long id, boolean a);

    Order findByIdAndIsPaid(Long id, boolean a);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItemList WHERE o.isPaid = true")
    List<Order> findPaidOrders();

    List<Order> findByCreateDateBeforeAndIsPaid(LocalDateTime minusTime, boolean b);
}
