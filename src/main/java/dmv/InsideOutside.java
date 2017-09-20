package dmv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InsideOutside {

	public static Boolean verbose=false;

	public static SufficientStats getSufficientStats(ArrayList<String> sentence, double[][][] pstop, double[][][]pattach) {
				
		// TODO Auto-generated method stub
		int ROOT=0;
		int n=sentence.size();
		int t=NT.tagset.length;
		Set<String> staglist = new HashSet<String>(sentence);
		if(verbose)
			System.out.println("taglist for sentence is "+staglist.toString());
				
		double[][][][] pinside = new double[t][3][n][n];
		double[][][][] poutside = new double[t][3][n][n];
		for(int tag=0;tag<t;tag++){
			for(int dir=0; dir<3; dir++){
				for(int i=0;i<n;i++){
					for(int j=0;j<n;j++){
						pinside[tag][dir][i][j] = Double.NEGATIVE_INFINITY;
						poutside[tag][dir][i][j] = Double.NEGATIVE_INFINITY;
					}
				}
			}
		}
		
		//System.out.println("Calcuating Inside probabilies");
		//Get Inside Probabilities
		//(i)base
		for(int j=0;j<n;j++){		
			int tagindex = Arrays.asList(NT.tagset).indexOf(sentence.get(j));
			////pinside[tagindex][0][j][j]=1.0;
			////pinside[tagindex][1][j][j]= pstop[tagindex][0][0] * pinside[tagindex][0][j][j];
			////pinside[tagindex][2][j][j]= pstop[tagindex][1][0] * pinside[tagindex][1][j][j];
			pinside[tagindex][0][j][j]=0.0;
			pinside[tagindex][1][j][j]= pstop[tagindex][0][1] + pinside[tagindex][0][j][j];
			pinside[tagindex][2][j][j]= pstop[tagindex][1][1] + pinside[tagindex][1][j][j];
			if(verbose){
				System.out.println("pinside["+tagindex+"][1]["+j+"]["+j+"]:"+pinside[tagindex][1][j][j]);
				System.out.println("pinside["+tagindex+"][2]["+j+"]["+j+"]:"+pinside[tagindex][2][j][j]);
			}
		}
		//(ii)recurrence
		for(int i=1;i<n;i++){
			for(int j=0;j<=(n-1-i);j++){
				int start = j, end = j+i;
				for(String s1:staglist){
					int tag= Arrays.asList(NT.tagset).indexOf(s1);
					
					//Sum over spans and tag2
					for(int k=start;k<end;k++){
						//w spans start to k, a spans k+1 to end
						//compute adj1(k,w): whether the word to the right(a) is adjacent to w
						int adj=0, adj1=0;
						if(Arrays.asList(NT.tagset).indexOf(sentence.get(k))==tag){
							adj=1;
						}
						if(Arrays.asList(NT.tagset).indexOf(sentence.get(k+1))==tag){
							adj1=1;
						}
						for(String s2:staglist){
							int tag2= Arrays.asList(NT.tagset).indexOf(s2);
														
							pinside[tag][0][start][end]=logsum(pinside[tag][0][start][end], Math.log1p(-1.0*Math.exp(pstop[tag][0][adj]))+(pattach[tag][0][tag2]+pinside[tag][0][start][k]+pinside[tag2][2][k+1][end]));
							pinside[tag][1][start][end]=logsum(pinside[tag][1][start][end], Math.log1p(-1.0*Math.exp(pstop[tag][1][adj1]))+(pattach[tag][1][tag2]+pinside[tag2][2][start][k]+pinside[tag][1][k+1][end]));
							//System.out.println("Interim: "+pinside[tag][0][start][end]+" "+pinside[tag][1][start][end]);
							if(verbose==true){
								//System.out.println("C1:"+pstop[tag][0][adj]);
								//System.out.println("C2:"+Math.log1p(-1.0*Math.exp(pstop[tag][0][adj])));
								//System.out.println("C3:pattach["+tag+"][0]["+tag2+"]: "+pattach[tag][0][tag2]);
								//System.out.println("C4:pinside["+tag+"][0]["+start+"]["+k+"]:"+pinside[tag][0][start][k]);
								//System.out.println("C5:"+pinside[tag2][2][k+1][end]);
								//System.out.println("R:pinside["+tag+"][0]["+start+"]["+end+"]:"+pinside[tag][0][start][end]);
								//System.out.println("C2:"+Math.log1p(-1.0*Math.exp(pstop[tag][1][adj])));
								//System.out.println("C3:pattach["+tag+"][0]["+tag2+"]: "+pattach[tag][1][tag2]);
								//System.out.println("C4:pinside["+tag+"][0]["+start+"]["+k+"]:"+pinside[tag][0][start][k]);
								//System.out.println("C5:"+pinside[tag2][2][k+1][end]);
								//System.out.println("R:pinside["+tag+"][1]["+start+"]["+end+"]:"+pinside[tag][1][start][end]);
							}
						}
						
					}
					
					//Complete Half-seal
					int adj2=0;
					if(Arrays.asList(NT.tagset).indexOf(sentence.get(end))==tag){
						adj2=1;
					}
					////pinside[tag][1][start][end]+= pstop[tag][0][adj2]*pinside[tag][0][start][end];
					pinside[tag][1][start][end]=logsum(pinside[tag][1][start][end], pstop[tag][0][adj2]+pinside[tag][0][start][end]);
					
					//Seal
					int adj3=0;
					if(Arrays.asList(NT.tagset).indexOf(sentence.get(start))==tag){
						adj3=1;
					}
					////pinside[tag][2][start][end] = pstop[tag][1][adj3]*pinside[tag][1][start][end];
					pinside[tag][2][start][end]=logsum(pinside[tag][2][start][end], pstop[tag][1][adj3]+pinside[tag][1][start][end]);
					
					if(verbose){
						System.out.println("pinside["+tag+"][0]["+start+"]["+end+"]:"+pinside[tag][0][start][end]);
						System.out.println("pinside["+tag+"][1]["+start+"]["+end+"]:"+pinside[tag][1][start][end]);
						System.out.println("pinside["+tag+"][2]["+start+"]["+end+"]:"+pinside[tag][2][start][end]);
					}
				}
			}
		}
		
		//Attach to root
		pinside[0][1][0][n-1]=Double.NEGATIVE_INFINITY;
		for(String s:staglist){
			int tag= Arrays.asList(NT.tagset).indexOf(s);
			////pinside[0][1][0][n-1]+= pattach[0][1][tag]*pinside[tag][2][0][n-1];
			pinside[0][1][0][n-1] = logsum(pinside[0][1][0][n-1],pattach[0][1][tag] + pinside[tag][2][0][n-1]);
		}
		
		//Get outside probabilities
		////poutside[0][1][0][n-1]=1.0;
		//System.out.println("Calcuating o probabilies");
		poutside[0][1][0][n-1]=0.0;
		
		for(int i=n-1;i>=0;i--){
			for(int j=0;j<=(n-1-i);j++){
				int start = j, end = j+i;
				
				if(i==n-1){
					for(String s:staglist){
						int tag= Arrays.asList(NT.tagset).indexOf(s);
						////poutside[tag][2][start][end] = pattach[0][1][tag] * poutside[0][1][0][n-1];
						poutside[tag][2][start][end] = pattach[0][1][tag] + poutside[0][1][0][n-1];
						if(verbose)
							System.out.println("Outside: base: poutside["+tag+"][2]["+start+"]["+end+"]:"+poutside[tag][2][start][end]);
					}
				}
				
				for(String s1:staglist){
					int tag= Arrays.asList(NT.tagset).indexOf(s1);
								
					//end<k
					for(int k=end+1;k<n;k++){
						for(String s2:staglist){
							int tag2= Arrays.asList(NT.tagset).indexOf(s2);
							
							int adj1=0;
							if(Arrays.asList(NT.tagset).indexOf(sentence.get(end+1))==tag2){
								adj1=1;
							}
												
							////poutside[tag][2][start][end]+= (1.0 - pstop[tag2][1][adj1]) * pattach[tag2][1][tag] *poutside[tag2][1][start][k] * pinside[tag2][1][end+1][k];
							poutside[tag][2][start][end]= logsum(poutside[tag][2][start][end], Math.log1p(-1.0*Math.exp(pstop[tag2][1][adj1]))+pattach[tag2][1][tag] + poutside[tag2][1][start][k] + pinside[tag2][1][end+1][k]);
							
							int adj4=0;
							if(Arrays.asList(NT.tagset).indexOf(sentence.get(end))==tag){
								adj4=1;
							}
							////poutside[tag][0][start][end]+= (1.0 - pstop[tag][0][adj4])*pattach[tag][0][tag2]*pinside[tag2][2][end+1][k]*poutside[tag][0][start][k];
							poutside[tag][0][start][end]= logsum(poutside[tag][0][start][end], Math.log1p(-1.0*Math.exp(pstop[tag][0][adj4]))+pattach[tag][0][tag2] +pinside[tag2][2][end+1][k] + poutside[tag][0][start][k] );
						}
					}
					//k<start
					for(int k=start-1;k>=0;k--){
						for(String s2:staglist){
							int tag2= Arrays.asList(NT.tagset).indexOf(s2);
							
							int adj2=0;
							if(Arrays.asList(NT.tagset).indexOf(sentence.get(start-1))==tag2){
								adj2=1;
							}
							
							////poutside[tag][2][start][end]+= (1.0 - pstop[tag2][0][adj2]) * pattach[tag2][0][tag] *poutside[tag2][0][k][end] * pinside[tag2][0][k][start-1];
							poutside[tag][2][start][end] = logsum(poutside[tag][2][start][end], Math.log1p(-1.0*Math.exp(pstop[tag2][0][adj2])) + pattach[tag2][0][tag] +poutside[tag2][0][k][end] + pinside[tag2][0][k][start-1]);
							
							int adj3=0;
							if(Arrays.asList(NT.tagset).indexOf(sentence.get(start))==tag){
								adj3=1;
							}
							////poutside[tag][1][start][end]+= (1.0 - pstop[tag][1][adj3])*pattach[tag][1][tag2]*pinside[tag2][2][k][start-1]*poutside[tag][1][k][end];
							poutside[tag][1][start][end] = logsum(poutside[tag][1][start][end], Math.log1p(-1.0*Math.exp(pstop[tag][1][adj3])) + pattach[tag][1][tag2] + pinside[tag2][2][k][start-1] + poutside[tag][1][k][end]); 
						}
					}	
					
					{
						//Final computation of poutside[tag][1][start][end] (follows knowing putside[tag][2][start][end])
						int adj3a=0;
						if(Arrays.asList(NT.tagset).indexOf(sentence.get(start))==tag){
							adj3a=1;
						}
						poutside[tag][1][start][end]=logsum(poutside[tag][1][start][end],pstop[tag][1][adj3a]+poutside[tag][2][start][end]);

						//Final computation of poutside[tag][0][start][end] (using poutside[tag][1][start][end])
						int adj4a=0;
						if(Arrays.asList(NT.tagset).indexOf(sentence.get(end))==tag){
							adj4a=1;
						}
						poutside[tag][0][start][end]=logsum(poutside[tag][0][start][end],pstop[tag][0][adj4a]+poutside[tag][1][start][end]);
					}
					
					if(verbose){
						System.out.println("Outside::poutside["+tag+"][2]["+start+"]["+end+"]"+poutside[tag][2][start][end]);
						System.out.println("Outside::poutside["+tag+"][1]["+start+"]["+end+"]"+poutside[tag][1][start][end]);
						System.out.println("Outside::poutside["+tag+"][0]["+start+"]["+end+"]"+poutside[tag][0][start][end]);
					}
				}	
				
			}
		}
		
		//Get sufficient statistics from Inside and outside probabilies, and model parameters
		//System.out.println("Calculating sufficient statistics");
		double[][][][]c = new double[t][3][n][n];
		double[][][][][]w = new double[t][3][t][n][n];
		
		for(String s:staglist){
			int tag= Arrays.asList(NT.tagset).indexOf(s);
			for(int dir=0;dir<=2;dir++){
				for(int k1=0;k1<n;k1++){
					for(int k2=0;k2<=(n-1-k1);k2++){
						int i = k2, j = k2+k1;
						
						//Calculate c_s
						////c[tag][dir][i][j] = pinside[tag][dir][i][j] * poutside[tag][dir][i][j] / pinside[ROOT][1][0][n-1];
						c[tag][dir][i][j] = Math.exp(pinside[tag][dir][i][j] + poutside[tag][dir][i][j] - pinside[ROOT][1][0][n-1]);
						//if(dir<2)
						//	System.out.println("Finding c! "+c[tag][dir][i][j]+" "+pinside[tag][dir][i][j]+" "+poutside[tag][dir][i][j]+" "+pinside[ROOT][1][0][n-1]);
						
						//Calculate w_s
						for(String s2:staglist){
							int tag2= Arrays.asList(NT.tagset).indexOf(s2);
							
							if(dir==1){
								//First transform w[tag][1][tag2][i][j] to log scale
								w[tag][1][tag2][i][j]=Double.NEGATIVE_INFINITY;
								
								//tag(i,j)=tag2(i,k)-tag(k+1,j) 
								for(int k=i;k<j;k++){
									int adj=0;
									if(Arrays.asList(NT.tagset).indexOf(sentence.get(k+1))==tag){
										adj=1;
									}
									////w[tag][1][tag2][i][j] += (1.0 - pstop[tag][1][adj])*pattach[tag][1][tag2]*pinside[tag2][2][i][k] * pinside[tag][1][k+1][j] * poutside[tag][1][i][j]/ pinside[ROOT][1][0][n-1];
									//System.out.println("W1:"+w[tag][1][tag2][i][j]+" "+Math.log1p(-1.0*Math.exp(pstop[tag][1][adj]))+" "+pattach[tag][1][tag2]+" "+pinside[tag2][2][i][k] +" "+ pinside[tag][1][k+1][j] +" "+ poutside[tag][1][i][j] +" "+ pinside[ROOT][1][0][n-1]);
									w[tag][1][tag2][i][j] = logsum(w[tag][1][tag2][i][j], Math.log1p(-1.0*Math.exp(pstop[tag][1][adj]))+pattach[tag][1][tag2]+pinside[tag2][2][i][k] + pinside[tag][1][k+1][j] + poutside[tag][1][i][j] - pinside[ROOT][1][0][n-1]);
								}
								w[tag][1][tag2][i][j] = Math.exp(w[tag][1][tag2][i][j]);
								//System.out.println("WIS:w["+tag+"][1]["+tag2+"]["+i+"]["+j+"]:"+w[tag][1][tag2][i][j]);
							}
							if(dir==0){
								//First transform to log space
								w[tag][0][tag2][i][j] = Double.NEGATIVE_INFINITY;
								
								//tag(i,j)=tag(i,k).tag2(k+1,j) 
								for(int k=i;k<j;k++){
									int adj=0;
									if(Arrays.asList(NT.tagset).indexOf(sentence.get(k))==tag){
										adj=1;
									}
									////w[tag][0][tag2][i][j] += (1.0 - pstop[tag][0][adj])*pattach[tag][0][tag2]*pinside[tag][0][i][k] * pinside[tag2][2][k+1][j] * poutside[tag][0][i][j]/ pinside[ROOT][1][0][n-1];
									w[tag][0][tag2][i][j] = logsum(w[tag][0][tag2][i][j], Math.log1p(-1.0*Math.exp(pstop[tag][0][adj]))+pattach[tag][0][tag2]+pinside[tag][0][i][k] + pinside[tag2][2][k+1][j] + poutside[tag][0][i][j] - pinside[ROOT][1][0][n-1]);
								}
								w[tag][0][tag2][i][j] = Math.exp(w[tag][0][tag2][i][j]);
								//System.out.println("WIS:w["+tag+"][0]["+tag2+"]["+i+"]["+j+"]:"+w[tag][0][tag2][i][j]);
							}
						}
					}
				}
			}
		}
		
		SufficientStats sf = new SufficientStats(c, w, pinside[ROOT][1][0][n-1]);
		
		return sf;
	}

	static double logsum(double x, double y){
		double logb = Math.max(x, y);
		double loga = Math.min(x, y);
		if(logb==Double.NEGATIVE_INFINITY){
			return Double.NEGATIVE_INFINITY;
		}
		double z = logb + Math.log1p(Math.exp(loga-logb));
		return z;
	}
	
	static double log1min(double x){
		return Math.log1p(-1*Math.exp(x));
	}

}
