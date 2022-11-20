package ntp.util;

public class SymbologyConverter {

	public static String convertNasdaqToCQS(String symbol)
	{
		symbol = symbol.replace("-", "p");
		symbol = symbol.replace(".", "/");
		symbol = symbol.replace("$", "/WD");
		int index = symbol.indexOf("+");
		if(index  !=-1 )
		{
			int i = symbol.charAt(symbol.length() -1);
			if( i > 64 && i < 91)
				symbol = symbol.replace("+", "/WS/");
			else
				symbol = symbol.replace("+", "/WS");
		}
		symbol = symbol.replace("*", "/CL");
		symbol = symbol.replace("#", "w");
		symbol = symbol.replace("!", "/EC");
		symbol = symbol.replace("@", "/PP");
		symbol = symbol.replace("%", "/CV");
		symbol = symbol.replace("^", "r");
		symbol = symbol.replace("=", "/U");
		symbol = symbol.replace("~", "/TEST");
		return symbol;
	}
	
	public static String convertCQSToNasdaq(String symbol)
	{
		symbol = symbol.replace("p", "-");
		symbol = symbol.replace("/WD", "$");
		symbol = symbol.replace("/WS/", "+");
		symbol = symbol.replace("/WS", "+");
		symbol = symbol.replace("/CL", "*");
		symbol = symbol.replace("w", "#");
		symbol = symbol.replace("/EC", "!");
		symbol = symbol.replace("/PP", "@");
		symbol = symbol.replace("/CV", "%");
		symbol = symbol.replace("r", "^");
		symbol = symbol.replace("/U", "=");
		symbol = symbol.replace("/TEST", "~");
		symbol = symbol.replace("/", ".");
		return symbol;
	}
	
	
	public static String convertNasdaqToCMS(String symbol)
	{
		symbol = symbol.replace("-", " PR");
		symbol = symbol.replace(".", " ");
		symbol = symbol.replace("$", " WD");
		symbol = symbol.replace("+", " WS");
		symbol = symbol.replace("*", " CL");
		symbol = symbol.replace("#", " WI");
		symbol = symbol.replace("!", " EC");
		symbol = symbol.replace("@", " PP");
		symbol = symbol.replace("%", " CV");
		symbol = symbol.replace("^", " RT");
		symbol = symbol.replace("=", " U");
		symbol = symbol.replace("~", " TEST");
		
		String[] arr = symbol.split(" ");
		if(arr.length >2)
		{
			String suffix = "";
			for(int i = 1; i < arr.length; i++)
				suffix += arr[i];
			symbol = arr[0] + " " + suffix;
		}
		return symbol;
	}
	/*public static void main(String[] args) {
		try
		{
		BufferedReader reader = new BufferedReader(new FileReader("A:\\symbols_equity.txt"));
		String ticker = null;
		while ( (ticker = reader.readLine()) != null)
		{
			String cqs = convertNasdaqToCMS(ticker);			
				System.out.println(ticker + " " + cqs );
		}
		reader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}*/

}
