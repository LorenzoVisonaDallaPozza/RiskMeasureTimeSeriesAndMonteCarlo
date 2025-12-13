package it.univr.riskmanagement;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;


/*
 * Questa è la classe test che produce i grafici a partire dalla specificazione dei vari parametri di modello. 
 */
public class Tests {

	public static void main(String[] args) throws IOException {
		
		// Abbiamo creato un oggetto di tipo dataManagement per poter richiamare i metodi di questa classe
		DataManagement tester = new DataManagement();
		
		
		tester.plotPricesStock1(); // Plottiamo i prezzi dello stock 1
		tester.plotPricesStock2(); // Plottiamo i prezzi dello stock 2
		
		
		// Decidiamo di investire 600 sul primo stock e 400 sul secondo stock
		double budget1 = 600;
		double budget2 = 400;
		
		
		// Abbiamo costruito l'array dei rendimenti richiamando il metodo getPortfolioReturns e li plottiamo.
		double[] returns = tester.getPortfolioReturns(budget1, budget2);
		tester.plotPortfolioReturns(budget1, budget2);
		
		System.out.println("I rendimenti assoluti del nostro portafolgio sono:");
		System.out.println(Arrays.toString(returns));
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// inizializziamo la finestra temporale mobile a 250 giorni.
		int windowLength = 250;
		// Utilizziamo l'alphaVaR di Basilea II, pari a 1%.
		double alphaVAR = 0.01;
		// Per il concetto di calibrazione di VaR ed ES utilizziamo un alphaES pari al 2.5%.
		double alphaES = 0.025;
		
		// Creaiamo il vettore dei VaR iterati
		double[] iteratedHVAR = RiskMeasures.iterateHistoricalVaR(returns, alphaVAR, windowLength);
		
		System.out.println("I VaR storici dal giorno " + (windowLength+1) + " al giorno " + returns.length + " sono:");
		System.out.println(Arrays.toString(iteratedHVAR));
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// Creaiamo il vettore degli ES iterati
		double[] iteratedHES = RiskMeasures.iterateHistoricalES(returns, alphaES, windowLength);
		
		System.out.println("Gli ES storici dal giorno " + (windowLength+1) + " al giorno " + returns.length + " sono:");
		System.out.println(Arrays.toString(iteratedHVAR));
		System.out.println();
		System.out.println();
		System.out.println();
	
		
		/*
		 * usiamo i metodi della classe RiskMeasures per plottare le due misure di rischio nel tempo,
		 * creando prima il vettore delle date
		 */
		LocalDate[] dates = DataCollectionAndPlotting.getDates();
		RiskMeasures.plotIterateHistoricalVaR(dates, iteratedHVAR, alphaVAR, windowLength);
		RiskMeasures.plotIterateHistoricalES(dates, iteratedHES, alphaES, windowLength);
		
		
		
		
		// PARTE OPZIONALE, ci servirà per testare la classe MonteCarloSimulation
		
		// Creiamo l'oggetto simulation per poter utilizzare i metodi della classe MonteCarloSimulation
		MonteCarloSimulation simulation = new MonteCarloSimulation();
		
		// Creaiamo il vettore dei VaR iterati simulati
		double[] iteratedSimulatedVAR = simulation.iteratedSimulatedVaR(windowLength, budget1, budget2);
		
		System.out.println("I VaR storici simulati dal giorno " + (windowLength+1) + " al giorno " + returns.length + " sono:");
		System.out.println(Arrays.toString(iteratedSimulatedVAR));
		System.out.println();
		System.out.println();
		System.out.println();
		
		// Creaiamo il vettore degli ES iterati simulati
		double[] iteratedSimulatedES = simulation.iteratedSimulatedES(windowLength, budget1, budget2);
		
		System.out.println("Gli ES storici simulati dal giorno " + (windowLength+1) + " al giorno " + returns.length + " sono:");
		System.out.println(Arrays.toString(iteratedSimulatedES));
		
		
		// Plottiamo le due misure di rischio trovate tramite la simulazione
		MonteCarloSimulation.plotIteratedSimulatedVaR(dates, iteratedSimulatedVAR, alphaVAR, windowLength);
		MonteCarloSimulation.plotIteratedSimulatedlES(dates, iteratedSimulatedES, alphaES, windowLength);	
	}
	
}