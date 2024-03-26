package com.project.portfolio.repository;

import com.project.portfolio.entity.TradeRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TradeRecordRepository extends MongoRepository<TradeRecord, String> {
    List<TradeRecord> findByEmailAndCoinName(String email, String coinName);
}
