package com.project.portfolio.service;

import com.project.exception.CustomException;
import com.project.market.entity.Ticker;
import com.project.market.service.TickerService;
import com.project.portfolio.entity.Portfolio;
import com.project.portfolio.entity.dto.TradeCurrencyDto;
import com.project.portfolio.repository.PortfolioRepository;
import com.project.reference.CheckPortfolioReference;
import com.project.reference.CheckUserReference;
import com.project.reference.CheckWalletReference;
import com.project.user.entity.User;
import com.project.wallet.entity.Wallet;
import com.project.wallet.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.project.exception.ErrorCode.NOT_ENOUGH_CASH;
import static com.project.exception.ErrorCode.NOT_ENOUGH_VOLUME;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {
    private final CheckPortfolioReference checkPortfolioReference;
    private final CheckWalletReference checkWalletReference;
    private final CheckUserReference checkUserReference;
    private final PortfolioRepository portfolioRepository;
    private final WalletRepository walletRepository;
    private final TickerService tickerService;

    public String buyVirtualCurrency(HttpServletRequest request, TradeCurrencyDto tradeCurrencyDto) {
        List<Ticker> tickerList = tickerService.searchCoinTicker(tradeCurrencyDto.getMarketName());
        Ticker ticker = tickerList.get(0);

        String email = checkUserReference.checkUserReference(request);
        User user = checkUserReference.findUserByEmail(email);
        Wallet wallet = checkWalletReference.findWalletByUser(user);

        double price = ticker.getTradePrice() * tradeCurrencyDto.getVolume();

        if (wallet.getBalance() < price) {
            throw new CustomException(NOT_ENOUGH_CASH);
        }

        if(checkPortfolioReference.existsByUserAndMarketName(user, ticker.getMarketName())) {
            Portfolio portfolio = checkPortfolioReference.findByUserAndMarketName(user, ticker.getMarketName());

            double totalPrice = portfolio.getPrice() + price;
            double totalVolume = portfolio.getVolume() + tradeCurrencyDto.getVolume();

            portfolio.patchPrice(totalPrice);
            portfolio.patchVolume(totalVolume);
            portfolio.patchAverageValue(totalPrice, totalVolume);

            portfolioRepository.save(portfolio);
        } else {
            Portfolio portfolio = Portfolio.builder()
                    .user(user)
                    .wallet(wallet)
                    .marketName(ticker.getMarketName())
                    .price(price)
                    .volume(tradeCurrencyDto.getVolume())
                    .averageValue(price / tradeCurrencyDto.getVolume())
                    .build();

            portfolioRepository.save(portfolio);
        }

        return ticker.getTradePrice() + " 원에 " + tradeCurrencyDto.getVolume() + " 개 구매 완료";
    }

    public String sellVirtualCurrency(HttpServletRequest request, TradeCurrencyDto tradeCurrencyDto) {
        List<Ticker> tickerList = tickerService.searchCoinTicker(tradeCurrencyDto.getMarketName());
        Ticker ticker = tickerList.get(0);

        String email = checkUserReference.checkUserReference(request);
        User user = checkUserReference.findUserByEmail(email);
        Wallet wallet = checkWalletReference.findWalletByUser(user);
        Portfolio portfolio = checkPortfolioReference.findByUserAndMarketName(user, tradeCurrencyDto.getMarketName());

        if (portfolio.getVolume() < tradeCurrencyDto.getVolume()) {
            throw new CustomException(NOT_ENOUGH_VOLUME);
        }

        double price = portfolio.getPrice() - ticker.getTradePrice() * tradeCurrencyDto.getVolume();
        double volume = portfolio.getVolume() - tradeCurrencyDto.getVolume();

        if (volume == 0) {
            portfolioRepository.delete(portfolio);
        } else {
            portfolio.patchPrice(price);
            portfolio.patchVolume(volume);
            portfolio.patchAverageValue(price, volume);

            portfolioRepository.save(portfolio);
        }

        wallet.plusBalance(price);
        walletRepository.save(wallet);

        return ticker.getTradePrice() + " 원에" + tradeCurrencyDto.getVolume() + " 개 판매 완료";
    }
}
