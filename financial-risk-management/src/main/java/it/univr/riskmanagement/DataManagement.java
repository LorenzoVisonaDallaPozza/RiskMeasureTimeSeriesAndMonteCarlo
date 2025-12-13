package it.univr.riskmanagement;

import java.io.IOException;
import java.util.Arrays;
import java.time.LocalDate;


 /*
  * Questa classe utilizza l'output di DataCollectionAndPlotting per calcolare i rendimenti storici del portafoglio d'investimento
  * e produrre i grafici corrispondenti.
  */
public class DataManagement {
	
	
	private double[] pricesStock1; // Osservazioni dei prezzi dello Stock 1
	private double[] pricesStock2; // Osservazioni dei prezzi dello Stock 2
	private LocalDate[] dates; // Date
	
	
	/*
	 * Costruttore della classe DataManagement:
	 * Carica i prezzi storici dei due titoli e le date corrispondenti dai file Excel,
	 * utilizzando i metodi della classe DataCollectionAndPlotting.
	 * Viene inoltre lanciata un'eccezione nel caso in cui i due titoli non abbiano
	 * lo stesso numero di osservazioni, per garantire la coerenza dei dati.
	 */
	public DataManagement() throws IOException {
		this.pricesStock1 = DataCollectionAndPlotting.getHistoricalPricesStock1();
		this.pricesStock2 = DataCollectionAndPlotting.getHistoricalPricesStock2();
		this.dates = DataCollectionAndPlotting.getDates();
		if (pricesStock1.length != pricesStock2.length) {
			throw new IOException ("The prices of the 2 stocks must be equal!");
		}
	}
	
	
	/*
	 * Questo metodo calcola i rendimenti assoluti del portafoglio.
	 * Viene creato un array di dimensione pari al numero di osservazioni - 1,
	 * poiché non è possibile calcolare il rendimento sull'ultima osservazione.
	 * Ogni elemento i-esimo dell'array rappresenta il rendimento assoluto calcolato
	 * tramite la formula vista in classe.
	 */
	public double[] getPortfolioReturns(double budget1, double budget2) {
		double[] returns = new double [dates.length-1];
		double c1 = budget1;
		double c2 = budget2;
		for (int i = 0; i < dates.length-1; i++) {
			returns[i] = (c1/pricesStock1[i])*pricesStock1[i+1] + (c2/pricesStock2[i])*pricesStock2[i+1]- c1 -c2;
		}
		return returns;
	}
	
	
	/*
	 * Implementazione della visualizzazione grafica dei dati tramite tre metodi distinti,
	 * ciascuno dedicato a un diverso tipo di grafico (prezzi stock1, prezzi stock2, rendimenti portafoglio).
	 * In ciascun metodo è presente un controllo che verifica che il numero di osservazioni
	 * (prezzi o rendimenti) corrisponda al numero di giorni corretto, al fine di garantire la coerenza dei dati.
	 * La visualizzazione dei dati avviene tramite il metodo .plotData() fornito dalla classe
	 * DataCollectionAndPlotting.
	 */
	public void plotPricesStock1() throws IOException {
		if (pricesStock1.length != dates.length) {
			throw new IOException ("The number of prices for stock 1 is different from the number of the dates!");
		}
		DataCollectionAndPlotting.plotData(dates, pricesStock1, "Prices Stock 1");
	}
	
	public void plotPricesStock2() throws IOException {
		if (pricesStock2.length != dates.length) {
			throw new IOException ("The number of prices for stock 2 is different from the number of the dates!");
		}
		DataCollectionAndPlotting.plotData(dates, pricesStock2, "Prices Stock 2");
	}
	
	public void plotPortfolioReturns(double budget1, double budget2) throws IOException {
		double[] returns = getPortfolioReturns(budget1, budget2);
		if (returns.length != dates.length-1) {
			throw new IOException ("The number of returns is different from the number of the dates!");
		}
		/*
		 * Per plottare coerentemente i rendimenti nel tempo, abbiamo fatto una copia dell'array delle date
		 * con una lunghezza pari al numero di osservazioni dei rendimenti, ovvero una in meno rispetto
		 * all'array originale delle date. Questo accorgimento è necessario perché il calcolo dei rendimenti
		 * richiede due prezzi consecutivi, e quindi produce un numero di valori pari a (n - 1).
		 */
        LocalDate[] datesToPlot = Arrays.copyOfRange(dates, 1, dates.length);
		DataCollectionAndPlotting.plotData(datesToPlot, returns, "Portfolio Returns");
	}
	
	
	
	
	// PARTE OPZIONALE, ci servirà per implementare la classe MonteCarloSimulation
	
	/*
	 * Questo metodo calcola i rendimenti logaritmici a partire dai prezzi storici.
	 * Viene creato un array di dimensione pari al numero di osservazioni dei prezzi meno uno,
	 * poiché il calcolo del log-rendimento richiede due prezzi consecutivi.
	 * L'elemento i-esimo dell'array viene calcolato come:
	 * log(prezzo_i+1 / prezzo_i), utilizzando il logaritmo naturale.
	 */
	public double[] getLogReturns(double[] pricesStock) {
		double[] logReturns = new double [pricesStock.length-1];
		for (int i = 0; i < pricesStock.length-1; i++) {
			logReturns[i] = Math.log(pricesStock[i+1]/pricesStock[i]);
		}
		return logReturns;
	}
	
	
	/*
	 * Questi due metodi ci permettono di calcolare la media e la devizione standard dei rendimenti logaritmici
	 * che diamo in input, ovvero quelli appartenenti alla window length.
	 */
	public double getExpectedValue (double[] logReturns) {
		double expectedValue = 0.0;
		for (int i = 0; i < logReturns.length; i ++) {
			expectedValue += logReturns[i]/logReturns.length;
		}
		return expectedValue;
	}
	
	public double getStdDeviation (double[] logReturns) {
		double stdDeviation = 0.0;
		double sumOfPowers = 0.0; // Qui identifichiamo la somma dei quadrati (x_i-E[x])^2.
		for (int j = 0; j < logReturns.length; j ++) {
			sumOfPowers += Math.pow(logReturns[j]-getExpectedValue(logReturns), 2);
		}
		stdDeviation = Math.sqrt(sumOfPowers/logReturns.length);
		return stdDeviation;
	}
	
}