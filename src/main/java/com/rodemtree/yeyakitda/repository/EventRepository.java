package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, Long>, EventRepositoryCustom {

}
