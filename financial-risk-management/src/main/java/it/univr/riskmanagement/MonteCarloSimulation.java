package it.univr.riskmanagement;

import java.util.Arrays;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.io.IOException;
import java.time.LocalDate;
//import org.apache.commons.math3.distribution.NormalDistribution;


/*
 * Questa classe permette di effettuare il calcolo delle miure di rischio tramite il metodo Monte Carlo 
 */
public class MonteCarloSimulation {
	
	
	// Inizializziamo il numero delle simulazioni che vogliamo eseguire
	private static int numberOfSimulation = 50000;
	// Ci servirà per poter commentare i grafici in seguito
	private static String n = String.valueOf(numberOfSimulation);
	
	
	// Questi vettori serviranno per gestire le simulazioni
	private double[] logReturnSimulated1 = new double [numberOfSimulation];
	private double[] logReturnSimulated2 = new double [numberOfSimulation];
	private double[] sampleQuantiles = new double [numberOfSimulation];
	private double[] portfolioLogReturnsSimulated = new double [numberOfSimulation];
	private double[] quantileLevel1 = new double [numberOfSimulation];
	private double[] quantileLevel2 = new double [numberOfSimulation];
	
	
	// Creiamo due varibili che gestiranno le misure di rischio delle simulazioni
	private double simulatedVaR;
	private double simulatedES;
	
	
	// Parametri coerenti con la consegna
	private static double alphaVAR = 0.01;
	private static double alphaES = 0.025;
	
	
	private double[] pricesStock1; // Osservazioni dei prezzi dello Stock 1
	private double[] pricesStock2; // Osservazioni dei prezzi dello Stock 2
	
	
	// oggetto che ci permette di gestire i dati
	DataManagement data = new DataManagement();
	
	
	
	/*
	 * Costruttore della classe MonteCarloSimulation:
	 * Carica i prezzi storici dei due titoli dai file Excel,
	 * utilizzando i metodi della classe DataCollectionAndPlotting.
	 * Viene inoltre lanciata un'eccezione nel caso in cui i due titoli non abbiano
	 * lo stesso numero di osservazioni, per garantire la coerenza dei dati.
	 */
	public MonteCarloSimulation() throws IOException {
		this.pricesStock1 = DataCollectionAndPlotting.getHistoricalPricesStock1();
		this.pricesStock2 = DataCollectionAndPlotting.getHistoricalPricesStock2();
		if (pricesStock1.length != pricesStock2.length) {
			throw new IOException ("The prices of the 2 stocks must be equal!");
		}
	}
	
	
	/*
	 * Questo metodo permette di generare i rendimenti logaritmici simulati.
	 * Calcolando media e deviazione standard di un intervallo di rendimenti e utilizzando la forumla
	 * vista in classe, possiamo andare a generare realizzazioni simulate per i nostri log-rendimenti.
	 * Sarebbe stato possibile semplificare il codice andando a generare un'oggetto di tipo
	 * NormalDistribution di parametri mu e sigma per andare a generare direttamente i quantili
	 * di una distribuzione normale tramite il metodo inverseCumulativeProbability().
	 * Per mantenere coerenza di notazione vista in classe, abbiamo proceduto con il primo metodo.
	 */
	private double[] normQuant (double[] logReturns, double[] quantileLevel) {
		NormalDistribution normal = new NormalDistribution();
		double mu = data.getExpectedValue(logReturns);
		double sigma = data.getStdDeviation(logReturns);
		for (int l = 0; l < quantileLevel.length; l++) {
			 /*
			  * prendendo in input un numero casuale tra 0 e 1, questa funzione permette di trovare
			  *  il quantile di una normale standard da inserire nella formula.
			  */
			double q = normal.inverseCumulativeProbability(quantileLevel[l]);
		    sampleQuantiles[l] = mu + sigma * q;
		}
		return sampleQuantiles;
	}
	
	
	/*
	 * Questo metodo ci permette di aggregare le simulazioni dei due rendimenti logaritmici
	 * per andare ad ottenere il rendimento assoluto del portafoglio
	 */
	private double[] getPortfolioLogReturnsSimulated (double[] logReturns1, double[] logReturns2, double budget1, double budget2) {
		for (int i = 0; i < numberOfSimulation; i++) {
			// generazione di due numeri casuali indipendenti tra 0 e 1
		    quantileLevel1[i] = Math.random();
		    quantileLevel2[i] = Math.random();
		}
		// vengono generati i rendimenti logaritmici simulati
		logReturnSimulated1 = normQuant(logReturns1, quantileLevel1);
		logReturnSimulated2 = normQuant(logReturns2, quantileLevel2);
		for (int i = 0; i < numberOfSimulation; i++) {
			// viene utilizzata la formula per l'aggregazione per i due rendimenti logaritmici
			portfolioLogReturnsSimulated[i] = budget1*(Math.exp(logReturnSimulated1[i])-1) + budget2*(Math.exp(logReturnSimulated2[i])-1);
		}
		return portfolioLogReturnsSimulated;
	}
	
	
	/*
	 * Questo metodo permette di calcolare il VaR storico delle simulazioni, prendendo una finestra
	 * temporale dei prezzi, otterremo i rendimenti logaritmici simulati, i quali, una volta aggregati,
	 * fungeranno da realizzazioni su cui si calcolerà il VaR storico.
	 */
	public double simulatedVaROfPortfolioLogReturn (double[] windowPricesStock1, double[] windowPricesStock2, double budget1, double budget2) {
		double[] logReturnsStock1 = new double[windowPricesStock1.length-1];
		logReturnsStock1 = data.getLogReturns(windowPricesStock1);
		double[] logReturnsStock2 = new double[windowPricesStock2.length-1];
		logReturnsStock2 = data.getLogReturns(windowPricesStock2);
		portfolioLogReturnsSimulated = getPortfolioLogReturnsSimulated(logReturnsStock1, logReturnsStock2, budget1, budget2);
		simulatedVaR = RiskMeasures.computeHistoricalVaR(portfolioLogReturnsSimulated, alphaVAR, numberOfSimulation);
		return simulatedVaR;
	}
	
	
	/*
	 * Questo metodo calcola il Value at Risk (VaR) storico iterativo, partendo dalle simulazioni, utilizzando una finestra
	 * mobile che permette di prendere i prezzi dei due stock.
	 * Un ciclo for scorre sui prezzi dello stock, aggiornando ad ogni iterazione la finestra:
	 * si elimina l’osservazione più vecchia e si aggiunge la successiva.
	 */
	public double[] iteratedSimulatedVaR (int windowLength, double budget1, double budget2) throws  IllegalArgumentException{
		if (pricesStock1.length-1 < windowLength) {
	        throw new IllegalArgumentException("The window length is longer than the return series");
	    }
		double[] iteratedSimulatedVaR = new double [pricesStock1.length - windowLength];
		for (int i = 0; i < pricesStock1.length - windowLength; i++) {
			double[] returnsForSimulationStock1 = Arrays.copyOfRange(pricesStock1, i, i+windowLength);
			double[] returnsForSimulationStock2 = Arrays.copyOfRange(pricesStock2, i, i+windowLength);
			iteratedSimulatedVaR[i] = simulatedVaROfPortfolioLogReturn(returnsForSimulationStock1, returnsForSimulationStock2, budget1, budget2);
		}
		return iteratedSimulatedVaR;
	}
	
	
	/*
	 * Lo stesso procedimento avviene per il calcolo dell'ES nei due metodi successivi
	 */
	public double simulatedESOfPortfolioLogReturn (double[] windowPricesStock1, double[] windowPricesStock2, double budget1, double budget2) {
		double[] logReturnsStock1 = new double[windowPricesStock1.length-1];
		logReturnsStock1 = data.getLogReturns(windowPricesStock1);
		double[] logReturnsStock2 = new double[windowPricesStock2.length-1];
		logReturnsStock2 = data.getLogReturns(windowPricesStock2);
		portfolioLogReturnsSimulated = getPortfolioLogReturnsSimulated(logReturnsStock1, logReturnsStock2, budget1, budget2);
		simulatedES = RiskMeasures.computeHistoricalES(portfolioLogReturnsSimulated, alphaES, numberOfSimulation);
		return simulatedES;
	}
	
