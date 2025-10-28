package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ThemeEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ThemeRepositoryCustom {
    List<ThemeEntity> findInProgressThemes(LocalDateTime now);
}
