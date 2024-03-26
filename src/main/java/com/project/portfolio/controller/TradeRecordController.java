package com.project.portfolio.controller;

import com.project.portfolio.entity.TradeRecord;
import com.project.portfolio.service.TradeRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class TradeRecordController {
    private final TradeRecordService tradeRecordService;

    @GetMapping
    public ResponseEntity<List<TradeRecord>> getTradeRecordList(HttpServletRequest request,
                                                                @RequestParam @Valid String coinName) {
        return ResponseEntity.ok(tradeRecordService.findTradeRecordList(request, coinName));
    }
}
