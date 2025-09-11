package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<ThemeEntity, Long>, ThemeRepositoryCustom {
}
