package dmv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DMVRead {

	public DMVRead() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	
	static HashMap<String, Double > hunaryprobs = new HashMap<String, Double >();
	
	public static void main(String[] args) {
		
		int maxIter;
		String trainFile="", trainoutfile="", testFile="", testoutfile="";

		//Read arguments
		if(args.length==3||args.length==5){
			maxIter=Integer.parseInt(args[0]);
			trainFile=args[1]; //	"/Users/shashans/Desktop/Assignment5/short.conll";
			trainoutfile=args[2];	//	"/Users/shashans/Desktop/Assignment5/short.conll.decoded";
			if(args.length>3){
				testFile=args[3];
				testoutfile=args[4];
			}
		}
		else{
			System.err.println("Correct usage: exec maxIters trainfile trainout [testfile testout]");
			return;
		}
		
		// Read and store Sentences
		ArrayList<ArrayList<String> >sentences = new ArrayList<ArrayList<String> >();
		try{
			ArrayList<String> sentence= new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(trainFile));
			String line;
			while ( (line=br.readLine()) != null ) {
				if(!line.isEmpty()){
					String[] toks = line.trim().split("\\s+");
					sentence.add(toks[3]); 
				}
				else{
					sentences.add(new ArrayList<String>(sentence));
					sentence.clear();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		//Learn parameters for DMV model(stop and attachment probabilities)
		ModelParameters m= DMVLearn.EM(sentences,maxIter);		
		double[][][] pstop=m.getPstop();
		double[][][] pattach=m.getPattach();
		
		//Annotate files
		Annotator.annotate(trainFile, trainoutfile, pstop, pattach);
		if(args.length>3)
			Annotator.annotate(testFile, testoutfile, pstop, pattach);				
	}
}
