package com.zstu.math.dao;

import com.zstu.math.entity.DataImportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataImportLogRepository extends JpaRepository<DataImportLog, Long> {
}