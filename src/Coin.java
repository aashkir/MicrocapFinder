public class Coin {
	private String symbol, name;
	private double price, dayPctChange, dayVolume, marketCap, circulatingSupply, totalSupply;
	
	Coin(String symbol, String name, double price, double dayVolume, double marketCap, double circulatingSupply, double totalSupply, double dayPctChange) {
		this.symbol = symbol;
		this.name = name;
		this.price = price;
		this.dayVolume = dayVolume;
		this.marketCap = marketCap;
		this.circulatingSupply = circulatingSupply;
		this.totalSupply = totalSupply;
		this.dayPctChange = dayPctChange;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}

	public double getDayVolume() {
		return dayVolume;
	}

	public double getMarketCap() {
		return marketCap;
	}
	
	public double getCirculatingSupply() {
		return circulatingSupply;
	}
	
	public double getTotalSupply() {
		return totalSupply;
	}

	public double getDayPctChange() {
		return dayPctChange;
	}
	
	@Override
	public String toString() {
		return symbol + ": " + price + " " + dayVolume + " " + marketCap + " " + "circulatingSupply" + " " + dayPctChange;
	}
}
