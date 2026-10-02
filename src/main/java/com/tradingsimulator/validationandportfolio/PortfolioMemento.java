package com.tradingsimulator.validationandportfolio;
import com.tradingsimulator.model.*; import java.math.BigDecimal; import java.util.*;
public record PortfolioMemento(BigDecimal cash, Map<String,Position> positions, List<Trade> trades, Map<String,Quote> quotes) { public PortfolioMemento { positions=Map.copyOf(positions); trades=List.copyOf(trades); quotes=Map.copyOf(quotes); } }
