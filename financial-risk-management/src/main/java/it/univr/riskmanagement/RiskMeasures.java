package it.univr.riskmanagement;

import java.time.LocalDate;
import java.util.Arrays;


/*
Questa classe definisce la misura di rischio di riferimento.
 */
public class RiskMeasures {
		
	
	//private static int k; // indice importante che inizializzeremo nei vari metodi.
	
	
	/*
	 * Questo metodo calcola il Value at Risk (VaR) storico a un determinato livello di confidenza.
	 * Per prima cosa, viene creata una copia dell’array dei rendimenti che viene poi ordinata.
	 * L’indice k viene calcolato in base al livello di confidenza scelto.
	 * Poiché gli array in Java sono indicizzati da 0, il valore corretto sarà preso
	 * alla posizione (k - 1).
	 * Il VaR storico corrisponde all’opposto del rendimento in quella posizione, in quanto
	 * rappresenta la perdita potenziale massima attesa.
	 */
	public static double computeHistoricalVaR(double[] returns, double alphaVAR, int n) {
		/*
		 *  k rappresenta l'indice dell'osservazione corrispondente al quantile alpha 
		 *  all'interno di una serie ordinata di n osservazioni.
		 */
		int k = (int) (n * alphaVAR) + 1;	
		double[] returnsSorted = Arrays.copyOf(returns, returns.length);
		Arrays.sort(returnsSorted);
		double VAR = - returnsSorted[k-1];
		return VAR; 
	}
	
	
	/*
	 * Questo metodo calcola L'Expected Shortfall (ES) storico a un determinato livello di confidenza.
	 * Per prima cosa, viene creata una copia dell’array dei rendimenti che viene poi ordinata.
	 * L’indice k viene calcolato in base al livello di confidenza scelto.
	 * Definiamo ES1 come la somma pesata delle peggiori k-1 osservazioni, a cui dobbiamo aggiungere
	 * una correzione interpolata per la frazione residua (ES2). 
	 * Pertanto, l'ES al livello alpha corrisponde alla perdita media condizionata 
	 * al superamento del VaR al livello alpha.
	 */
	public static double computeHistoricalES(double[] returns, double alphaES, int n) {
		int k = (int) (n * alphaES) + 1;
		double[] returnsSorted = Arrays.copyOf(returns, returns.length);
		Arrays.sort(returnsSorted);
		double ES1 = 0.0;
		double ES2;
		for (int i = 0; i < k-1; i++) {
			ES1 += - returnsSorted[i]/(n*alphaES);
		}
		ES2 = - (1.0/alphaES)*returnsSorted[k-1]*(alphaES-((double)(k-1)/n));
		double ES = ES1+ES2;
		return ES;
	}

	
	/*
	 * Questo metodo calcola il Value at Risk (VaR) storico iterativo utilizzando una finestra mobile.
	 * Prima dell’elaborazione viene effettuato un controllo per assicurarsi che
	 * la dimensione della finestra temporale non sia superiore al numero totale
	 * di osservazioni disponibili dei rendimenti.
	 * Viene poi creato un array che conterrà i valori di VaR calcolati per ciascuna finestra.
	 * La lunghezza di questo array sarà pari a: numero osservazioni - dimensione finestra.
	 * Un ciclo for scorre sui rendimenti, aggiornando ad ogni iterazione la finestra:
	 * si elimina l’osservazione più vecchia e si aggiunge la successiva.
	 * Su ogni finestra viene calcolato il VaR storico, che viene memorizzato nel risultato.
	 * Questo approccio consente di osservare l’evoluzione del rischio nel tempo.
	 */
	public static double[] iterateHistoricalVaR(double[] returns, double alphaVAR, int windowLength) throws  IllegalArgumentException{
		if (returns.length < windowLength) {
		        throw new IllegalArgumentException("The window length is longer than the return series");
		    }
		double [] iteratedHVAR = new double [returns.length-windowLength];
		for (int i = 0; i < returns.length-windowLength; i++) {
			 double[] window = Arrays.copyOfRange(returns, i, i + windowLength);
			 iteratedHVAR[i] = computeHistoricalVaR(window, alphaVAR, windowLength);
			}
        return iteratedHVAR;
	}		
	
	
	/*
	 * Per l'iterazione dell'Expected Shortfall è stato utilizzando lo stesso procedimento del metodo 
	 * iterateHistoricalVaR.
	 */
	public static double[] iterateHistoricalES(double[] returns, double alphaES, int windowLength) throws  IllegalArgumentException{
		if (returns.length < windowLength) {
	        throw new IllegalArgumentException("The window length is longer than the return series");
	    }
		double [] iteratedHES = new double [returns.length-windowLength];
		for (int i = 0; i < returns.length-windowLength; i++) {
			double[] window = Arrays.copyOfRange(returns, i, i + windowLength);
			iteratedHES[i] = computeHistoricalES(window, alphaES, windowLength);
		}
		return iteratedHES;
	}
	
	
	/*
	 * I due metodi sottostanti permettono di visualizzare graficamente l’evoluzione del VaR
	 * e dell’Expected Shortfall (ES) storici calcolati con una finestra mobile.
	 * Prima della visualizzazione, viene eseguito un controllo per evitare che
	 * la lunghezza della finestra sia superiore al numero totale di osservazioni disponibili.
	 * Per garantire la coerenza temporale tra i dati di rischio e le date,
	 * è stato creato un sottoinsieme dell’array delle date che parte
	 * dall’ultimo giorno della prima finestra (window length).
	 * I grafici sono generati utilizzando il metodo plotData() della classe DataCollectionAndPlotting,
	 * e rappresentano visivamente l’andamento nel tempo del VaR e dell’ES storici.
	 */
	public static void plotIterateHistoricalVaR(LocalDate[] dates, double[] iteratedHVAR, double alpha, int windowLength) throws  IllegalArgumentException{
		if (iteratedHVAR.length < windowLength) {
	        throw new IllegalArgumentException("The iterated VaRs are shorted than the window length");
	    }
	    // Crea una nuova lista di dates a partire dal giorno windowLength+1
	    LocalDate[] subsetDates = Arrays.copyOfRange(dates, windowLength+1, dates.length);
	    DataCollectionAndPlotting.plotData(subsetDates, iteratedHVAR, "Daily historical VaR");
	}
	
	public static void plotIterateHistoricalES(LocalDate[] dates, double[] iteratedHES, double alpha, int windowLength) throws  IllegalArgumentException{
		if (iteratedHES.length < windowLength) {
	        throw new IllegalArgumentException("The iterated ESs are shorted than the window length");
	    }
	    // Crea una nuova lista di dates a partire dal giorno windowLength+1
	    LocalDate[] subsetDates = Arrays.copyOfRange(dates, windowLength+1, dates.length);
	    DataCollectionAndPlotting.plotData(subsetDates, iteratedHES, "Daily historical ES");
	}
	
}