	public double[] iteratedSimulatedES (int windowLength, double budget1, double budget2) throws  IllegalArgumentException{
		if (pricesStock1.length-1 < windowLength) {
	        throw new IllegalArgumentException("The window length is longer than the return series");
	    }
		double[] iteratedSimulatedES = new double [pricesStock1.length - windowLength];
		for (int i = 0; i < pricesStock1.length - windowLength; i++) {
			double[] returnsForSimulationStock1 = Arrays.copyOfRange(pricesStock1, i, i+windowLength);
			double[] returnsForSimulationStock2 = Arrays.copyOfRange(pricesStock2, i, i+windowLength);
			iteratedSimulatedES[i] = simulatedESOfPortfolioLogReturn(returnsForSimulationStock1, returnsForSimulationStock2, budget1, budget2);
		}
		return iteratedSimulatedES;
	}
	
	
	/*
	 * I due metodi sottostanti permettono di visualizzare graficamente l’evoluzione del VaR
	 * e dell’ES storici, partendo dalle simulazioni, calcolati con una finestra mobile.
	 * Prima della visualizzazione, viene eseguito un controllo per evitare che
	 * la lunghezza della finestra sia superiore al numero totale di osservazioni disponibili.
	 * Per garantire la coerenza temporale tra le misure di rischio e le date,
	 * è stato creato un sottoinsieme dell’array delle date che parte
	 * dall’ultimo giorno della prima finestra (window length).
	 * I grafici sono generati utilizzando il metodo plotData() della classe DataCollectionAndPlotting,
	 * e rappresentano visivamente l’andamento nel tempo del VaR e dell’ES storici.
	 */
	public static void plotIteratedSimulatedVaR(LocalDate[] dates, double[] iteratedSimulatedVAR,
			double alpha, int windowLength) throws  IllegalArgumentException{
		if (iteratedSimulatedVAR.length < windowLength) {
	        throw new IllegalArgumentException("The iterated VaRs are shorted than the window length");
	    }
	    // Crea una nuova lista di dates a partire dal giorno windowLength+1
	    LocalDate[] subsetDates = Arrays.copyOfRange(dates, windowLength+1, dates.length);
	    DataCollectionAndPlotting.plotData(subsetDates, iteratedSimulatedVAR, "Daily Simulated VaR using Monte Carlo Method with " +n+ " simulations");
	}
	
	public static void plotIteratedSimulatedlES(LocalDate[] dates, double[] iteratedSimulatedES,
			double alpha, int windowLength) throws  IllegalArgumentException{
		if (iteratedSimulatedES.length < windowLength) {
	        throw new IllegalArgumentException("The iterated ESs are shorted than the window length");
	    }// Crea una nuova lista di dates a partire dal giorno windowLength+1
	    LocalDate[] subsetDates = Arrays.copyOfRange(dates, windowLength+1, dates.length);
	    DataCollectionAndPlotting.plotData(subsetDates, iteratedSimulatedES, "Daily Simulated ES using Monte Carlo Method with " +n+ " simulations");
	}

}