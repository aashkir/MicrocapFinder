import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class Microcaps extends Application {
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO create export button
		CoinGridPane gridPane = new CoinGridPane();
		HBox menu = new HBox(15);
		Label lblSetMarketCap = new Label("Max MarketCap");
		TextField tfSetMarketCap = new TextField(String.format("%d", gridPane.getMarketCapLimit()));
		Label lblSetMinPct = new Label("Minimum percent of available tokens");
		TextField tfSetMinPct = new TextField(String.format("%.2f", gridPane.getPctAvailableLimit() * 100));
		
		Button btnRefresh = new Button("Refresh");
		btnRefresh.setOnAction(e -> {
			try {
				int marketCapLimit = Integer.parseInt(tfSetMarketCap.getText());
				double minPct = Double.parseDouble(tfSetMinPct.getText());
				if (minPct < 0 || minPct > 100)
					throw new NumberFormatException("Enter a positive number between 0 and 100");
				if (marketCapLimit < 0)
					throw new NumberFormatException("Enter a positive integer");
				gridPane.setMarketCapLimit(marketCapLimit);
				gridPane.setPctAvailableLimit(minPct / 100);
				gridPane.refresh();
			}	catch (NumberFormatException numFormatException)	{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(numFormatException.getLocalizedMessage());
				alert.showAndWait();
			}
		});
		Button btnExport = new Button("Export");
		
		menu.getChildren().addAll(lblSetMarketCap, tfSetMarketCap, lblSetMinPct, tfSetMinPct, btnRefresh, btnExport);
		VBox mainPane = new VBox(15);
		mainPane.setPadding(new Insets(11, 12, 13, 14));
		ScrollPane scrollPane = new ScrollPane(gridPane);
		mainPane.getChildren().addAll(scrollPane, menu);
		Scene scene = new Scene(mainPane);
		primaryStage.setTitle("MicroCap Finder");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	class CoinGridPane extends GridPane {
		private ArrayList<Coin> coins;
		private int marketCapLimit = 250000;
		private double pctAvailableLimit = 0.7;
		
		// constructor
		CoinGridPane() {
			setCoins();
			setUpCoinGrid();
		}
		
		private void setCoins() {
			try {
				this.coins = createCoinList();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		private void setUpCoinGrid() {		
			this.setHgap(5.5);
			this.setVgap(5.5);
			this.setPadding(new Insets(11.5, 12.5, 13.5, 14.5));
			// title row
			this.add(new Label("NAME"), 0, 0);
			this.add(new Label("SYMBOL"), 1, 0);
			this.add(new Label("PRICE (USD)"), 2, 0);
			this.add(new Label("MARKET CAP"), 3, 0);
			this.add(new Label("TOTAL SUPPLY"), 4, 0);
			this.add(new Label("AVAILABLE SUPPLY"), 5, 0);
			this.add(new Label("VOL/MC"), 6, 0);
			this.add(new Label("PCT AVAILABLE"), 7, 0);
			this.add(new Label("CHANGE (24h)"), 8, 0);
			
			for(int i = 0; i < coins.size(); i++) {
				double VMCratio = coins.get(i).getDayVolume() / coins.get(i).getMarketCap();
				double pctAvailable = coins.get(i).getCirculatingSupply() / coins.get(i).getTotalSupply();
				this.add(new Label(coins.get(i).getName()), 0, i + 1);
				this.add(new Label(coins.get(i).getSymbol()), 1, i + 1);
				this.add(new Label(String.valueOf(coins.get(i).getPrice())), 2, i + 1);
				this.add(new Label(String.format("%,.0f", coins.get(i).getMarketCap())), 3, i + 1);
				this.add(new Label(String.format("%,.0f", coins.get(i).getTotalSupply())), 4, i + 1);
				this.add(new Label(String.format("%,.0f", coins.get(i).getCirculatingSupply())), 5, i + 1);
				this.add(new Label(String.format("%.2f%%", VMCratio * 100)), 6, i + 1);
				this.add(new Label(String.format("%.2f%%", pctAvailable * 100)), 7, i + 1);
				this.add(new Label(String.format("%+.2f%%", coins.get(i).getDayPctChange())), 8, i + 1);
			}
		}
		
		private ArrayList<Coin> createCoinList() throws MalformedURLException {
			ArrayList<Coin> coins = new ArrayList<>();
			
			URL url = new URL("https://api.coinmarketcap.com/v1/ticker/?limit=0");
			try (InputStream input = url.openStream();
				 JsonParser parser = Json.createParser(input);) {
				String symbol = "XX", name = "Unknown";
				double price = -1, dayVolume = -1, marketCap = -1, dayPctChange = -1, availableSupply = -1, totalSupply = -1;
				while (parser.hasNext()) {
					Event e = parser.next(); // next json object
					if (e == Event.KEY_NAME) {
						switch (parser.getString()) {
							case "name":
								parser.next();
								name = parser.getString();
								break;
							case "symbol":
								parser.next();
								symbol = parser.getString();
								break;
							case "price_usd":
								Event b = parser.next();
								if(b == Event.VALUE_STRING)
									price = Double.valueOf(parser.getString());
								break;
							case "24h_volume_usd":
								Event c = parser.next();
								if(c == Event.VALUE_STRING)
									dayVolume = Double.valueOf(parser.getString());
								break;
							case "market_cap_usd":
								Event d = parser.next();
								if(d == Event.VALUE_STRING)
									marketCap = Double.valueOf(parser.getString());
								break;
							case "available_supply":
								Event f = parser.next();
								if(f == Event.VALUE_STRING)
									availableSupply = Double.valueOf(parser.getString());
								break;
							case "total_supply":
								Event g = parser.next();
								if(g == Event.VALUE_STRING)
									totalSupply = Double.valueOf(parser.getString());
								break;
							case "percent_change_24h":
								Event h = parser.next();
								if(h == Event.VALUE_STRING)
									dayPctChange = Double.valueOf(parser.getString());
								Coin coin = new Coin(symbol, name, price, dayVolume, marketCap, availableSupply, totalSupply, dayPctChange);
								if (verifyCoin(coin))
									coins.add(coin);
								break;
							default:
								parser.next();
								break;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return coins;
		}
		
		private boolean verifyCoin(Coin coin) {
			if (coin.getMarketCap() < marketCapLimit && coin.getMarketCap() > 0) {
				// avoid large supply coins
				if (coin.getTotalSupply() < 50000000) {
					System.out.print("MC: " + coin.getMarketCap() + " ");
					double VMCRatio = coin.getDayVolume() / coin.getMarketCap();
					System.out.print("Available pct: " +VMCRatio + " ");
					if(VMCRatio > 0.02) {
						// avoid premined coins
						double pctAvailable = coin.getCirculatingSupply() / coin.getTotalSupply();
						System.out.print("pct available: " +VMCRatio + " ");
						if (pctAvailable > pctAvailableLimit && pctAvailable <= 1.01) {
							System.out.println("Verified Coin!");
							return true;
						}
						System.out.println();
					}
					System.out.println();
				}
				System.out.println();
			}
			System.out.println("Failed Coin!");
			return false;
		}
		// refreshes coin listing
		public void refresh() {
			setCoins();
			this.getChildren().clear();
			setUpCoinGrid();
		}
		
		// getters & setters
		public int getMarketCapLimit() {
			return marketCapLimit;
		}
		
		public void setMarketCapLimit(int marketCapLimit) {
			this.marketCapLimit = marketCapLimit;
		}
		
		public double getPctAvailableLimit() {
			return pctAvailableLimit;
		}
		
		public void setPctAvailableLimit(double pctAvailableLimit) {
			this.pctAvailableLimit = pctAvailableLimit;
		}
	}
}
