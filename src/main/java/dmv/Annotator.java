package dmv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Annotator {

	public Annotator() {
		// TODO Auto-generated constructor stub
	}
	
	public static void annotate(String inputFile, String outputFile, double[][][] pstop, double[][][] pattach){
		try{
			ArrayList<String> sentence= new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			ArrayList<String[]> tokList = new ArrayList<String[]>();
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile,false));
			
			String line;
			while ( (line=br.readLine()) != null ) {
				if(!line.isEmpty()){
					//System.out.println("Line is:"+ line);
					String[] toks = line.trim().split("\\s+");
					tokList.add(toks);
					sentence.add(toks[3]); 
				}
				else{
					Decoder d= new Decoder();
					int[] parent = d.ckyDecode(sentence, pstop, pattach);
					for(int i=0;i<sentence.size();i++){
						String str="";
						for(int j=0;j<9;j++){
							if(j!=6){
								str=str+tokList.get(i)[j]+"\t";
							}
							else{
								str=str+Integer.toString(parent[i])+"\t";
							}
						}
						str=str+tokList.get(i)[9]+"\n";
						writer.write(str);
					}
					sentence.clear();
					tokList.clear();
					writer.write("\n");
				}
			}
			br.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
