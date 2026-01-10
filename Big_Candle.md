Praktyczne Features dla LightGBM (tylko relatywne/procentowe)
📊 MOMENTUM (już są 0-100 lub znormalizowane)
RSI (7, 14, 21) → 0-100
Stochastic %K, %D → 0-100
Stochastic RSI → 0-100
Williams %R → -100 do 0
CCI / 200 → znormalizowany
MFI → 0-100
CMO → -100 do 100
Ultimate Oscillator → 0-100
ROC (%) → już procentowy
RSI distance from 50 → -50 do 50
RSI slope (zmiana RSI z ostatnich N świec)
📈 TREND (odległości jako %)
(Close - EMA8) / EMA8 * 100
(Close - EMA21) / EMA21 * 100
(Close - EMA50) / EMA50 * 100
(Close - EMA100) / EMA100 * 100
(Close - EMA200) / EMA200 * 100
(Close - SMA20) / SMA20 * 100
(Close - SMA50) / SMA50 * 100
(Close - SMA200) / SMA200 * 100
(Close - VWMA) / VWMA * 100
(Close - HMA) / HMA * 100
(EMA8 - EMA21) / EMA21 * 100 → spread MA
(EMA21 - EMA50) / EMA50 * 100
(EMA50 - EMA200) / EMA200 * 100
ADX → 0-100
+DI → 0-100
-DI → 0-100
(+DI - -DI) / 100 → DI spread znormalizowany
Aroon Up → 0-100
Aroon Down → 0-100
Aroon Oscillator → -100 do 100
Linear Regression Slope / Close * 100
MA Slope: (MA - MA[n]) / MA[n] * 100
📉 VOLATILITY (znormalizowane)
ATR / Close * 100 → ATR%
ATR(7) / ATR(21) → ratio krótki/długi
BB %B → 0 do 1 (może wychodzić poza)
BB Width / Close * 100 → szerokość jako %
(Close - BB_lower) / (BB_upper - BB_lower) → pozycja w BB
Keltner %K → pozycja w kanale 0-1
Keltner Width / Close * 100
(High - Low) / Close * 100 → range%
Range / ATR → ratio
Current ATR percentile (ranking w ostatnich N świecach) → 0-100
StdDev / Close * 100
StdDev(14) / StdDev(50) → volatility ratio
📊 VOLUME (relatywne)
Volume / Volume_SMA20 → RVOL (1.0 = średnia)
Volume / Volume_SMA50
Volume percentile (ostatnie 100 świec) → 0-100
(Volume - Volume_min) / (Volume_max - Volume_min) → 0-1
OBV change % (za ostatnie N świec)
CMF → -1 do 1
Volume ROC %
Up_Volume / Total_Volume (ostatnie N świec) → 0-1
Volume * (Close - Open) / (High - Low) → znormalizowany pressure
🕯️ PRICE ACTION (jako % range lub ceny)
Świeca pojedyncza
Body / Range → 0-1 (jak duże body vs cały range)
Upper_Wick / Range → 0-1
Lower_Wick / Range → 0-1
(Close - Low) / Range → 0-1 (gdzie zamknięcie w świecy)
(Close - Open) / Open * 100 → body %
Gap: (Open - Prev_Close) / Prev_Close * 100
Is_Bullish → 1 lub 0
Multi-świecowe
Return 1 świeca: (Close - Close[1]) / Close[1] * 100
Return 3 świece: (Close - Close[3]) / Close[3] * 100
Return 5, 10, 20, 50 świec (%)
Highest_High(N) distance: (High_max - Close) / Close * 100
Lowest_Low(N) distance: (Close - Low_min) / Close * 100
Position in range(N): (Close - Low_min) / (High_max - Low_min) → 0-1
Consecutive bullish candles / N → 0-1
Consecutive bearish candles / N → 0-1
Higher_Highs_count / N → 0-1
Higher_Closes_count / N → 0-1
🎯 CANDLE PATTERNS (binarne lub siła)
Każdy pattern jako: 0 (brak), 1 (bullish), -1 (bearish)
lub siła: -100 do 100

1 świeca
Doji
Hammer
Inverted Hammer
Shooting Star
Marubozu
Spinning Top
2 świece
Engulfing
Harami
Piercing / Dark Cloud
Tweezer
3+ świece
Morning/Evening Star
Three Soldiers / Three Crows
Three Inside Up/Down
🔢 STATISTICAL (percentyle i z-score)
Close percentile (ostatnie 100 świec) → 0-100
Close percentile (ostatnie 500 świec) → 0-100
Close Z-score (ostatnie 20 świec)
RSI percentile → 0-100
Volume percentile → 0-100
ATR percentile → 0-100
Skewness returns (ostatnie 20 świec)
Kurtosis returns (ostatnie 20 świec)
Drawdown: (Close - Running_Max) / Running_Max * 100
Drawup: (Close - Running_Min) / Running_Min * 100
🔀 MACD (znormalizowane)
MACD / Close * 100
MACD Signal / Close * 100
MACD Histogram / Close * 100
MACD Histogram change (current - previous)
MACD > Signal → 1/0
MACD > 0 → 1/0
📏 SUPPORT/RESISTANCE (odległości %)
(Close - Pivot) / Pivot * 100
(Close - S1) / S1 * 100
(Close - R1) / R1 * 100
(Close - Prev_Day_High) / Close * 100
(Close - Prev_Day_Low) / Close * 100
(Close - Prev_Week_High) / Close * 100
Distance to nearest round number / Close * 100
(Close - VWAP) / VWAP * 100
🔄 LAG FEATURES (jako % zmiany)
Return[t-1], Return[t-2], Return[t-3], Return[t-5], Return[t-10]
RSI[t-1], RSI[t-2], RSI[t-3]
RSI change: RSI - RSI[t-1]
ATR%[t-1], ATR%[t-2]
RVOL[t-1], RVOL[t-2]
BB%B[t-1], BB%B[t-2]
ADX[t-1], ADX[t-2]
Body%[t-1], Body%[t-2]
🎯 COMPOSITE (kombinowane)
Trend Score: (count of price > MA) / total_MA → 0-1
Bullish Patterns count / All Patterns count → 0-1
Momentum Agreement: (RSI>50 + Stoch>50 + MACD>0) / 3 → 0-1
Volatility vs Trend: ATR percentile - ADX → spread
Volume Confirmation: jeśli bullish candle & RVOL>1 → 1, else 0